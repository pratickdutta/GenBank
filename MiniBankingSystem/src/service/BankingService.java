package service;

import dao.AccountDAO;
import dao.TransactionDAO;
import model.Account;
import model.Account.Status;
import model.Transaction;
import model.Transaction.TransactionStatus;
import model.Transaction.TransactionType;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * BankingService — business logic for all financial operations.
 *
 * This is where the most important JDBC demonstrations live:
 *   - Deposit   : single-step transaction with commit
 *   - Withdrawal: single-step transaction with validation and commit
 *   - Transfer  : multi-step JDBC transaction with setAutoCommit(false),
 *                 commit() on success, and rollback() on any failure
 */
public class BankingService {

    private final AccountDAO     accountDAO     = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    // ------------------------------------------------------------------- deposit

    /**
     * Deposits an amount into the given account.
     *
     * JDBC transaction flow:
     *   setAutoCommit(false) → UPDATE balance → INSERT transaction → commit()
     *   If any step fails    → rollback()
     *
     * @param account the account to credit
     * @param amount  the deposit amount (must be > 0)
     * @throws IllegalArgumentException if amount is invalid
     * @throws SQLException             if a database error occurs
     */
    public void deposit(Account account, BigDecimal amount)
            throws IllegalArgumentException, SQLException {

        validateAmount(amount);

        BigDecimal newBalance = account.getBalance().add(amount);

        // Open a single connection and manage the transaction manually
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);          // ← JDBC: disable auto-commit
            try {
                // Step 1: update balance
                accountDAO.updateBalance(conn, account.getAccountId(), newBalance);

                // Step 2: record transaction
                Transaction tx = new Transaction(
                        account.getAccountId(),
                        TransactionType.DEPOSIT,
                        amount,
                        "Deposit to account " + account.getAccountNumber(),
                        TransactionStatus.SUCCESS
                );
                transactionDAO.recordTransaction(conn, tx);

                conn.commit();                  // ← JDBC: commit both steps atomically
                account.setBalance(newBalance); // update in-memory object

                System.out.println("[JDBC] Deposit committed successfully.");

            } catch (SQLException e) {
                conn.rollback();                // ← JDBC: rollback on failure
                System.out.println("[JDBC] Deposit rolled back due to error: " + e.getMessage());
                throw e;
            }
        }
    }

    // ----------------------------------------------------------------- withdrawal

    /**
     * Withdraws an amount from the given account.
     *
     * JDBC transaction flow (same pattern as deposit):
     *   setAutoCommit(false) → validate balance → UPDATE → INSERT → commit()
     *   On failure           → rollback()
     *
     * @param account the account to debit
     * @param amount  the withdrawal amount (must be > 0 and ≤ current balance)
     * @throws IllegalArgumentException if amount is invalid or insufficient balance
     * @throws SQLException             if a database error occurs
     */
    public void withdraw(Account account, BigDecimal amount)
            throws IllegalArgumentException, SQLException {

        validateAmount(amount);

        if (amount.compareTo(account.getBalance()) > 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance. Available: ₹" +
                    String.format("%,.2f", account.getBalance()));
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                accountDAO.updateBalance(conn, account.getAccountId(), newBalance);

                Transaction tx = new Transaction(
                        account.getAccountId(),
                        TransactionType.WITHDRAWAL,
                        amount,
                        "Withdrawal from account " + account.getAccountNumber(),
                        TransactionStatus.SUCCESS
                );
                transactionDAO.recordTransaction(conn, tx);

                conn.commit();
                account.setBalance(newBalance);

                System.out.println("[JDBC] Withdrawal committed successfully.");

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("[JDBC] Withdrawal rolled back: " + e.getMessage());
                throw e;
            }
        }
    }

    // ----------------------------------------------------------------- transfer

    /**
     * Transfers money from one account to another.
     *
     * This is the PRIMARY JDBC TRANSACTION DEMONSTRATION in the project.
     *
     * JDBC transaction flow:
     * <pre>
     *   connection.setAutoCommit(false)
     *          │
     *          ▼
     *   UPDATE accounts SET balance = senderNewBalance WHERE account_id = sender
     *          │
     *          ▼
     *   UPDATE accounts SET balance = receiverNewBalance WHERE account_id = receiver
     *          │
     *          ▼
     *   INSERT INTO transactions (sender debit record)
     *          │
     *          ▼
     *   INSERT INTO transactions (receiver credit record)
     *          │
     *     all OK?
     *     ├─ YES → connection.commit()
     *     └─ NO  → connection.rollback()   ← sender balance is RESTORED
     * </pre>
     *
     * @param senderAccount   the account to debit
     * @param receiverNumber  the destination account number
     * @param amount          the amount to transfer
     * @throws IllegalArgumentException if input is invalid or balance is insufficient
     * @throws SQLException             if a database error occurs
     */
    public void transfer(Account senderAccount, String receiverNumber,
                         BigDecimal amount)
            throws IllegalArgumentException, SQLException {

        validateAmount(amount);

        if (receiverNumber == null || receiverNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Receiver account number cannot be empty.");
        }

        if (senderAccount.getAccountNumber().equals(receiverNumber.trim())) {
            throw new IllegalArgumentException(
                    "Cannot transfer money to your own account.");
        }

        if (amount.compareTo(senderAccount.getBalance()) > 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance. Available: ₹" +
                    String.format("%,.2f", senderAccount.getBalance()));
        }

        // One connection, one transaction — this is what JDBC transaction management means
        try (Connection conn = DBConnection.getConnection()) {

            // ─── STEP 0: Disable auto-commit ──────────────────────────────────────────
            conn.setAutoCommit(false);
            System.out.println("[JDBC] setAutoCommit(false) — transaction started");

            try {
                // ─── STEP 1: Verify receiver account exists ────────────────────────────
                Account receiverAccount = accountDAO.findByAccountNumber(receiverNumber.trim());
                if (receiverAccount == null) {
                    throw new IllegalArgumentException(
                            "Receiver account not found: " + receiverNumber);
                }
                if (receiverAccount.getStatus() == Status.INACTIVE) {
                    throw new IllegalArgumentException(
                            "Receiver account is inactive.");
                }

                // ─── STEP 2: Calculate new balances ───────────────────────────────────
                BigDecimal senderNewBalance   = senderAccount.getBalance().subtract(amount);
                BigDecimal receiverNewBalance = receiverAccount.getBalance().add(amount);

                // ─── STEP 3: Deduct from sender ───────────────────────────────────────
                boolean senderUpdated = accountDAO.updateBalance(
                        conn, senderAccount.getAccountId(), senderNewBalance);
                if (!senderUpdated) {
                    throw new SQLException("Failed to debit sender account.");
                }
                System.out.println("[JDBC] Sender balance deducted: ₹" + amount);

                // ─── STEP 4: Add to receiver ──────────────────────────────────────────
                boolean receiverUpdated = accountDAO.updateBalance(
                        conn, receiverAccount.getAccountId(), receiverNewBalance);
                if (!receiverUpdated) {
                    throw new SQLException("Failed to credit receiver account.");
                }
                System.out.println("[JDBC] Receiver balance credited: ₹" + amount);

                // ─── STEP 5: Record sender transaction ────────────────────────────────
                Transaction senderTx = new Transaction(
                        senderAccount.getAccountId(),
                        TransactionType.TRANSFER,
                        amount,
                        "Transfer to account " + receiverNumber,
                        TransactionStatus.SUCCESS
                );
                senderTx.setReferenceId(receiverAccount.getAccountId());
                int senderTxId = transactionDAO.recordTransaction(conn, senderTx);

                // ─── STEP 6: Record receiver transaction ──────────────────────────────
                Transaction receiverTx = new Transaction(
                        receiverAccount.getAccountId(),
                        TransactionType.TRANSFER,
                        amount,
                        "Transfer from account " + senderAccount.getAccountNumber(),
                        TransactionStatus.SUCCESS
                );
                receiverTx.setReferenceId(senderAccount.getAccountId());
                transactionDAO.recordTransaction(conn, receiverTx);

                // ─── STEP 7: COMMIT ───────────────────────────────────────────────────
                conn.commit();
                System.out.println("[JDBC] commit() — all steps successful. Transfer complete.");

                // Update in-memory account balance
                senderAccount.setBalance(senderNewBalance);

            } catch (SQLException | IllegalArgumentException e) {
                // ─── ON ANY FAILURE: ROLLBACK ─────────────────────────────────────────
                conn.rollback();
                System.out.println("[JDBC] rollback() — transfer aborted: " + e.getMessage());
                throw e;
            }
        }
    }

    // ------------------------------------------------------------ transaction history

    /**
     * Returns all transactions for the given account.
     *
     * @param accountId the account ID to look up
     * @return list of {@link Transaction} objects, newest first
     * @throws SQLException if a database error occurs
     */
    public List<Transaction> getTransactionHistory(int accountId) throws SQLException {
        return transactionDAO.findByAccountId(accountId);
    }

    // ----------------------------------------------------------- admin operations

    /**
     * Toggles an account between ACTIVE and INACTIVE (admin only).
     *
     * @param accountNumber the account number to toggle
     * @return the new status
     * @throws SQLException if a database error occurs or account not found
     */
    public Status toggleAccountStatus(String accountNumber) throws SQLException {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        Status newStatus = (account.getStatus() == Status.ACTIVE)
                ? Status.INACTIVE : Status.ACTIVE;
        accountDAO.updateStatus(accountNumber, newStatus);
        return newStatus;
    }

    // ------------------------------------------------------------------ validation

    /**
     * Validates that an amount is a positive number.
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
}
