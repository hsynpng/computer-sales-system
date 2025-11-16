package computersales.model.decorator;

import computersales.model.User;

public abstract class UserDecorator implements User {
    protected User user;

    public UserDecorator(User user) {
        this.user = user;
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

    public User getUser() {
        return user;
    }
} 