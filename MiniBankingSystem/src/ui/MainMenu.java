package ui;

import model.Account;
import service.AuthService;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * MainMenu — the application entry point UI.
 *
 * Shows the initial "Login / Register / Exit" selection and routes the
 * user to either the CustomerMenu or AdminMenu.
 */
public class MainMenu {

    private final AuthService   authService   = new AuthService();
    private final Scanner       scanner;

    public MainMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    // ----------------------------------------------------------------- run loop

    /**
     * Main application loop. Runs until the user chooses to exit.
     */
    public void run() {

        printWelcome();

        boolean running = true;

        while (running) {
            printDivider('=', 42);
            System.out.println("         MINI BANKING SYSTEM");
            printDivider('=', 42);
            System.out.println(" 1. Customer Login");
            System.out.println(" 2. Customer Registration");
            System.out.println(" 3. Admin Login");
            System.out.println(" 4. Exit");
            printDivider('-', 42);
            System.out.print(" Enter Choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> customerLogin();
                case "2" -> customerRegister();
                case "3" -> adminLogin();
                case "4" -> {
                    System.out.println("\n Thank you for using Mini Banking System. Goodbye!\n");
                    running = false;
                }
                default -> System.out.println("\n [!] Invalid choice. Please enter 1–4.\n");
            }
        }
    }

    // ---------------------------------------------------------------- customer login

    private void customerLogin() {
        System.out.println();
        System.out.println(" --- Customer Login ---");
        System.out.print(" Email   : ");
        String email = scanner.nextLine().trim();

        System.out.print(" Password: ");
        String password = scanner.nextLine().trim();

        try {
            Account account = authService.login(email, password);
            System.out.println("\n [✓] Login successful! Welcome, "
                    + account.getCustomerName() + "\n");

            // Hand off to customer dashboard
            new CustomerMenu(scanner).show(account);

        } catch (IllegalArgumentException e) {
            System.out.println("\n [!] " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.out.println("\n [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // -------------------------------------------------------------- customer register

    private void customerRegister() {
        System.out.println();
        System.out.println(" --- New Customer Registration ---");

        System.out.print(" Full Name       : ");
        String name = scanner.nextLine();

        System.out.print(" Email           : ");
        String email = scanner.nextLine();

        System.out.print(" Phone Number    : ");
        String phone = scanner.nextLine();

        System.out.print(" Address         : ");
        String address = scanner.nextLine();

        System.out.print(" Date of Birth (dd-MM-yyyy): ");
        String dob = scanner.nextLine();

        System.out.print(" Password (min 6 chars)     : ");
        String password = scanner.nextLine();

        System.out.print(" Account Type (SAVINGS/CURRENT): ");
        String accountType = scanner.nextLine();

        try {
            Account account = authService.register(
                    name, email, phone, address, dob, password, accountType);

            System.out.println();
            printDivider('-', 42);
            System.out.println(" [✓] Registration Successful!");
            System.out.printf("     Account Number : %s%n", account.getAccountNumber());
            System.out.printf("     Account Holder : %s%n", account.getCustomerName());
            System.out.printf("     Account Type   : %s%n", account.getAccountType());
            System.out.printf("     Initial Balance: \u20B90.00%n");
            printDivider('-', 42);
            System.out.println(" Please login to continue.\n");

        } catch (IllegalArgumentException e) {
            System.out.println("\n [!] Registration failed: " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.out.println("\n [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // ----------------------------------------------------------------- admin login

    private void adminLogin() {
        System.out.println();
        System.out.println(" --- Admin Login ---");
        System.out.print(" Admin Email   : ");
        String email = scanner.nextLine().trim();

        System.out.print(" Admin Password: ");
        String password = scanner.nextLine().trim();

        AdminMenu adminMenu = new AdminMenu(scanner);

        if (adminMenu.authenticate(email, password)) {
            System.out.println("\n [✓] Admin login successful!\n");
            adminMenu.show();
        } else {
            System.out.println("\n [!] Invalid admin credentials.\n");
        }
    }

    // ----------------------------------------------------------------------- helpers

    private void printWelcome() {
        System.out.println();
        printDivider('*', 42);
        System.out.println("    WELCOME TO MINI BANKING SYSTEM");
        System.out.println("        Java + JDBC + MySQL");
        printDivider('*', 42);
        System.out.println();
    }

    private void printDivider(char ch, int width) {
        System.out.println(String.valueOf(ch).repeat(width));
    }
}
