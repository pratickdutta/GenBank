# 🏦 GenBank — Neoclassical Banking Portal & JDBC Management System

A full-stack Java banking application featuring an embedded H2 database, a REST API backend, and a neoclassical web interface. Its sculpted tactile controls keep their geometry and press/hover behavior, with a refreshed high-contrast palette.

**Live demo:** [genbank-88l7.onrender.com](https://genbank-88l7.onrender.com)

---

## 🚀 Key Features
- **Zero-Configuration Embedded Database**: Built with H2 (MySQL Mode); no standalone MySQL installation required. Auto-provisions relational tables on startup.
- **Pure JDBC Core**: Implements Type-4 driver loading, parameterized queries (`PreparedStatement`), `ResultSet` mapping, and ACID transactions (`setAutoCommit(false)`, `commit()`, `rollback()`).
- **Neoclassical Web Interface**: A sepia-lit vaulted European hall with ceiling ribs, columns, and a receding stone floor; translucent sandstone panels, brass accents, Cormorant Garamond headings, and Lucide line icons.
- **Tactile Controls**: Existing 3D button geometry and press/hover states remain, with dark forest, teal-green, oxblood, and stone fills for clearer contrast.
- **Financial Operations**: User authentication, account creation, real-time balance tracking, deposits, withdrawals, and inter-account fund transfers.

---

## 🛠️ Technology Stack
| Component | Technology |
| :--- | :--- |
| **Language** | Java 21 / 17 |
| **Database** | H2 Embedded Database Engine (MySQL Mode) |
| **Server** | Java Embedded `HttpServer` (REST API) |
| **Frontend** | HTML5, CSS3 (Neoclassical visual theme), Vanilla JavaScript, Lucide icons |
| **Build & Run** | `run.bat` / Maven |

---

## ⚡ Quick Start
1. Clone the repository:
   ```bash
   git clone https://github.com/pratickdutta/GenBank.git
   cd GenBank/MiniBankingSystem
   ```
2. Launch the application:
   - On Windows: Double-click or run `run.bat`
   - With Maven: `mvn clean compile exec:java -Dexec.mainClass="WebServer"`
3. Open your browser:
   - Navigate to **`http://localhost:8080`**
4. Test Accounts:
   - **Alice**: `alice@example.com` / `password123` (₹50,000)
   - **Bob**: `bob@example.com` / `password123` (₹25,000)

---

## 📚 In-Depth Guides
- [Database & JDBC Integration Guide](MiniBankingSystem/DATABASE_AND_JDBC_GUIDE.md)
- [Presentation Briefing](MiniBankingSystem/PRESENTATION_BRIEFING.md)
- [Tactile Button Integration Walkthrough](MiniBankingSystem/tactile_button_integration.md)
