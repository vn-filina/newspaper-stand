package ru.kiosk.repository;

import ru.kiosk.db.ConnectionFactory;
import ru.kiosk.domain.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class JdbcInventoryRepository implements InventoryRepository {
    private final ConnectionFactory cf;

    public JdbcInventoryRepository(ConnectionFactory cf) {
        this.cf = cf;
        cf.beforeFirstUse();
        init();
    }

    private Connection conn() throws SQLException { return cf.get(); }

    private void init() {
        try (Connection c = conn(); Statement st = c.createStatement()) {
            try { st.execute("PRAGMA journal_mode = WAL"); } catch (SQLException ignored) {}
        } catch (SQLException e) { throw new RuntimeException("PRAGMA init failed", e); }

        String[] ddl = new String[]{
                """
            CREATE TABLE IF NOT EXISTS stock(
              id            INTEGER PRIMARY KEY AUTOINCREMENT,
              type          TEXT NOT NULL CHECK (type IN ('newspaper','magazine','book')),
              title         TEXT NOT NULL,
              issue         TEXT,
              release_date  TEXT,
              author        TEXT,
              publisher     TEXT,
              pages         INTEGER,
              quantity      INTEGER NOT NULL CHECK (quantity >= 0)
            )
            """,
                """
            CREATE UNIQUE INDEX IF NOT EXISTS ux_newspaper
              ON stock(type, title, issue, release_date)
              WHERE type='newspaper'
            """,
                """
            CREATE UNIQUE INDEX IF NOT EXISTS ux_magazine
              ON stock(type, title, issue, release_date)
              WHERE type='magazine'
            """,
                """
            CREATE UNIQUE INDEX IF NOT EXISTS ux_book
              ON stock(type, title, author, publisher)
              WHERE type='book'
            """
        };

        try (Connection c = conn()) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                for (String sql : ddl) st.execute(sql);
            }
            c.commit();
            c.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("DDL init failed", e);
        }
    }

    @Override public void load() {}
    @Override public void save() {}

    @Override
    public Optional<StockRecord> findByKey(ProductKey key) {
        KeyParts kp = KeyParts.parse(key);
        String sql = switch (kp.type) {
            case "newspaper", "magazine" ->
                    "SELECT * FROM stock WHERE type=? AND title=? AND issue=? AND release_date=?";
            case "book" ->
                    "SELECT * FROM stock WHERE type=? AND title=? AND author=? AND publisher=?";
            default -> throw new IllegalStateException("Unknown type " + kp.type);
        };
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, kp.type);
            ps.setString(i++, kp.title);
            if ("book".equals(kp.type)) {
                ps.setString(i++, kp.author);
                ps.setString(i  , kp.publisher);
            } else {
                ps.setString(i++, kp.issue);
                ps.setString(i  , kp.releaseDate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Product p = rowToProduct(rs);
                return Optional.of(new StockRecord(ProductKey.of(p), p, rs.getInt("quantity")));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<StockRecord> findAll() {
        String sql = "SELECT * FROM stock ORDER BY type, title";
        List<StockRecord> list = new ArrayList<>();
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product p = rowToProduct(rs);
                list.add(new StockRecord(ProductKey.of(p), p, rs.getInt("quantity")));
            }
            return list;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void save(StockRecord r) {
        int updated = updateByKey(r);
        if (updated == 0) insertNew(r);
    }

    @Override
    public void delete(ProductKey key) {
        KeyParts kp = KeyParts.parse(key);
        String where = ("book".equals(kp.type))
                ? "title=? AND author=? AND publisher=?"
                : "title=? AND issue=? AND release_date=?";
        String sql = "DELETE FROM stock WHERE type=? AND " + where;
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, kp.type);
            ps.setString(i++, kp.title);
            if ("book".equals(kp.type)) {
                ps.setString(i++, kp.author);
                ps.setString(i  , kp.publisher);
            } else {
                ps.setString(i++, kp.issue);
                ps.setString(i  , kp.releaseDate);
            }
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void renameKey(ProductKey oldKey, ProductKey newKey) {
        KeyParts o = KeyParts.parse(oldKey);
        KeyParts n = KeyParts.parse(newKey);
        String set, where;
        if ("book".equals(o.type)) {
            set   = "title=?, author=?, publisher=?";
            where = "type=? AND title=? AND author=? AND publisher=?";
        } else {
            set   = "title=?, issue=?, release_date=?";
            where = "type=? AND title=? AND issue=? AND release_date=?";
        }
        String sql = "UPDATE stock SET " + set + " WHERE " + where;
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, n.title);
            if ("book".equals(o.type)) {
                ps.setString(i++, n.author);
                ps.setString(i++, n.publisher);
            } else {
                ps.setString(i++, n.issue);
                ps.setString(i++, n.releaseDate);
            }
            ps.setString(i++, o.type);
            ps.setString(i++, o.title);
            if ("book".equals(o.type)) {
                ps.setString(i++, o.author);
                ps.setString(i  , o.publisher);
            } else {
                ps.setString(i++, o.issue);
                ps.setString(i  , o.releaseDate);
            }
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private int updateByKey(StockRecord r) {
        Product p = r.product();
        String where = ("book".equals(p.type()))
                ? "title=? AND author=? AND publisher=?"
                : "title=? AND issue=? AND release_date=?";
        String sql = """
            UPDATE stock
               SET quantity=?,
                   title=?, issue=?, release_date=?, author=?, publisher=?, pages=?
             WHERE type=? AND %s
            """.formatted(where);
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setInt(i++, r.quantity());
            fillProductFields(ps, p, i); i += 6;
            ps.setString(i++, p.type());
            fillKeyForProduct(ps, p, i);
            return ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private void insertNew(StockRecord r) {
        Product p = r.product();
        String sql = "INSERT INTO stock(type,title,issue,release_date,author,publisher,pages,quantity) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = conn(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, p.type());
            ps.setString(i++, p.title());
            if (p instanceof Newspaper n) {
                ps.setString(i++, n.issue());
                ps.setString(i++, n.releaseDate().toString());
                ps.setNull(i++, Types.VARCHAR);
                ps.setNull(i++, Types.VARCHAR);
                ps.setNull(i++, Types.INTEGER);
            } else if (p instanceof Magazine m) {
                ps.setString(i++, m.issue());
                ps.setString(i++, m.releaseDate().toString());
                ps.setNull(i++, Types.VARCHAR);
                ps.setNull(i++, Types.VARCHAR);
                ps.setInt(i++, m.pages());
            } else if (p instanceof Book b) {
                ps.setNull(i++, Types.VARCHAR);
                ps.setNull(i++, Types.VARCHAR);
                ps.setString(i++, b.author());
                ps.setString(i++, b.publisher());
                ps.setInt(i++, b.pages());
            }
            ps.setInt(i, r.quantity());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private static void fillProductFields(PreparedStatement ps, Product p, int startIndex) throws SQLException {
        int i = startIndex;
        ps.setString(i++, p.title());
        if (p instanceof Newspaper n) {
            ps.setString(i++, n.issue());
            ps.setString(i++, n.releaseDate().toString());
            ps.setNull(i++, Types.VARCHAR);
            ps.setNull(i++, Types.VARCHAR);
            ps.setNull(i,   Types.INTEGER);
        } else if (p instanceof Magazine m) {
            ps.setString(i++, m.issue());
            ps.setString(i++, m.releaseDate().toString());
            ps.setNull(i++, Types.VARCHAR);
            ps.setNull(i++, Types.VARCHAR);
            ps.setInt(i,   m.pages());
        } else {
            Book b = (Book) p;
            ps.setNull(i++, Types.VARCHAR);
            ps.setNull(i++, Types.VARCHAR);
            ps.setString(i++, b.author());
            ps.setString(i++, b.publisher());
            ps.setInt(i,   b.pages());
        }
    }

    private static void fillKeyForProduct(PreparedStatement ps, Product p, int startIndex) throws SQLException {
        int i = startIndex;
        if ("book".equals(p.type())) {
            Book b = (Book) p;
            ps.setString(i++, b.title());
            ps.setString(i++, b.author());
            ps.setString(i,   b.publisher());
        } else if ("newspaper".equals(p.type())) {
            Newspaper n = (Newspaper) p;
            ps.setString(i++, n.title());
            ps.setString(i++, n.issue());
            ps.setString(i,   n.releaseDate().toString());
        } else {
            Magazine m = (Magazine) p;
            ps.setString(i++, m.title());
            ps.setString(i++, m.issue());
            ps.setString(i,   m.releaseDate().toString());
        }
    }

    private static Product rowToProduct(ResultSet rs) throws SQLException {
        String type = rs.getString("type");
        String title = rs.getString("title");
        return switch (type) {
            case "newspaper" -> new Newspaper(title, rs.getString("issue"),
                    LocalDate.parse(rs.getString("release_date")));
            case "magazine" -> new Magazine(title, rs.getString("issue"),
                    LocalDate.parse(rs.getString("release_date")), rs.getInt("pages"));
            case "book" -> new Book(title, rs.getString("author"),
                    rs.getString("publisher"), rs.getInt("pages"));
            default -> throw new IllegalStateException("Unknown type " + type);
        };
    }

    private static final class KeyParts {
        final String type, title, issue, releaseDate, author, publisher;
        private KeyParts(String type, String title, String issue, String releaseDate, String author, String publisher) {
            this.type = type; this.title = title; this.issue = issue; this.releaseDate = releaseDate;
            this.author = author; this.publisher = publisher;
        }
        static KeyParts parse(ProductKey key) {
            String[] a = key.toString().split("\\|", -1);
            String type = a[0], title = a[1];
            if ("book".equals(type)) return new KeyParts(type, title, null, null, a[2], a[3]);
            return new KeyParts(type, title, a[2], a[3], null, null);
        }
    }
}
