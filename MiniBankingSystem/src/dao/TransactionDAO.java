package dao;

import model.Transaction;
import model.Transaction.TransactionType;
import model.Transaction.TransactionStatus;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDAO — Data Access Object for the TRANSACTIONS table.
 *
 * JDBC concepts demonstrated:
 *   - PreparedStatement with nullable parameter (referenceId)
 *   - ResultSet mapping
 *   - Accepting an external Connection for participation in caller-managed transactions
 *   - try-with-resources
 */
public class TransactionDAO {

    // -------------------------------------------------------------------- record

    /**
     * Inserts a transaction record.
     *
     * Accepts an external {@link Connection} so that recording the transaction
     * participates in the same JDBC transaction as the balance updates
     * during a fund transfer (commit/rollback together).
     *
     * @param conn        the active transactional connection
     * @param transaction the transaction to record
     * @return the generated transaction_id, or -1 on failure
     * @throws SQLException if a database error occurs
     */
    public int recordTransaction(Connection conn,
                                 Transaction transaction) throws SQLException {

        String sql =
            "INSERT INTO transactions " +
            "(account_id, transaction_type, amount, reference_id, description, status) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        // Use the caller's connection — do NOT open a new one
        try (PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, transaction.getAccountId());
            ps.setString(2, transaction.getTransactionType().name());
            ps.setBigDecimal(3, transaction.getAmount());

            // referenceId is nullable (only populated for transfers)
            if (transaction.getReferenceId() != null) {
                ps.setInt(4, transaction.getReferenceId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setString(5, transaction.getDescription());
            ps.setString(6, transaction.getStatus().name());

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

    // -------------------------------------------- history by account (customer)

    /**
     * Returns all transactions for a given account, newest first.
     *
     * @param accountId the account whose history to retrieve
     * @return list of transactions
     * @throws SQLException if a database error occurs
     */
    public List<Transaction> findByAccountId(int accountId) throws SQLException {

        List<Transaction> list = new ArrayList<>();
        String sql =
            "SELECT * FROM transactions " +
            "WHERE account_id = ? " +
            "ORDER BY transaction_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    // -------------------------------------------------- all transactions (admin)

    /**
     * Returns all transaction records (admin view).
     * Uses Statement because there are no user-supplied parameters.
     *
     * @return list of all transactions
     * @throws SQLException if a database error occurs
     */
    public List<Transaction> findAll() throws SQLException {

        List<Transaction> list = new ArrayList<>();
        String sql =
            "SELECT * FROM transactions ORDER BY transaction_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    // ------------------------------------------------------------ ResultSet mapper

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Transaction} object.
     */
    private Transaction mapResultSet(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setAccountId(rs.getInt("account_id"));
        t.setTransactionType(TransactionType.valueOf(rs.getString("transaction_type")));
        t.setAmount(rs.getBigDecimal("amount"));

        int refId = rs.getInt("reference_id");
        t.setReferenceId(rs.wasNull() ? null : refId);

        t.setDescription(rs.getString("description"));
        t.setStatus(TransactionStatus.valueOf(rs.getString("status")));

        Timestamp ts = rs.getTimestamp("transaction_date");
        if (ts != null) {
            t.setTransactionDate(ts.toLocalDateTime());
        }

        return t;
    }
}
