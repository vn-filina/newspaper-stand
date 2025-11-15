package ru.kiosk.repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.kiosk.domain.*;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class FileInventoryRepository implements InventoryRepository {
    private final Path file;
    private final Map<ProductKey, StockRecord> storage = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FileInventoryRepository(String path) {
        this.file = Path.of(path);
        try { Files.createDirectories(file.getParent()); } catch (Exception ignored) {}
    }

    @Override public void load() {
        if (!Files.exists(file)) return;
        try (Reader r = Files.newBufferedReader(file)) {
            Type listType = new TypeToken<List<Snapshot>>(){}.getType();
            List<Snapshot> list = gson.fromJson(r, listType);
            storage.clear();
            if (list != null) {
                for (Snapshot s : list) {
                    Product product = s.toDomain();
                    ProductKey key = ProductKey.of(product);           // <— ключ восстанавливаем по продукту
                    storage.put(key, new StockRecord(key, product, s.quantity));
                }
            }
            System.out.println("Загружено позиций: " + storage.size());
        } catch (Exception e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
        }
    }

    @Override public void save() {
        List<Snapshot> list = storage.values().stream().map(Snapshot::from).toList();
        try (Writer w = Files.newBufferedWriter(file)) {
            gson.toJson(list, w);
        } catch (Exception e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    @Override public Optional<StockRecord> findByKey(ProductKey key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override public List<StockRecord> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override public void save(StockRecord record) {
        storage.put(record.key(), record);
    }

    @Override public void delete(ProductKey key) {
        storage.remove(key);
    }

    @Override public void renameKey(ProductKey oldKey, ProductKey newKey) {
        StockRecord rec = storage.remove(oldKey);
        if (rec != null) storage.put(newKey, new StockRecord(newKey, rec.product(), rec.quantity()));
    }
    private static final class Snapshot {
        // ProductKey key;  // — убрали
        String type, title, issue, author, publisher;
        String releaseDate; // ISO
        Integer pages;
        int quantity;

        static Snapshot from(StockRecord r) {
            Snapshot s = new Snapshot();
            Product p = r.product();
            s.type = p.type();
            s.title = p.title();
            if (p instanceof Newspaper n) {
                s.issue = n.issue();
                s.releaseDate = n.releaseDate().toString();
            } else if (p instanceof Magazine m) {
                s.issue = m.issue();
                s.releaseDate = m.releaseDate().toString();
                s.pages = m.pages();
            } else if (p instanceof Book b) {
                s.author = b.author();
                s.publisher = b.publisher();
                s.pages = b.pages();
            }
            s.quantity = r.quantity();
            return s;
        }

        Product toDomain() {
            return switch (type) {
                case "newspaper" -> new Newspaper(title, issue, LocalDate.parse(releaseDate));
                case "magazine"  -> new Magazine(title, issue, LocalDate.parse(releaseDate), pages);
                case "book"      -> new Book(title, author, publisher, pages);
                default -> throw new IllegalStateException("Unknown type " + type);
            };
        }
    }
}