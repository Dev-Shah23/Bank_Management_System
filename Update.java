import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Update.java
 * Handles deposit, withdrawal, and transfer operations.
 * Each transaction runs in its own thread using ExecutorService.
 *
 * Concepts: Multithreading (Runnable, Thread),
 * Synchronization (synchronized methods in Account_Info),
 * ExecutorService (thread pool)
 *
 * FIX: callback is now BiConsumer<Boolean, String>
 * Boolean = success or failure
 * String = message to show on the UI
 */
public class Update {

    // Thread pool: max 5 concurrent transactions
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(5);

    private CustomerManager manager;

    public Update(CustomerManager manager) {
        this.manager = manager;
    }

    // ─── Public Transaction Methods (called from JavaFX UI) ───────

    public void depositAsync(String accountNumber, double amount,
            BiConsumer<Boolean, String> onComplete) {
        threadPool.execute(new TransactionThread(
                "DEPOSIT", accountNumber, null, amount, onComplete));
    }

    public void withdrawAsync(String accountNumber, double amount,
            BiConsumer<Boolean, String> onComplete) {
        threadPool.execute(new TransactionThread(
                "WITHDRAW", accountNumber, null, amount, onComplete));
    }

    public void transferAsync(String fromAccount, String toAccount,
            double amount, BiConsumer<Boolean, String> onComplete) {
        threadPool.execute(new TransactionThread(
                "TRANSFER", fromAccount, toAccount, amount, onComplete));
    }

    public void shutdown() {
        threadPool.shutdown();
        System.out.println("[Update] Thread pool shut down.");
    }

    // ─── Inner Runnable Class ─────────────────────────────────────

    private class TransactionThread implements Runnable {

        private String type;
        private String fromAccountNumber;
        private String toAccountNumber;
        private double amount;
        private BiConsumer<Boolean, String> onComplete;

        public TransactionThread(String type, String fromAccountNumber,
                String toAccountNumber, double amount,
                BiConsumer<Boolean, String> onComplete) {
            this.type = type;
            this.fromAccountNumber = fromAccountNumber;
            this.toAccountNumber = toAccountNumber;
            this.amount = amount;
            this.onComplete = onComplete;
        }

        @Override
        public void run() {
            System.out.println("[Thread " + Thread.currentThread().getName() +
                    "] Starting " + type + " of " + amount);

            Account_Info fromAccount = manager.findAccount(fromAccountNumber);
            if (fromAccount == null) {
                runCallback(false, "Account not found: " + fromAccountNumber);
                return;
            }

            boolean success = false;
            String message = "";

            switch (type) {

                case "DEPOSIT":
                    if (amount <= 0) {
                        message = "Deposit amount must be greater than zero.";
                    } else {
                        fromAccount.deposit(amount);
                        success = true;
                        message = "Deposited Rs." + String.format("%.2f", amount) + " successfully!";
                    }
                    break;

                case "WITHDRAW":
                    if (amount <= 0) {
                        message = "Withdrawal amount must be greater than zero.";
                    } else if (fromAccount.getBalance() < amount) {
                        message = "Insufficient funds! Available balance: Rs."
                                + String.format("%.2f", fromAccount.getBalance());
                    } else {
                        success = fromAccount.withdraw(amount);
                        message = success
                                ? "Withdrew Rs." + String.format("%.2f", amount) + " successfully!"
                                : "Withdrawal failed. Please try again.";
                    }
                    break;

                case "TRANSFER":
                    if (toAccountNumber == null || toAccountNumber.trim().isEmpty()) {
                        message = "Please enter a target account number.";
                        break;
                    }
                    Account_Info toAccount = manager.findAccount(toAccountNumber);
                    if (toAccount == null) {
                        // ← THIS is the fix: nonexistent account now returns a clear message
                        message = "Target account \"" + toAccountNumber + "\" does not exist.";
                        break;
                    }
                    if (toAccountNumber.equals(fromAccountNumber)) {
                        message = "Cannot transfer to the same account.";
                        break;
                    }
                    if (fromAccount.getBalance() < amount) {
                        message = "Insufficient funds! Available balance: Rs."
                                + String.format("%.2f", fromAccount.getBalance());
                        break;
                    }
                    success = fromAccount.transfer(amount, toAccount);
                    message = success
                            ? "Transferred Rs." + String.format("%.2f", amount)
                                    + " to " + toAccountNumber + " successfully!"
                            : "Transfer failed. Please try again.";
                    break;

                default:
                    message = "Unknown transaction type: " + type;
            }

            if (success)
                manager.saveToFiles();

            System.out.println("[Thread " + Thread.currentThread().getName() +
                    "] " + (success ? "SUCCESS" : "FAILED") + ": " + message);

            runCallback(success, message);
        }

        private void runCallback(boolean success, String message) {
            if (onComplete != null) {
                javafx.application.Platform.runLater(() -> onComplete.accept(success, message));
            }
        }
    }

    // ─── Daemon Thread: Interest Calculator ──────────────────────

    public void startInterestCalculator(CustomerManager manager) {
        Thread t = new Thread(() -> {
            System.out.println("[Interest] Calculator thread started.");
            while (true) {
                try {
                    Thread.sleep(60_000);
                    for (Customer_Info<Account_Info> customer : manager.getAllCustomers()) {
                        Account_Info account = customer.getAccountData();
                        if (account == null)
                            continue;
                        if (account.getAccountType() == Account_Info.AccountType.SAVINGS) {
                            double interest = account.getBalance() * 0.005;
                            account.deposit(interest);
                            System.out.println("[Interest] Applied Rs."
                                    + String.format("%.2f", interest)
                                    + " to " + account.getAccountNumber());
                        }
                    }
                    manager.saveToFiles();
                } catch (InterruptedException e) {
                    System.out.println("[Interest] Thread interrupted.");
                    break;
                }
            }
        });
        t.setDaemon(true);
        t.setName("Interest-Calculator");
        t.start();
    }

    // ─── Daemon Thread: Low Balance Alert ────────────────────────

    public void startLowBalanceAlert(CustomerManager manager) {
        Thread t = new Thread(() -> {
            System.out.println("[Alert] Low balance monitor started.");
            while (true) {
                try {
                    Thread.sleep(30_000);
                    for (Customer_Info<Account_Info> customer : manager.getAllCustomers()) {
                        Account_Info account = customer.getAccountData();
                        if (account != null && account.getBalance() < 500.0) {
                            System.out.println("[ALERT] Low balance! "
                                    + customer.getName() + " | Rs."
                                    + String.format("%.2f", account.getBalance()));
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("[Alert] Thread interrupted.");
                    break;
                }
            }
        });
        t.setDaemon(true);
        t.setName("LowBalance-Alert");
        t.start();
    }
}
