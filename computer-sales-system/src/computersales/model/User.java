package computersales.model;

import java.time.LocalDateTime;

public interface User {
    String getUsername();
    String getPassword();
    String getEmail();
    String getPhone();
    double getBalance();
    void setBalance(double balance);
    String getRole();
    boolean isPremium();
    boolean isVIP();
    boolean isVerified();
    int getTrustLevel();
    LocalDateTime getCreatedAt();
    LocalDateTime getLastLogin();
    String getProfilePicture();
    String getAddress();
    String getPreferredPaymentMethod();
    String getNotificationPreferences();
} 