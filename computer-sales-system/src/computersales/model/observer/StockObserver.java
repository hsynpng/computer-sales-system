package computersales.model.observer;

import computersales.model.Product;

public class StockObserver implements Observer {
    private Product product;
    private int lastKnownStock;
    // Son bilinen stoku tutar

    public StockObserver(Product product) {
        this.product = product;
        this.lastKnownStock = product.getStock();
        product.attach(this); // Kendini gözlemci olarak kaydeder
    }

    @Override
    public void update() {
        int currentStock = product.getStock();
        if (currentStock != lastKnownStock) {
            System.out.println("Stok Değişikliği - Ürün: " + product.getName());
            System.out.println("Eski Stok: " + lastKnownStock);
            System.out.println("Yeni Stok: " + currentStock);
            
            if (currentStock < 5) {
                System.out.println("UYARI: Stok seviyesi düşük!");
            }
            
            lastKnownStock = currentStock;
        }
    }
} 