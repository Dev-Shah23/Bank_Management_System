import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main.java
 * Entry point for the Bank Management System JavaFX application.
 * Initializes the CustomerManager, Update service, background threads,
 * and launches the Login screen.
 *
 * Concept: JavaFX Application lifecycle
 */
public class Main extends Application {

    // Shared instances — passed between screens
    public static CustomerManager customerManager;
    public static Update updateService;

    @Override
    public void start(Stage primaryStage) {
        // Initialize core services
        customerManager = new CustomerManager();
        updateService   = new Update(customerManager);

        // Start background daemon threads
        updateService.startInterestCalculator(customerManager);
        updateService.startLowBalanceAlert(customerManager);

        // Launch the Login screen
        Login loginScreen = new Login(primaryStage, customerManager);
        loginScreen.show();
    }

    @Override
    public void stop() {
        // Called when the app window is closed
        if (updateService != null) updateService.shutdown();
        if (customerManager != null) customerManager.saveToFiles();
        System.out.println("[Main] Application closed. Data saved.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
