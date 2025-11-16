package computersales.model.observer;

import computersales.model.Product;

public class PriceObserver implements Observer {
    private Product product;
    private double lastKnownPrice;

    // fiyat değişimini gözlemlemek.

    public PriceObserver(Product product) {
        this.product = product;
        this.lastKnownPrice = product.getPrice();
        product.attach(this);
    }

    @Override
    public void update() {
        double currentPrice = product.getPrice();
        if (currentPrice != lastKnownPrice) {
            System.out.println("Fiyat Değişikliği - Ürün: " + product.getName());
            System.out.println("Eski Fiyat: " + lastKnownPrice + " TL");
            System.out.println("Yeni Fiyat: " + currentPrice + " TL");
            
            double priceDifference = currentPrice - lastKnownPrice;
            if (priceDifference > 0) {
                System.out.println("Fiyat artışı: +" + priceDifference + " TL");
            } else {
                System.out.println("Fiyat düşüşü: " + priceDifference + " TL");
            }
            
            lastKnownPrice = currentPrice;
        }
    }
} 