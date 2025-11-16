package computersales.test;

import computersales.model.Product;
import computersales.service.DatabaseService;
import computersales.service.ProductManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class GecmisTesti {
    private DatabaseService dbService;
    private ProductManager urunYoneticisi;
    private static final String SATICI = "satici";
    private static final String ALICI = "alici";
    private static final String SIFRE = "123456";
    private static final String EMAIL = "test@test.com";
    private static final String PHONE = "5555555555";

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Satış Geçmişi Testi Başlıyor ===");
        dbService = DatabaseService.getInstance();
        urunYoneticisi = new ProductManager(dbService);
        
        // Kullanıcıların var olup olmadığını kontrol et
        if (dbService.getUserByUsername(SATICI) == null) {
            System.out.println("Satıcı kullanıcısı oluşturuluyor: " + SATICI);
            dbService.register(SATICI, SIFRE, "seller", EMAIL, PHONE);
        } else {
            System.out.println("Satıcı kullanıcısı zaten mevcut: " + SATICI);
        }
        
        if (dbService.getUserByUsername(ALICI) == null) {
            System.out.println("Alıcı kullanıcısı oluşturuluyor: " + ALICI);
            dbService.register(ALICI, SIFRE, "buyer", EMAIL, PHONE);
        } else {
            System.out.println("Alıcı kullanıcısı zaten mevcut: " + ALICI);
        }
    }

    @Test
    void satisGecmisiTesti() {
        System.out.println("\n--- Satış Geçmişi Testi Başlıyor ---");
        
        // Test ürünü oluştur
        Product urun = new Product(0, "Test Laptop", "Test açıklama", 1000.0, SATICI, "Laptop");
        urun.setStock(5);
        System.out.println("Test ürünü oluşturuldu: " + urun.getName());
        
        // Ürünü ekle
        boolean urunEklendi = urunYoneticisi.addProduct(urun);
        assertTrue(urunEklendi, "Ürün eklenemedi");
        System.out.println("Ürün başarıyla eklendi. ID: " + urun.getId());
        
        // Ürünü satın al
        boolean satinAlindi = urunYoneticisi.purchaseProduct(urun.getId(), ALICI);
        assertTrue(satinAlindi, "Ürün satın alınamadı");
        System.out.println("Ürün başarıyla satın alındı");
        
        // Satış geçmişini kaydet
        boolean satisGecmisiEklendi = dbService.addSalesHistory(urun.getId(), SATICI, ALICI);
        assertTrue(satisGecmisiEklendi, "Satış geçmişi eklenemedi");
        System.out.println("Satış geçmişi başarıyla kaydedildi");
        
        // Satış geçmişini kontrol et
        List<Map<String, Object>> satisGecmisi = dbService.getSalesHistory(SATICI);
        assertNotNull(satisGecmisi, "Satış geçmişi null olamaz");
        assertFalse(satisGecmisi.isEmpty(), "Satış geçmişi boş olamaz");
        System.out.println("Satış geçmişi başarıyla alındı. Kayıt sayısı: " + satisGecmisi.size());
        
        // Satış detaylarını kontrol et
        Map<String, Object> satis = satisGecmisi.get(0);
        assertEquals(urun.getName(), satis.get("name"), "Ürün adı eşleşmiyor");
        assertEquals(urun.getPrice(), satis.get("price"), "Ürün fiyatı eşleşmiyor");
        System.out.println("Satış detayları doğrulandı:");
        System.out.println("- Ürün Adı: " + satis.get("name"));
        System.out.println("- Fiyat: " + satis.get("price"));
        System.out.println("- Satıcı: " + satis.get("seller_username"));
        System.out.println("- Alıcı: " + satis.get("buyer_username"));
        
        System.out.println("--- Satış Geçmişi Testi Tamamlandı ---\n");
    }
}