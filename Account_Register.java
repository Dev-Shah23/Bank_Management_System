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
 * FIX: now uses BiConsumer<Boolean, String> callback so errors from
 *      the background thread (e.g. nonexistent account) show on the UI.
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
        Label title = new Label("Transactions");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.DARKBLUE);

        Label balanceLabel = new Label("Current Balance: Rs." +
            (account != null ? String.format("%.2f", account.getBalance()) : "0.00"));
        balanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        balanceLabel.setTextFill(Color.DARKGREEN);

        // ── Transaction Type Selector ────────────────────────────
        ToggleGroup typeGroup   = new ToggleGroup();
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
        amountField.setPromptText("Enter amount (Rs.)");
        amountField.setMaxWidth(280);

        // ── Transfer Target Field ────────────────────────────────
        Label targetLabel = new Label("Target Account Number:");
        targetLabel.setVisible(false);

        TextField targetAccountField = new TextField();
        targetAccountField.setPromptText("e.g. ACC000002");
        targetAccountField.setMaxWidth(280);
        targetAccountField.setVisible(false);

        transferBtn.setOnAction(e -> {
            targetLabel.setVisible(true);
            targetAccountField.setVisible(true);
        });
        depositBtn.setOnAction(e -> {
            targetLabel.setVisible(false);
            targetAccountField.setVisible(false);
        });
        withdrawBtn.setOnAction(e -> {
            targetLabel.setVisible(false);
            targetAccountField.setVisible(false);
        });

        // ── Feedback Label (shows success OR error from thread) ──
        Label feedbackLabel = new Label("");
        feedbackLabel.setFont(Font.font("Arial", 13));
        feedbackLabel.setWrapText(true);
        feedbackLabel.setMaxWidth(300);

        // ── Submit Button ─────────────────────────────────────────
        Button submitBtn = new Button("Submit Transaction");
        submitBtn.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white;" +
                           "-fx-font-size: 14px; -fx-padding: 10 30 10 30;" +
                           "-fx-background-radius: 5;");

        submitBtn.setOnAction(e -> {

            // ── Validate amount input ─────────────────────────────
            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                feedbackLabel.setTextFill(Color.RED);
                feedbackLabel.setText("Please enter a valid positive amount.");
                return;
            }

            if (account == null) {
                feedbackLabel.setTextFill(Color.RED);
                feedbackLabel.setText("No account linked to this customer.");
                return;
            }

            // ── Disable button & show processing ─────────────────
            submitBtn.setDisable(true);
            feedbackLabel.setTextFill(Color.GRAY);
            feedbackLabel.setText("Processing...");

            // ── Build the callback — this runs on JavaFX thread ──
            // Boolean success: true = green message, false = red message
            // String message: exact error or success text from Update.java
            java.util.function.BiConsumer<Boolean, String> callback = (success, message) -> {
                submitBtn.setDisable(false);
                if (success) {
                    feedbackLabel.setTextFill(Color.GREEN);
                    // Refresh balance display on success
                    balanceLabel.setText("Current Balance: Rs." +
                        String.format("%.2f", account.getBalance()));
                    amountField.clear();
                    targetAccountField.clear();
                } else {
                    feedbackLabel.setTextFill(Color.RED);
                }
                feedbackLabel.setText(message);
            };

            // ── Submit to correct async method ────────────────────
            RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
            String type = selected.getText();

            switch (type) {
                case "Deposit":
                    updateService.depositAsync(account.getAccountNumber(), amount, callback);
                    break;

                case "Withdraw":
                    updateService.withdrawAsync(account.getAccountNumber(), amount, callback);
                    break;

                case "Transfer":
                    String target = targetAccountField.getText().trim();
                    if (target.isEmpty()) {
                        submitBtn.setDisable(false);
                        feedbackLabel.setTextFill(Color.RED);
                        feedbackLabel.setText("Please enter a target account number.");
                        return;
                    }
                    updateService.transferAsync(
                        account.getAccountNumber(), target, amount, callback
                    );
                    break;
            }
        });

        // ── Back Button ──────────────────────────────────────────
        Button backBtn = new Button("Back to Dashboard");
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

        stage.setScene(new Scene(root, 520, 580));
        stage.show();
    }
}
