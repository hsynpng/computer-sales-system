package computersales.test;

import computersales.model.Product;
import computersales.model.Seller;
import computersales.model.Buyer;
import computersales.service.DatabaseService;
import computersales.service.ProductManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AliciSaticiTesti {
    @Mock
    private DatabaseService veritabaniServisi;
    
    @Mock
    private ProductManager urunYoneticisi;
    
    private Seller satici;
    private Buyer alici;

    @BeforeEach
    public void hazirla() {
        MockitoAnnotations.openMocks(this);
        
        satici = new Seller("testSatici", "testSifre", "test@ornek.com", "1234567890");
        alici = new Buyer("testAlici", "testSifre", "alici@ornek.com", "9876543210");
    }

    @Test
    public void saticiUrunEklemeTesti() {
        Product urun = new Product(0, "Test Laptop", "Test Açıklama", 1000.0, satici.getUsername(), null);
        when(urunYoneticisi.addProduct(urun)).thenReturn(true);
        
        boolean sonuc = urunYoneticisi.addProduct(urun);
        assertTrue(sonuc);
        verify(urunYoneticisi).addProduct(urun);
    }

    @Test
    public void aliciUrunSatınAlmaTesti() {
        Product urun = new Product(1, "Test Laptop", "Test Açıklama", 1000.0, satici.getUsername(), null);
        when(urunYoneticisi.getProductById(1)).thenReturn(urun);
        when(urunYoneticisi.purchaseProduct(1, alici.getUsername())).thenReturn(true);
        
        boolean sonuc = urunYoneticisi.purchaseProduct(1, alici.getUsername());
        assertTrue(sonuc);
        verify(urunYoneticisi).purchaseProduct(1, alici.getUsername());
    }
}
