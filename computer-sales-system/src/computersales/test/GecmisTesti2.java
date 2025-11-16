package computersales.test;

import computersales.model.Product;
import computersales.service.DatabaseService;
import computersales.service.ProductManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class GecmisTesti2 {
    private DatabaseService dbService;
    private ProductManager urunYoneticisi;
    private static final String SATICI = "satici";
    private static final String ALICI = "alici";
    private static final String SIFRE = "123456";
    private static final String EMAIL = "test@test.com";
    private static final String PHONE = "5555555555";

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Satın Alma Geçmişi Testi Başlıyor ===");
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
    void satinAlmaGecmisiTesti() {
        System.out.println("\n--- Satın Alma Geçmişi Testi Başlıyor ---");
        
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
        
        // Satın alma geçmişini kaydet
        boolean satinAlmaGecmisiEklendi = dbService.addPurchaseHistory(urun.getId(), ALICI, SATICI);
        assertTrue(satinAlmaGecmisiEklendi, "Satın alma geçmişi eklenemedi");
        System.out.println("Satın alma geçmişi başarıyla kaydedildi");
        
        // Satın alma geçmişini kontrol et
        List<Map<String, Object>> satinAlmaGecmisi = dbService.getPurchaseHistory(ALICI);
        assertNotNull(satinAlmaGecmisi, "Satın alma geçmişi null olamaz");
        assertFalse(satinAlmaGecmisi.isEmpty(), "Satın alma geçmişi boş olamaz");
        System.out.println("Satın alma geçmişi başarıyla alındı. Kayıt sayısı: " + satinAlmaGecmisi.size());
        
        // Satın alma detaylarını kontrol et
        Map<String, Object> satinAlma = satinAlmaGecmisi.get(0);
        assertEquals(urun.getName(), satinAlma.get("name"), "Ürün adı eşleşmiyor");
        assertEquals(urun.getPrice(), satinAlma.get("price"), "Ürün fiyatı eşleşmiyor");
        System.out.println("Satın alma detayları doğrulandı:");
        System.out.println("- Ürün Adı: " + satinAlma.get("name"));
        System.out.println("- Fiyat: " + satinAlma.get("price"));
        System.out.println("- Alıcı: " + satinAlma.get("buyer_username"));
        System.out.println("- Satıcı: " + satinAlma.get("seller_username"));
        
        System.out.println("--- Satın Alma Geçmişi Testi Tamamlandı ---\n");
    }
} 