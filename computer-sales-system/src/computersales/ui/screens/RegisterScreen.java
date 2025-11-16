package computersales.ui.screens;

import computersales.service.DatabaseService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class RegisterScreen extends VBox {
    private DatabaseService databaseService;
    private Stage primaryStage;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField emailField;
    private TextField phoneField;
    private ComboBox<String> roleComboBox;
    private Button registerButton;
    private Button backButton;

    public RegisterScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.databaseService = DatabaseService.getInstance();
        setupUI();
    }

    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(40));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #f0f0f0;");

        VBox registerBox = new VBox(15);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setMaxWidth(400);
        registerBox.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label titleLabel = new Label("Kayıt Ol");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Yeni hesap oluştur");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        usernameField = new TextField();
        usernameField.setPromptText("Kullanıcı Adı");
        usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        usernameField.setMaxWidth(300);

        passwordField = new PasswordField();
        passwordField.setPromptText("Şifre");
        passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        passwordField.setMaxWidth(300);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Şifreyi Tekrar Girin");
        confirmPasswordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        confirmPasswordField.setMaxWidth(300);

        emailField = new TextField();
        emailField.setPromptText("E-posta");
        emailField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        emailField.setMaxWidth(300);

        phoneField = new TextField();
        phoneField.setPromptText("Telefon");
        phoneField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        phoneField.setMaxWidth(300);

        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Alıcı", "Satıcı");
        roleComboBox.setPromptText("Rol Seçin");
        roleComboBox.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        roleComboBox.setMaxWidth(300);

        registerButton = new Button("Kayıt Ol");
        registerButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        registerButton.setOnAction(e -> handleRegister(
            usernameField.getText(),
            passwordField.getText(),
            confirmPasswordField.getText(),
            emailField.getText(),
            phoneField.getText(),
            roleComboBox.getValue()
        ));

        backButton = new Button("Geri Dön");
        backButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        backButton.setOnAction(e -> handleBack());

        registerBox.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Separator(),
                usernameField,
                passwordField,
                confirmPasswordField,
                emailField,
                phoneField,
                roleComboBox,
                registerButton,
                backButton
        );

        getChildren().add(registerBox);
    }

    private void handleRegister(String username, String password, String confirmPassword, String email, String phone, String role) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || phone.isEmpty() || role == null) {
            showError("Lütfen tüm alanları doldurun");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Şifreler eşleşmiyor");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Geçerli bir e-posta adresi girin");
            return;
        }

        // Kullanıcı adı kontrolü
        if (username.length() < 3) {
            showError("Kullanıcı adı en az 3 karakter olmalıdır");
            return;
        }

        // Rol değerini veritabanı formatına dönüştür
        String dbRole = role.equals("Alıcı") ? "buyer" : "seller";

        try {
            // Önce kullanıcı adının kullanılabilir olup olmadığını kontrol et
            if (databaseService.getUserByUsername(username) != null) {
                showError("Bu kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin.");
                return;
            }

            if (databaseService.register(username, password, dbRole, email, phone)) {
                showSuccess("Kayıt başarılı! Giriş yapabilirsiniz.");
                handleBack();
            } else {
                showError("Kayıt işlemi başarısız oldu. Lütfen tekrar deneyin.");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                showError("Bu kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin.");
            } else {
                showError("Kayıt olurken bir hata oluştu: " + e.getMessage());
            }
        }
    }

    private void handleBack() {
        primaryStage.setScene(new Scene(new LoginScreen(primaryStage)));
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
} 