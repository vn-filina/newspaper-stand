package ru.kiosk.dto;

public record ProductResponse (boolean ok, String message) {
    public static ProductResponse ok(String m)    { return new ProductResponse(true, m); }
    public static ProductResponse error(String m) { return new ProductResponse(false, m); }
}
