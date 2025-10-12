package ru.kiosk.domain;

import java.time.LocalDate;
import java.util.Objects;
public final class Newspaper extends Product {
    private final String issue;
    private final LocalDate releaseDate;

    public Newspaper(String title, String issue, LocalDate releaseDate) {
        super(title);
        if (issue == null || issue.isBlank()) throw new IllegalArgumentException("issue is blank");
        if (releaseDate == null) throw new IllegalArgumentException("releaseDate is null");
        this.issue = issue.trim();
        this.releaseDate = releaseDate;
    }

    public String issue() { return issue; }
    public LocalDate releaseDate() { return releaseDate; }

    @Override public String type() { return "newspaper"; }

    @Override public String human() {
        return "Газета \"" + title() + "\", №" + issue + ", " + releaseDate;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Newspaper n)) return false;
        return title().equals(n.title()) &&
                issue.equals(n.issue) &&
                releaseDate.equals(n.releaseDate);
    }
    @Override public int hashCode() { return Objects.hash(title(), issue, releaseDate); }
}
