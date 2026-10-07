# Mini Banking System — JDBC College Project

A Java console application demonstrating core **JDBC** concepts through a simplified banking system.

---

## Technology Stack

| Component   | Technology                          |
|-------------|-------------------------------------|
| Language    | Java 21 / 17                        |
| Database    | H2 Database Engine (Embedded file)  |
| Web UI      | HTML5, CSS3, Vanilla JS, Lucide icons (Sepia neoclassical hall; blended sandstone panels; high-contrast tactile buttons) |
| Server      | Embedded `HttpServer` (REST API)    |
| Build & Run | `run.bat` / Maven                   |

---

## Quick Start (Web Application)

1. Double click or run **`run.bat`** from the project root.
2. Open your browser and navigate to: **`http://localhost:8080`**
3. Log in with sample credentials:
   - **Alice**: `alice@example.com` / `password123`
   - **Bob**: `bob@example.com` / `password123`

> For detailed documentation on database architecture, access options, and JDBC code flow, see **[DATABASE_AND_JDBC_GUIDE.md](DATABASE_AND_JDBC_GUIDE.md)**.

---

## JDBC Demonstrations

| JDBC Concept              | Location                                         |
|---------------------------|--------------------------------------------------|
| Type 4 Driver loading     | `DBConnection.java` — `Class.forName(...)`       |
| `DriverManager`           | `DBConnection.getConnection()`                   |
| `PreparedStatement`       | All DAO methods with user input                  |
| `Statement`               | `findAll()` methods (no user parameters)         |
| `ResultSet`               | All `findBy*()` methods                          |
| `SQLException`            | Caught in every DAO and service method           |
| `setAutoCommit(false)`    | `BankingService` — deposit, withdraw, transfer   |
| `commit()`                | `BankingService` — on success                    |
| `rollback()`              | `BankingService` — on any failure                |
| try-with-resources        | Every method that opens a `Connection`           |

---

## Admin Login

```
Email   : admin@bank.com
Password: admin123
```

---

## Key JDBC Transaction Flow (Transfer)

```
connection.setAutoCommit(false)
    │
    ├─ UPDATE accounts (deduct from sender)
    ├─ UPDATE accounts (add to receiver)
    ├─ INSERT transactions (sender record)
    ├─ INSERT transactions (receiver record)
    │
    ├─ All OK? → connection.commit()
    └─ Any error? → connection.rollback()
```
