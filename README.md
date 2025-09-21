# SQLForge  
*(Single Page SQL Enhancement Platform)*

/*
Copyright © 2025 Devin B. Royal.
All Rights Reserved.
*/

## 🚀 Overview
**SQLForge** is an original, one-of-a-kind single-page Java application designed to **enhance SQL as we know it**.  
It combines a sandboxed in-memory database with a natural-language-to-SQL helper, query advisor, explain-plan analysis, and query history tracking — all inside a self-contained web app.

SQLForge’s mission is to empower developers, DBAs, and learners with **safer, smarter, and more intuitive SQL tools**.

---

## ✨ Key Features
- **Natural Language → SQL**  
  Convert plain English instructions into executable SQL statements (rule-based, no external AI needed).  

- **Safe Sandboxed Execution**  
  Queries run inside an **H2 database** with destructive statements blocked (DROP, DELETE, ALTER, etc.).  

- **Query Advisor**  
  Provides optimization tips such as index recommendations and common pitfalls.  

- **Explain Plans**  
  Run `EXPLAIN` automatically to visualize query execution flow.  

- **History Tracking**  
  Per-user query history with versioning for repeatability.  

- **Single Page UI**  
  Integrated frontend served directly by the backend.  

---

## 🛠️ Tech Stack
- **Backend:** Java 17+, Spring Boot 3, H2 Database  
- **Frontend:** HTML5, Vanilla JS (single-page app)  
- **Build Tool:** Maven  
- **Security:** Query validation, sandbox enforcement, input sanitization  

---

## 📂 Project Structure
DUKE_OS/sqlforge/
├── pom.xml
├── README.md
└── src
└── main
├── java/com/sqlforge/...
└── resources
├── application.properties
└── static/
├── index.html
├── app.js
└── styles.css

---

## ⚙️ Installation & Run

### Requirements
- Java 17+  
- Maven 3.9+  

### Build & Run
```bash
cd DUKE_OS/sqlforge
mvn clean package -DskipTests
java -jar target/sqlforge-1.0.0.jar
Access
Open your browser at:
👉 http://localhost:8080/
🔐 Security Model
Only read-safe SQL statements (SELECT, WITH, EXPLAIN) are allowed.
Destructive SQL (DROP, ALTER, DELETE, UPDATE, etc.) is blocked by regex firewall.
Network sandboxing with H2 embedded mode prevents outside interference.
Error handling and input validation ensure resilience.
🧩 API Endpoints
POST /api/run → Execute a SQL query
POST /api/explain → Explain plan of a query
POST /api/advice → Get optimization tips
POST /api/nl-to-sql → Convert natural language → SQL
GET /api/history?userId=... → Retrieve query history
📖 Example Usage
Natural language:
list employees in engineering
Generated SQL:
SELECT * FROM employees WHERE dept = 'Engineering' LIMIT 100;
🔮 Future Enhancements
Visual query execution plan graphs
Deeper SQL grammar parsing (expand NL-to-SQL)
Persistent query history with user authentication
Export/import datasets in CSV/JSON
WebAssembly-based SQL sandboxing for portability
🧑‍💻 Author
Devin B. Royal
Chief Technology Officer (CTO), Original Systems Engineer
💡 Visionary in secure, production-grade Java systems
📌 This project is part of DUKEªٱ OS R&D initiatives.

