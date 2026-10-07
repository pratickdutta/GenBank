package dao;

import model.Account;
import model.Account.AccountType;
import model.Account.Status;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AccountDAO — Data Access Object for the ACCOUNTS table.
 *
 * JDBC concepts demonstrated:
 *   - PreparedStatement for all parameterised queries
 *   - ResultSet mapping
 *   - Generated keys retrieval
 *   - try-with-resources
 *
 * NOTE: Balance update methods accept an external Connection parameter so that the
 * caller (BankingService) can manage the JDBC transaction (setAutoCommit / commit /
 * rollback) across multiple DAO calls during a fund transfer.
 */
public class AccountDAO {

    // -------------------------------------------------------------------- create

    /**
     * Inserts a new account into the ACCOUNTS table.
     *
     * @param account the account to create
     * @return the generated account_id, or -1 on failure
     * @throws SQLException if a database error occurs
     */
    public int createAccount(Account account) throws SQLException {

        String sql = "INSERT INTO accounts " +
                     "(account_number, customer_id, account_type, balance, status) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, account.getAccountNumber());
            ps.setInt(2, account.getCustomerId());
            ps.setString(3, account.getAccountType().name());
            ps.setBigDecimal(4, account.getBalance());
            ps.setString(5, account.getStatus().name());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    // ------------------------------------------------------- find by account number

    /**
     * Looks up an account by its 10-digit account number.
     * Also performs a JOIN to fetch the customer name for display.
     *
     * @param accountNumber the account number to search
     * @return the matching {@link Account}, or {@code null} if not found
     * @throws SQLException if a database error occurs
     */
    public Account findByAccountNumber(String accountNumber) throws SQLException {

        String sql =
            "SELECT a.*, c.name AS customer_name " +
            "FROM accounts a " +
            "JOIN customers c ON a.customer_id = c.customer_id " +
            "WHERE a.account_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    // -------------------------------------------------- find by customer id

    /**
     * Returns the account belonging to a given customer.
     *
     * @param customerId the customer's database ID
     * @return the customer's {@link Account}, or {@code null} if not found
     * @throws SQLException if a database error occurs
     */
    public Account findByCustomerId(int customerId) throws SQLException {

        String sql =
            "SELECT a.*, c.name AS customer_name " +
            "FROM accounts a " +
            "JOIN customers c ON a.customer_id = c.customer_id " +
            "WHERE a.customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------- all accounts (admin)

    /**
     * Returns all accounts (for admin view).
     * Uses Statement because there are no user-supplied parameters.
     *
     * @return list of all accounts
     * @throws SQLException if a database error occurs
     */
    public List<Account> findAll() throws SQLException {

        List<Account> accounts = new ArrayList<>();
        String sql =
            "SELECT a.*, c.name AS customer_name " +
            "FROM accounts a " +
            "JOIN customers c ON a.customer_id = c.customer_id " +
            "ORDER BY a.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                accounts.add(mapResultSet(rs));
            }
        }
        return accounts;
    }

    // ----------------------------------------------- update balance (TRANSACTIONAL)

    /**
     * Updates the balance for a given account.
     *
     * <strong>Important:</strong> This method accepts an existing {@link Connection}
     * so that it participates in a caller-managed JDBC transaction.
     * The caller must call {@code conn.setAutoCommit(false)} before invoking this,
     * and {@code conn.commit()} or {@code conn.rollback()} afterwards.
     *
     * @param conn      the transactional connection to use
     * @param accountId the account whose balance to update
     * @param newBalance the new balance to set
     * @return {@code true} if the update affected exactly one row
     * @throws SQLException if a database error occurs
     */
    public boolean updateBalance(Connection conn, int accountId,
                                 BigDecimal newBalance) throws SQLException {

        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";

        // Do NOT open a new Connection here — use the one passed in by the caller
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, accountId);
            return ps.executeUpdate() == 1;
        }
    }

    // ----------------------------------------------- activate / deactivate (admin)

    /**
     * Toggles the status of an account between ACTIVE and INACTIVE.
     *
     * @param accountNumber the account number to update
     * @param newStatus     the desired status
     * @return {@code true} if the update succeeded
     * @throws SQLException if a database error occurs
     */
    public boolean updateStatus(String accountNumber, Status newStatus) throws SQLException {

        String sql = "UPDATE accounts SET status = ? WHERE account_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.name());
            ps.setString(2, accountNumber);
            return ps.executeUpdate() == 1;
        }
    }

    // ------------------------------------------------------------ ResultSet mapper

    /**
     * Maps the current row of a {@link ResultSet} to an {@link Account} object.
     */
    private Account mapResultSet(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountId(rs.getInt("account_id"));
        a.setAccountNumber(rs.getString("account_number"));
        a.setCustomerId(rs.getInt("customer_id"));
        a.setAccountType(AccountType.valueOf(rs.getString("account_type")));
        a.setBalance(rs.getBigDecimal("balance"));
        a.setStatus(Status.valueOf(rs.getString("status")));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            a.setCreatedAt(createdAt.toLocalDateTime());
        }

        // customer_name is available only when the query includes the JOIN
        try {
            a.setCustomerName(rs.getString("customer_name"));
        } catch (SQLException ignored) {
            // Column not present — that's fine
        }

        return a;
    }
}
