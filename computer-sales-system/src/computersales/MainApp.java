package computersales;

import computersales.ui.screens.LoginScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Computer Sales System");
        primaryStage.setScene(new Scene(new LoginScreen(primaryStage)));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 