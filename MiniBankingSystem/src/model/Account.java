package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class representing a bank account.
 * Maps to the ACCOUNTS table in the database.
 */
public class Account {

    // Account types
    public enum AccountType { SAVINGS, CURRENT }

    // Account status
    public enum Status { ACTIVE, INACTIVE }

    private int           accountId;
    private String        accountNumber;   // 10-digit generated number
    private int           customerId;
    private AccountType   accountType;
    private BigDecimal    balance;
    private Status        status;
    private LocalDateTime createdAt;

    // optional: customer name joined from CUSTOMERS (not stored in ACCOUNTS)
    private String        customerName;

    // ------------------------------------------------------------------ constructors

    public Account() {}

    public Account(String accountNumber, int customerId,
                   AccountType accountType, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.customerId    = customerId;
        this.accountType   = accountType;
        this.balance       = balance;
        this.status        = Status.ACTIVE;
    }

    // ---------------------------------------------------------------------- getters

    public int          getAccountId()     { return accountId; }
    public String       getAccountNumber() { return accountNumber; }
    public int          getCustomerId()    { return customerId; }
    public AccountType  getAccountType()   { return accountType; }
    public BigDecimal   getBalance()       { return balance; }
    public Status       getStatus()        { return status; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public String       getCustomerName()  { return customerName; }

    // ---------------------------------------------------------------------- setters

    public void setAccountId(int accountId)           { this.accountId     = accountId; }
    public void setAccountNumber(String accountNumber){ this.accountNumber  = accountNumber; }
    public void setCustomerId(int customerId)          { this.customerId    = customerId; }
    public void setAccountType(AccountType type)       { this.accountType   = type; }
    public void setBalance(BigDecimal balance)         { this.balance       = balance; }
    public void setStatus(Status status)               { this.status        = status; }
    public void setCreatedAt(LocalDateTime createdAt)  { this.createdAt     = createdAt; }
    public void setCustomerName(String customerName)   { this.customerName  = customerName; }

    // ----------------------------------------------------------------------- display

    @Override
    public String toString() {
        return String.format(
            "Account Number : %s%n" +
            "Account Holder : %s%n" +
            "Account Type   : %s%n" +
            "Current Balance: \u20B9%,.2f%n" +
            "Status         : %s",
            accountNumber,
            customerName != null ? customerName : "N/A",
            accountType,
            balance,
            status
        );
    }
}
