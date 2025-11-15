package ru.kiosk.db;

import ru.kiosk.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DriverManagerConnectionFactory implements ConnectionFactory {
    private final String url;

    public DriverManagerConnectionFactory(String url) {
        this.url = url;
        try { Class.forName("org.sqlite.JDBC"); } catch (ClassNotFoundException ignored) {}
    }

    @Override public Connection get() throws SQLException { return DriverManager.getConnection(url); }
    @Override public String url() { return url; }

    @Override public void beforeFirstUse() {
        AppConfig.ensureDirIfSQLite(url);
    }
}
