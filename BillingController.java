import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * BillingController.java
 * Controller for Billing.fxml (Scene Builder layout).
 *
 * Rubric: Billing management + Scene Builder and various other components usage
 *
 * Loaded via FXMLLoader — JavaFX connects the @FXML fields
 * automatically to the fx:id attributes in Billing.fxml.
 */
public class BillingController {

    // ── @FXML fields wired to Billing.fxml fx:id attributes ──────
    @FXML private Label lblAccountNo;
    @FXML private Label lblAccountType;
    @FXML private Label lblCustomerName;
    @FXML private Label lblBalance;
    @FXML private Label lblTotalTx;
    @FXML private Label headerSubtitle;

    @FXML private TableView<String> transactionTable;
    @FXML private TableColumn<String, String> colEntry;

    // Passed in before the screen is shown
    private Stage stage;
    private CustomerManager manager;
    private Customer_Info<Account_Info> customer;

    // ─── Called by FXMLLoader after all @FXML fields are injected ─
    @FXML
    public void initialize() {
        // Wire the TableColumn to display the string value of each row
        colEntry.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue())
        );
    }

    /**
     * Called from Dashboard after FXMLLoader creates this controller.
     * Populates all labels and the transaction table.
     */
    public void setData(Stage stage, CustomerManager manager,
                        Customer_Info<Account_Info> customer) {
        this.stage    = stage;
        this.manager  = manager;
        this.customer = customer;

        Account_Info account = customer.getAccountData();

        // ── Populate summary labels ───────────────────────────────
        lblCustomerName.setText(customer.getName());
        headerSubtitle.setText("Statement for " + customer.getName()
                               + "  |  ID: " + customer.getCustomerID());

        if (account != null) {
            lblAccountNo.setText(account.getAccountNumber());
            lblAccountType.setText(account.getAccountType().toString());
            lblBalance.setText("Rs. " + String.format("%.2f", account.getBalance()));
            lblTotalTx.setText(String.valueOf(account.getTransactionHistory().size()));

            // ── Populate transaction table ────────────────────────
            ObservableList<String> entries = FXCollections.observableArrayList();
            for (Object entry : account.getTransactionHistory()) {
                entries.add(entry.toString());
            }
            transactionTable.setItems(entries);
        } else {
            lblAccountNo.setText("No account linked");
            lblAccountType.setText("—");
            lblBalance.setText("Rs. 0.00");
            lblTotalTx.setText("0");
        }
    }

    // ── Button Handlers (wired via onAction in FXML) ──────────────

    @FXML
    private void handlePrint() {
        // Generates a plain-text bill and shows it in an Alert dialog
        Account_Info account = customer.getAccountData();

        StringBuilder bill = new StringBuilder();
        bill.append("===========================================\n");
        bill.append("         BANK MANAGEMENT SYSTEM           \n");
        bill.append("              BILLING STATEMENT           \n");
        bill.append("===========================================\n");
        bill.append("Customer    : ").append(customer.getName()).append("\n");
        bill.append("Customer ID : ").append(customer.getCustomerID()).append("\n");
        bill.append("Email       : ").append(customer.getEmail()).append("\n");
        bill.append("Phone       : ").append(customer.getPhone()).append("\n");
        bill.append("-------------------------------------------\n");

        if (account != null) {
            bill.append("Account No  : ").append(account.getAccountNumber()).append("\n");
            bill.append("Type        : ").append(account.getAccountType()).append("\n");
            bill.append("Balance     : Rs. ")
                .append(String.format("%.2f", account.getBalance())).append("\n");
            bill.append("===========================================\n");
            bill.append("           TRANSACTION HISTORY            \n");
            bill.append("===========================================\n");
            for (Object entry : account.getTransactionHistory()) {
                bill.append(entry.toString()).append("\n");
            }
        }

        bill.append("===========================================\n");
        bill.append("         Thank you for banking with us!   \n");
        bill.append("===========================================\n");

        // Show in a dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Billing Statement");
        alert.setHeaderText("Statement for " + customer.getName());

        TextArea area = new TextArea(bill.toString());
        area.setEditable(false);
        area.setWrapText(false);
        area.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        area.setPrefSize(520, 400);

        alert.getDialogPane().setContent(area);
        alert.getDialogPane().setPrefWidth(560);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        Dashboard dashboard = new Dashboard(stage, manager, customer);
        dashboard.show();
    }

    // ─── Static factory: load FXML and return configured controller ─

    /**
     * Loads Billing.fxml, injects data, and shows the screen.
     * Called from Dashboard.java.
     */
    public static void open(Stage stage, CustomerManager manager,
                            Customer_Info<Account_Info> customer) {
        try {
            FXMLLoader loader = new FXMLLoader(
                BillingController.class.getResource("Billing.fxml")
            );
            Parent root = loader.load();

            BillingController controller = loader.getController();
            controller.setData(stage, manager, customer);

            // Apply the global CSS stylesheet
            Scene scene = new Scene(root, 600, 580);
            scene.getStylesheets().add(
                BillingController.class.getResource("application.css").toExternalForm()
            );

            stage.setTitle("Billing Statement — " + customer.getName());
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.out.println("[Billing] Failed to load FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
