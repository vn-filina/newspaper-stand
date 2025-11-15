package ru.kiosk.service;

import ru.kiosk.domain.*;
import ru.kiosk.dto.*;
import ru.kiosk.repository.InventoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
public class InventoryService {
    private final InventoryRepository repo;
    public InventoryService(InventoryRepository repo) { this.repo = repo; }
    public ProductResponse receipt(ReceiptRequest req) {
        Product p = req.dto().toDomain();
        ProductKey key = ProductKey.of(p);
        int qty = req.quantity();
        Optional<StockRecord> ex = repo.findByKey(key);

        if (ex.isPresent()) {
            ex.get().increase(qty);
            return ProductResponse.ok("Поступило +" + qty + ". Остаток: " + ex.get().quantity());
        } else {
            repo.save(new StockRecord(key, p, qty));
            return ProductResponse.ok("Добавлен новый товар. Остаток: " + qty);
        }
    }
    public ProductResponse sale(SaleRequest req) {
        Product p = req.dto().toDomain();
        ProductKey key = ProductKey.of(p);
        int qty = req.quantity();

        Optional<StockRecord> rec = repo.findByKey(key);
        if (rec.isEmpty()) return ProductResponse.error("Товар не найден на складе");
        if (rec.get().quantity() < qty) return ProductResponse.error("Недостаточно на складе. Доступно: " + rec.get().quantity());

        rec.get().decrease(qty);
        if (rec.get().quantity() == 0) repo.delete(key);
        return ProductResponse.ok("Продано " + qty + ". Остаток: " + rec.get().quantity());
    }
    public ProductResponse update(UpdateRequest req) {
        ProductKey key = ProductKey.of(req.key().toDomain());
        Optional<StockRecord> recOpt = repo.findByKey(key);
        if (recOpt.isEmpty()) return ProductResponse.error("Товар для редактирования не найден");

        StockRecord rec = recOpt.get();
        Product patched = req.patch().applyTo(rec.product());
        ProductKey newKey = ProductKey.of(patched);

        if (!newKey.equals(key) && repo.findByKey(newKey).isPresent()) {
            // объединяем остатки, старую запись удаляем
            repo.findByKey(newKey).ifPresent(existing -> existing.increase(rec.quantity()));
            repo.delete(key);
        } else {
            rec.replaceProduct(patched);
            rec.renameKey(newKey);
            repo.save(rec);
        }
        return ProductResponse.ok("Товар обновлён");
    }
    public List<String> describeAll() {
        return repo.findAll().stream()
                .sorted((a,b)->a.key().toString().compareTo(b.key().toString()))
                .map(StockRecord::toHumanString)
                .collect(Collectors.toList());
    }
}
