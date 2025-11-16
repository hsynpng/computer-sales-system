package computersales.model.decorator;

import computersales.model.User;
import java.time.LocalDateTime;

public class PremiumUserDecorator implements User {
    protected User user;
    private static final double DISCOUNT_RATE = 0.10; // %10 indirim

    public PremiumUserDecorator(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public double getDiscountRate() {
        return DISCOUNT_RATE;
    }

    public double calculateDiscountedPrice(double originalPrice) {
        return originalPrice * (1 - DISCOUNT_RATE);
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
        return true; // Premium decorator her zaman true döndürür
    }

    @Override
    public boolean isVIP() {
        return user.isVIP();
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