package ru.kiosk.repository;

import ru.kiosk.domain.ProductKey;
import ru.kiosk.domain.StockRecord;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    void load();        //загрузка
    void save();        //сохранение

    Optional<StockRecord> findByKey(ProductKey key);
    List<StockRecord> findAll();

    void save(StockRecord record);
    void delete(ProductKey key);
    void renameKey(ProductKey oldKey, ProductKey newKey);
}
