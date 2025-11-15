package ru.kiosk.api;

import ru.kiosk.dto.*;
import ru.kiosk.service.InventoryService;
import java.util.List;
public class InventoryApi {
    private final InventoryService service;
    public InventoryApi(InventoryService service) { this.service = service; }

    public ProductResponse receipt(ReceiptRequest r){ return service.receipt(r); }
    public ProductResponse sale(SaleRequest r){ return service.sale(r); }
    public ProductResponse update(UpdateRequest r){ return service.update(r); }
    public List<String> findAll(){ return service.describeAll(); }
}