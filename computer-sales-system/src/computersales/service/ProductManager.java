package computersales.service;

import computersales.model.Product;
import java.util.List;

public class ProductManager {
    private DatabaseService databaseService;

    public ProductManager(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public List<Product> getAvailableProducts() {
        return databaseService.getAvailableProducts();
    }

    public List<Product> getProductsBySeller(String sellerUsername) {
        return databaseService.getProductsBySeller(sellerUsername);
    }

    public boolean addProduct(Product product) {
        return databaseService.addProduct(product);
    }

    public boolean deleteProduct(int productId) {
        return databaseService.deleteProduct(productId);
    }

    public Product getProductById(int productId) {
        List<Product> allProducts = databaseService.getAllProducts();
        return allProducts.stream()
                .filter(product -> product.getId() == productId)
                .findFirst()
                .orElse(null);
    }

    public boolean purchaseProduct(int productId, String buyerUsername) {
        Product product = getProductById(productId);
        if (product == null || !product.isAvailable() || product.getStock() <= 0) {
            return false;
        }
        
        // Stok güncelleme
        boolean stockUpdated = databaseService.updateProductStock(productId, product.getStock() - 1);
        if (!stockUpdated) {
            return false;
        }
        
        // Ürünü satın alan kişiyi güncelle
        return databaseService.updateProductBuyer(productId, buyerUsername);
    }
} 