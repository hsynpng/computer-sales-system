package computersales.ui.screens;

import computersales.model.Buyer;
import computersales.model.Product;
import computersales.model.User;
import computersales.model.decorator.PremiumUserDecorator;
import computersales.model.decorator.VIPUserDecorator;
import computersales.model.decorator.VerifiedUserDecorator;
import computersales.model.observer.PriceObserver;
import computersales.model.observer.StockObserver;
import computersales.service.DatabaseService;
import computersales.service.ProductManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BuyerScreen extends VBox {
    private DatabaseService databaseService;
    private Buyer buyer;
    private ListView<Product> productListView;
    private Label balanceLabel;
    private Label userTypeLabel;
    private Label discountLabel;
    private Button addBalanceButton;
    private Button buyButton;
    private Button logoutButton;
    private Button prioritySupportButton;
    private Stage primaryStage;
    private VBox userDetails;

    public BuyerScreen(Buyer buyer, Stage primaryStage) {
        this.buyer = buyer;
        this.primaryStage = primaryStage;
        this.databaseService = DatabaseService.getInstance();
        setupUI();
    }

    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #f0f0f0;");

        VBox mainBox = new VBox(20);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setMaxWidth(1000);
        mainBox.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Üst bilgi kartı
        HBox topInfoCard = new HBox(20);
        topInfoCard.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
        topInfoCard.setAlignment(Pos.CENTER_LEFT);
        topInfoCard.setPadding(new Insets(20));

        // Kullanıcı avatarı ve temel bilgiler
        VBox userBasicInfo = new VBox(10);
        userBasicInfo.setAlignment(Pos.CENTER_LEFT);

        Label welcomeLabel = new Label("Hoş Geldiniz,");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label usernameLabel = new Label(buyer.getUsername());
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        usernameLabel.setStyle("-fx-text-fill: #3498db;");

        Button editProfileButton = new Button("Profili Düzenle");
        editProfileButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        editProfileButton.setOnAction(e -> handleEditProfile());

        userBasicInfo.getChildren().addAll(welcomeLabel, usernameLabel, editProfileButton);

        // Kullanıcı detayları
        createUserInfoSection();

        // Kullanıcı durumu
        VBox userStatus = new VBox(10);
        userStatus.setAlignment(Pos.CENTER_RIGHT);
        userStatus.setPadding(new Insets(0, 0, 0, 40));

        User currentUser = databaseService.getUserByUsername(buyer.getUsername());
        if (currentUser != null) {
            if (currentUser instanceof PremiumUserDecorator) {
                Label premiumLabel = createStatusLabel("Premium Üye", "#f1c40f");
                userStatus.getChildren().add(premiumLabel);
            } else if (currentUser instanceof VIPUserDecorator) {
                Label vipLabel = createStatusLabel("VIP Üye", "#9b59b6");
                userStatus.getChildren().add(vipLabel);
            }
            if (currentUser instanceof VerifiedUserDecorator) {
                Label verifiedLabel = createStatusLabel("Doğrulanmış Üye", "#2ecc71");
                userStatus.getChildren().add(verifiedLabel);
            }
        }

        topInfoCard.getChildren().addAll(userBasicInfo, userDetails, userStatus);

        // Ana içerik
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20, 0, 0, 0));

        Label productsLabel = new Label("Mevcut Ürünler");
        productsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        productsLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Ürün listesi
        productListView = new ListView<>();
        productListView.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-radius: 5;");
        productListView.setMaxHeight(400);
        productListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Ürün listesine tıklama olayı ekle
        productListView.setOnMouseClicked(event -> {
            Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                showProductDetails(selectedProduct);
            }
        });

        productListView.setCellFactory(param -> new ListCell<Product>() {
            private Label stockLabel;
            private Label priceChangeLabel;
            private VBox notificationBox;

            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                    setStyle("");
                } else {
                    VBox productBox = new VBox(5);
                    productBox.setPadding(new Insets(10));

                    HBox headerBox = new HBox(10);
                    headerBox.setAlignment(Pos.CENTER_LEFT);

                    Label nameLabel = new Label(product.getName());
                    nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    nameLabel.setStyle("-fx-text-fill: #2c3e50;");

                    Label priceLabel = new Label(String.format("₺%.2f", product.getPrice()));
                    priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    priceLabel.setStyle("-fx-text-fill: #e74c3c;");

                    headerBox.getChildren().addAll(nameLabel, priceLabel);

                    Label descLabel = new Label(product.getDescription());
                    descLabel.setFont(Font.font("Arial", 12));
                    descLabel.setStyle("-fx-text-fill: #7f8c8d;");
                    descLabel.setWrapText(true);

                    HBox sellerBox = new HBox(5);
                    sellerBox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label sellerIcon = new Label("👤");
                    sellerIcon.setFont(Font.font("Arial", 12));
                    
                    Label sellerLabel = new Label("Satıcı: " + product.getSellerUsername());
                    sellerLabel.setFont(Font.font("Arial", 12));
                    sellerLabel.setStyle("-fx-text-fill: #34495e;");
                    
                    sellerBox.getChildren().addAll(sellerIcon, sellerLabel);

                    // Stok durumu göstergesi
                    stockLabel = new Label();
                    updateStockLabel(product.getStock());
                    stockLabel.setStyle("-fx-font-weight: bold;");

                    // Fiyat değişikliği göstergesi
                    priceChangeLabel = new Label();
                    priceChangeLabel.setStyle("-fx-font-weight: bold;");

                    // Bildirim kutusu
                    notificationBox = new VBox(5);
                    notificationBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 5; -fx-background-radius: 5;");
                    notificationBox.setVisible(false);

                    productBox.getChildren().addAll(headerBox, descLabel, sellerBox, stockLabel, priceChangeLabel, notificationBox);
                    setGraphic(productBox);

                    if (isSelected()) {
                        setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 5;");
                    } else {
                        setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-radius: 5;");
                    }
                }
            }

            private void updateStockLabel(int stock) {
                if (stock <= 0) {
                    stockLabel.setText("Stokta Yok");
                    stockLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                } else if (stock < 5) {
                    stockLabel.setText("Son " + stock + " ürün kaldı!");
                    stockLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                } else {
                    stockLabel.setText("Stokta: " + stock + " adet");
                    stockLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                }
            }

            private void showPriceChange(double oldPrice, double newPrice) {
                double difference = newPrice - oldPrice;
                String changeText = difference > 0 ? 
                    "↑ " + String.format("%.2f TL", difference) : 
                    "↓ " + String.format("%.2f TL", Math.abs(difference));
                
                priceChangeLabel.setText(changeText);
                priceChangeLabel.setStyle("-fx-text-fill: " + (difference > 0 ? "#e74c3c" : "#2ecc71") + "; -fx-font-weight: bold;");
                
                // 3 saniye sonra göstergeleri gizle
                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(event -> {
                    priceChangeLabel.setText("");
                    priceChangeLabel.setStyle("");
                });
                delay.play();
            }
        });

        // Butonlar
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        addBalanceButton = new Button("Bakiye Ekle");
        addBalanceButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        addBalanceButton.setOnAction(e -> handleAddBalance());

        buyButton = new Button("Satın Al");
        buyButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        buyButton.setOnAction(e -> handleBuy());

        Button purchaseHistoryButton = new Button("Satın Alma Geçmişi");
        purchaseHistoryButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        purchaseHistoryButton.setOnAction(e -> showPurchaseHistory());

        logoutButton = new Button("Çıkış Yap");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        logoutButton.setOnAction(e -> handleLogout());

        buttonBox.getChildren().addAll(addBalanceButton, buyButton, purchaseHistoryButton, logoutButton);

        contentBox.getChildren().addAll(productsLabel, productListView, buttonBox);

        mainBox.getChildren().addAll(topInfoCard, new Separator(), contentBox);
        getChildren().add(mainBox);

        updateProductList();
    }

    private void createUserInfoSection() {
        userDetails = new VBox(15);
        userDetails.setAlignment(Pos.CENTER_LEFT);
        userDetails.setPadding(new Insets(0, 0, 0, 40));

        HBox emailBox = createInfoBox("E-posta", buyer.getEmail(), "✉");
        HBox phoneBox = createInfoBox("Telefon", buyer.getPhone(), "📱");
        HBox addressBox = createInfoBox("Adres", buyer.getAddress(), "📍");
        HBox balanceBox = createInfoBox("Bakiye", String.format("%.2f TL", buyer.getBalance()), "💰");

        userDetails.getChildren().addAll(emailBox, phoneBox, addressBox, balanceBox);
    }

    private HBox createInfoBox(String label, String value, String icon) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 16));

        Label labelText = new Label(label + ":");
        labelText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        labelText.setStyle("-fx-text-fill: #7f8c8d;");

        Label valueText = new Label(value);
        valueText.setFont(Font.font("Arial", 12));
        valueText.setStyle("-fx-text-fill: #2c3e50;");

        box.getChildren().addAll(iconLabel, labelText, valueText);
        return box;
    }

    private Label createStatusLabel(String text, String color) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setStyle(String.format("-fx-text-fill: %s; -fx-padding: 5 10; -fx-background-color: %s20; -fx-background-radius: 5;", color, color));
        return label;
    }

    private void handleAddBalance() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Bakiye Ekle");
        dialog.setHeaderText(null);
        dialog.setContentText("Eklemek istediğiniz miktarı girin:");

        dialog.showAndWait().ifPresent(amount -> {
            try {
                double value = Double.parseDouble(amount);
                if (value > 0) {
                    double newBalance = buyer.getBalance() + value;
                    if (databaseService.updateUserBalance(buyer.getUsername(), newBalance)) {
                        buyer.setBalance(newBalance);
                        // Bakiye etiketini güncelle
                        balanceLabel.setText(String.format("Bakiye: %.2f TL", newBalance));
                        // Bakiye kutusunu güncelle
                        HBox balanceBox = createInfoBox("Bakiye", String.format("%.2f TL", newBalance), "💰");
                        // Eski bakiye kutusunu bul ve değiştir
                        for (int i = 0; i < userDetails.getChildren().size(); i++) {
                            if (userDetails.getChildren().get(i) instanceof HBox) {
                                HBox box = (HBox) userDetails.getChildren().get(i);
                                if (box.getChildren().size() > 0 && box.getChildren().get(0) instanceof Label) {
                                    Label iconLabel = (Label) box.getChildren().get(0);
                                    if (iconLabel.getText().equals("💰")) {
                                        userDetails.getChildren().set(i, balanceBox);
                                        break;
                                    }
                                }
                            }
                        }
                        showSuccess("Bakiye başarıyla güncellendi!");
                    } else {
                        showError("Bakiye güncellenemedi");
                    }
                } else {
                    showError("Lütfen pozitif bir miktar girin");
                }
            } catch (NumberFormatException e) {
                showError("Lütfen geçerli bir sayı girin");
            }
        });
    }

    private void handleBuy() {
        Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showError("Lütfen satın almak için bir ürün seçin");
            return;
        }

        // Stok kontrolü
        if (selectedProduct.getStock() <= 0) {
            showError("Bu ürün stokta bulunmamaktadır");
            return;
        }

        if (selectedProduct.getPrice() > buyer.getBalance()) {
            showError("Yetersiz bakiye");
            return;
        }

        double finalPrice = selectedProduct.getPrice();
        
        User currentUser = databaseService.getUserByUsername(buyer.getUsername());
        
        if (currentUser instanceof PremiumUserDecorator) {
            PremiumUserDecorator premiumUser = (PremiumUserDecorator) currentUser;
            finalPrice = premiumUser.calculateDiscountedPrice(finalPrice);
        }
        
        if (currentUser instanceof VIPUserDecorator) {
            VIPUserDecorator vipUser = (VIPUserDecorator) currentUser;
            finalPrice = vipUser.calculateSpecialDiscountedPrice(finalPrice);
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Satın Alma Onayı");
        confirmDialog.setHeaderText("Ürün Satın Alma");
        confirmDialog.setContentText("Bu ürünü ₺" + finalPrice + " karşılığında satın almak istediğinizden emin misiniz?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Alıcının yeni bakiyesini hesapla
            double buyerNewBalance = buyer.getBalance() - finalPrice;
            
            // Satıcının mevcut ve yeni bakiyesini al
            double sellerCurrentBalance = databaseService.getUserBalance(selectedProduct.getSellerUsername());
            double sellerNewBalance = sellerCurrentBalance + finalPrice;

            // Stok miktarını güncelle
            int newStock = selectedProduct.getStock() - 1;

            // Tüm işlemleri tek bir transaction içinde yap
            if (databaseService.updateUserBalance(buyer.getUsername(), buyerNewBalance) && 
                databaseService.updateUserBalance(selectedProduct.getSellerUsername(), sellerNewBalance) &&
                databaseService.updateProductStock(selectedProduct.getId(), newStock)) {
                
                // Satış geçmişine kayıt ekle
                if (databaseService.addSaleRecord(
                    selectedProduct.getId(),
                    selectedProduct.getSellerUsername(),
                    buyer.getUsername(),
                    1, // quantity
                    finalPrice,
                    finalPrice // totalAmount (1 adet için aynı)
                )) {
                    // Alıcının bakiyesini güncelle
                    buyer.setBalance(buyerNewBalance);
                    
                    // Bakiye kutusunu güncelle
                    HBox balanceBox = createInfoBox("Bakiye", String.format("%.2f TL", buyerNewBalance), "💰");
                    for (int i = 0; i < userDetails.getChildren().size(); i++) {
                        if (userDetails.getChildren().get(i) instanceof HBox) {
                            HBox box = (HBox) userDetails.getChildren().get(i);
                            if (box.getChildren().size() > 0 && box.getChildren().get(0) instanceof Label) {
                                Label iconLabel = (Label) box.getChildren().get(0);
                                if (iconLabel.getText().equals("💰")) {
                                    userDetails.getChildren().set(i, balanceBox);
                                    break;
                                }
                            }
                        }
                    }
                    
                    updateProductList();
                    showSuccess("Ürün başarıyla satın alındı!");
                } else {
                    showError("Satış kaydı oluşturulamadı");
                }
            } else {
                showError("Satın alma işlemi tamamlanamadı");
            }
        }
    }

    private void handleLogout() {
        // Çıkış yapmadan önce son değişiklikleri kaydet
        User currentUser = databaseService.getUserByUsername(buyer.getUsername());
        if (currentUser != null) {
            // Dekoratör katmanlarını kaldırarak temel kullanıcı nesnesini al
            while (currentUser instanceof PremiumUserDecorator || 
                   currentUser instanceof VIPUserDecorator || 
                   currentUser instanceof VerifiedUserDecorator) {
                if (currentUser instanceof PremiumUserDecorator) {
                    currentUser = ((PremiumUserDecorator) currentUser).getUser();
                } else if (currentUser instanceof VIPUserDecorator) {
                    currentUser = ((VIPUserDecorator) currentUser).getUser();
                } else if (currentUser instanceof VerifiedUserDecorator) {
                    currentUser = ((VerifiedUserDecorator) currentUser).getUser();
                }
            }
            // Temel kullanıcı nesnesini Buyer olarak dönüştür
            if (currentUser instanceof Buyer) {
                buyer = (Buyer) currentUser;
            }
        }
        primaryStage.setScene(new Scene(new LoginScreen(primaryStage)));
    }

    private void setupObservers(Product product) {
        // Stok değişikliği observer'ı
        StockObserver stockObserver = new StockObserver(product) {
            @Override
            public void update() {
                Platform.runLater(() -> {
                    // ListView'ı güncelle
                    productListView.refresh();
                });
            }
        };

        // Fiyat değişikliği observer'ı
        PriceObserver priceObserver = new PriceObserver(product) {
            @Override
            public void update() {
                Platform.runLater(() -> {
                    // ListView'ı güncelle
                    productListView.refresh();
                });
            }
        };
    }

    private void updateProductList() {
        List<Product> products = databaseService.getAllProducts();
        productListView.getItems().clear();
        productListView.getItems().addAll(products);
        
        // Her ürün için observer'ları kur
        for (Product product : products) {
            setupObservers(product);
        }
    }

    private void updateUserTypeIndicators(User user) {
        User currentUser = databaseService.getUserByUsername(user.getUsername());
        
        if (currentUser instanceof PremiumUserDecorator) {
            PremiumUserDecorator premiumUser = (PremiumUserDecorator) currentUser;
            userTypeLabel.setText("Premium Üye");
            userTypeLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
            discountLabel.setText("İndirim: %" + (premiumUser.getDiscountRate() * 100));
            discountLabel.setStyle("-fx-text-fill: green;");
        }

        if (currentUser instanceof VIPUserDecorator) {
            VIPUserDecorator vipUser = (VIPUserDecorator) currentUser;
            userTypeLabel.setText("VIP Üye");
            userTypeLabel.setStyle("-fx-text-fill: purple; -fx-font-weight: bold;");
            discountLabel.setText("Özel İndirim: %" + (vipUser.getSpecialDiscountRate() * 100));
            discountLabel.setStyle("-fx-text-fill: green;");
            prioritySupportButton.setVisible(true);
        }

        if (currentUser instanceof VerifiedUserDecorator) {
            VerifiedUserDecorator verifiedUser = (VerifiedUserDecorator) currentUser;
            if (!userTypeLabel.getText().isEmpty()) {
                userTypeLabel.setText(userTypeLabel.getText() + " ✓");
            } else {
                userTypeLabel.setText("Doğrulanmış Üye ✓");
                userTypeLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Başarılı");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleEditProfile() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Profil Düzenle");
        dialog.setHeaderText("Profil Bilgilerinizi Güncelleyin");

        // Dialog içeriği
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField emailField = new TextField(buyer.getEmail());
        emailField.setPromptText("E-posta");
        TextField phoneField = new TextField(buyer.getPhone());
        phoneField.setPromptText("Telefon");
        TextArea addressField = new TextArea(buyer.getAddress());
        addressField.setPromptText("Adres");
        addressField.setPrefRowCount(3);

        grid.add(new Label("E-posta:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Telefon:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Adres:"), 0, 2);
        grid.add(addressField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Butonlar
        ButtonType saveButtonType = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Sonuç dönüşümü
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(emailField.getText(), phoneField.getText());
            }
            return null;
        });

        // Dialog sonucunu işle
        dialog.showAndWait().ifPresent(result -> {
            String email = result.getKey();
            String phone = result.getValue();
            String address = addressField.getText();

            if (databaseService.updateUserInfo(buyer.getUsername(), email, phone, address)) {
                buyer.setEmail(email);
                buyer.setPhone(phone);
                buyer.setAddress(address);
                showSuccess("Profil bilgileri başarıyla güncellendi!");
                
                // Ekranı yenile
                refreshScreen();
            } else {
                showError("Profil güncellenirken bir hata oluştu");
            }
        });
    }

    private void refreshScreen() {
        // Veritabanından güncel kullanıcı bilgilerini al
        User updatedUser = databaseService.getUserByUsername(buyer.getUsername());
        if (updatedUser != null) {
            // Yeni bir BuyerScreen oluştur
            BuyerScreen newScreen = new BuyerScreen((Buyer) updatedUser, primaryStage);
            // Mevcut sahneyi yeni ekranla değiştir
            primaryStage.getScene().setRoot(newScreen);
        }
    }

    private void showProductDetails(Product product) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ürün Detayları");
        dialog.setHeaderText(null);

        // Dialog içeriği
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        // Ürün başlığı
        Label titleLabel = new Label(product.getName());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Fiyat
        double finalPrice = product.getPrice();
        User currentUser = databaseService.getUserByUsername(buyer.getUsername());
        
        if (currentUser instanceof PremiumUserDecorator) {
            PremiumUserDecorator premiumUser = (PremiumUserDecorator) currentUser;
            finalPrice = premiumUser.calculateDiscountedPrice(finalPrice);
        }
        
        if (currentUser instanceof VIPUserDecorator) {
            VIPUserDecorator vipUser = (VIPUserDecorator) currentUser;
            finalPrice = vipUser.calculateSpecialDiscountedPrice(finalPrice);
        }

        Label priceLabel = new Label(String.format("Fiyat: ₺%.2f", finalPrice));
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        priceLabel.setStyle("-fx-text-fill: #e74c3c;");

        // İndirim varsa göster
        if (finalPrice < product.getPrice()) {
            Label originalPriceLabel = new Label(String.format("Orijinal Fiyat: ₺%.2f", product.getPrice()));
            originalPriceLabel.setFont(Font.font("Arial", 12));
            originalPriceLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-strikethrough: true;");
            content.getChildren().add(originalPriceLabel);
        }

        // Stok durumu
        Label stockLabel = new Label("Stok: " + product.getStock() + " adet");
        stockLabel.setFont(Font.font("Arial", 14));
        stockLabel.setStyle("-fx-text-fill: " + (product.getStock() < 5 ? "#e74c3c" : "#2ecc71") + "; -fx-font-weight: bold;");

        // Detaylı bilgiler
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);
        detailsGrid.setPadding(new Insets(10, 0, 0, 0));

        addDetailRow(detailsGrid, 0, "Kategori:", product.getCategory());
        addDetailRow(detailsGrid, 1, "Marka:", product.getBrand());
        addDetailRow(detailsGrid, 2, "Model:", product.getModel());
        addDetailRow(detailsGrid, 3, "Durum:", product.getCondition());
        addDetailRow(detailsGrid, 4, "Üretim Yılı:", String.valueOf(product.getYear()));
        addDetailRow(detailsGrid, 5, "Garanti:", product.isWarranty() ? "Var" : "Yok");

        // Açıklama
        Label descTitle = new Label("Ürün Açıklaması:");
        descTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        descTitle.setStyle("-fx-text-fill: #2c3e50;");

        TextArea descriptionArea = new TextArea(product.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef;");

        // Satıcı bilgisi
        Label sellerLabel = new Label("Satıcı: " + product.getSellerUsername());
        sellerLabel.setFont(Font.font("Arial", 12));
        sellerLabel.setStyle("-fx-text-fill: #7f8c8d;");

        content.getChildren().addAll(
            titleLabel,
            priceLabel,
            stockLabel,
            new Separator(),
            detailsGrid,
            new Separator(),
            descTitle,
            descriptionArea,
            sellerLabel
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Dialog boyutunu ayarla
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(600);

        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        labelNode.setStyle("-fx-text-fill: #7f8c8d;");

        Label valueNode = new Label(value != null ? value : "-");
        valueNode.setFont(Font.font("Arial", 12));
        valueNode.setStyle("-fx-text-fill: #2c3e50;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void showPurchaseHistory() {
        List<Map<String, Object>> purchaseHistory = databaseService.getPurchaseHistory(buyer.getUsername());
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Satın Alma Geçmişi");
        dialog.setHeaderText("Satın Aldığınız Ürünler");
        
        // Dialog içeriği
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        // Tablo oluştur
        TableView<Map<String, Object>> table = new TableView<>();
        
        // Tarih sütunu
        TableColumn<Map<String, Object>, String> dateColumn = new TableColumn<>("Tarih");
        dateColumn.setCellValueFactory(data -> {
            Timestamp timestamp = (Timestamp) data.getValue().get("saleDate");
            String formattedDate = timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            return new SimpleStringProperty(formattedDate);
        });
        
        // Ürün sütunu
        TableColumn<Map<String, Object>, String> productColumn = new TableColumn<>("Ürün");
        productColumn.setCellValueFactory(data -> {
            String productName = (String) data.getValue().get("productName");
            String brand = (String) data.getValue().get("brand");
            String model = (String) data.getValue().get("model");
            return new SimpleStringProperty(String.format("%s (%s %s)", productName, brand, model));
        });
        
        // Satıcı sütunu
        TableColumn<Map<String, Object>, String> sellerColumn = new TableColumn<>("Satıcı");
        sellerColumn.setCellValueFactory(data -> 
            new SimpleStringProperty((String) data.getValue().get("sellerUsername")));
        
        // Miktar sütunu
        TableColumn<Map<String, Object>, Integer> quantityColumn = new TableColumn<>("Miktar");
        quantityColumn.setCellValueFactory(data -> 
            new SimpleIntegerProperty((Integer) data.getValue().get("quantity")).asObject());
        
        // Birim Fiyat sütunu
        TableColumn<Map<String, Object>, Double> priceColumn = new TableColumn<>("Birim Fiyat");
        priceColumn.setCellValueFactory(data -> 
            new SimpleDoubleProperty((Double) data.getValue().get("price")).asObject());
        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f TL", price));
                }
            }
        });
        
        // Toplam Tutar sütunu
        TableColumn<Map<String, Object>, Double> totalColumn = new TableColumn<>("Toplam Tutar");
        totalColumn.setCellValueFactory(data -> 
            new SimpleDoubleProperty((Double) data.getValue().get("totalAmount")).asObject());
        totalColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f TL", total));
                }
            }
        });
        
        table.getColumns().addAll(dateColumn, productColumn, sellerColumn, quantityColumn, priceColumn, totalColumn);
        
        // Verileri tabloya ekle
        table.getItems().addAll(purchaseHistory);
        
        // Özet bilgileri
        int totalPurchases = purchaseHistory.size();
        double totalSpent = purchaseHistory.stream()
            .mapToDouble(p -> (Double) p.get("totalAmount"))
            .sum();
        double averagePurchase = totalPurchases > 0 ? totalSpent / totalPurchases : 0;
        
        Label summaryLabel = new Label(String.format(
            "Toplam Satın Alma: %d\nToplam Harcama: %.2f TL\nOrtalama Satın Alma: %.2f TL",
            totalPurchases, totalSpent, averagePurchase
        ));
        summaryLabel.setStyle("-fx-font-weight: bold;");
        
        content.getChildren().addAll(table, summaryLabel);
        
        // Dialog boyutunu ayarla
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(800, 600);
        
        // Kapatma butonu
        ButtonType closeButton = new ButtonType("Kapat", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        
        dialog.showAndWait();
    }
} 