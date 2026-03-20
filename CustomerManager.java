import java.io.*;
import java.util.*;

/**
 * CustomerManager.java
 * Central manager for all customers and accounts.
 * Handles: adding, finding, deleting customers,
 *          and saving/loading data from files.
 *
 * Concepts: File I/O (BufferedReader, BufferedWriter, FileWriter),
 *           ArrayList, Generics usage
 */
public class CustomerManager {

    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String ACCOUNTS_FILE  = "accounts.txt";

    // In-memory lists loaded from file on startup
    private List<Customer_Info<Account_Info>> customers;
    private List<Account_Info<Account_Info.AccountType>> accounts;

    // ─── Constructor ─────────────────────────────────────────────
    public CustomerManager() {
        customers = new ArrayList<>();
        accounts  = new ArrayList<>();
        loadFromFiles(); // load saved data on startup
    }

    // ─── Customer Operations ──────────────────────────────────────

    /**
     * Registers a new customer and their account.
     */
    public boolean registerCustomer(String name, String email,
                                    String phone, String password,
                                    Account_Info.AccountType accountType,
                                    double initialDeposit) {
        // Check duplicate email
        for (Customer_Info<Account_Info> c : customers) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                System.out.println("[Manager] Email already registered.");
                return false;
            }
        }

        String customerID   = generateCustomerID();
        String accountNumber = generateAccountNumber();

        // Create account
        Account_Info<Account_Info.AccountType> account =
            new Account_Info<>(accountNumber, customerID, accountType, initialDeposit);

        // Create customer and link account
        Customer_Info<Account_Info> customer =
            new Customer_Info<>(customerID, name, email, phone, password);
        customer.setAccountData(account);

        customers.add(customer);
        accounts.add(account);

        saveToFiles(); // persist immediately
        System.out.println("[Manager] Registered: " + customer);
        return true;
    }

    /**
     * Finds a customer by their ID.
     */
    public Customer_Info<Account_Info> findCustomerByID(String id) {
        for (Customer_Info<Account_Info> c : customers) {
            if (c.getCustomerID().equals(id)) return c;
        }
        return null;
    }

    /**
     * Finds a customer by email.
     */
    public Customer_Info<Account_Info> findCustomerByEmail(String email) {
        for (Customer_Info<Account_Info> c : customers) {
            if (c.getEmail().equalsIgnoreCase(email)) return c;
        }
        return null;
    }

    /**
     * Finds an account by account number.
     */
    public Account_Info<Account_Info.AccountType> findAccount(String accountNumber) {
        for (Account_Info<Account_Info.AccountType> a : accounts) {
            if (a.getAccountNumber().equals(accountNumber)) return a;
        }
        return null;
    }

    /**
     * Validates login credentials.
     * @return the Customer_Info if valid, null otherwise
     */
    public Customer_Info<Account_Info> login(String email, String password) {
        Customer_Info<Account_Info> customer = findCustomerByEmail(email);
        if (customer != null && customer.verifyPassword(password)) {
            System.out.println("[Manager] Login successful: " + customer.getName());
            return customer;
        }
        System.out.println("[Manager] Login failed for: " + email);
        return null;
    }

    /**
     * Deletes a customer and their account.
     */
    public boolean deleteCustomer(String customerID) {
        Customer_Info<Account_Info> customer = findCustomerByID(customerID);
        if (customer == null) return false;

        Account_Info linked = customer.getAccountData();
        customers.remove(customer);
        accounts.remove(linked);

        saveToFiles();
        System.out.println("[Manager] Deleted customer: " + customerID);
        return true;
    }

    public List<Customer_Info<Account_Info>> getAllCustomers() {
        return Collections.unmodifiableList(customers);
    }

    // ─── File I/O ─────────────────────────────────────────────────

    /**
     * Saves all customers and accounts to text files.
     * Concept: File I/O — BufferedWriter, FileWriter
     */
    public void saveToFiles() {
        // Save customers
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CUSTOMERS_FILE))) {
            for (Customer_Info<Account_Info> c : customers) {
                bw.write(c.toFileString());
                bw.newLine();
            }
            System.out.println("[IO] Customers saved.");
        } catch (IOException e) {
            System.out.println("[IO] Error saving customers: " + e.getMessage());
        }

        // Save accounts
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ACCOUNTS_FILE))) {
            for (Account_Info<Account_Info.AccountType> a : accounts) {
                bw.write(a.toFileString());
                bw.newLine();
            }
            System.out.println("[IO] Accounts saved.");
        } catch (IOException e) {
            System.out.println("[IO] Error saving accounts: " + e.getMessage());
        }
    }

    /**
     * Loads customers and accounts from text files on startup.
     * Concept: File I/O — BufferedReader, FileReader
     */
    public void loadFromFiles() {
        // Load accounts first (so we can link them to customers)
        File accountsFile = new File(ACCOUNTS_FILE);
        if (accountsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(accountsFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Account_Info<Account_Info.AccountType> a = Account_Info.fromFileString(line);
                    if (a != null) accounts.add(a);
                }
                System.out.println("[IO] Loaded " + accounts.size() + " accounts.");
            } catch (IOException e) {
                System.out.println("[IO] Error loading accounts: " + e.getMessage());
            }
        }

        // Load customers and link to their accounts
        File customersFile = new File(CUSTOMERS_FILE);
        if (customersFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(customersFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Customer_Info<Account_Info> c = Customer_Info.fromFileString(line);
                    if (c != null) {
                        // Link account to customer
                        for (Account_Info<Account_Info.AccountType> a : accounts) {
                            if (a.getOwnerID().equals(c.getCustomerID())) {
                                c.setAccountData(a);
                                break;
                            }
                        }
                        customers.add(c);
                    }
                }
                System.out.println("[IO] Loaded " + customers.size() + " customers.");
            } catch (IOException e) {
                System.out.println("[IO] Error loading customers: " + e.getMessage());
            }
        }
    }

    // ─── ID Generators ────────────────────────────────────────────

    private String generateCustomerID() {
        return "CUST" + String.format("%04d", customers.size() + 1);
    }

    private String generateAccountNumber() {
        return "ACC" + String.format("%06d", accounts.size() + 1);
    }
}
