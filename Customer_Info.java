/**
 * Customer_Info.java
 * Stores all information about a bank customer.
 * Concept: Generics — the class is generic so it can hold
 * any type of associated data (e.g., Account_Info, documents, etc.)
 * Implements Reportable interface.
 */
public class Customer_Info<T> implements Reportable {

    private String customerID;
    private String name;
    private String email;
    private String phone;
    private String encryptedPassword;  // stored encrypted via AES
    private T accountData;             // Generic: holds linked account info

    // ─── Constructor ────────────────────────────────────────────
    public Customer_Info(String customerID, String name, String email,
                         String phone, String plainPassword) {
        this.customerID       = customerID;
        this.name             = name;
        this.email            = email;
        this.phone            = phone;
        this.encryptedPassword = AES.encrypt(plainPassword); // encrypt on creation
    }

    // ─── Getters ─────────────────────────────────────────────────
    public String getCustomerID()        { return customerID; }
    public String getName()              { return name; }
    public String getEmail()             { return email; }
    public String getPhone()             { return phone; }
    public String getEncryptedPassword() { return encryptedPassword; }
    public T getAccountData()            { return accountData; }

    // ─── Setters ─────────────────────────────────────────────────
    public void setName(String name)         { this.name = name; }
    public void setEmail(String email)       { this.email = email; }
    public void setPhone(String phone)       { this.phone = phone; }
    public void setAccountData(T data)       { this.accountData = data; }

    /**
     * Updates the password — encrypts the new one before saving.
     */
    public void setPassword(String newPlainPassword) {
        this.encryptedPassword = AES.encrypt(newPlainPassword);
    }

    /**
     * Verifies a login password attempt.
     * @param attempt the plain text password typed by user
     * @return true if correct
     */
    public boolean verifyPassword(String attempt) {
        return AES.verify(attempt, this.encryptedPassword);
    }

    // ─── Reportable Interface Implementation ─────────────────────

    @Override
    public String generateReport() {
        return String.format(
            "=== Customer Report ===\n" +
            "ID     : %s\n" +
            "Name   : %s\n" +
            "Email  : %s\n" +
            "Phone  : %s\n" +
            "Account: %s\n",
            customerID, name, email, phone,
            (accountData != null ? accountData.toString() : "No account linked")
        );
    }

    @Override
    public void printStatement() {
        System.out.println(generateReport());
    }

    /**
     * Serializes customer to a CSV-style string for file storage.
     * Format: customerID,name,email,phone,encryptedPassword
     */
    public String toFileString() {
        return customerID + "," + name + "," + email + "," +
               phone + "," + encryptedPassword;
    }

    /**
     * Reconstructs a Customer_Info from a saved file line.
     * Note: accountData is NOT included here (loaded separately).
     */
    public static Customer_Info<Account_Info> fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) return null;
        Customer_Info<Account_Info> c = new Customer_Info<>(
            parts[0], parts[1], parts[2], parts[3], "PLACEHOLDER"
        );
        // Restore already-encrypted password directly (bypass re-encryption)
        c.encryptedPassword = parts[4];
        return c;
    }

    @Override
    public String toString() {
        return "Customer[" + customerID + " | " + name + "]";
    }
}
