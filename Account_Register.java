import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

/**
 * Account_Register.java
 * Transaction screen — handles Deposit, Withdraw, and Transfer.
 * Each transaction is submitted as a background thread via Update.java.
 *
 * Concept: JavaFX basics, calls Update.java for threading
 */
public class Account_Register {

    private Stage stage;
    private CustomerManager manager;
    private Customer_Info<Account_Info> customer;
    private Update updateService;

    public Account_Register(Stage stage, CustomerManager manager,
                            Customer_Info<Account_Info> customer) {
        this.stage         = stage;
        this.manager       = manager;
        this.customer      = customer;
        this.updateService = Main.updateService;
    }

    public void show() {
        stage.setTitle("Transactions — " + customer.getName());

        Account_Info account = customer.getAccountData();

        // ── Header ───────────────────────────────────────────────
        Label title = new Label("💸 Transactions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.DARKBLUE);

        Label balanceLabel = new Label("Current Balance: ₹" +
            (account != null ? String.format("%.2f", account.getBalance()) : "0.00"));
        balanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        balanceLabel.setTextFill(Color.DARKGREEN);

        // ── Transaction Type Selector ────────────────────────────
        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton depositBtn  = new RadioButton("Deposit");
        RadioButton withdrawBtn = new RadioButton("Withdraw");
        RadioButton transferBtn = new RadioButton("Transfer");
        depositBtn.setToggleGroup(typeGroup);
        withdrawBtn.setToggleGroup(typeGroup);
        transferBtn.setToggleGroup(typeGroup);
        depositBtn.setSelected(true);

        HBox typeBox = new HBox(20, depositBtn, withdrawBtn, transferBtn);
        typeBox.setAlignment(Pos.CENTER);

        // ── Amount Field ─────────────────────────────────────────
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount (₹)");
        amountField.setMaxWidth(280);

        // ── Transfer Target Field (shown only for Transfer) ──────
        TextField targetAccountField = new TextField();
        targetAccountField.setPromptText("Target Account Number");
        targetAccountField.setMaxWidth(280);
        targetAccountField.setVisible(false);

        Label targetLabel = new Label("Target Account:");
        targetLabel.setVisible(false);

        transferBtn.setOnAction(e -> {
            targetAccountField.setVisible(true);
            targetLabel.setVisible(true);
        });
        depositBtn.setOnAction(e -> {
            targetAccountField.setVisible(false);
            targetLabel.setVisible(false);
        });
        withdrawBtn.setOnAction(e -> {
            targetAccountField.setVisible(false);
            targetLabel.setVisible(false);
        });

        // ── Feedback Label ───────────────────────────────────────
        Label feedbackLabel = new Label("");
        feedbackLabel.setFont(Font.font("Arial", 13));

        // ── Submit Button ────────────────────────────────────────
        Button submitBtn = new Button("Submit Transaction");
        submitBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;" +
                           "-fx-font-size: 14px; -fx-padding: 10 30 10 30;" +
                           "-fx-background-radius: 5;");

        submitBtn.setOnAction(e -> {
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                feedbackLabel.setTextFill(Color.RED);
                feedbackLabel.setText("Enter a valid positive amount.");
                return;
            }

            if (account == null) {
                feedbackLabel.setTextFill(Color.RED);
                feedbackLabel.setText("No account linked.");
                return;
            }

            submitBtn.setDisable(true);
            feedbackLabel.setTextFill(Color.GRAY);
            feedbackLabel.setText("Processing...");

            // Determine transaction type and run on background thread
            RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
            String type = selected.getText();

            Runnable onComplete = () -> {
                submitBtn.setDisable(false);
                balanceLabel.setText("Current Balance: ₹" +
                    String.format("%.2f", account.getBalance()));
                feedbackLabel.setTextFill(Color.GREEN);
                feedbackLabel.setText(type + " of ₹" + amount + " completed!");
                amountField.clear();
                targetAccountField.clear();
            };

            switch (type) {
                case "Deposit":
                    updateService.depositAsync(account.getAccountNumber(), amount, onComplete);
                    break;
                case "Withdraw":
                    updateService.withdrawAsync(account.getAccountNumber(), amount, onComplete);
                    break;
                case "Transfer":
                    String target = targetAccountField.getText().trim();
                    if (target.isEmpty()) {
                        feedbackLabel.setTextFill(Color.RED);
                        feedbackLabel.setText("Enter target account number.");
                        submitBtn.setDisable(false);
                        return;
                    }
                    updateService.transferAsync(account.getAccountNumber(), target, amount, onComplete);
                    break;
            }
        });

        // ── Back Button ──────────────────────────────────────────
        Button backBtn = new Button("← Back to Dashboard");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: gray;");
        backBtn.setOnAction(e -> {
            Dashboard dashboard = new Dashboard(stage, manager, customer);
            dashboard.show();
        });

        // ── Layout ───────────────────────────────────────────────
        VBox form = new VBox(12,
            balanceLabel,
            new Separator(),
            new Label("Transaction Type:"),
            typeBox,
            new Label("Amount:"),
            amountField,
            targetLabel,
            targetAccountField,
            feedbackLabel,
            submitBtn,
            backBtn
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");

        VBox root = new VBox(20, title, form);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f4ff;");

        stage.setScene(new Scene(root, 520, 560));
        stage.show();
    }
}
