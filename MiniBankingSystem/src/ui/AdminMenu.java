package ui;

import dao.AccountDAO;
import dao.CustomerDAO;
import dao.TransactionDAO;
import model.Account;
import model.Account.Status;
import model.Customer;
import model.Transaction;
import service.BankingService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * AdminMenu — console UI for the administrator dashboard.
 *
 * The admin can view customers, accounts, transactions, and
 * activate/deactivate accounts. All DB work is done via DAO classes.
 */
public class AdminMenu {

    // Hard-coded admin credentials — fine for an academic project
    private static final String ADMIN_EMAIL    = "admin@bank.com";
    private static final String ADMIN_PASSWORD = "admin123";

    private final CustomerDAO    customerDAO    = new CustomerDAO();
    private final AccountDAO     accountDAO     = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final BankingService bankingService = new BankingService();
    private final Scanner        scanner;

    public AdminMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    // --------------------------------------------------------------- login check

    /**
     * Validates admin credentials.
     *
     * @param email    the entered email
     * @param password the entered password
     * @return {@code true} if credentials match
     */
    public boolean authenticate(String email, String password) {
        return ADMIN_EMAIL.equals(email.trim()) &&
               ADMIN_PASSWORD.equals(password.trim());
    }

    // ---------------------------------------------------------------- main menu

    /**
     * Shows the admin dashboard loop.
     */
    public void show() {

        boolean running = true;

        while (running) {
            printDivider('=', 42);
            System.out.println("           ADMIN PANEL");
            printDivider('=', 42);
            System.out.println(" 1. View All Customers");
            System.out.println(" 2. View All Accounts");
            System.out.println(" 3. Search Account by Number");
            System.out.println(" 4. View All Transactions");
            System.out.println(" 5. Activate / Deactivate Account");
            System.out.println(" 6. Logout");
            printDivider('-', 42);
            System.out.print(" Enter Choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllCustomers();
                case "2" -> viewAllAccounts();
                case "3" -> searchAccount();
                case "4" -> viewAllTransactions();
                case "5" -> toggleAccountStatus();
                case "6" -> {
                    System.out.println("\n Admin logged out.\n");
                    running = false;
                }
                default  -> System.out.println("\n [!] Invalid choice. Please enter 1–6.\n");
            }
        }
    }

    // --------------------------------------------------------- 1. all customers

    private void viewAllCustomers() {
        System.out.println();
        try {
            List<Customer> customers = customerDAO.findAll();
            if (customers.isEmpty()) {
                System.out.println(" No customers registered yet.\n");
                return;
            }
            System.out.printf("%-4s  %-20s  %-25s  %-13s%n",
                    "ID", "NAME", "EMAIL", "PHONE");
            printDivider('-', 68);
            for (Customer c : customers) {
                System.out.printf("%-4d  %-20s  %-25s  %-13s%n",
                        c.getCustomerId(), c.getName(), c.getEmail(), c.getPhone());
            }
            printDivider('-', 68);
            System.out.println();
        } catch (SQLException e) {
            System.out.println(" [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // ---------------------------------------------------------- 2. all accounts

    private void viewAllAccounts() {
        System.out.println();
        try {
            List<Account> accounts = accountDAO.findAll();
            if (accounts.isEmpty()) {
                System.out.println(" No accounts found.\n");
                return;
            }
            System.out.printf("%-12s  %-18s  %-10s  %-12s  %-8s%n",
                    "ACCOUNT NO", "HOLDER", "TYPE", "BALANCE", "STATUS");
            printDivider('-', 68);
            for (Account a : accounts) {
                System.out.printf("%-12s  %-18s  %-10s  \u20B9%-11.2f  %-8s%n",
                        a.getAccountNumber(),
                        a.getCustomerName(),
                        a.getAccountType(),
                        a.getBalance(),
                        a.getStatus());
            }
            printDivider('-', 68);
            System.out.println();
        } catch (SQLException e) {
            System.out.println(" [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // ----------------------------------------------------- 3. search by account number

    private void searchAccount() {
        System.out.println();
        System.out.print(" Enter account number to search: ");
        String accountNumber = scanner.nextLine().trim();

        try {
            Account account = accountDAO.findByAccountNumber(accountNumber);
            if (account == null) {
                System.out.println(" [!] Account not found: " + accountNumber + "\n");
                return;
            }
            printDivider('-', 42);
            System.out.println(account);
            printDivider('-', 42);
            System.out.println();
        } catch (SQLException e) {
            System.out.println(" [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // ------------------------------------------------------- 4. all transactions

    private void viewAllTransactions() {
        System.out.println();
        try {
            List<Transaction> transactions = transactionDAO.findAll();
            if (transactions.isEmpty()) {
                System.out.println(" No transactions recorded yet.\n");
                return;
            }
            System.out.printf("%-6s  %-10s  %-12s  %-12s  %-19s  %-8s%n",
                    "ID", "ACCOUNT", "TYPE", "AMOUNT", "DATE", "STATUS");
            printDivider('-', 75);
            for (Transaction tx : transactions) {
                System.out.printf("%-6d  %-10d  %-12s  \u20B9%-11.2f  %-19s  %-8s%n",
                        tx.getTransactionId(),
                        tx.getAccountId(),
                        tx.getTransactionType(),
                        tx.getAmount(),
                        tx.getTransactionDate(),
                        tx.getStatus());
            }
            printDivider('-', 75);
            System.out.println();
        } catch (SQLException e) {
            System.out.println(" [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // ----------------------------------------------- 5. activate / deactivate

    private void toggleAccountStatus() {
        System.out.println();
        System.out.print(" Enter account number to toggle status: ");
        String accountNumber = scanner.nextLine().trim();

        try {
            Status newStatus = bankingService.toggleAccountStatus(accountNumber);
            System.out.printf("%n [✓] Account %s is now %s.%n%n",
                    accountNumber, newStatus);
        } catch (IllegalArgumentException e) {
            System.out.println("\n [!] " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.out.println("\n [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // ----------------------------------------------------------------------- helpers

    private void printDivider(char ch, int width) {
        System.out.println(String.valueOf(ch).repeat(width));
    }
}
