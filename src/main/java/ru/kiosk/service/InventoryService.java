package ru.kiosk.service;

import ru.kiosk.repository.InventoryRepository;
public class InventoryService {
    private final InventoryRepository repo;
    public InventoryService(InventoryRepository repo) { this.repo = repo; }
}
