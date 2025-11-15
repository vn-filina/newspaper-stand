package ru.kiosk.app;

import ru.kiosk.api.InventoryApi;
import ru.kiosk.repository.FileInventoryRepository;
import ru.kiosk.repository.InventoryRepository;
import ru.kiosk.service.InventoryService;
import ru.kiosk.ui.ConsoleController;
import ru.kiosk.repository.JdbcInventoryRepository;
import ru.kiosk.config.AppConfig;
import ru.kiosk.db.ConnectionFactory;
import ru.kiosk.db.DriverManagerConnectionFactory;


public class Main {
    public static void main(String[] args) {
        String url = AppConfig.jdbcUrl();

        ConnectionFactory cf = new DriverManagerConnectionFactory(url);
        InventoryRepository repo = new JdbcInventoryRepository(cf);
        InventoryService service = new InventoryService(repo);
        InventoryApi api = new InventoryApi(service);

        new ConsoleController(api).run();
    }
}
