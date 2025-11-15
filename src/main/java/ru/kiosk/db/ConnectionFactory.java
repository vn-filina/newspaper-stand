package ru.kiosk.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory {
    Connection get() throws SQLException;
    String url();
    default void beforeFirstUse() {}
}
