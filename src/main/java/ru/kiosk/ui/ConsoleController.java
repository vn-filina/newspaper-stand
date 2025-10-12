package ru.kiosk.ui;

import ru.kiosk.api.InventoryApi;
public class ConsoleController {
    private final InventoryApi api;
    public ConsoleController(InventoryApi api) { this.api = api; }

    public void run() {
        throw new UnsupportedOperationException("Console UI is not implemented yet");
    }
}
