package ru.kiosk.domain;

public final class StockRecord {        //это для склада
    private ProductKey key;
    private Product product;
    private int quantity;

    public StockRecord(ProductKey key, Product product, int quantity) {
        if (key == null || product == null) throw new IllegalArgumentException("key/product is null");
        if (quantity < 0) throw new IllegalArgumentException("quantity < 0");
        this.key = key; this.product = product; this.quantity = quantity;
    }

    public ProductKey key() { return key; }
    public Product product() { return product; }
    public int quantity() { return quantity; }

    public void increase(int q) { if (q < 0) throw new IllegalArgumentException(); quantity += q; }
    public void decrease(int q) { if (q < 0 || q > quantity) throw new IllegalArgumentException(); quantity -= q; }

    public void replaceProduct(Product p) { if (p == null) throw new IllegalArgumentException(); this.product = p; }
    public void renameKey(ProductKey newKey) { if (newKey == null) throw new IllegalArgumentException(); this.key = newKey; }

    public String toHumanString() { return product.human() + " — остаток: " + quantity + " шт."; }

}


