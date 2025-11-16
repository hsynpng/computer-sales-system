package computersales.ui.screens;

import computersales.model.User;
import computersales.model.Buyer;
import computersales.model.Seller;
import computersales.service.DatabaseService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginScreen extends VBox {
    private DatabaseService databaseService;
    private Stage primaryStage;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;
    private Button forgotPasswordButton;

    public LoginScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.databaseService = DatabaseService.getInstance();
        setupUI();
    }

    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(40));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #f0f0f0;");

        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setMaxWidth(400);
        loginBox.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label titleLabel = new Label("Bilgisayar Satış Sistemi");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Giriş Yap");
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

        loginButton = new Button("Giriş Yap");
        loginButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        loginButton.setOnAction(e -> handleLogin());

        registerButton = new Button("Kayıt Ol");
        registerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        registerButton.setOnAction(e -> handleRegister());

        forgotPasswordButton = new Button("Şifremi Unuttum");
        forgotPasswordButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-font-weight: bold; -fx-padding: 10 20;");
        forgotPasswordButton.setOnAction(e -> handleForgotPassword());

        loginBox.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Separator(),
                usernameField,
                passwordField,
                loginButton,
                registerButton,
                forgotPasswordButton
        );

        getChildren().add(loginBox);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Lütfen kullanıcı adı ve şifre girin");
            return;
        }

        // Admin kontrolü
        if (username.equals("admin") && password.equals("admin123")) {
            primaryStage.setScene(new Scene(new AdminScreen(primaryStage)));
            return;
        }

        System.out.println("\n=== Login Screen Debug Info ===");
        System.out.println("Attempting login for user: " + username);
        
        User user = databaseService.login(username, password);
        if (user != null) {
            System.out.println("User object received from database: " + user.getClass().getName());
            
            // Decorator'ları kaldırarak temel kullanıcı nesnesini al
            User baseUser = user;
            while (baseUser != null) {
                if (baseUser instanceof computersales.model.decorator.VerifiedUserDecorator) {
                    baseUser = ((computersales.model.decorator.VerifiedUserDecorator) baseUser).getUser();
                } else if (baseUser instanceof computersales.model.decorator.PremiumUserDecorator) {
                    baseUser = ((computersales.model.decorator.PremiumUserDecorator) baseUser).getUser();
                } else if (baseUser instanceof computersales.model.decorator.VIPUserDecorator) {
                    baseUser = ((computersales.model.decorator.VIPUserDecorator) baseUser).getUser();
                } else {
                    break;
                }
                System.out.println("Base user after unwrapping: " + baseUser.getClass().getName());
            }

            System.out.println("Final base user type: " + baseUser.getClass().getName());

            // Temel kullanıcı nesnesinin tipini kontrol et
            if (baseUser instanceof Buyer) {
                System.out.println("Creating BuyerScreen");
                primaryStage.setScene(new Scene(new BuyerScreen((Buyer)baseUser, primaryStage)));
            } else if (baseUser instanceof Seller) {
                System.out.println("Creating SellerScreen");
                primaryStage.setScene(new Scene(new SellerScreen((Seller)baseUser, primaryStage)));
            } else {
                System.out.println("Invalid user type: " + baseUser.getClass().getName());
                showError("Geçersiz kullanıcı tipi");
            }
        } else {
            System.out.println("Login failed - no user returned from database");
            showError("Geçersiz kullanıcı adı veya şifre");
        }
        System.out.println("=== End Login Screen Debug Info ===\n");
    }

    private void handleRegister() {
        primaryStage.setScene(new Scene(new RegisterScreen(primaryStage)));
    }

    private void handleForgotPassword() {
        primaryStage.setScene(new Scene(new ForgotPasswordScreen(primaryStage)));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Scene getView() {
        return new Scene(this, 800, 600);
    }
} 