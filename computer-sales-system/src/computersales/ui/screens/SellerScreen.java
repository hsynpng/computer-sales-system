package computersales.ui.screens;

import computersales.model.Product;
import computersales.model.Seller;
import computersales.model.User;
import computersales.model.decorator.PremiumUserDecorator;
import computersales.model.decorator.VIPUserDecorator;
import computersales.model.decorator.VerifiedUserDecorator;
import computersales.service.DatabaseService;
import computersales.service.ProductManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import java.util.Optional;

public class SellerScreen extends VBox {
    private DatabaseService databaseService;
    private Seller seller;
    private ListView<Product> productListView;
    private Label balanceLabel;
    private Label sellerStatusLabel;
    private Label userInfoLabel;
    private Button addProductButton;
    private Button deleteProductButton;
    private Button logoutButton;
    private Button bulkAddButton;
    private Button updateStockButton;
    private Stage primaryStage;
    private ProductManager productManager;

    public SellerScreen(Seller seller, Stage primaryStage) {
        this.seller = seller;
        this.primaryStage = primaryStage;
        this.databaseService = DatabaseService.getInstance();
        this.productManager = new ProductManager(databaseService);
        
        primaryStage.setTitle("Satıcı Ekranı - " + seller.getUsername());
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);

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

        Label usernameLabel = new Label(seller.getUsername());
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        usernameLabel.setStyle("-fx-text-fill: #3498db;");

        Button editProfileButton = new Button("Profili Düzenle");
        editProfileButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        editProfileButton.setOnAction(e -> handleEditProfile());

        userBasicInfo.getChildren().addAll(welcomeLabel, usernameLabel, editProfileButton);

        // Kullanıcı detayları
        VBox userDetails = new VBox(15);
        userDetails.setAlignment(Pos.CENTER_LEFT);
        userDetails.setPadding(new Insets(0, 0, 0, 40));

        HBox emailBox = createInfoBox("E-posta", seller.getEmail(), "✉");
        HBox phoneBox = createInfoBox("Telefon", seller.getPhone(), "📱");
        HBox balanceBox = createInfoBox("Bakiye", String.format("%.2f TL", seller.getBalance()), "💰");

        userDetails.getChildren().addAll(emailBox, phoneBox, balanceBox);

        // Kullanıcı durumu
        VBox userStatus = new VBox(10);
        userStatus.setAlignment(Pos.CENTER_RIGHT);
        userStatus.setPadding(new Insets(0, 0, 0, 40));

        User currentUser = databaseService.getUserByUsername(seller.getUsername());
        if (currentUser != null) {
            if (currentUser instanceof PremiumUserDecorator) {
                Label premiumLabel = createStatusLabel("Premium Satıcı", "#f1c40f");
                userStatus.getChildren().add(premiumLabel);
            } else if (currentUser instanceof VIPUserDecorator) {
                Label vipLabel = createStatusLabel("VIP Satıcı", "#9b59b6");
                userStatus.getChildren().add(vipLabel);
            }
            if (currentUser instanceof VerifiedUserDecorator) {
                Label verifiedLabel = createStatusLabel("Doğrulanmış Satıcı", "#2ecc71");
                userStatus.getChildren().add(verifiedLabel);
            }
        }

        topInfoCard.getChildren().addAll(userBasicInfo, userDetails, userStatus);

        // Ana içerik
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20, 0, 0, 0));

        Label productsLabel = new Label("Ürünleriniz");
        productsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        productsLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Ürün listesi
        productListView = new ListView<>();
        productListView.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-radius: 5;");
        productListView.setMaxHeight(400);
        productListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        productListView.setCellFactory(param -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                    setStyle("");
                } else {
                    VBox productBox = new VBox(5);
                    productBox.setPadding(new Insets(10));

                    Label nameLabel = new Label(product.getName());
                    nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    nameLabel.setStyle("-fx-text-fill: #2c3e50;");

                    Label priceLabel = new Label(String.format("₺%.2f", product.getPrice()));
                    priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    priceLabel.setStyle("-fx-text-fill: #e74c3c;");

                    Label stockLabel = new Label("Stok: " + product.getStock() + " adet");
                    stockLabel.setFont(Font.font("Arial", 12));
                    stockLabel.setStyle("-fx-text-fill: " + (product.getStock() < 5 ? "#e74c3c" : "#2ecc71") + "; -fx-font-weight: bold;");

                    Label descLabel = new Label(product.getDescription());
                    descLabel.setFont(Font.font("Arial", 12));
                    descLabel.setStyle("-fx-text-fill: #7f8c8d;");
                    descLabel.setWrapText(true);

                    productBox.getChildren().addAll(nameLabel, priceLabel, stockLabel, descLabel);
                    setGraphic(productBox);

                    if (isSelected()) {
                        setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 5;");
                    } else {
                        setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-radius: 5;");
                    }
                }
            }
        });

        // Butonlar
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        addProductButton = new Button("Ürün Ekle");
        addProductButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        addProductButton.setOnAction(e -> showProductAdditionDialog());

        Button editProductButton = new Button("Ürün Düzenle");
        editProductButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        editProductButton.setOnAction(e -> handleEditProduct());

        Button salesHistoryButton = new Button("Satış Geçmişi");
        salesHistoryButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        salesHistoryButton.setOnAction(e -> showSalesHistory());

        deleteProductButton = new Button("Ürün Sil");
        deleteProductButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        deleteProductButton.setOnAction(e -> handleDeleteProduct());

        logoutButton = new Button("Çıkış Yap");
        logoutButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        logoutButton.setOnAction(e -> handleLogout());

        buttonBox.getChildren().addAll(addProductButton, editProductButton, salesHistoryButton, deleteProductButton, logoutButton);

        contentBox.getChildren().addAll(productsLabel, productListView, buttonBox);

        mainBox.getChildren().addAll(topInfoCard, new Separator(), contentBox);
        getChildren().add(mainBox);

        updateProductList();
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

    private void handleAddProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Ürün Ekle");
        dialog.setHeaderText(null);

        ButtonType addButtonType = new ButtonType("Ekle", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Temel bilgiler
        TextField nameField = new TextField();
        nameField.setPromptText("İsim");
        nameField.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5;");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Açıklama");
        descriptionField.setPrefRowCount(3);
        descriptionField.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5;");

        TextField priceField = new TextField();
        priceField.setPromptText("Fiyat");
        priceField.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5;");

        // Yeni alanlar
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            "İşlemci", "Anakart", "RAM", "Ekran Kartı", "Depolama", 
            "Güç Kaynağı", "Kasa", "Soğutma", "Monitör", "Klavye", 
            "Mouse", "Diğer"
        );
        categoryCombo.setPromptText("Kategori Seçin");
        categoryCombo.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5;");

        TextField brandField = new TextField();
        brandField.setPromptText("Marka");
        brandField.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5;");

        TextField modelField = new TextField();
        modelField.setPromptText("Model");
        modelField.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5;");

        ComboBox<String> conditionCombo = new ComboBox<>();
        conditionCombo.getItems().addAll("Yeni", "İkinci El", "Yenilenmiş");
        conditionCombo.setPromptText("Durum Seçin");
        conditionCombo.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5;");

        TextField yearField = new TextField();
        yearField.setPromptText("Üretim Yılı");
        yearField.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5;");

        CheckBox warrantyCheck = new CheckBox("Garanti Kapsamında");
        warrantyCheck.setStyle("-fx-padding: 8;");

        TextField stockField = new TextField();
        stockField.setPromptText("Stok Miktarı");
        stockField.setStyle("-fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5;");

        // Grid'e ekle
        grid.add(new Label("İsim:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Açıklama:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Fiyat:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Kategori:"), 0, 3);
        grid.add(categoryCombo, 1, 3);
        grid.add(new Label("Marka:"), 0, 4);
        grid.add(brandField, 1, 4);
        grid.add(new Label("Model:"), 0, 5);
        grid.add(modelField, 1, 5);
        grid.add(new Label("Durum:"), 0, 6);
        grid.add(conditionCombo, 1, 6);
        grid.add(new Label("Üretim Yılı:"), 0, 7);
        grid.add(yearField, 1, 7);
        grid.add(new Label("Garanti:"), 0, 8);
        grid.add(warrantyCheck, 1, 8);
        grid.add(new Label("Stok:"), 0, 9);
        grid.add(stockField, 1, 9);

        dialog.getDialogPane().setContent(grid);

        nameField.requestFocus();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText().trim();
                    String description = descriptionField.getText().trim();
                    String priceText = priceField.getText().trim();
                    String category = categoryCombo.getValue();
                    String brand = brandField.getText().trim();
                    String model = modelField.getText().trim();
                    String condition = conditionCombo.getValue();
                    String yearText = yearField.getText().trim();
                    String stockText = stockField.getText().trim();

                    if (name.isEmpty() || description.isEmpty() || priceText.isEmpty() || 
                        category == null || brand.isEmpty() || model.isEmpty() || 
                        condition == null || yearText.isEmpty() || stockText.isEmpty()) {
                        showError("Lütfen tüm alanları doldurun");
                        return null;
                    }

                    double price;
                    int year;
                    int stock;
                    try {
                        price = Double.parseDouble(priceText);
                        year = Integer.parseInt(yearText);
                        stock = Integer.parseInt(stockText);
                        
                        if (price <= 0) {
                            showError("Fiyat 0'dan büyük olmalıdır");
                            return null;
                        }
                        if (year < 2000 || year > 2024) {
                            showError("Geçerli bir üretim yılı girin (2000-2024)");
                            return null;
                        }
                        if (stock < 0) {
                            showError("Stok miktarı negatif olamaz");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showError("Lütfen geçerli sayısal değerler girin");
                        return null;
                    }

                    Product product = new Product(0, name, description, price, seller.getUsername(), category);
                    product.setBrand(brand);
                    product.setModel(model);
                    product.setCondition(condition);
                    product.setYear(year);
                    product.setStock(stock);
                    product.setWarranty(warrantyCheck.isSelected());

                    return product;
                } catch (Exception e) {
                    showError("Ürün oluşturulurken bir hata oluştu");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(product -> {
            try {
                if (databaseService.addProduct(product)) {
                    updateProductList();
                    showSuccess("Ürün başarıyla eklendi!");
                } else {
                    showError("Ürün eklenemedi");
                }
            } catch (Exception e) {
                showError("Ürün eklenirken bir hata oluştu");
            }
        });
    }

    private void handleDeleteProduct() {
        Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showError("Lütfen silmek için bir ürün seçin");
            return;
        }

        // Stok kontrolü
        if (selectedProduct.getStock() > 0) {
            showError("Sadece stokta olmayan ürünleri silebilirsiniz");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Silme Onayı");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Bu ürünü silmek istediğinizden emin misiniz?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (databaseService.deleteProduct(selectedProduct.getId())) {
                    updateProductList();
                    showSuccess("Ürün başarıyla silindi!");
                } else {
                    showError("Ürün silinemedi");
                }
            }
        });
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

        TextField emailField = new TextField(seller.getEmail());
        emailField.setPromptText("E-posta");
        TextField phoneField = new TextField(seller.getPhone());
        phoneField.setPromptText("Telefon");
        TextArea addressField = new TextArea(seller.getAddress());
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

            if (databaseService.updateUserInfo(seller.getUsername(), email, phone, address)) {
                seller.setEmail(email);
                seller.setPhone(phone);
                seller.setAddress(address);
                showSuccess("Profil bilgileri başarıyla güncellendi!");
                
                // Ekranı yenile
                refreshScreen();
            } else {
                showError("Profil güncellenirken bir hata oluştu");
            }
        });
    }

    private void logout() {
        // Çıkış yapmadan önce son değişiklikleri kaydet
        User currentUser = databaseService.getUserByUsername(seller.getUsername());
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
            // Temel kullanıcı nesnesini Seller olarak dönüştür
            if (currentUser instanceof Seller) {
                seller = (Seller) currentUser;
            }
        }
        primaryStage.setScene(new Scene(new LoginScreen(primaryStage)));
    }

    private void refreshScreen() {
        // Veritabanından güncel kullanıcı bilgilerini al
        User updatedUser = databaseService.getUserByUsername(seller.getUsername());
        if (updatedUser != null) {
            // Yeni bir SellerScreen oluştur
            SellerScreen newScreen = new SellerScreen((Seller) updatedUser, primaryStage);
            // Mevcut sahneyi yeni ekranla değiştir
            primaryStage.getScene().setRoot(newScreen);
        }
    }

    private void updateProductList() {
        List<Product> products = databaseService.getProductsBySeller(seller.getUsername());
        productListView.getItems().clear();
        productListView.getItems().addAll(products);
        
        // Debug için stok bilgilerini yazdır
        System.out.println("\n=== Product List Update ===");
        for (Product product : products) {
            System.out.println("Product: " + product.getName() + ", Stock: " + product.getStock());
        }
        System.out.println("=== End Product List Update ===\n");
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

    private boolean isPremium(User user) {
        if (user == null) return false;
        String role = user.getRole().toLowerCase();
        return role.contains("premium") || role.contains("premium seller");
    }

    private boolean isVIP(User user) {
        if (user == null) return false;
        String role = user.getRole().toLowerCase();
        return role.contains("vip") || role.contains("vip seller");
    }

    private boolean isVerified(User user) {
        while (user != null) {
            if (user instanceof VerifiedUserDecorator) {
                return true;
            }
            if (user instanceof PremiumUserDecorator) {
                user = ((PremiumUserDecorator) user).getUser();
            } else if (user instanceof VIPUserDecorator) {
                user = ((VIPUserDecorator) user).getUser();
            } else if (user instanceof VerifiedUserDecorator) {
                user = ((VerifiedUserDecorator) user).getUser();
            } else {
                break;
            }
        }
        return false;
    }

    private void updateSellerStatus(User user) {
        // Veritabanından güncel kullanıcı bilgilerini al
        User currentUser = databaseService.getUserByUsername(user.getUsername());
        if (currentUser == null) return;

        String status = "";
        String role = currentUser.getRole();
        
        // Premium veya VIP durumunu kontrol et
        if (role.equalsIgnoreCase("VIP seller") || role.equalsIgnoreCase("vıp seller")) {
            status = "VIP Satıcı";
            sellerStatusLabel.setStyle("-fx-text-fill: purple; -fx-font-weight: bold;");
        } else if (role.equalsIgnoreCase("Premium seller")) {
            status = "Premium Satıcı";
            sellerStatusLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        } else if (isVerified(currentUser)) {
            status = "Doğrulanmış Satıcı";
            sellerStatusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else {
            status = "Standart Satıcı";
            sellerStatusLabel.setStyle("-fx-text-fill: gray;");
        }

        sellerStatusLabel.setText(status);
    }

    private void updateUserInfo() {
        User currentUser = databaseService.getUserByUsername(seller.getUsername());
        if (currentUser != null) {
            seller.setEmail(currentUser.getEmail());
            seller.setPhone(currentUser.getPhone());
            seller.setAddress(currentUser.getAddress());
            
            // Ana VBox'ı al
            VBox mainBox = (VBox) getChildren().get(0);
            // Üst bilgi kartını al (HBox)
            HBox topInfoCard = (HBox) mainBox.getChildren().get(0);
            // Kullanıcı detaylarını içeren VBox'ı al
            VBox userDetails = (VBox) topInfoCard.getChildren().get(1);
            
            // Mevcut içeriği temizle
            userDetails.getChildren().clear();
            
            // Yeni bilgi kutularını oluştur ve ekle
            HBox emailBox = createInfoBox("E-posta", seller.getEmail(), "✉");
            HBox phoneBox = createInfoBox("Telefon", seller.getPhone(), "📱");
            HBox balanceBox = createInfoBox("Bakiye", String.format("%.2f TL", seller.getBalance()), "💰");
            
            userDetails.getChildren().addAll(emailBox, phoneBox, balanceBox);
        }
    }

    private void showProductAdditionDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Ürün Ekleme Seçeneği");
        dialog.setHeaderText("Nasıl ürün eklemek istersiniz?");

        ButtonType singleProductType = new ButtonType("Tek Ürün Ekle");
        ButtonType bulkProductType = new ButtonType("Toplu Ürün Ekle");
        ButtonType cancelType = new ButtonType("İptal", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(singleProductType, bulkProductType, cancelType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == singleProductType) {
                return "single";
            } else if (dialogButton == bulkProductType) {
                return "bulk";
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(choice -> {
            if (choice.equals("single")) {
                handleAddProduct();
            } else if (choice.equals("bulk")) {
                handleBulkAddProducts();
            }
        });
    }

    private void showSalesHistory() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Satış Geçmişi");
        dialog.setHeaderText(null);

        // Dialog içeriği
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        // Başlık
        Label titleLabel = new Label("Satış Geçmişi");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Tablo
        TableView<Map<String, Object>> tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: white; -fx-border-color: #e9ecef; -fx-border-radius: 5;");

        // Sütunlar
        TableColumn<Map<String, Object>, String> dateColumn = new TableColumn<>("Tarih");
        dateColumn.setCellValueFactory(data -> {
            Timestamp timestamp = (Timestamp) data.getValue().get("sale_date");
            return new SimpleStringProperty(timestamp != null ? 
                timestamp.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "");
        });

        TableColumn<Map<String, Object>, String> productColumn = new TableColumn<>("Ürün");
        productColumn.setCellValueFactory(data -> {
            Object name = data.getValue().get("name");
            return new SimpleStringProperty(name != null ? name.toString() : "");
        });

        TableColumn<Map<String, Object>, String> brandModelColumn = new TableColumn<>("Marka/Model");
        brandModelColumn.setCellValueFactory(data -> {
            String brand = (String) data.getValue().get("brand");
            String model = (String) data.getValue().get("model");
            return new SimpleStringProperty((brand != null ? brand : "") + " " + (model != null ? model : ""));
        });

        TableColumn<Map<String, Object>, String> buyerColumn = new TableColumn<>("Alıcı");
        buyerColumn.setCellValueFactory(data -> {
            Object buyer = data.getValue().get("buyer_username");
            return new SimpleStringProperty(buyer != null ? buyer.toString() : "");
        });

        TableColumn<Map<String, Object>, String> quantityColumn = new TableColumn<>("Adet");
        quantityColumn.setCellValueFactory(data -> {
            Object quantity = data.getValue().get("quantity");
            return new SimpleStringProperty(quantity != null ? quantity.toString() : "0");
        });

        TableColumn<Map<String, Object>, String> priceColumn = new TableColumn<>("Birim Fiyat");
        priceColumn.setCellValueFactory(data -> {
            Double price = (Double) data.getValue().get("price");
            return new SimpleStringProperty(price != null ? String.format("%.2f TL", price) : "0.00 TL");
        });

        TableColumn<Map<String, Object>, String> totalColumn = new TableColumn<>("Toplam");
        totalColumn.setCellValueFactory(data -> {
            Double total = (Double) data.getValue().get("total_amount");
            return new SimpleStringProperty(total != null ? String.format("%.2f TL", total) : "0.00 TL");
        });

        // Sütun genişliklerini ayarla
        dateColumn.setPrefWidth(120);
        productColumn.setPrefWidth(150);
        brandModelColumn.setPrefWidth(150);
        buyerColumn.setPrefWidth(100);
        quantityColumn.setPrefWidth(60);
        priceColumn.setPrefWidth(100);
        totalColumn.setPrefWidth(100);

        tableView.getColumns().addAll(dateColumn, productColumn, brandModelColumn, 
                                    buyerColumn, quantityColumn, priceColumn, totalColumn);

        // Satış geçmişini yükle
        List<Map<String, Object>> salesHistory = databaseService.getSalesHistory(seller.getUsername());
        tableView.getItems().addAll(salesHistory);

        // Özet bilgiler
        VBox summaryBox = new VBox(10);
        summaryBox.setPadding(new Insets(20, 0, 0, 0));
        summaryBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5;");

        // Toplam satış sayısı
        int totalSales = salesHistory.size();
        Label totalSalesLabel = new Label("Toplam Satış: " + totalSales + " adet");
        totalSalesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Toplam gelir
        double totalRevenue = salesHistory.stream()
            .mapToDouble(sale -> {
                Double total = (Double) sale.get("total_amount");
                return total != null ? total : 0.0;
            })
            .sum();
        Label totalRevenueLabel = new Label(String.format("Toplam Gelir: %.2f TL", totalRevenue));
        totalRevenueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Ortalama satış tutarı
        double averageSale = totalSales > 0 ? totalRevenue / totalSales : 0;
        Label averageSaleLabel = new Label(String.format("Ortalama Satış: %.2f TL", averageSale));
        averageSaleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        summaryBox.getChildren().addAll(totalSalesLabel, totalRevenueLabel, averageSaleLabel);

        content.getChildren().addAll(titleLabel, tableView, summaryBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Dialog boyutunu ayarla
        dialog.getDialogPane().setPrefWidth(900);
        dialog.getDialogPane().setPrefHeight(600);

        dialog.showAndWait();
    }

    private void handleEditProduct() {
        Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showError("Lütfen düzenlemek için bir ürün seçin");
            return;
        }

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Ürün Düzenle");
        dialog.setHeaderText("Ürün bilgilerini güncelleyin");

        ButtonType saveButtonType = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedProduct.getName());
        nameField.setPromptText("İsim");

        TextArea descriptionField = new TextArea(selectedProduct.getDescription());
        descriptionField.setPromptText("Açıklama");
        descriptionField.setPrefRowCount(3);

        TextField priceField = new TextField(String.valueOf(selectedProduct.getPrice()));
        priceField.setPromptText("Fiyat");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(
            "İşlemci", "Anakart", "RAM", "Ekran Kartı", "Depolama", 
            "Güç Kaynağı", "Kasa", "Soğutma", "Monitör", "Klavye", 
            "Mouse", "Diğer"
        );
        categoryCombo.setValue(selectedProduct.getCategory());

        TextField brandField = new TextField(selectedProduct.getBrand());
        brandField.setPromptText("Marka");

        TextField modelField = new TextField(selectedProduct.getModel());
        modelField.setPromptText("Model");

        ComboBox<String> conditionCombo = new ComboBox<>();
        conditionCombo.getItems().addAll("Yeni", "İkinci El", "Yenilenmiş");
        conditionCombo.setValue(selectedProduct.getCondition());

        TextField yearField = new TextField(String.valueOf(selectedProduct.getYear()));
        yearField.setPromptText("Üretim Yılı");

        CheckBox warrantyCheck = new CheckBox("Garanti Kapsamında");
        warrantyCheck.setSelected(selectedProduct.isWarranty());

        TextField stockField = new TextField(String.valueOf(selectedProduct.getStock()));
        stockField.setPromptText("Stok Miktarı");

        grid.add(new Label("İsim:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Açıklama:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Fiyat:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Kategori:"), 0, 3);
        grid.add(categoryCombo, 1, 3);
        grid.add(new Label("Marka:"), 0, 4);
        grid.add(brandField, 1, 4);
        grid.add(new Label("Model:"), 0, 5);
        grid.add(modelField, 1, 5);
        grid.add(new Label("Durum:"), 0, 6);
        grid.add(conditionCombo, 1, 6);
        grid.add(new Label("Üretim Yılı:"), 0, 7);
        grid.add(yearField, 1, 7);
        grid.add(new Label("Garanti:"), 0, 8);
        grid.add(warrantyCheck, 1, 8);
        grid.add(new Label("Stok:"), 0, 9);
        grid.add(stockField, 1, 9);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    String description = descriptionField.getText().trim();
                    String priceText = priceField.getText().trim();
                    String category = categoryCombo.getValue();
                    String brand = brandField.getText().trim();
                    String model = modelField.getText().trim();
                    String condition = conditionCombo.getValue();
                    String yearText = yearField.getText().trim();
                    String stockText = stockField.getText().trim();

                    if (name.isEmpty() || description.isEmpty() || priceText.isEmpty() || 
                        category == null || brand.isEmpty() || model.isEmpty() || 
                        condition == null || yearText.isEmpty() || stockText.isEmpty()) {
                        showError("Lütfen tüm alanları doldurun");
                        return null;
                    }

                    double price;
                    int year;
                    int stock;
                    try {
                        price = Double.parseDouble(priceText);
                        year = Integer.parseInt(yearText);
                        stock = Integer.parseInt(stockText);
                        
                        if (price <= 0) {
                            showError("Fiyat 0'dan büyük olmalıdır");
                            return null;
                        }
                        if (year < 2000 || year > 2024) {
                            showError("Geçerli bir üretim yılı girin (2000-2024)");
                            return null;
                        }
                        if (stock < 0) {
                            showError("Stok miktarı negatif olamaz");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showError("Lütfen geçerli sayısal değerler girin");
                        return null;
                    }

                    selectedProduct.setName(name);
                    selectedProduct.setDescription(description);
                    selectedProduct.setPrice(price);
                    selectedProduct.setCategory(category);
                    selectedProduct.setBrand(brand);
                    selectedProduct.setModel(model);
                    selectedProduct.setCondition(condition);
                    selectedProduct.setYear(year);
                    selectedProduct.setWarranty(warrantyCheck.isSelected());
                    selectedProduct.setStock(stock);

                    return selectedProduct;
                } catch (Exception e) {
                    showError("Ürün güncellenirken bir hata oluştu");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(product -> {
            if (databaseService.updateProduct(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getBrand(),
                product.getModel(),
                product.getCondition(),
                product.getYear(),
                product.isWarranty(),
                product.getStock()
            )) {
                updateProductList();
                showSuccess("Ürün başarıyla güncellendi!");
            } else {
                showError("Ürün güncellenemedi");
            }
        });
    }

    private void handleLogout() {
        // Çıkış yapmadan önce son değişiklikleri kaydet
        User currentUser = databaseService.getUserByUsername(seller.getUsername());
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
            // Temel kullanıcı nesnesini Seller olarak dönüştür
            if (currentUser instanceof Seller) {
                seller = (Seller) currentUser;
            }
        }
        primaryStage.setScene(new Scene(new LoginScreen(primaryStage)));
    }

    private void handleBulkAddProducts() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CSV Dosyası Seç");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Dosyaları", "*.csv")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int successCount = 0;
                int failCount = 0;
                
                // Başlık satırını atla
                reader.readLine();
                
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length >= 10) {
                        try {
                            Product product = new Product(
                                0,
                                values[0].trim(), // name
                                values[1].trim(), // description
                                Double.parseDouble(values[2].trim()), // price
                                seller.getUsername(),
                                values[3].trim() // category
                            );
                            
                            product.setBrand(values[4].trim());
                            product.setModel(values[5].trim());
                            product.setCondition(values[6].trim());
                            product.setYear(Integer.parseInt(values[7].trim()));
                            product.setWarranty(Boolean.parseBoolean(values[8].trim()));
                            product.setStock(Integer.parseInt(values[9].trim()));
                            
                            if (databaseService.addProduct(product)) {
                                successCount++;
                            } else {
                                failCount++;
                            }
                        } catch (Exception e) {
                            failCount++;
                        }
                    } else {
                        failCount++;
                    }
                }
                
                updateProductList();
                showSuccess(String.format("Toplu ürün ekleme tamamlandı!\nBaşarılı: %d\nBaşarısız: %d", successCount, failCount));
            } catch (IOException e) {
                showError("Dosya okuma hatası: " + e.getMessage());
            }
        }
    }
} 