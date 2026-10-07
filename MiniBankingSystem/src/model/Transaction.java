package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class representing a financial transaction record.
 * Maps to the TRANSACTIONS table in the database.
 */
public class Transaction {

    // Transaction types as defined in the PRD
    public enum TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER }

    // Final status of the transaction
    public enum TransactionStatus { SUCCESS, FAILED }

    private int               transactionId;
    private int               accountId;
    private TransactionType   transactionType;
    private BigDecimal        amount;
    private Integer           referenceId;        // nullable — used for transfers
    private String            description;
    private TransactionStatus status;
    private LocalDateTime     transactionDate;

    // ------------------------------------------------------------------ constructors

    public Transaction() {}

    public Transaction(int accountId, TransactionType transactionType,
                       BigDecimal amount, String description, TransactionStatus status) {
        this.accountId       = accountId;
        this.transactionType = transactionType;
        this.amount          = amount;
        this.description     = description;
        this.status          = status;
    }

    // ---------------------------------------------------------------------- getters

    public int               getTransactionId()   { return transactionId; }
    public int               getAccountId()        { return accountId; }
    public TransactionType   getTransactionType()  { return transactionType; }
    public BigDecimal        getAmount()           { return amount; }
    public Integer           getReferenceId()      { return referenceId; }
    public String            getDescription()      { return description; }
    public TransactionStatus getStatus()           { return status; }
    public LocalDateTime     getTransactionDate()  { return transactionDate; }

    // ---------------------------------------------------------------------- setters

    public void setTransactionId(int id)                 { this.transactionId   = id; }
    public void setAccountId(int accountId)              { this.accountId       = accountId; }
    public void setTransactionType(TransactionType type) { this.transactionType = type; }
    public void setAmount(BigDecimal amount)             { this.amount          = amount; }
    public void setReferenceId(Integer referenceId)      { this.referenceId     = referenceId; }
    public void setDescription(String description)       { this.description     = description; }
    public void setStatus(TransactionStatus status)      { this.status          = status; }
    public void setTransactionDate(LocalDateTime date)   { this.transactionDate = date; }
}
