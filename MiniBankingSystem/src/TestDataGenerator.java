import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class TestDataGenerator {

    public static void main(String[] args) throws Exception {
        // Force the DBConnection class to load, which initializes the schema
        Class.forName("util.DBConnection");
        
        System.out.println("Connecting to H2 database...");
        String url = "jdbc:h2:file:./banking_data;MODE=MySQL;DB_CLOSE_DELAY=-1";
        
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            System.out.println("Connected. Inserting test data...");
            
            // Insert Customers
            String insertCustomer = "INSERT INTO customers (name, email, phone, address, date_of_birth, password) " +
                                  "VALUES (?, ?, ?, ?, '1990-01-01', 'password123')";
            
            try (PreparedStatement ps = conn.prepareStatement(insertCustomer, Statement.RETURN_GENERATED_KEYS)) {
                // Customer 1
                ps.setString(1, "Alice Smith");
                ps.setString(2, "alice@example.com");
                ps.setString(3, "555-0101");
                ps.setString(4, "100 Surreal Lane");
                ps.executeUpdate();
                int aliceId = getGeneratedId(ps);
                
                // Customer 2
                ps.setString(1, "Bob Jones");
                ps.setString(2, "bob@example.com");
                ps.setString(3, "555-0202");
                ps.setString(4, "200 Finance Blvd");
                ps.executeUpdate();
                int bobId = getGeneratedId(ps);
                
                // Insert Accounts
                String insertAccount = "INSERT INTO accounts (account_number, customer_id, account_type, balance) " +
                                     "VALUES (?, ?, ?, ?)";
                                     
                try (PreparedStatement psAcc = conn.prepareStatement(insertAccount)) {
                    // Alice's Account
                    psAcc.setString(1, "1000000001");
                    psAcc.setInt(2, aliceId);
                    psAcc.setString(3, "SAVINGS");
                    psAcc.setBigDecimal(4, new BigDecimal("50000.00"));
                    psAcc.executeUpdate();
                    
                    // Bob's Account
                    psAcc.setString(1, "1000000002");
                    psAcc.setInt(2, bobId);
                    psAcc.setString(3, "CURRENT");
                    psAcc.setBigDecimal(4, new BigDecimal("25000.00"));
                    psAcc.executeUpdate();
                }
            }
            
            System.out.println("Test data inserted successfully!");
            System.out.println("--------------------------------------------------");
            System.out.println("Test Accounts Created:");
            System.out.println("1. Alice Smith (alice@example.com)");
            System.out.println("   Account: 1000000001 | Password: password123 | Balance: 50,000");
            System.out.println("2. Bob Jones (bob@example.com)");
            System.out.println("   Account: 1000000002 | Password: password123 | Balance: 25,000");
            System.out.println("--------------------------------------------------");
        }
    }
    
    private static int getGeneratedId(PreparedStatement ps) throws Exception {
        try (var rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new Exception("Failed to get generated key");
        }
    }
}
