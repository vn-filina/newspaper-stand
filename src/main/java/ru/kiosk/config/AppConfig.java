package ru.kiosk.config;

import java.nio.file.Files;
import java.nio.file.Path;

public final class AppConfig {
    private AppConfig() {}

    public static String jdbcUrl() {
        String env = System.getenv("KIOSK_JDBC_URL");
        if (env != null && !env.isBlank()) return env.trim();
        String prop = System.getProperty("kiosk.jdbc.url");
        if (prop != null && !prop.isBlank()) return prop.trim();
        return "jdbc:sqlite:data/inventory.db";
    }

    public static void ensureDirIfSQLite(String url) {
        try {
            String prefix = "jdbc:sqlite:";
            String pathStr = url.startsWith(prefix) ? url.substring(prefix.length()) : null;
            if (pathStr == null) return;
            Path p = Path.of(pathStr).toAbsolutePath();
            if (p.getParent() != null) Files.createDirectories(p.getParent());
        } catch (Exception e) {
            throw new RuntimeException("Cannot create DB directory for url=" + url, e);
        }
    }
}

