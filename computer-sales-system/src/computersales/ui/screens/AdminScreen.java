package computersales.ui.screens;

import computersales.model.User;
import computersales.model.decorator.PremiumUserDecorator;
import computersales.model.decorator.VIPUserDecorator;
import computersales.model.decorator.VerifiedUserDecorator;
import computersales.service.DatabaseService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.List;

public class AdminScreen extends VBox {
    private DatabaseService databaseService;
    private ListView<User> userListView;
    private Stage primaryStage;

    public AdminScreen(Stage primaryStage) {
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

        Label titleLabel = new Label("Yönetici Paneli");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label("Kullanıcı Yönetimi");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");

        // Kullanıcı listesi
        userListView = new ListView<>();
        userListView.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-radius: 5;");
        userListView.setMaxHeight(400);
        userListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        userListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String status = "";
                    String baseRole = user.getRole();
                    
                    // Doğrulama durumunu kontrol et
                    if (user instanceof VerifiedUserDecorator) {
                        status = "[✓]";
                    }
                    
                    // Premium veya VIP durumunu kontrol et
                    if (user instanceof PremiumUserDecorator) {
                        if (!status.isEmpty()) {
                            status += " ";
                        }
                        status += "[Premium]";
                        baseRole = baseRole.replace("Premium ", "");
                    } else if (user instanceof VIPUserDecorator) {
                        if (!status.isEmpty()) {
                            status += " ";
                        }
                        status += "[VIP]";
                        baseRole = baseRole.replace("VIP ", "");
                    }
                    
                    setText(String.format("%s (%s)%s\nEmail: %s\nTelefon: %s",
                            user.getUsername(),
                            baseRole,
                            status.isEmpty() ? "" : " " + status,
                            user.getEmail(),
                            user.getPhone()));
                    
                    if (isSelected()) {
                        setStyle("-fx-padding: 10; -fx-background-color: #c0c0c0; -fx-border-color: #2ecc71; -fx-border-width: 2; -fx-border-radius: 5; -fx-text-fill: black;");
                    } else {
                        setStyle("-fx-padding: 10; -fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-radius: 5; -fx-text-fill: black;");
                    }
                }
            }
        });

        // Yenile butonu ekle
        Button refreshButton = new Button("Listeyi Yenile");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        refreshButton.setOnAction(e -> updateUserList());

        // Butonlar
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button premiumButton = new Button("Premium Yap");
        premiumButton.setStyle("-fx-background-color: gold; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        premiumButton.setOnAction(e -> handlePremiumToggle());

        Button vipButton = new Button("VIP Yap");
        vipButton.setStyle("-fx-background-color: purple; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        vipButton.setOnAction(e -> handleVIPToggle());

        Button verifyButton = new Button("Doğrula");
        verifyButton.setStyle("-fx-background-color: blue; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        verifyButton.setOnAction(e -> handleVerifyToggle());

        Button logoutButton = new Button("Çıkış Yap");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        logoutButton.setOnAction(e -> handleLogout());

        buttonBox.getChildren().addAll(refreshButton, premiumButton, vipButton, verifyButton, logoutButton);

        mainBox.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Separator(),
                userListView,
                buttonBox
        );

        getChildren().add(mainBox);
        updateUserList();
    }

    private void updateUserList() {
        List<User> users = databaseService.getAllUsers();
        if (users != null && !users.isEmpty()) {
            userListView.getItems().clear();
            userListView.getItems().addAll(users);
        } else {
            showError("Kullanıcı listesi alınamadı veya boş");
        }
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

    private boolean isPremium(User user) {
        while (user != null) {
            if (user instanceof PremiumUserDecorator) {
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

    private boolean isVIP(User user) {
        while (user != null) {
            if (user instanceof VIPUserDecorator) {
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

    private void handlePremiumToggle() {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Lütfen bir kullanıcı seçin");
            return;
        }

        // Önce doğrulama kontrolü yap
        if (!isVerified(selectedUser)) {
            showError("Kullanıcı önce doğrulanmalıdır!");
            return;
        }

        boolean isCurrentlyPremium = isPremium(selectedUser);
        boolean isCurrentlyVIP = isVIP(selectedUser);

        if (isCurrentlyPremium) {
            if (databaseService.downgradeFromPremium(selectedUser.getUsername())) {
                showSuccess("Premium üyelik kaldırıldı");
            } else {
                showError("Premium üyelik kaldırılırken hata oluştu");
            }
        } else {
            // Eğer kullanıcı VIP ise, önce VIP'i kaldır
            if (isCurrentlyVIP) {
                if (!databaseService.downgradeFromVIP(selectedUser.getUsername())) {
                    showError("VIP üyelik kaldırılırken hata oluştu");
                    return;
                }
            }
            
            if (databaseService.upgradeToPremium(selectedUser.getUsername())) {
                showSuccess("Kullanıcı Premium üye yapıldı");
            } else {
                showError("Premium üyelik eklenirken hata oluştu");
            }
        }
        updateUserList();
    }

    private void handleVIPToggle() {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Lütfen bir kullanıcı seçin");
            return;
        }

        // Önce doğrulama kontrolü yap
        if (!isVerified(selectedUser)) {
            showError("Kullanıcı önce doğrulanmalıdır!");
            return;
        }

        boolean isCurrentlyVIP = isVIP(selectedUser);
        boolean isCurrentlyPremium = isPremium(selectedUser);

        if (isCurrentlyVIP) {
            if (databaseService.downgradeFromVIP(selectedUser.getUsername())) {
                showSuccess("VIP üyelik kaldırıldı");
            } else {
                showError("VIP üyelik kaldırılırken hata oluştu");
            }
        } else {
            // Eğer kullanıcı Premium ise, önce Premium'u kaldır
            if (isCurrentlyPremium) {
                if (!databaseService.downgradeFromPremium(selectedUser.getUsername())) {
                    showError("Premium üyelik kaldırılırken hata oluştu");
                    return;
                }
            }
            
            if (databaseService.upgradeToVIP(selectedUser.getUsername())) {
                showSuccess("Kullanıcı VIP üye yapıldı");
            } else {
                showError("VIP üyelik eklenirken hata oluştu");
            }
        }
        updateUserList();
    }

    private void handleVerifyToggle() {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Lütfen bir kullanıcı seçin");
            return;
        }

        boolean isCurrentlyVerified = isVerified(selectedUser);

        if (isCurrentlyVerified) {
            // Eğer kullanıcı Premium veya VIP ise, önce onları kaldır
            if (isPremium(selectedUser)) {
                databaseService.downgradeFromPremium(selectedUser.getUsername());
            }
            if (isVIP(selectedUser)) {
                databaseService.downgradeFromVIP(selectedUser.getUsername());
            }

            if (databaseService.removeVerification(selectedUser.getUsername())) {
                showSuccess("Doğrulama kaldırıldı");
            } else {
                showError("Doğrulama kaldırılırken hata oluştu");
            }
        } else {
            if (databaseService.verifyUser(selectedUser.getUsername())) {
                showSuccess("Kullanıcı doğrulandı");
            } else {
                showError("Doğrulama eklenirken hata oluştu");
            }
        }
        updateUserList();
    }

    private void handleLogout() {
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

    private void createUserControls(User user) {
        VBox controls = new VBox(10);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));

        // Premium kontrolü
        CheckBox premiumCheck = new CheckBox("Premium Üye");
        premiumCheck.setSelected(user instanceof PremiumUserDecorator);
        premiumCheck.setOnAction(e -> {
            if (premiumCheck.isSelected()) {
                if (databaseService.upgradeToPremium(user.getUsername())) {
                    showSuccess("Kullanıcı Premium üye yapıldı");
                } else {
                    showError("Premium üyelik eklenirken hata oluştu");
                    premiumCheck.setSelected(false);
                }
            } else {
                if (databaseService.downgradeFromPremium(user.getUsername())) {
                    showSuccess("Premium üyelik kaldırıldı");
                } else {
                    showError("Premium üyelik kaldırılırken hata oluştu");
                    premiumCheck.setSelected(true);
                }
            }
            updateUserList();
        });

        // VIP kontrolü
        CheckBox vipCheck = new CheckBox("VIP Üye");
        vipCheck.setSelected(user instanceof VIPUserDecorator);
        vipCheck.setOnAction(e -> {
            if (vipCheck.isSelected()) {
                if (databaseService.upgradeToVIP(user.getUsername())) {
                    showSuccess("Kullanıcı VIP üye yapıldı");
                } else {
                    showError("VIP üyelik eklenirken hata oluştu");
                    vipCheck.setSelected(false);
                }
            } else {
                if (databaseService.downgradeFromVIP(user.getUsername())) {
                    showSuccess("VIP üyelik kaldırıldı");
                } else {
                    showError("VIP üyelik kaldırılırken hata oluştu");
                    vipCheck.setSelected(true);
                }
            }
            updateUserList();
        });

        // Doğrulama kontrolü
        CheckBox verifiedCheck = new CheckBox("Doğrulanmış Üye");
        verifiedCheck.setSelected(user instanceof VerifiedUserDecorator);
        verifiedCheck.setOnAction(e -> {
            if (verifiedCheck.isSelected()) {
                if (databaseService.verifyUser(user.getUsername())) {
                    showSuccess("Kullanıcı doğrulandı");
                } else {
                    showError("Doğrulama eklenirken hata oluştu");
                    verifiedCheck.setSelected(false);
                }
            } else {
                if (databaseService.removeVerification(user.getUsername())) {
                    showSuccess("Doğrulama kaldırıldı");
                } else {
                    showError("Doğrulama kaldırılırken hata oluştu");
                    verifiedCheck.setSelected(true);
                }
            }
            updateUserList();
        });

        // Güvenilirlik seviyesi kontrolü
        if (user instanceof VerifiedUserDecorator) {
            HBox trustLevelBox = new HBox(10);
            trustLevelBox.setAlignment(Pos.CENTER);
            
            Label trustLabel = new Label("Güvenilirlik Seviyesi:");
            Spinner<Integer> trustSpinner = new Spinner<>(1, 5, ((VerifiedUserDecorator) user).getTrustLevel());
            trustSpinner.setEditable(true);
            
            Button updateTrustButton = new Button("Güncelle");
            updateTrustButton.setOnAction(e -> {
                int newTrustLevel = trustSpinner.getValue();
                if (databaseService.updateUserTrustLevel(user.getUsername(), newTrustLevel)) {
                    showSuccess("Güvenilirlik seviyesi güncellendi");
                    updateUserList();
                } else {
                    showError("Güvenilirlik seviyesi güncellenirken hata oluştu");
                }
            });
            
            trustLevelBox.getChildren().addAll(trustLabel, trustSpinner, updateTrustButton);
            controls.getChildren().add(trustLevelBox);
        }

        controls.getChildren().addAll(premiumCheck, vipCheck, verifiedCheck);
    }
} 