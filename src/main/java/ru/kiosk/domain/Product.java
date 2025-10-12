package ru.kiosk.domain;

public abstract class Product {
    private final String title;

    protected Product(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title is blank");
        this.title = title.trim();
    }

    public String title() { return title; }
    public abstract String type();
    public abstract String human();
}
