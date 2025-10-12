package ru.kiosk.domain;

import java.time.LocalDate;
import java.util.Objects;
public final class Magazine extends Product {
    private final String issue;
    private final LocalDate releaseDate;
    private final int pages;

    public Magazine(String title, String issue, LocalDate releaseDate, int pages) {
        super(title);
        if (issue == null || issue.isBlank()) throw new IllegalArgumentException("issue is blank");
        if (releaseDate == null) throw new IllegalArgumentException("releaseDate is null");
        if (pages <= 0) throw new IllegalArgumentException("pages must be > 0");
        this.issue = issue.trim();
        this.releaseDate = releaseDate;
        this.pages = pages;
    }

    public String issue() { return issue; }
    public LocalDate releaseDate() { return releaseDate; }
    public int pages() { return pages; }

    @Override public String type() { return "magazine"; }

    @Override public String human() {
        return "Журнал \"" + title() + "\", №" + issue + ", " + releaseDate + ", " + pages + " стр.";
    }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Magazine m)) return false;
        return pages == m.pages &&
                title().equals(m.title()) &&
                issue.equals(m.issue) &&
                releaseDate.equals(m.releaseDate);
    }
    @Override public int hashCode() { return Objects.hash(title(), issue, releaseDate, pages); }

}
