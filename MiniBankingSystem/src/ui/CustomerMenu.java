package ui;

import dao.AccountDAO;
import dao.CustomerDAO;
import dao.TransactionDAO;
import model.Account;
import model.Customer;
import model.Transaction;
import service.BankingService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * CustomerMenu — console UI for the customer dashboard.
 *
 * All business logic and JDBC work is delegated to BankingService.
 * This class only handles user input/output.
 */
public class CustomerMenu {

    private final BankingService bankingService = new BankingService();
    private final AccountDAO     accountDAO     = new AccountDAO();
    private final Scanner        scanner;

    public CustomerMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------------------------------------------------------ main menu

    /**
     * Displays the customer dashboard and handles menu choices until the
     * customer selects Logout.
     *
     * @param account the currently logged-in account
     */
    public void show(Account account) {

        boolean running = true;

        while (running) {
            printDivider('=', 42);
            System.out.println("         MINI BANKING SYSTEM");
            printDivider('=', 42);
            System.out.println(" Welcome, " + account.getCustomerName());
            System.out.println();
            System.out.println(" 1. View Account Details");
            System.out.println(" 2. Check Balance");
            System.out.println(" 3. Deposit Money");
            System.out.println(" 4. Withdraw Money");
            System.out.println(" 5. Transfer Money");
            System.out.println(" 6. Transaction History");
            System.out.println(" 7. Logout");
            printDivider('-', 42);
            System.out.print(" Enter Choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAccountDetails(account);
                case "2" -> checkBalance(account);
                case "3" -> depositMoney(account);
                case "4" -> withdrawMoney(account);
                case "5" -> transferMoney(account);
                case "6" -> viewTransactionHistory(account);
                case "7" -> {
                    System.out.println("\n Logged out successfully. Goodbye, "
                            + account.getCustomerName() + "!\n");
                    running = false;
                }
                default  -> System.out.println("\n [!] Invalid choice. Please enter 1–7.\n");
            }
        }
    }

    // ---------------------------------------------------------- 1. account details

    private void viewAccountDetails(Account account) {
        System.out.println();
        printDivider('-', 42);
        System.out.println("       ACCOUNT DETAILS");
        printDivider('-', 42);
        System.out.println(account);
        printDivider('-', 42);
        System.out.println();
    }

    // ------------------------------------------------------------- 2. balance

    private void checkBalance(Account account) {
        System.out.println();
        printDivider('-', 42);
        System.out.printf(" Account Number : %s%n", account.getAccountNumber());
        System.out.printf(" Account Holder : %s%n", account.getCustomerName());
        System.out.printf(" Current Balance: \u20B9%,.2f%n", account.getBalance());
        printDivider('-', 42);
        System.out.println();
    }

    // ------------------------------------------------------------- 3. deposit

    private void depositMoney(Account account) {
        System.out.println();
        System.out.print(" Enter deposit amount: \u20B9");
        String input = scanner.nextLine().trim();

        try {
            BigDecimal amount = new BigDecimal(input);
            bankingService.deposit(account, amount);

            System.out.printf("%n [✓] Deposit successful!%n");
            System.out.printf("     New Balance: \u20B9%,.2f%n%n", account.getBalance());

        } catch (NumberFormatException e) {
            System.out.println("\n [!] Invalid amount. Please enter a numeric value.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("\n [!] " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.out.println("\n [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // ------------------------------------------------------------ 4. withdrawal

    private void withdrawMoney(Account account) {
        System.out.println();
        System.out.printf(" Current Balance: \u20B9%,.2f%n", account.getBalance());
        System.out.print(" Enter withdrawal amount: \u20B9");
        String input = scanner.nextLine().trim();

        try {
            BigDecimal amount = new BigDecimal(input);
            bankingService.withdraw(account, amount);

            System.out.printf("%n [✓] Withdrawal successful!%n");
            System.out.printf("     New Balance: \u20B9%,.2f%n%n", account.getBalance());

        } catch (NumberFormatException e) {
            System.out.println("\n [!] Invalid amount. Please enter a numeric value.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("\n [!] " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.out.println("\n [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // ------------------------------------------------------------- 5. transfer

    private void transferMoney(Account account) {
        System.out.println();
        System.out.print(" Enter receiver account number: ");
        String receiverNumber = scanner.nextLine().trim();

        System.out.printf(" Current Balance: \u20B9%,.2f%n", account.getBalance());
        System.out.print(" Enter transfer amount: \u20B9");
        String input = scanner.nextLine().trim();

        try {
            BigDecimal amount = new BigDecimal(input);

            // Confirm before executing
            System.out.printf("%n Transfer \u20B9%,.2f → Account %s?%n", amount, receiverNumber);
            System.out.print(" Confirm (yes/no): ");
            String confirm = scanner.nextLine().trim();

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println(" Transfer cancelled.\n");
                return;
            }

            bankingService.transfer(account, receiverNumber, amount);

            System.out.printf("%n [✓] Transfer of \u20B9%,.2f successful!%n", amount);
            System.out.printf("     Remaining Balance: \u20B9%,.2f%n%n",
                    account.getBalance());

        } catch (NumberFormatException e) {
            System.out.println("\n [!] Invalid amount. Please enter a numeric value.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("\n [!] " + e.getMessage() + "\n");
        } catch (SQLException e) {
            System.out.println("\n [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // ---------------------------------------------------- 6. transaction history

    private void viewTransactionHistory(Account account) {
        System.out.println();
        try {
            List<Transaction> history =
                    bankingService.getTransactionHistory(account.getAccountId());

            if (history.isEmpty()) {
                System.out.println(" No transactions found.\n");
                return;
            }

            // Table header
            System.out.printf("%-6s  %-12s  %-12s  %-19s  %-8s%n",
                    "ID", "TYPE", "AMOUNT", "DATE", "STATUS");
            printDivider('-', 65);

            // Table rows
            for (Transaction tx : history) {
                System.out.printf("%-6d  %-12s  \u20B9%-11.2f  %-19s  %-8s%n",
                        tx.getTransactionId(),
                        tx.getTransactionType(),
                        tx.getAmount(),
                        tx.getTransactionDate(),
                        tx.getStatus());
            }
            printDivider('-', 65);
            System.out.println();

        } catch (SQLException e) {
            System.out.println(" [!] Database error: " + e.getMessage() + "\n");
        }
    }

    // ----------------------------------------------------------------------- helpers

    private void printDivider(char ch, int width) {
        System.out.println(String.valueOf(ch).repeat(width));
    }
}
