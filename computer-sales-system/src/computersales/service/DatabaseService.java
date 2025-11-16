package computersales.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import computersales.model.User;
import computersales.model.Buyer;
import computersales.model.Seller;
import computersales.model.Product;
import computersales.model.decorator.PremiumUserDecorator;
import computersales.model.decorator.VIPUserDecorator;
import computersales.model.decorator.VerifiedUserDecorator;

public class DatabaseService {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/ikiniceldb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    private static DatabaseService instance;
    private Connection connection;

    private DatabaseService() {
        initializeConnection();
        createTables();
    }

    private void initializeConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            connection.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
            connection = null;
        }
    }

    private synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private void createTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Users tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "phone TEXT," +
                    "role TEXT NOT NULL," +
                    "balance DOUBLE PRECISION DEFAULT 0.0," +
                    "is_premium BOOLEAN DEFAULT false," +
                    "is_vip BOOLEAN DEFAULT false," +
                    "is_verified BOOLEAN DEFAULT false," +
                    "trust_level INTEGER DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Products tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "id SERIAL PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "price DOUBLE PRECISION NOT NULL," +
                    "seller_username TEXT NOT NULL," +
                    "buyer_username TEXT," +
                    "category TEXT," +
                    "brand TEXT," +
                    "model TEXT," +
                    "condition TEXT," +
                    "year INTEGER," +
                    "warranty BOOLEAN DEFAULT false," +
                    "stock INTEGER DEFAULT 0," +
                    "is_available BOOLEAN DEFAULT true," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (seller_username) REFERENCES users(username)," +
                    "FOREIGN KEY (buyer_username) REFERENCES users(username)" +
                    ")");

            // Sales History tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS sales_history (" +
                    "id SERIAL PRIMARY KEY," +
                    "product_id INTEGER NOT NULL," +
                    "seller_username TEXT NOT NULL," +
                    "buyer_username TEXT NOT NULL," +
                    "quantity INTEGER NOT NULL DEFAULT 1," +
                    "price DOUBLE PRECISION NOT NULL," +
                    "total_amount DOUBLE PRECISION NOT NULL," +
                    "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)," +
                    "FOREIGN KEY (seller_username) REFERENCES users(username)," +
                    "FOREIGN KEY (buyer_username) REFERENCES users(username)" +
                    ")");

            // Purchase History tablosu
            stmt.execute("CREATE TABLE IF NOT EXISTS purchase_history (" +
                    "id SERIAL PRIMARY KEY," +
                    "product_id INTEGER NOT NULL," +
                    "buyer_username TEXT NOT NULL," +
                    "seller_username TEXT NOT NULL," +
                    "quantity INTEGER NOT NULL DEFAULT 1," +
                    "price DOUBLE PRECISION NOT NULL," +
                    "total_amount DOUBLE PRECISION NOT NULL," +
                    "purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)," +
                    "FOREIGN KEY (buyer_username) REFERENCES users(username)," +
                    "FOREIGN KEY (seller_username) REFERENCES users(username)" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            return users;
        }

        try {
            String query = "SELECT * FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                boolean isPremium = rs.getBoolean("is_premium");
                boolean isVIP = rs.getBoolean("is_vip");
                boolean isVerified = rs.getBoolean("is_verified");

                User user;
                if (role.equals("buyer")) {
                    user = new Buyer(username, password, email, phone);
                } else {
                    user = new Seller(username, password, email, phone);
                }

                if (isVerified) {
                    user = new VerifiedUserDecorator(user);
                }
                if (isPremium) {
                    user = new PremiumUserDecorator(user);
                }
                if (isVIP) {
                    user = new VIPUserDecorator(user);
                }

                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean upgradeToPremium(String username) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE users SET is_premium = true WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean downgradeFromPremium(String username) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE users SET is_premium = false WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean upgradeToVIP(String username) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE users SET is_vip = true WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean downgradeFromVIP(String username) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE users SET is_vip = false WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyUser(String username) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE users SET is_verified = true WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeVerification(String username) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE users SET is_verified = false WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserTrustLevel(String username, int trustLevel) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE users SET trust_level = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, trustLevel);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User login(String username, String password) {
        Connection conn = getConnection();
        if (conn == null) {
            System.out.println("Database connection failed");
            return null;
        }

        try {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                double balance = rs.getDouble("balance");
                boolean isPremium = rs.getBoolean("is_premium");
                boolean isVIP = rs.getBoolean("is_vip");
                boolean isVerified = rs.getBoolean("is_verified");

                System.out.println("\n=== User Login Debug Info ===");
                System.out.println("Username: " + username);
                System.out.println("Role: " + role);
                System.out.println("Email: " + email);
                System.out.println("Phone: " + phone);
                System.out.println("Balance: " + balance);
                System.out.println("Is Premium: " + isPremium);
                System.out.println("Is VIP: " + isVIP);
                System.out.println("Is Verified: " + isVerified);

                User baseUser;
                if (role.equals("buyer")) {
                    baseUser = new Buyer(username, password, email, phone);
                    ((Buyer) baseUser).setBalance(balance);
                    System.out.println("Created Buyer object: " + baseUser.getClass().getName());
                } else if (role.equals("seller")) {
                    baseUser = new Seller(username, password, email, phone);
                    ((Seller) baseUser).setBalance(balance);
                    System.out.println("Created Seller object: " + baseUser.getClass().getName());
                } else {
                    System.out.println("Invalid role: " + role);
                    return null;
                }

                User decoratedUser = baseUser;
                if (isVerified) {
                    decoratedUser = new VerifiedUserDecorator(decoratedUser);
                    System.out.println("Added VerifiedUserDecorator: " + decoratedUser.getClass().getName());
                }
                if (isPremium) {
                    decoratedUser = new PremiumUserDecorator(decoratedUser);
                    System.out.println("Added PremiumUserDecorator: " + decoratedUser.getClass().getName());
                }
                if (isVIP) {
                    decoratedUser = new VIPUserDecorator(decoratedUser);
                    System.out.println("Added VIPUserDecorator: " + decoratedUser.getClass().getName());
                }

                System.out.println("Final user object type: " + decoratedUser.getClass().getName());
                System.out.println("=== End Debug Info ===\n");

                return decoratedUser;
            } else {
                System.out.println("No user found with username: " + username);
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String password, String role, String email, String phone) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "INSERT INTO users (username, password, role, email, phone) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPassword(String username, String email, String newPassword) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE users SET password = ? WHERE username = ? AND email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUserByUsername(String username) {
        Connection conn = getConnection();
        if (conn == null) {
            return null;
        }

        try {
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String password = rs.getString("password");
                String role = rs.getString("role");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                double balance = rs.getDouble("balance");
                boolean isPremium = rs.getBoolean("is_premium");
                boolean isVIP = rs.getBoolean("is_vip");
                boolean isVerified = rs.getBoolean("is_verified");

                User user;
                if (role.equals("buyer")) {
                    user = new Buyer(username, password, email, phone);
                    ((Buyer) user).setBalance(balance);
                } else {
                    user = new Seller(username, password, email, phone);
                    ((Seller) user).setBalance(balance);
                }

                if (isVerified) {
                    user = new VerifiedUserDecorator(user);
                }
                if (isPremium) {
                    user = new PremiumUserDecorator(user);
                }
                if (isVIP) {
                    user = new VIPUserDecorator(user);
                }

                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getUserBalance(String username) {
        Connection conn = getConnection();
        if (conn == null) {
            return 0.0;
        }

        try {
            String query = "SELECT balance FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean updateUserBalance(String username, double newBalance) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE users SET balance = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getAvailableProducts() {
        List<Product> products = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            return products;
        }

        try {
            String query = "SELECT * FROM products WHERE is_available = true";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                String sellerUsername = rs.getString("seller_username");
                String category = rs.getString("category");
                String condition = rs.getString("condition");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                int year = rs.getInt("year");
                boolean isAvailable = rs.getBoolean("is_available");
                int stock = rs.getInt("stock");

                Product product = new Product(id, name, description, price, sellerUsername, category);
                product.setCondition(condition);
                product.setBrand(brand);
                product.setModel(model);
                product.setYear(year);
                product.setAvailable(isAvailable);
                product.setStock(stock);

                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean updateProductBuyer(int productId, String buyerUsername) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE products SET buyer_username = ?, is_available = false WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, buyerUsername);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addProduct(Product product) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "INSERT INTO products (name, description, price, seller_username, category, brand, model, condition, year, warranty, stock, is_available) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setString(4, product.getSellerUsername());
            pstmt.setString(5, product.getCategory());
            pstmt.setString(6, product.getBrand());
            pstmt.setString(7, product.getModel());
            pstmt.setString(8, product.getCondition());
            pstmt.setInt(9, product.getYear());
            pstmt.setBoolean(10, product.isWarranty());
            pstmt.setInt(11, product.getStock());
            pstmt.setBoolean(12, product.isAvailable());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int productId) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getProductsBySeller(String sellerUsername) {
        List<Product> products = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            return products;
        }

        try {
            String query = "SELECT * FROM products WHERE seller_username = ? AND is_available = true";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, sellerUsername);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                String category = rs.getString("category");
                String condition = rs.getString("condition");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                int year = rs.getInt("year");
                boolean isAvailable = rs.getBoolean("is_available");
                int stock = rs.getInt("stock");

                Product product = new Product(id, name, description, price, sellerUsername, category);
                product.setCondition(condition);
                product.setBrand(brand);
                product.setModel(model);
                product.setYear(year);
                product.setAvailable(isAvailable);
                product.setStock(stock);

                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean updateProductStock(int productId, int newStock) {
        Connection conn = getConnection();
        if (conn == null) {
            System.out.println("Database connection failed");
            return false;
        }

        String query = "UPDATE products SET stock = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, productId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Stock updated successfully for product ID: " + productId);
                return true;
            } else {
                System.out.println("No product found with ID: " + productId);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error updating product stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProductPrice(int productId, double newPrice) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE products SET price = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            return products;
        }

        try {
            String query = "SELECT * FROM products WHERE is_available = true";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                String sellerUsername = rs.getString("seller_username");
                String category = rs.getString("category");
                String condition = rs.getString("condition");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                int year = rs.getInt("year");
                boolean isAvailable = rs.getBoolean("is_available");
                int stock = rs.getInt("stock");
                boolean warranty = rs.getBoolean("warranty");

                Product product = new Product(id, name, description, price, sellerUsername, category);
                product.setCondition(condition);
                product.setBrand(brand);
                product.setModel(model);
                product.setYear(year);
                product.setAvailable(isAvailable);
                product.setStock(stock);
                product.setWarranty(warranty);

                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean updateUserInfo(String username, String email, String phone, String address) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE users SET email = ?, phone = ?, address = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, phone);
            pstmt.setString(3, address);
            pstmt.setString(4, username);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User information updated successfully for: " + username);
                return true;
            } else {
                System.out.println("No user found with username: " + username);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error updating user information: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(int productId, String name, String description, double price, 
                               String category, String brand, String model, String condition, 
                               int year, boolean warranty, int stock) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "UPDATE products SET name = ?, description = ?, price = ?, category = ?, " +
                      "brand = ?, model = ?, condition = ?, year = ?, warranty = ?, stock = ? " +
                      "WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, price);
            pstmt.setString(4, category);
            pstmt.setString(5, brand);
            pstmt.setString(6, model);
            pstmt.setString(7, condition);
            pstmt.setInt(8, year);
            pstmt.setBoolean(9, warranty);
            pstmt.setInt(10, stock);
            pstmt.setInt(11, productId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Product updated successfully: " + name);
                return true;
            } else {
                System.out.println("No product found with ID: " + productId);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSaleRecord(int productId, String sellerUsername, String buyerUsername, 
                               int quantity, double price, double totalAmount) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        String query = "INSERT INTO sales_history (product_id, seller_username, buyer_username, " +
                      "quantity, price, total_amount) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            pstmt.setString(2, sellerUsername);
            pstmt.setString(3, buyerUsername);
            pstmt.setInt(4, quantity);
            pstmt.setDouble(5, price);
            pstmt.setDouble(6, totalAmount);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSalesHistory(int productId, String sellerUsername, String buyerUsername) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        try {
            // Önce ürün bilgilerini al
            String productQuery = "SELECT price FROM products WHERE id = ?";
            PreparedStatement productStmt = conn.prepareStatement(productQuery);
            productStmt.setInt(1, productId);
            ResultSet rs = productStmt.executeQuery();

            if (rs.next()) {
                double price = rs.getDouble("price");
                int quantity = 1; // Varsayılan miktar
                double totalAmount = price * quantity;

                String query = "INSERT INTO sales_history (product_id, seller_username, buyer_username, quantity, price, total_amount) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, productId);
                pstmt.setString(2, sellerUsername);
                pstmt.setString(3, buyerUsername);
                pstmt.setInt(4, quantity);
                pstmt.setDouble(5, price);
                pstmt.setDouble(6, totalAmount);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addPurchaseHistory(int productId, String buyerUsername, String sellerUsername) {
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }

        try {
            // Önce ürün bilgilerini al
            String productQuery = "SELECT price FROM products WHERE id = ?";
            PreparedStatement productStmt = conn.prepareStatement(productQuery);
            productStmt.setInt(1, productId);
            ResultSet rs = productStmt.executeQuery();

            if (rs.next()) {
                double price = rs.getDouble("price");
                int quantity = 1; // Varsayılan miktar
                double totalAmount = price * quantity;

                String query = "INSERT INTO purchase_history (product_id, buyer_username, seller_username, quantity, price, total_amount) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, productId);
                pstmt.setString(2, buyerUsername);
                pstmt.setString(3, sellerUsername);
                pstmt.setInt(4, quantity);
                pstmt.setDouble(5, price);
                pstmt.setDouble(6, totalAmount);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Map<String, Object>> getSalesHistory(String sellerUsername) {
        Connection conn = getConnection();
        if (conn == null) {
            return new ArrayList<>();
        }

        String query = "SELECT p.*, sh.sale_date FROM sales_history sh " +
                      "JOIN products p ON sh.product_id = p.id " +
                      "WHERE sh.seller_username = ? " +
                      "ORDER BY sh.sale_date DESC";

        List<Map<String, Object>> salesHistory = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, sellerUsername);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> sale = new HashMap<>();
                sale.put("id", rs.getInt("id"));
                sale.put("name", rs.getString("name"));
                sale.put("description", rs.getString("description"));
                sale.put("price", rs.getDouble("price"));
                sale.put("category", rs.getString("category"));
                sale.put("sale_date", rs.getTimestamp("sale_date"));
                salesHistory.add(sale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return salesHistory;
    }

    public List<Map<String, Object>> getPurchaseHistory(String buyerUsername) {
        Connection conn = getConnection();
        if (conn == null) {
            return new ArrayList<>();
        }

        String query = "SELECT p.*, ph.purchase_date FROM purchase_history ph " +
                      "JOIN products p ON ph.product_id = p.id " +
                      "WHERE ph.buyer_username = ? " +
                      "ORDER BY ph.purchase_date DESC";

        List<Map<String, Object>> purchaseHistory = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, buyerUsername);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> purchase = new HashMap<>();
                purchase.put("id", rs.getInt("id"));
                purchase.put("name", rs.getString("name"));
                purchase.put("description", rs.getString("description"));
                purchase.put("price", rs.getDouble("price"));
                purchase.put("category", rs.getString("category"));
                purchase.put("purchase_date", rs.getTimestamp("purchase_date"));
                purchaseHistory.add(purchase);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return purchaseHistory;
    }
}