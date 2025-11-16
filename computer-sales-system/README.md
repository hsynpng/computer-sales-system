# 💻 Computer Sales System

> **Modern Desktop Application for Computer Sales Management**

Modern, kullanıcı dostu ve tam özellikli bir masaüstü bilgisayar satış yönetim sistemi. JavaFX ile geliştirilmiş bu proje, alıcılar, satıcılar ve yöneticiler için kapsamlı bir e-ticaret çözümü sunar.

![Status](https://img.shields.io/badge/Status-Ready-brightgreen) ![Java](https://img.shields.io/badge/Java-21-orange) ![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-blue) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-42.6.0-blue) ![Maven](https://img.shields.io/badge/Maven-3.11.0-red) ![License](https://img.shields.io/badge/License-MIT-green)

---

## ✨ Özellikler

- 🎨 **Modern JavaFX Arayüzü**: Kullanıcı dostu ve profesyonel görünüm
- 👥 **Çoklu Kullanıcı Rolleri**: Alıcı, Satıcı ve Yönetici rolleri
- 🛍️ **Ürün Yönetimi**: Ürün ekleme, düzenleme, silme ve listeleme
- 💰 **Bakiye Sistemi**: Alıcı ve satıcı bakiyesi yönetimi
- 📊 **Satış Geçmişi**: Detaylı satış ve satın alma geçmişi takibi
- 🔔 **Observer Pattern**: Fiyat ve stok değişikliklerinde otomatik bildirimler
- 🎭 **Decorator Pattern**: Premium, VIP ve Doğrulanmış kullanıcı özellikleri
- 📦 **Stok Yönetimi**: Gerçek zamanlı stok takibi ve uyarıları
- 🔐 **Güvenli Kimlik Doğrulama**: Kullanıcı kayıt, giriş ve şifre sıfırlama
- 📈 **İstatistikler**: Satış ve gelir istatistikleri
- 🧪 **Unit Testler**: JUnit 5 ve Mockito ile kapsamlı test kapsamı

---

## 🛠️ Kullanılan Teknolojiler

- **Java 21** - Modern Java özellikleri
- **JavaFX 21.0.1** - Desktop GUI framework
- **PostgreSQL 42.6.0** - İlişkisel veritabanı
- **Maven 3.11.0** - Bağımlılık yönetimi ve build tool
- **JUnit 5.10.0** - Unit test framework
- **Mockito 5.5.0** - Mocking framework

---

## 📦 Kurulum

### Gereksinimler

- **Java 21** veya üzeri
- **Maven 3.6+**
- **PostgreSQL 12+**
- **JavaFX SDK 21+** (Maven ile otomatik indirilir)

### Adım 1: Projeyi Klonlayın

```bash
git clone https://github.com/hsynpng/computer-sales-system.git
cd computer-sales-system
```

### Adım 2: PostgreSQL Veritabanını Kurun

1. PostgreSQL'i yükleyin ve başlatın
2. Yeni bir veritabanı oluşturun:

```sql
CREATE DATABASE ikiniceldb;
```

### Adım 3: Veritabanı Bağlantı Bilgilerini Güncelleyin

⚠️ **GÜVENLİK NOTU**: Projeyi kullanmadan önce veritabanı bağlantı bilgilerinizi güncellemeniz gerekmektedir!

`src/computersales/service/DatabaseService.java` dosyasında aşağıdaki satırları kendi veritabanı bilgilerinizle değiştirin:

```java
private static final String DB_URL = "jdbc:postgresql://localhost:5432/ikiniceldb";
private static final String USER = "postgres";        // Kendi kullanıcı adınız
private static final String PASSWORD = "postgres";    // Kendi şifreniz
```

**ÖNEMLİ**: Bu bilgileri GitHub'a yüklemeyin! `.gitignore` dosyasına eklenmiştir, ancak yine de dikkatli olun.

### Adım 4: Maven Bağımlılıklarını Yükleyin

```bash
mvn clean install
```

### Adım 5: Uygulamayı Çalıştırın

#### Maven ile:

```bash
mvn javafx:run
```

#### IDE ile:

- IntelliJ IDEA veya Eclipse'de projeyi açın
- `MainApp.java` dosyasını çalıştırın
- Main class: `computersales.MainApp`

---

## 🎯 Kullanım

### İlk Kullanım

1. Uygulamayı başlattığınızda **Kayıt Ol** ekranı açılır
2. Yeni bir kullanıcı hesabı oluşturun (Alıcı veya Satıcı)
3. Giriş yapın ve sistemi kullanmaya başlayın

### Alıcı Özellikleri

- ✅ Tüm ürünleri görüntüleme
- ✅ Ürün satın alma
- ✅ Bakiye ekleme
- ✅ Satın alma geçmişini görüntüleme
- ✅ Premium/VIP üyelik avantajları

### Satıcı Özellikleri

- ✅ Ürün ekleme, düzenleme ve silme
- ✅ Toplu ürün ekleme (CSV)
- ✅ Satış geçmişini görüntüleme
- ✅ Stok yönetimi
- ✅ Gelir takibi

### Yönetici Özellikleri

- ✅ Tüm kullanıcıları görüntüleme
- ✅ Kullanıcı yönetimi
- ✅ Sistem istatistikleri

---

## 🏗️ Proje Yapısı

```
computer-sales-system/
├── src/
│   └── computersales/
│       ├── MainApp.java                 # Ana uygulama giriş noktası
│       ├── model/                       # Veri modelleri
│       │   ├── User.java               # Temel kullanıcı sınıfı
│       │   ├── Buyer.java              # Alıcı modeli
│       │   ├── Seller.java             # Satıcı modeli
│       │   ├── Product.java            # Ürün modeli
│       │   ├── decorator/               # Decorator Pattern
│       │   │   ├── UserDecorator.java
│       │   │   ├── PremiumUserDecorator.java
│       │   │   ├── VIPUserDecorator.java
│       │   │   └── VerifiedUserDecorator.java
│       │   └── observer/                # Observer Pattern
│       │       ├── Observer.java
│       │       ├── Subject.java
│       │       ├── PriceObserver.java
│       │       └── StockObserver.java
│       ├── service/                     # İş mantığı servisleri
│       │   ├── DatabaseService.java     # Veritabanı işlemleri
│       │   └── ProductManager.java      # Ürün yönetimi
│       ├── ui/                          # Kullanıcı arayüzü
│       │   └── screens/
│       │       ├── LoginScreen.java
│       │       ├── RegisterScreen.java
│       │       ├── BuyerScreen.java
│       │       ├── SellerScreen.java
│       │       ├── AdminScreen.java
│       │       └── ForgotPasswordScreen.java
│       └── test/                        # Unit testler
│           ├── AliciSaticiTesti.java
│           ├── GecmisTesti.java
│           └── GecmisTesti2.java
├── pom.xml                              # Maven yapılandırması
├── README.md                            # Bu dosya
├── LICENSE                              # MIT Lisansı
└── .gitignore                           # Git ignore dosyası
```

---

## 🎨 Tasarım Desenleri

Bu proje aşağıdaki tasarım desenlerini kullanmaktadır:

- **Singleton Pattern**: `DatabaseService` için tek instance
- **Decorator Pattern**: Kullanıcı özelliklerini dinamik olarak ekleme (Premium, VIP, Verified)
- **Observer Pattern**: Fiyat ve stok değişikliklerinde bildirimler
- **Factory Pattern**: Kullanıcı nesnelerinin oluşturulması

---

## 🧪 Test Çalıştırma

```bash
# Tüm testleri çalıştır
mvn test

# Belirli bir test sınıfını çalıştır
mvn test -Dtest=AliciSaticiTesti
```

---

## 🔒 Güvenlik Notları

⚠️ **ÖNEMLİ GÜVENLİK UYARILARI:**

1. **Veritabanı Bilgileri**: 
   - Projeyi kullanmadan önce `DatabaseService.java` dosyasındaki veritabanı bağlantı bilgilerini mutlaka değiştirin
   - Şifrelerinizi asla kod içinde hardcode etmeyin
   - Production ortamında environment variables veya configuration dosyaları kullanın

2. **Şifre Güvenliği**:
   - Şu anda şifreler plain text olarak saklanmaktadır
   - Production kullanımı için şifreleri hash'leyin (BCrypt, Argon2 vb.)

3. **SQL Injection**:
   - Proje PreparedStatement kullanarak SQL injection'a karşı korumalıdır
   - Yeni özellikler eklerken PreparedStatement kullanmaya devam edin

---

## 🤝 Katkıda Bulunma

1. Bu repository'yi fork edin
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add amazing feature'`)
4. Branch'inizi push edin (`git push origin feature/amazing-feature`)
5. Bir Pull Request oluşturun

---

## 📞 Destek ve İletişim

**👤 Geliştirici:** Hüseyin Polat

- 📧 **E-posta:** [hsynpng@gmail.com](mailto:hsynpng@gmail.com)
- 🌍 **Web:** [www.hsynpng.com](https://www.hsynpng.com)
- 💼 **LinkedIn:** [hsynpngx](https://www.linkedin.com/in/hsynpngx/)
- 💬 **GitHub:** [@hsynpng](https://github.com/hsynpng)

---

## 📄 Lisans

Bu proje MIT Lisansı altında lisanslanmıştır. Detaylar için `LICENSE` dosyasına bakın.

---

## 🙏 Teşekkürler

- **[OpenJFX](https://openjfx.io/)** - JavaFX framework için
- **[PostgreSQL](https://www.postgresql.org/)** - Güçlü veritabanı sistemi için
- **[JUnit](https://junit.org/)** - Test framework için
- Tüm katkıda bulunanlara ve projeyi kullananlara teşekkürler!

---

<div align="center">

**⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın! ⭐**

**Geliştirici:** [Hüseyin Polat](https://www.hsynpng.com) ❤️

</div>

