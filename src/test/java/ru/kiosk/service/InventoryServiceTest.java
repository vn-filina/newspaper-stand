package ru.kiosk.service;

import org.junit.jupiter.api.Test;
import ru.kiosk.domain.ProductKey;
import ru.kiosk.domain.StockRecord;
import ru.kiosk.dto.*;
import ru.kiosk.repository.InventoryRepository;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemRepo implements InventoryRepository {
    Map<ProductKey, StockRecord> map = new HashMap<>();
    public void load() {}
    public void save() {}
    public Optional<StockRecord> findByKey(ProductKey key) { return Optional.ofNullable(map.get(key)); }
    public List<StockRecord> findAll() { return new ArrayList<>(map.values()); }
    public void save(StockRecord record) { map.put(record.key(), record); }
    public void delete(ProductKey key) { map.remove(key); }
    public void renameKey(ProductKey oldKey, ProductKey newKey) {
        StockRecord r = map.remove(oldKey);
        if (r != null) map.put(newKey, new StockRecord(newKey, r.product(), r.quantity()));
    }
}

public class InventoryServiceTest {

    @Test
    void receiptIncreasesStock() {
        var service = new InventoryService(new InMemRepo());
        var dto = ProductDto.newspaper("Ведомости", "123", java.time.LocalDate.of(2025,10,1));

        var res1 = service.receipt(new ReceiptRequest(dto, 3));
        assertTrue(res1.ok());

        var res2 = service.receipt(new ReceiptRequest(dto, 2));
        assertTrue(res2.ok());

        var list = service.describeAll();
        assertTrue(list.get(0).contains("остаток: 5"));
    }

    @Test
    void saleFailsWhenNotEnough() {
        var repo = new InMemRepo();
        var service = new InventoryService(repo);
        var dto = ProductDto.magazine("Forbes", "10", java.time.LocalDate.of(2025,9,1), 96);

        service.receipt(new ReceiptRequest(dto, 1));
        var res = service.sale(new SaleRequest(dto, 2));
        assertFalse(res.ok());
        assertTrue(res.message().contains("Недостаточно"));
    }

    @Test
    void updateMergesWhenKeyChangesToExisting() {
        var repo = new InMemRepo();
        var service = new InventoryService(repo);
        var dto1 = ProductDto.book("Clean Code", "Robert Martin", "Prentice", 464);
        var dto2 = ProductDto.book("Clean Code", "Robert Martin", "Prentice", 464);

        service.receipt(new ReceiptRequest(dto1, 2));
        service.receipt(new ReceiptRequest(dto2, 1));

        var res = service.update(new UpdateRequest(dto1, ProductDto.patch(null, null, null, "10", null, null)));
        assertTrue(res.ok());

        var list = service.describeAll();
        assertTrue(list.get(0).contains("остаток: 3"));
    }
}
