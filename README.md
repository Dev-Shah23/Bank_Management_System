Here's your complete class explanation guide, covering every file, concept, and how they all connect:---

## 📁 File-by-File Explanation

---

### 1. `Main.java` — The Entry Point
This is where the entire application starts. It extends `Application` which is the JavaFX requirement for any GUI app. The `start()` method is automatically called by JavaFX when the app launches. Inside it, we create the two shared objects — `CustomerManager` and `Update` — that every other screen uses. We also kick off the two background daemon threads here. The `stop()` method is automatically called when the user closes the window, and we use it to save all data to files before shutting down.

---

### 2. `AES.java` — Encryption & Data Integrity
AES stands for Advanced Encryption Standard. This file uses Java's built-in `javax.crypto` library. We have a 16-character secret key (`BankSecureKey123`) which is 128-bit AES. When a customer registers, their password is immediately encrypted using `encrypt()` before being stored anywhere — in memory or in the file. When they log in, we encrypt what they typed and compare it to the stored encrypted value using `verify()`. This means the raw password is never stored anywhere, which is how real banking systems protect user data.

---

### 3. `Transactable.java` & `Reportable.java` — Interfaces
These are pure interfaces — they contain only method signatures with no implementation. `Transactable` defines `deposit()`, `withdraw()`, `transfer()`, and `getBalance()`. `Reportable` defines `generateReport()` and `printStatement()`. `Account_Info` implements both of these. The purpose of interfaces here is to enforce a contract — any class that says it implements `Transactable` **must** provide all four methods. This is the Java way of achieving abstraction.

---

### 4. `Customer_Info.java` — Generic Class
This is where **Generics** are demonstrated. The class is declared as `Customer_Info<T>` — the `T` is a type parameter, meaning it can hold any type of associated data. In our system, T is always `Account_Info`, so a customer holds their linked account. But because it's generic, theoretically T could be a document, a profile picture, or anything else. The class stores the customer's name, email, phone, and encrypted password. It implements `Reportable` so it can print a customer report. The `toFileString()` and `fromFileString()` methods handle converting the object to a comma-separated line for saving to a file and reading back.

---

### 5. `Account_Info.java` — Generic + Synchronized + Interfaces
This is the most concept-heavy file. It is also generic — declared as `Account_Info<T extends Enum<T>>` — meaning the type T must be an Enum. Our `AccountType` enum (SAVINGS, CURRENT, FIXED_DEPOSIT) is used as T. It implements both `Transactable` and `Reportable`.

The critical part is **synchronization**. Every method that touches the balance — `deposit()`, `withdraw()`, `transfer()` — is marked `synchronized`. Here's why this matters: imagine two people withdrawing money at the same time on two different threads. Without `synchronized`, both threads could read the balance as ₹1000 simultaneously, both see enough funds, both subtract ₹800, and the balance becomes -₹600. With `synchronized`, only one thread can enter the method at a time — the second thread waits until the first is completely done. The account also maintains a `transactionHistory` ArrayList that logs every operation with a timestamp.

---

### 6. `CustomerManager.java` — File I/O
This is the central manager class. It holds two ArrayLists — one for customers and one for accounts. On startup, `loadFromFiles()` is called, which uses `BufferedReader` and `FileReader` to read `customers.txt` and `accounts.txt` line by line and reconstruct the objects. Every time a transaction or registration happens, `saveToFiles()` is called, which uses `BufferedWriter` and `FileWriter` to write all current data back to the files. This is how data persists between sessions. It also handles `registerCustomer()`, `login()`, `findCustomerByID()`, and `deleteCustomer()`.

---

### 7. `Update.java` — Multithreading & Synchronization
This file contains all the threading logic. Here's a breakdown of every thread in the system:

**Thread 1 & 2 & 3... — Transaction Threads (from the thread pool)**
We use `ExecutorService` with a fixed thread pool of 5 threads. When the user clicks Deposit, Withdraw, or Transfer in the UI, a new `TransactionThread` (which implements `Runnable`) is submitted to the pool. It runs in the background so the UI never freezes. Inside `run()`, it calls the synchronized methods on `Account_Info`, so even if 5 transactions are submitted at once, they queue up safely on the account's lock.

**Thread — Interest Calculator (Daemon)**
This thread starts when the app launches and runs forever in the background. Every 60 seconds it wakes up, loops through all customers, finds SAVINGS accounts, calculates 0.5% of the current balance, and deposits it. It's a daemon thread, meaning it automatically dies when the main app closes — you don't need to stop it manually.

**Thread — Low Balance Alert (Daemon)**
Also starts at launch, also runs forever. Every 30 seconds it checks all accounts. If any balance is below ₹500, it prints an alert to the console. Also a daemon thread.

---

### 8. `Login.java` — JavaFX Screen
This is a JavaFX screen built using `VBox` (vertical layout), `TextField`, `PasswordField`, `Label`, and `Button`. The `Scene` wraps all the UI components, and the `Stage` is the actual window. When the login button is clicked, it calls `manager.login()` which does the AES password verification. On success, a small anonymous thread waits 800ms for the success message to show, then uses `Platform.runLater()` to switch to the Dashboard — `Platform.runLater` is required because you can only update the JavaFX UI from the JavaFX thread, not from background threads.

---

### 9. `Outer_Register.java` — JavaFX Screen
Registration form with fields for name, email, phone, password, account type (ComboBox dropdown), and initial deposit. On submit, it validates all fields, parses the deposit amount, and calls `manager.registerCustomer()`. On success it navigates back to Login.

---

### 10. `Dashboard.java` — JavaFX Screen
Shown after login. Uses `BorderPane` layout — header at the top, content in the center. Displays the customer's name, ID, account number, type, and balance. Has three navigation buttons — Transactions, History, and Logout. The History button opens an `Alert` dialog with a `TextArea` showing the full transaction log pulled from `Account_Info.getTransactionHistory()`.

---

### 11. `Account_Register.java` — JavaFX Screen
The transaction screen. Uses `RadioButton` with a `ToggleGroup` to let the user pick Deposit, Withdraw, or Transfer. When Transfer is selected, an extra field for the target account number becomes visible. On submit, it calls the appropriate async method in `Update.java` — `depositAsync()`, `withdrawAsync()`, or `transferAsync()`. The button is disabled during processing to prevent double-clicking. The `onComplete` callback runs on the JavaFX thread via `Platform.runLater()` to refresh the balance display once the background thread finishes.

---

## 🧵 Summary: How Many Threads?

| Thread | Type | When | Purpose |
|---|---|---|---|
| JavaFX Main Thread | Main | Always | Runs the UI |
| Transaction threads (up to 5) | Pool | On each transaction | Deposit/Withdraw/Transfer |
| Interest Calculator | Daemon | App start | Adds interest every 60s |
| Low Balance Alert | Daemon | App start | Warns every 30s |
| Navigation threads | Anonymous | Screen switches | 800ms delay then screen change |

**Total: up to 9 threads can be active at the same time**, and synchronization on `Account_Info` ensures they never corrupt the balance data.
