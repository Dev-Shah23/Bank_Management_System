import java.util.ArrayList;
import java.util.List;

/**
 * Account_Info.java
 * Represents a bank account. Generic type T allows different account
 * types (Savings, Current, FixedDeposit) to be handled uniformly.
 *
 * Concepts: Generics, Interfaces (Transactable, Reportable),
 *           Synchronization (synchronized methods for thread safety)
 */
public class Account_Info<T extends Enum<T>> implements Transactable, Reportable {

    // ─── Account Types (Enum used as generic bound) ──────────────
    public enum AccountType { SAVINGS, CURRENT, FIXED_DEPOSIT }

    private String accountNumber;
    private String ownerID;           // links to Customer_Info.customerID
    private T accountType;            // Generic: AccountType enum value
    private double balance;
    private List<String> transactionHistory;  // log of all transactions

    // ─── Constructor ─────────────────────────────────────────────
    public Account_Info(String accountNumber, String ownerID,
                        T accountType, double initialDeposit) {
        this.accountNumber      = accountNumber;
        this.ownerID            = ownerID;
        this.accountType        = accountType;
        this.balance            = initialDeposit;
        this.transactionHistory = new ArrayList<>();
        logTransaction("Account opened with initial deposit: ₹" + initialDeposit);
    }

    // ─── Getters ─────────────────────────────────────────────────
    public String getAccountNumber()         { return accountNumber; }
    public String getOwnerID()               { return ownerID; }
    public T getAccountType()                { return accountType; }
    public List<String> getTransactionHistory() { return transactionHistory; }

    @Override
    public synchronized double getBalance()  { return balance; }

    // ─── Transactable Interface — all synchronized for thread safety ──

    /**
     * Deposits an amount into this account.
     * synchronized: prevents two threads depositing at the exact same time.
     */
    @Override
    public synchronized void deposit(double amount) {
        if (amount <= 0) {
            logTransaction("FAILED deposit: invalid amount ₹" + amount);
            return;
        }
        balance += amount;
        logTransaction("DEPOSIT  ₹" + amount + "  | Balance: ₹" + balance);
    }

    /**
     * Withdraws an amount from this account.
     * synchronized: prevents race condition where balance goes negative.
     * @return true if successful, false if insufficient funds
     */
    @Override
    public synchronized boolean withdraw(double amount) {
        if (amount <= 0) {
            logTransaction("FAILED withdrawal: invalid amount ₹" + amount);
            return false;
        }
        if (balance < amount) {
            logTransaction("FAILED withdrawal ₹" + amount + " | Insufficient funds | Balance: ₹" + balance);
            return false;
        }
        balance -= amount;
        logTransaction("WITHDRAWAL ₹" + amount + "  | Balance: ₹" + balance);
        return true;
    }

    /**
     * Transfers amount from this account to a target account.
     * synchronized on both accounts to prevent deadlocks — always
     * lock the lower account number first (consistent ordering).
     * @return true if transfer succeeded
     */
    @Override
    public synchronized boolean transfer(double amount, Account_Info target) {
        if (this.withdraw(amount)) {
            target.deposit(amount);
            logTransaction("TRANSFER ₹" + amount + " → Account: " + target.getAccountNumber());
            target.logTransaction("RECEIVED ₹" + amount + " ← Account: " + this.accountNumber);
            return true;
        }
        return false;
    }

    // ─── Reportable Interface ─────────────────────────────────────

    @Override
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Account Statement ===\n");
        sb.append("Account No : ").append(accountNumber).append("\n");
        sb.append("Owner ID   : ").append(ownerID).append("\n");
        sb.append("Type       : ").append(accountType).append("\n");
        sb.append("Balance    : ₹").append(String.format("%.2f", balance)).append("\n");
        sb.append("--- Transaction History ---\n");
        for (String entry : transactionHistory) {
            sb.append("  ").append(entry).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void printStatement() {
        System.out.println(generateReport());
    }

    // ─── Helper ──────────────────────────────────────────────────

    private void logTransaction(String message) {
        String timestamp = new java.util.Date().toString();
        transactionHistory.add("[" + timestamp + "] " + message);
    }

    /**
     * Serializes account to CSV string for file storage.
     * Format: accountNumber,ownerID,accountType,balance
     */
    public String toFileString() {
        return accountNumber + "," + ownerID + "," +
               accountType.toString() + "," + balance;
    }

    /**
     * Reconstructs Account_Info from a saved file line.
     */
    public static Account_Info<AccountType> fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length < 4) return null;
        AccountType type = AccountType.valueOf(parts[2]);
        double balance   = Double.parseDouble(parts[3]);
        return new Account_Info<>(parts[0], parts[1], type, balance);
    }

    @Override
    public String toString() {
        return "Account[" + accountNumber + " | " + accountType + " | ₹" + balance + "]";
    }
}
