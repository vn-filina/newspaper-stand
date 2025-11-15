package ru.kiosk.domain;

import java.time.LocalDate;
import java.util.Objects;
public final class ProductKey {
    private final String type;
    private final String title;
    private final String issue;     //газета
    private final LocalDate date;       //газета
    private final String author;        //книга
    private final String publisher;     //книга

    private ProductKey(String type, String title, String issue, LocalDate date, String author, String publisher) {
        this.type = type; this.title = title; this.issue = issue; this.date = date; this.author = author; this.publisher = publisher;
    }

    public static ProductKey of(Product p) {
        return switch (p.type()) {
            case "newspaper" -> {
                Newspaper n = (Newspaper) p;
                yield new ProductKey(p.type(), p.title(), n.issue(), n.releaseDate(), null, null);
            }
            case "magazine" -> {
                Magazine m = (Magazine) p;
                yield new ProductKey(p.type(), p.title(), m.issue(), m.releaseDate(), null, null);
            }
            case "book" -> {
                Book b = (Book) p;
                yield new ProductKey(p.type(), p.title(), null, null, b.author(), b.publisher());
            }
            default -> throw new IllegalStateException("Unknown type " + p.type());
        };
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductKey k)) return false;
        return Objects.equals(type, k.type) &&
                Objects.equals(title, k.title) &&
                Objects.equals(issue, k.issue) &&
                Objects.equals(date, k.date) &&
                Objects.equals(author, k.author) &&
                Objects.equals(publisher, k.publisher);
    }

    @Override public int hashCode() { return Objects.hash(type, title, issue, date, author, publisher); }

    @Override public String toString() {
        return switch (type) {
            case "newspaper" -> "newspaper|" + title + "|" + issue + "|" + date;
            case "magazine"  -> "magazine|"  + title + "|" + issue + "|" + date;
            case "book"      -> "book|"      + title + "|" + author + "|" + publisher;
            default -> type + "|" + title;
        };
    }
}
