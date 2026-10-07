package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * DBConnection — Utility class that provides a JDBC Connection to H2 embedded database.
 *
 * JDBC Concepts demonstrated:
 *   - Class.forName() / DriverManager (Type 4 driver loading)
 *   - Connection obtained via DriverManager.getConnection()
 *   - Credentials loaded from db.properties to avoid hard-coding
 *   - Schema auto-initialisation on first run (no manual SQL setup needed)
 */
public class DBConnection {

    private static final Logger LOG = Logger.getLogger(DBConnection.class.getName());

    // Loaded once from db.properties on the classpath
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (in == null) {
                throw new RuntimeException(
                        "db.properties not found on classpath.");
            }
            PROPS.load(in);
            applyEnvironmentOverride("GENBANK_DB_URL", "db.url");
            applyEnvironmentOverride("GENBANK_DB_USER", "db.user");
            applyEnvironmentOverride("GENBANK_DB_PASSWORD", "db.password");

            // Explicitly load the H2 Type-4 JDBC driver
            Class.forName("org.h2.Driver");

            // Auto-initialise the schema on first run
            initSchema();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to initialise JDBC driver: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise database schema: " + e.getMessage(), e);
        }
    }

    private static void applyEnvironmentOverride(String environmentKey, String propertyKey) {
        String value = System.getenv(environmentKey);
        if (value != null) {
            PROPS.setProperty(propertyKey, value);
        }
    }

    /**
     * Returns a new JDBC Connection for every call.
     * Callers are responsible for closing the connection (use try-with-resources).
     *
     * @return a live {@link Connection} to the H2 database
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        String url      = PROPS.getProperty("db.url");
        String user     = PROPS.getProperty("db.user");
        String password = PROPS.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Creates all tables if they do not already exist.
     * This replaces the manual mysql schema.sql step — runs automatically on startup.
     */
    private static void initSchema() throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password"));
             Statement stmt = conn.createStatement()) {

            // customers table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id   INT            NOT NULL AUTO_INCREMENT,
                    name          VARCHAR(100)   NOT NULL,
                    email         VARCHAR(100)   NOT NULL UNIQUE,
                    phone         VARCHAR(15)    NOT NULL,
                    address       TEXT           NOT NULL,
                    date_of_birth DATE           NOT NULL,
                    password      VARCHAR(255)   NOT NULL,
                    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (customer_id)
                )
            """);

            // accounts table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    account_id     INT              NOT NULL AUTO_INCREMENT,
                    account_number VARCHAR(10)      NOT NULL UNIQUE,
                    customer_id    INT              NOT NULL,
                    account_type   VARCHAR(10)      NOT NULL DEFAULT 'SAVINGS',
                    balance        DECIMAL(15, 2)   NOT NULL DEFAULT 0.00,
                    status         VARCHAR(10)      NOT NULL DEFAULT 'ACTIVE',
                    created_at     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (account_id),
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
                )
            """);

            // transactions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id   INT              NOT NULL AUTO_INCREMENT,
                    account_id       INT              NOT NULL,
                    transaction_type VARCHAR(15)      NOT NULL,
                    amount           DECIMAL(15, 2)   NOT NULL,
                    reference_id     INT              DEFAULT NULL,
                    description      VARCHAR(255)     DEFAULT NULL,
                    status           VARCHAR(10)      NOT NULL DEFAULT 'SUCCESS',
                    transaction_date TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (transaction_id),
                    FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
                )
            """);

            // Indices
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_accounts_customer_id ON accounts(customer_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id)");

            LOG.info("[DB] Schema initialised successfully.");
        }
    }

    // Utility class — no instances allowed
    private DBConnection() {}
}
