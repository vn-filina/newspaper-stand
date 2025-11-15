package ru.kiosk.app;

import ru.kiosk.api.InventoryApi;
import ru.kiosk.repository.FileInventoryRepository;
import ru.kiosk.repository.InventoryRepository;
import ru.kiosk.service.InventoryService;
import ru.kiosk.ui.ConsoleController;

public class Main {
    public static void main(String[] args) {
        String path = "data/inventory.json";

        InventoryRepository repo = new FileInventoryRepository(path);
        InventoryService service = new InventoryService(repo);
        InventoryApi api = new InventoryApi(service);

        repo.load();                                   // загрузка данных
        Runtime.getRuntime().addShutdownHook(new Thread(repo::save)); // автосохранение

        new ConsoleController(api).run();              // консольное меню
    }
}
