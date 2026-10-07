package dao;

import model.Customer;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerDAO — Data Access Object for the CUSTOMERS table.
 *
 * JDBC demonstrated here:
 *   - PreparedStatement for all DML / parameterised queries
 *   - ResultSet for reading customer rows
 *   - SQLException for database error handling
 *   - try-with-resources for automatic Connection / Statement / ResultSet closing
 */
public class CustomerDAO {

    // ----------------------------------------------------------------- register

    /**
     * Inserts a new customer into the CUSTOMERS table.
     *
     * @param customer the customer to insert
     * @return the generated customer_id, or -1 on failure
     * @throws SQLException if a database error occurs
     */
    public int registerCustomer(Customer customer) throws SQLException {

        String sql = "INSERT INTO customers (name, email, phone, address, " +
                     "date_of_birth, password) VALUES (?, ?, ?, ?, ?, ?)";

        // try-with-resources automatically closes Connection and PreparedStatement
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.setDate(5, Date.valueOf(customer.getDateOfBirth()));
            ps.setString(6, customer.getPassword());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Retrieve the auto-generated primary key
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    // -------------------------------------------------------------------- login

    /**
     * Finds a customer by email and password (used for login).
     *
     * Demonstrates PreparedStatement with two parameters to prevent SQL injection.
     *
     * @param email    the customer's email address
     * @param password the customer's password
     * @return the matching {@link Customer}, or {@code null} if not found
     * @throws SQLException if a database error occurs
     */
    public Customer findByEmailAndPassword(String email, String password) throws SQLException {

        String sql = "SELECT * FROM customers WHERE email = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    // --------------------------------------------------------------- find by id

    /**
     * Retrieves a customer by their primary key.
     *
     * @param customerId the customer's database ID
     * @return the matching {@link Customer}, or {@code null} if not found
     * @throws SQLException if a database error occurs
     */
    public Customer findById(int customerId) throws SQLException {

        String sql = "SELECT * FROM customers WHERE customer_id = ?";

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

    // ----------------------------------------------------------------- all customers (admin)

    /**
     * Returns all customers in the database.
     * Uses a plain {@link Statement} because there are no user-supplied parameters.
     *
     * @return list of all customers
     * @throws SQLException if a database error occurs
     */
    public List<Customer> findAll() throws SQLException {

        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY created_at DESC";

        try (Connection conn   = DBConnection.getConnection();
             Statement  stmt   = conn.createStatement();
             ResultSet  rs     = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapResultSet(rs));
            }
        }
        return customers;
    }

    // ---------------------------------------------------------------- email exists

    /**
     * Checks whether an email address is already registered.
     *
     * @param email the email to check
     * @return {@code true} if the email is already in use
     * @throws SQLException if a database error occurs
     */
    public boolean emailExists(String email) throws SQLException {

        String sql = "SELECT COUNT(*) FROM customers WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------ ResultSet mapper

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Customer} object.
     * Called after rs.next() has already moved to a valid row.
     */
    private Customer mapResultSet(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            c.setDateOfBirth(dob.toLocalDate());
        }

        c.setPassword(rs.getString("password"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            c.setCreatedAt(createdAt.toLocalDateTime());
        }

        return c;
    }
}
