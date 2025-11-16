package computersales.model.decorator;

import computersales.model.User;
import java.time.LocalDateTime;

public class VIPUserDecorator implements User {
    protected User user;
    private static final double SPECIAL_DISCOUNT_RATE = 0.15; // %15 özel indirim

    public VIPUserDecorator(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public double getSpecialDiscountRate() {
        return SPECIAL_DISCOUNT_RATE;
    }

    public double calculateSpecialDiscountedPrice(double originalPrice) {
        return originalPrice * (1 - SPECIAL_DISCOUNT_RATE);
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getPhone() {
        return user.getPhone();
    }

    @Override
    public double getBalance() {
        return user.getBalance();
    }

    @Override
    public void setBalance(double balance) {
        user.setBalance(balance);
    }

    @Override
    public String getRole() {
        return user.getRole();
    }

    @Override
    public boolean isPremium() {
        return user.isPremium();
    }

    @Override
    public boolean isVIP() {
        return true; // VIP decorator her zaman true döndürür
    }

    @Override
    public boolean isVerified() {
        return user.isVerified();
    }

    @Override
    public int getTrustLevel() {
        return user.getTrustLevel();
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return user.getCreatedAt();
    }

    @Override
    public LocalDateTime getLastLogin() {
        return user.getLastLogin();
    }

    @Override
    public String getProfilePicture() {
        return user.getProfilePicture();
    }

    @Override
    public String getAddress() {
        return user.getAddress();
    }

    @Override
    public String getPreferredPaymentMethod() {
        return user.getPreferredPaymentMethod();
    }

    @Override
    public String getNotificationPreferences() {
        return user.getNotificationPreferences();
    }
} 