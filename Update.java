import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Update.java
 * Handles deposit, withdrawal, and transfer operations.
 * Each transaction runs in its own thread using ExecutorService.
 *
 * Concepts: Multithreading (Runnable, Thread),
 *           Synchronization (synchronized methods in Account_Info),
 *           ExecutorService (thread pool)
 */
public class Update {

    // Thread pool: max 5 concurrent transactions
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(5);

    private CustomerManager manager;

    public Update(CustomerManager manager) {
        this.manager = manager;
    }

    // ─── Public Transaction Methods (called from JavaFX UI) ───────

    /**
     * Runs a deposit in a background thread.
     * @param accountNumber account to deposit into
     * @param amount        amount to deposit
     * @param onComplete    callback runnable to refresh UI after done
     */
    public void depositAsync(String accountNumber, double amount, Runnable onComplete) {
        threadPool.execute(new TransactionThread(
            "DEPOSIT", accountNumber, null, amount, onComplete
        ));
    }

    /**
     * Runs a withdrawal in a background thread.
     */
    public void withdrawAsync(String accountNumber, double amount, Runnable onComplete) {
        threadPool.execute(new TransactionThread(
            "WITHDRAW", accountNumber, null, amount, onComplete
        ));
    }

    /**
     * Runs a transfer in a background thread.
     */
    public void transferAsync(String fromAccount, String toAccount,
                              double amount, Runnable onComplete) {
        threadPool.execute(new TransactionThread(
            "TRANSFER", fromAccount, toAccount, amount, onComplete
        ));
    }

    /**
     * Shuts down the thread pool gracefully.
     * Call this when the app closes.
     */
    public void shutdown() {
        threadPool.shutdown();
        System.out.println("[Update] Thread pool shut down.");
    }

    // ─── Inner Runnable Class ─────────────────────────────────────

    /**
     * TransactionThread.java (inner class)
     * Represents a single transaction task that runs on a thread.
     * Concept: Runnable interface, Thread
     */
    private class TransactionThread implements Runnable {

        private String type;
        private String fromAccountNumber;
        private String toAccountNumber;
        private double amount;
        private Runnable onComplete;

        public TransactionThread(String type, String fromAccountNumber,
                                 String toAccountNumber, double amount,
                                 Runnable onComplete) {
            this.type              = type;
            this.fromAccountNumber = fromAccountNumber;
            this.toAccountNumber   = toAccountNumber;
            this.amount            = amount;
            this.onComplete        = onComplete;
        }

        @Override
        public void run() {
            System.out.println("[Thread " + Thread.currentThread().getName() +
                               "] Starting " + type + " of ₹" + amount);

            Account_Info fromAccount = manager.findAccount(fromAccountNumber);

            if (fromAccount == null) {
                System.out.println("[Thread] Account not found: " + fromAccountNumber);
                runCallback();
                return;
            }

            boolean success = false;

            switch (type) {
                case "DEPOSIT":
                    fromAccount.deposit(amount);
                    success = true;
                    break;

                case "WITHDRAW":
                    success = fromAccount.withdraw(amount);
                    break;

                case "TRANSFER":
                    Account_Info toAccount = manager.findAccount(toAccountNumber);
                    if (toAccount == null) {
                        System.out.println("[Thread] Target account not found: " + toAccountNumber);
                        break;
                    }
                    success = fromAccount.transfer(amount, toAccount);
                    break;

                default:
                    System.out.println("[Thread] Unknown transaction type: " + type);
            }

            // Save updated state to file
            if (success) {
                manager.saveToFiles();
                System.out.println("[Thread " + Thread.currentThread().getName() +
                                   "] " + type + " completed successfully.");
            } else {
                System.out.println("[Thread " + Thread.currentThread().getName() +
                                   "] " + type + " failed.");
            }

            runCallback();
        }

        // Run the UI callback on JavaFX thread
        private void runCallback() {
            if (onComplete != null) {
                javafx.application.Platform.runLater(onComplete);
            }
        }
    }

    // ─── Background Daemon Thread: Interest Calculator ────────────

    /**
     * Starts a daemon thread that calculates and applies interest
     * every 60 seconds for SAVINGS accounts.
     * Concept: Daemon Thread, synchronized account methods
     */
    public void startInterestCalculator(CustomerManager manager) {
        Thread interestThread = new Thread(() -> {
            System.out.println("[Interest] Calculator thread started.");
            while (true) {
                try {
                    Thread.sleep(60_000); // every 60 seconds
                    for (Customer_Info<Account_Info> customer : manager.getAllCustomers()) {
                        Account_Info account = customer.getAccountData();
                        if (account == null) continue;
                        if (account.getAccountType() == Account_Info.AccountType.SAVINGS) {
                            double interest = account.getBalance() * 0.005; // 0.5% per interval
                            account.deposit(interest);
                            System.out.println("[Interest] Applied ₹" +
                                String.format("%.2f", interest) +
                                " interest to account " + account.getAccountNumber());
                        }
                    }
                    manager.saveToFiles();
                } catch (InterruptedException e) {
                    System.out.println("[Interest] Thread interrupted.");
                    break;
                }
            }
        });

        interestThread.setDaemon(true); // dies when app closes
        interestThread.setName("Interest-Calculator");
        interestThread.start();
    }

    // ─── Background Daemon Thread: Low Balance Alert ─────────────

    /**
     * Starts a daemon thread that monitors all accounts
     * and prints an alert if balance drops below ₹500.
     * Concept: Daemon Thread
     */
    public void startLowBalanceAlert(CustomerManager manager) {
        Thread alertThread = new Thread(() -> {
            System.out.println("[Alert] Low balance monitor started.");
            while (true) {
                try {
                    Thread.sleep(30_000); // check every 30 seconds
                    for (Customer_Info<Account_Info> customer : manager.getAllCustomers()) {
                        Account_Info account = customer.getAccountData();
                        if (account != null && account.getBalance() < 500.0) {
                            System.out.println("[ALERT] Low balance! Customer: " +
                                customer.getName() + " | Account: " +
                                account.getAccountNumber() + " | Balance: ₹" +
                                account.getBalance());
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("[Alert] Thread interrupted.");
                    break;
                }
            }
        });

        alertThread.setDaemon(true);
        alertThread.setName("LowBalance-Alert");
        alertThread.start();
    }
}
