package computersales.model;

import computersales.model.observer.Observer;
import computersales.model.observer.Subject;
import java.util.ArrayList;
import java.util.List;

public class Product implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private int id;
    private String name;
    private String description;
    private double price;
    private String sellerUsername;
    private String category;
    private String brand;
    private String model;
    private String condition;
    private int year;
    private boolean warranty;
    private int stock;
    private boolean available;

    public Product(int id, String name, String description, double price, String sellerUsername, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.sellerUsername = sellerUsername;
        this.category = category;
        this.available = true;
        this.stock = 0;
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
        notifyObservers();
    }

    public void setPrice(double price) {
        this.price = price;
        notifyObservers();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isWarranty() {
        return warranty;
    }

    public void setWarranty(boolean warranty) {
        this.warranty = warranty;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("%s - %s %s (%s)", name, brand, model, category);
    }
}