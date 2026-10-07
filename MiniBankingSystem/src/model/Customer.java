package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class representing a bank customer.
 * Maps to the CUSTOMERS table in the database.
 */
public class Customer {

    private int         customerId;
    private String      name;
    private String      email;
    private String      phone;
    private String      address;
    private LocalDate   dateOfBirth;
    private String      password;          // stored as hash in production; plain for demo
    private LocalDateTime createdAt;

    // ------------------------------------------------------------------ constructors

    public Customer() {}

    public Customer(String name, String email, String phone,
                    String address, LocalDate dateOfBirth, String password) {
        this.name        = name;
        this.email       = email;
        this.phone       = phone;
        this.address     = address;
        this.dateOfBirth = dateOfBirth;
        this.password    = password;
    }

    // ---------------------------------------------------------------------- getters

    public int          getCustomerId()  { return customerId; }
    public String       getName()        { return name; }
    public String       getEmail()       { return email; }
    public String       getPhone()       { return phone; }
    public String       getAddress()     { return address; }
    public LocalDate    getDateOfBirth() { return dateOfBirth; }
    public String       getPassword()    { return password; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // ---------------------------------------------------------------------- setters

    public void setCustomerId(int customerId)       { this.customerId  = customerId; }
    public void setName(String name)                { this.name        = name; }
    public void setEmail(String email)              { this.email       = email; }
    public void setPhone(String phone)              { this.phone       = phone; }
    public void setAddress(String address)          { this.address     = address; }
    public void setDateOfBirth(LocalDate dob)       { this.dateOfBirth = dob; }
    public void setPassword(String password)        { this.password    = password; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ----------------------------------------------------------------------- display

    @Override
    public String toString() {
        return String.format(
            "Customer ID : %d%n" +
            "Name        : %s%n" +
            "Email       : %s%n" +
            "Phone       : %s%n" +
            "Address     : %s%n" +
            "Date of Birth: %s",
            customerId, name, email, phone, address, dateOfBirth
        );
    }
}
