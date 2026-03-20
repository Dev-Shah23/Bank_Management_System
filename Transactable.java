/**
 * Transactable.java
 * Interface defining core banking transaction operations.
 * Concept: Interfaces
 */
public interface Transactable {
    void deposit(double amount);
    boolean withdraw(double amount);
    boolean transfer(double amount, Account_Info target);
    double getBalance();
}
