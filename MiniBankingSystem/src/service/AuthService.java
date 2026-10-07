package service;

import dao.AccountDAO;
import dao.CustomerDAO;
import model.Account;
import model.Account.AccountType;
import model.Customer;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;

/**
 * AuthService — handles customer registration and login.
 *
 * Keeps authentication logic out of the UI layer.
 * Delegates all database work to CustomerDAO and AccountDAO.
 */
public class AuthService {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO  accountDAO  = new AccountDAO();
    private final Random      random      = new Random();

    // ----------------------------------------------------------------- register

    /**
     * Registers a new customer and creates their bank account.
     *
     * Steps:
     *  1. Validate all input fields.
     *  2. Check whether the email is already registered.
     *  3. Insert the customer (CustomerDAO).
     *  4. Generate a unique 10-digit account number.
     *  5. Create the account (AccountDAO).
     *
     * @param name        full name
     * @param email       email address
     * @param phone       phone number
     * @param address     residential address
     * @param dobString   date of birth as "dd-MM-yyyy"
     * @param password    plain-text password
     * @param accountType "SAVINGS" or "CURRENT"
     * @return the newly created {@link Account}, or {@code null} on failure
     * @throws IllegalArgumentException if input validation fails
     * @throws SQLException             if a database error occurs
     */
    public Account register(String name, String email, String phone,
                            String address, String dobString,
                            String password, String accountType)
            throws IllegalArgumentException, SQLException {

        // ---------- validation
        if (isBlank(name))     throw new IllegalArgumentException("Name cannot be empty.");
        if (isBlank(email))    throw new IllegalArgumentException("Email cannot be empty.");
        if (isBlank(phone))    throw new IllegalArgumentException("Phone cannot be empty.");
        if (isBlank(address))  throw new IllegalArgumentException("Address cannot be empty.");
        if (isBlank(password)) throw new IllegalArgumentException("Password cannot be empty.");
        if (password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.");

        // parse date of birth
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobString.trim(),
                                  DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use dd-MM-yyyy.");
        }

        // parse account type
        AccountType type;
        try {
            type = AccountType.valueOf(accountType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Account type must be SAVINGS or CURRENT.");
        }

        // ---------- duplicate email check
        if (customerDAO.emailExists(email.trim())) {
            throw new IllegalArgumentException("Email already registered. Please log in.");
        }

        // ---------- create customer record
        Customer customer = new Customer(
                name.trim(), email.trim(), phone.trim(),
                address.trim(), dob, password.trim()
        );
        int customerId = customerDAO.registerCustomer(customer);
        if (customerId == -1) {
            throw new SQLException("Failed to register customer.");
        }

        // ---------- generate a unique 10-digit account number
        String accountNumber = generateAccountNumber();

        // ---------- create account record
        Account account = new Account(
                accountNumber, customerId, type, BigDecimal.ZERO
        );
        int accountId = accountDAO.createAccount(account);
        if (accountId == -1) {
            throw new SQLException("Failed to create bank account.");
        }

        account.setAccountId(accountId);
        account.setCustomerName(name.trim());
        return account;
    }

    // -------------------------------------------------------------------- login

    /**
     * Authenticates a customer by email and password, then loads their account.
     *
     * @param email    the customer's email
     * @param password the customer's password
     * @return the customer's {@link Account} on success
     * @throws IllegalArgumentException if credentials are invalid
     * @throws SQLException             if a database error occurs
     */
    public Account login(String email, String password)
            throws IllegalArgumentException, SQLException {

        if (isBlank(email) || isBlank(password)) {
            throw new IllegalArgumentException("Email and password cannot be empty.");
        }

        Customer customer = customerDAO.findByEmailAndPassword(
                email.trim(), password.trim());

        if (customer == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        Account account = accountDAO.findByCustomerId(customer.getCustomerId());
        if (account == null) {
            throw new IllegalArgumentException("No account found for this customer.");
        }

        if (account.getStatus() == Account.Status.INACTIVE) {
            throw new IllegalArgumentException(
                    "Account is inactive. Please contact admin.");
        }

        return account;
    }

    // --------------------------------------------------- helper: account number

    /**
     * Generates a 10-digit account number starting with "10".
     * In production this would query the DB to guarantee uniqueness.
     */
    private String generateAccountNumber() {
        // Start with 10 and append 8 random digits → 10-digit number
        long suffix = (long) (Math.random() * 100_000_000L);
        return String.format("10%08d", suffix);
    }

    // --------------------------------------------------------------- blank check

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
