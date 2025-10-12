package ru.kiosk.domain;

import java.util.Objects;

public final class Book extends Product {
    private final String author;
    private final String publisher;
    private final int pages;

    public Book(String title, String author, String publisher, int pages) {
        super(title);
        if (author == null || author.isBlank()) throw new IllegalArgumentException("author is blank");
        if (publisher == null || publisher.isBlank()) throw new IllegalArgumentException("publisher is blank");
        if (pages <= 0) throw new IllegalArgumentException("pages must be > 0");
        this.author = author.trim();
        this.publisher = publisher.trim();
        this.pages = pages;
    }

    public String author() { return author; }
    public String publisher() { return publisher; }
    public int pages() { return pages; }

    @Override public String type() { return "book"; }

    @Override public String human() {
        return "Книга \"" + title() + "\" — " + author + ", " + publisher + ", " + pages + " стр.";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book b)) return false;
        return pages == b.pages &&
                title().equals(b.title()) &&
                author.equals(b.author) &&
                publisher.equals(b.publisher);
    }
    @Override public int hashCode() { return Objects.hash(title(), author, publisher, pages); }
}
