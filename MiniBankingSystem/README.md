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

## Deploy to Render

The repository includes a Render Blueprint at `../render.yaml` and a multi-stage Dockerfile. In Render, create a Blueprint from this repository and select `render.yaml`; it builds and runs the Java service with the required persistent disk. For local container testing, run these commands from `MiniBankingSystem`:

```sh
docker build -t genbank .
docker run --rm -p 10000:10000 -v genbank-data:/var/data genbank
```

The service uses Render's `PORT` environment variable and binds to `0.0.0.0`. H2 data is stored under `/var/data`, which the Blueprint mounts as a 1 GB persistent disk. Persistent disks require a paid Render web-service plan and limit the service to one instance. The Blueprint therefore selects the `starter` plan; review Render's current pricing and change the region in `render.yaml` if needed before creating the service.

The `GET /health` endpoint checks database connectivity and returns HTTP 200 when ready. Configure UptimeRobot as an HTTP(S) monitor for `https://<your-render-service>.onrender.com/health`. Monitoring pings can detect downtime, but do not guarantee that a Render instance stays awake; instance sleep behavior depends on the selected Render plan.

The default local `db.properties` configuration remains in place unless `GENBANK_DB_URL`, `GENBANK_DB_USER`, or `GENBANK_DB_PASSWORD` is set. Do not deploy this academic/demo application for real financial use: customer passwords are stored in plain text, and the app lacks production-grade authentication and authorization controls. A production launch requires a security redesign and a managed database.

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
