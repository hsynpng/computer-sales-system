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

public class ForgotPasswordScreen extends VBox {
    private DatabaseService databaseService;
    private Stage primaryStage;
    private TextField usernameField;
    private TextField emailField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private Button resetButton;
    private Button backButton;

    public ForgotPasswordScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.databaseService = DatabaseService.getInstance();
        setupUI();
    }

    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(40));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #f0f0f0;");

        VBox resetBox = new VBox(15);
        resetBox.setAlignment(Pos.CENTER);
        resetBox.setMaxWidth(400);
        resetBox.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label titleLabel = new Label("Şifremi Unuttum");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Şifrenizi sıfırlamak için e-posta adresinizi girin");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        usernameField = new TextField();
        usernameField.setPromptText("Kullanıcı Adı");
        usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        usernameField.setMaxWidth(300);

        emailField = new TextField();
        emailField.setPromptText("E-posta");
        emailField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        emailField.setMaxWidth(300);

        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Yeni Şifre");
        newPasswordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        newPasswordField.setMaxWidth(300);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Yeni Şifreyi Tekrar Girin");
        confirmPasswordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5;");
        confirmPasswordField.setMaxWidth(300);

        resetButton = new Button("Şifremi Sıfırla");
        resetButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        resetButton.setOnAction(e -> handleReset());

        backButton = new Button("Geri Dön");
        backButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        backButton.setOnAction(e -> handleBack());

        resetBox.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Separator(),
                usernameField,
                emailField,
                newPasswordField,
                confirmPasswordField,
                resetButton,
                backButton
        );

        getChildren().add(resetBox);
    }

    private void handleReset() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Lütfen tüm alanları doldurun");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Şifreler eşleşmiyor");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Geçerli bir e-posta adresi girin");
            return;
        }

        try {
            if (databaseService.resetPassword(username, email, newPassword)) {
                showSuccess("Şifre sıfırlama başarılı! Yeni şifrenizle giriş yapabilirsiniz.");
                handleBack();
            } else {
                showError("Şifre sıfırlama başarısız. Kullanıcı adı ve e-posta adresinizi kontrol edin.");
            }
        } catch (Exception e) {
            showError("Şifre sıfırlama işlemi sırasında bir hata oluştu");
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