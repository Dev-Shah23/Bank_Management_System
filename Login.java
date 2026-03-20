import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

/**
 * Login.java
 * JavaFX login screen. Allows existing customers to log in
 * or navigate to the registration screen.
 *
 * Concept: JavaFX basics (Scene, Stage, VBox, TextField, Button, Label)
 */
public class Login {

    private Stage stage;
    private CustomerManager manager;

    public Login(Stage stage, CustomerManager manager) {
        this.stage   = stage;
        this.manager = manager;
    }

    public void show() {
        stage.setTitle("Bank Management System — Login");

        // ── Title ────────────────────────────────────────────────
        Label title = new Label("🏦 Bank Management System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.DARKBLUE);

        Label subtitle = new Label("Please log in to continue");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.GRAY);

        // ── Form Fields ──────────────────────────────────────────
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setMaxWidth(300);

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        passField.setMaxWidth(300);

        // ── Feedback Label ───────────────────────────────────────
        Label feedbackLabel = new Label("");
        feedbackLabel.setTextFill(Color.RED);

        // ── Buttons ──────────────────────────────────────────────
        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;" +
                          "-fx-font-size: 14px; -fx-padding: 8 30 8 30;" +
                          "-fx-background-radius: 5;");

        Button registerBtn = new Button("New Customer? Register");
        registerBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a73e8;" +
                             "-fx-underline: true; -fx-cursor: hand;");

        // ── Button Actions ───────────────────────────────────────
        loginBtn.setOnAction(e -> {
            String email    = emailField.getText().trim();
            String password = passField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                feedbackLabel.setText("Please fill in all fields.");
                return;
            }

            Customer_Info<Account_Info> customer = manager.login(email, password);
            if (customer != null) {
                feedbackLabel.setTextFill(Color.GREEN);
                feedbackLabel.setText("Login successful! Welcome, " + customer.getName());
                // Open dashboard after short delay
                new Thread(() -> {
                    try { Thread.sleep(800); } catch (InterruptedException ex) {}
                    javafx.application.Platform.runLater(() -> {
                        Dashboard dashboard = new Dashboard(stage, manager, customer);
                        dashboard.show();
                    });
                }).start();
            } else {
                feedbackLabel.setTextFill(Color.RED);
                feedbackLabel.setText("Invalid email or password.");
            }
        });

        registerBtn.setOnAction(e -> {
            Outer_Register reg = new Outer_Register(stage, manager);
            reg.show();
        });

        // ── Layout ───────────────────────────────────────────────
        VBox form = new VBox(10,
            emailLabel, emailField,
            passLabel, passField,
            feedbackLabel,
            loginBtn, registerBtn
        );
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setMaxWidth(380);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");

        VBox root = new VBox(20, title, subtitle, form);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f0f4ff;");

        stage.setScene(new Scene(root, 520, 480));
        stage.setResizable(false);
        stage.show();
    }
}
