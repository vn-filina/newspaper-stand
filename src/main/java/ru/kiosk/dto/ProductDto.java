package ru.kiosk.dto;

import ru.kiosk.domain.*;
import java.time.LocalDate;
public final class ProductDto {
    public String type;     //газета или журнал или книга
    public String title;

    public String issue;        //для газеты и журнала
    public String releaseDate;
    public Integer pages;

    public String author;       //для книги
    public String publisher;

    public static ProductDto newspaper(String title, String issue, LocalDate date) {
        ProductDto d = new ProductDto();
        d.type = "newspaper"; d.title = title; d.issue = issue; d.releaseDate = date.toString();
        return d;
    }
    public static ProductDto magazine(String title, String issue, LocalDate date, int pages) {
        ProductDto d = new ProductDto();
        d.type = "magazine"; d.title = title; d.issue = issue; d.releaseDate = date.toString(); d.pages = pages;
        return d;
    }
    public static ProductDto book(String title, String author, String publisher, int pages) {
        ProductDto d = new ProductDto();
        d.type = "book"; d.title = title; d.author = author; d.publisher = publisher; d.pages = pages;
        return d;
    }

    public static ProductDto patch(String title, String issue, String date, String pages,
                                   String author, String publisher) {
        ProductDto d = new ProductDto();
        d.title = empty(title);
        d.issue = empty(issue);
        d.releaseDate = empty(date);
        d.author = empty(author);
        d.publisher = empty(publisher);
        if (pages != null && !pages.isBlank()) d.pages = Integer.parseInt(pages.trim());
        return d;
    }
    private static String empty(String s){ return (s==null||s.isBlank())?null:s.trim(); }

    public Product toDomain() {
        return switch (type) {
            case "newspaper" -> new Newspaper(title, issue, LocalDate.parse(releaseDate));
            case "magazine"  -> new Magazine(title, issue, LocalDate.parse(releaseDate), pages);
            case "book"      -> new Book(title, author, publisher, pages);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    public Product applyTo(Product original) {
        if (original instanceof Newspaper n) {
            return new Newspaper(
                    title   != null ? title   : n.title(),
                    issue   != null ? issue   : n.issue(),
                    releaseDate != null ? LocalDate.parse(releaseDate) : n.releaseDate()
            );
        } else if (original instanceof Magazine m) {
            return new Magazine(
                    title   != null ? title   : m.title(),
                    issue   != null ? issue   : m.issue(),
                    releaseDate != null ? LocalDate.parse(releaseDate) : m.releaseDate(),
                    pages   != null ? pages   : m.pages()
            );
        } else if (original instanceof Book b) {
            return new Book(
                    title     != null ? title     : b.title(),
                    author    != null ? author    : b.author(),
                    publisher != null ? publisher : b.publisher(),
                    pages     != null ? pages     : b.pages()
            );
        }
        return original;
    }
}
