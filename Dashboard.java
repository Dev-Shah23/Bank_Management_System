import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Dashboard.java
 * Main screen shown after successful login.
 * Displays customer info, account balance, and navigation to
 * Transaction and History screens.
 *
 * Concept: JavaFX basics (GridPane, HBox, VBox, Label, Button)
 */
public class Dashboard {

    private Stage stage;
    private CustomerManager manager;
    private Customer_Info<Account_Info> customer;

    public Dashboard(Stage stage, CustomerManager manager,
            Customer_Info<Account_Info> customer) {
        this.stage = stage;
        this.manager = manager;
        this.customer = customer;
    }

    public void show() {
        stage.setTitle("Dashboard — " + customer.getName());

        Account_Info account = customer.getAccountData();

        // ── Header ───────────────────────────────────────────────
        Label greeting = new Label("Welcome, " + customer.getName() + " 👋");
        greeting.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        greeting.setTextFill(Color.WHITE);

        Label idLabel = new Label("Customer ID: " + customer.getCustomerID());
        idLabel.setTextFill(Color.LIGHTBLUE);

        VBox header = new VBox(5, greeting, idLabel);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #1a73e8;");

        // ── Account Card ─────────────────────────────────────────
        Label accNumLabel = new Label("Account No:  " + (account != null ? account.getAccountNumber() : "N/A"));
        Label accTypeLabel = new Label("Account Type: " + (account != null ? account.getAccountType() : "N/A"));
        Label balanceLabel = new Label(
                "Balance:  ₹" + (account != null ? String.format("%.2f", account.getBalance()) : "0.00"));
        balanceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        balanceLabel.setTextFill(Color.DARKGREEN);

        accNumLabel.setFont(Font.font("Arial", 14));
        accTypeLabel.setFont(Font.font("Arial", 14));

        VBox accountCard = new VBox(10, accNumLabel, accTypeLabel, balanceLabel);
        accountCard.setPadding(new Insets(20));
        accountCard.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");

        // ── Navigation Buttons ───────────────────────────────────
        Button transactBtn = makeNavButton("Deposit / Withdraw / Transfer", "#1a73e8");
        Button historyBtn = makeNavButton("Transaction History", "#34a853");
        Button billingBtn = makeNavButton("Billing Statement", "#f57c00");
        Button logoutBtn = makeNavButton("Logout", "#ea4335");

        transactBtn.setOnAction(e -> {
            Account_Register txScreen = new Account_Register(stage, manager, customer);
            txScreen.show();
        });

        historyBtn.setOnAction(e -> {
            showHistoryAlert(account);
        });

        billingBtn.setOnAction(e -> {
            BillingController.open(stage, manager, customer);
        });

        logoutBtn.setOnAction(e -> {
            Login login = new Login(stage, manager);
            login.show();
        });

        // ── Layout ───────────────────────────────────────────────
        VBox content = new VBox(15,
                new Label("Account Overview"),
                accountCard,
                new Separator(),
                transactBtn,
                historyBtn,
                billingBtn,
                logoutBtn);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f0f4ff;");

        Label contentTitle = (Label) content.getChildren().get(0);
        contentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        contentTitle.setTextFill(Color.DARKBLUE);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(content);

        stage.setScene(new Scene(root, 520, 500));
        stage.show();
    }

    private Button makeNavButton(String text, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;" +
                "-fx-font-size: 14px; -fx-padding: 12 20 12 20;" +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }

    private void showHistoryAlert(Account_Info account) {
        if (account == null)
            return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction History");
        alert.setHeaderText("Account: " + account.getAccountNumber());

        StringBuilder sb = new StringBuilder();
        for (Object entry : account.getTransactionHistory()) {
            sb.append(entry.toString()).append("\n");
        }
        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefSize(500, 300);

        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }
}
