import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

/**
 * Outer_Register.java
 * Registration landing screen — collects customer & account info,
 * then calls CustomerManager to save the new customer.
 *
 * Concept: JavaFX basics, ComboBox, form validation
 */
public class Outer_Register {

    private Stage stage;
    private CustomerManager manager;

    public Outer_Register(Stage stage, CustomerManager manager) {
        this.stage   = stage;
        this.manager = manager;
    }

    public void show() {
        stage.setTitle("Bank Management System — Register");

        // ── Title ────────────────────────────────────────────────
        Label title = new Label("Create New Account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.DARKBLUE);

        // ── Form Fields ──────────────────────────────────────────
        TextField nameField  = makeField("Full Name");
        TextField emailField = makeField("Email Address");
        TextField phoneField = makeField("Phone Number");
        PasswordField passField    = new PasswordField();
        passField.setPromptText("Create Password");
        passField.setMaxWidth(300);

        ComboBox<String> accountTypeBox = new ComboBox<>();
        accountTypeBox.getItems().addAll("SAVINGS", "CURRENT", "FIXED_DEPOSIT");
        accountTypeBox.setValue("SAVINGS");
        accountTypeBox.setMaxWidth(300);

        TextField depositField = makeField("Initial Deposit (₹)");

        Label feedbackLabel = new Label("");
        feedbackLabel.setTextFill(Color.RED);

        // ── Buttons ──────────────────────────────────────────────
        Button registerBtn = new Button("Register");
        registerBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;" +
                             "-fx-font-size: 14px; -fx-padding: 8 30 8 30;" +
                             "-fx-background-radius: 5;");

        Button backBtn = new Button("← Back to Login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: gray;");

        // ── Button Actions ───────────────────────────────────────
        registerBtn.setOnAction(e -> {
            String name    = nameField.getText().trim();
            String email   = emailField.getText().trim();
            String phone   = phoneField.getText().trim();
            String pass    = passField.getText();
            String accType = accountTypeBox.getValue();

            double deposit;
            try {
                deposit = Double.parseDouble(depositField.getText().trim());
                if (deposit < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                feedbackLabel.setText("Enter a valid deposit amount.");
                return;
            }

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                feedbackLabel.setText("Please fill in all fields.");
                return;
            }

            Account_Info.AccountType type = Account_Info.AccountType.valueOf(accType);
            boolean success = manager.registerCustomer(name, email, phone, pass, type, deposit);

            if (success) {
                feedbackLabel.setTextFill(Color.GREEN);
                feedbackLabel.setText("Registration successful! Please login.");
                new Thread(() -> {
                    try { Thread.sleep(1200); } catch (InterruptedException ex) {}
                    javafx.application.Platform.runLater(() -> {
                        Login login = new Login(stage, manager);
                        login.show();
                    });
                }).start();
            } else {
                feedbackLabel.setTextFill(Color.RED);
                feedbackLabel.setText("Email already exists. Try another.");
            }
        });

        backBtn.setOnAction(e -> {
            Login login = new Login(stage, manager);
            login.show();
        });

        // ── Layout ───────────────────────────────────────────────
        VBox form = new VBox(10,
            makeLabel("Full Name:"),         nameField,
            makeLabel("Email:"),             emailField,
            makeLabel("Phone:"),             phoneField,
            makeLabel("Password:"),          passField,
            makeLabel("Account Type:"),      accountTypeBox,
            makeLabel("Initial Deposit:"),   depositField,
            feedbackLabel,
            registerBtn, backBtn
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(30));
        form.setMaxWidth(400);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");

        VBox root = new VBox(20, title, form);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f4ff;");

        stage.setScene(new Scene(root, 520, 620));
        stage.show();
    }

    private TextField makeField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(300);
        return tf;
    }

    private Label makeLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 13));
        return l;
    }
}
