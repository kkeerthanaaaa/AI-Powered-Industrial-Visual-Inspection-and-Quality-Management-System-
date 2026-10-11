# Industrial Visual Inspection and Quality Management System

**Enterprise Quality Assurance & Automated Vision Metrology Platform**  
*Developed for:* **ABC Precision Manufacturing Pvt. Ltd.**

---

## 1. Project Title
**AI-Powered Industrial Visual Inspection and Quality Management System**  
(Pure Java SE & Java EE MVC Architecture)

---

## 2. Project Overview
ABC Precision Manufacturing Pvt. Ltd. is an ISO/IATF 16949-certified supplier manufacturing critical precision components for the automotive and aerospace industries. This enterprise software platform provides a centralized, high-reliability solution to digitize and automate visual inspection workflows, defect classification, root-cause corrective action (CAPA) tracking, and management quality reporting.

The system features dual presentation tiers:
1. **Desktop GUI Client (Java Swing):** A high-performance, dark-themed operations dashboard designed for shop-floor inspection stations and metrology laboratories.
2. **Web Portal (Java Servlets & JSP):** A responsive MVC web application for plant supervisors, quality engineers, and corporate executives across the company intranet.

---

## 3. Problem Statement
Manual optical inspection on high-speed machining and assembly lines suffers from:
- Subjective human inspector fatigue and inconsistent defect classification.
- Delays in logging non-conformances and communicating deviations to machining centers.
- Disconnected paper logs that prevent real-time yield trend analysis.
- Lack of an immutable audit trail linking specific part images to inspector decisions and engineering corrective actions.

---

## 4. Objectives
- Establish an end-to-end digital lifecycle: Product -> Batch -> Image -> AI Simulation -> Inspector Review -> CAPA -> Executive Reporting.
- Guarantee sub-second response times on routine shop-floor CRUD operations and under 3 seconds for comprehensive quality analytics.
- Enforce strict Role-Based Access Control (RBAC) across four operational personas.
- Adhere strictly to **100% Pure Java Architecture** without third-party frameworks (no Spring, Hibernate, Python, Flask, or JavaScript runtime dependencies).

---

## 5. Key Features
- **Product & Lot Management:** Complete cataloging of precision automotive and aerospace parts with material specs, customer requirements, and batch assignments.
- **Machinery & Operator Directory:** Real-time tracking of CNC machining centers, stamping presses, and certified inspection operators across shifts.
- **Image Metrology & File I/O:** Pure Java image processing (`BufferedImage`, `ImageIO`), automatic validation of dimensions, file size constraints, and optical metadata.
- **Simulated Computer Vision AI Inference:** Polymorphic AI prediction simulator providing defect labels, bounding box coordinates, severity, and confidence metrics.
- **Defect Taxonomy & Classification:** Standardized defect classification with 8 industrial defect categories, severity rankings, and standard shop remedies.
- **CAPA Lifecycle Management:** Closed-loop Corrective and Preventive Action workflow (OPEN -> IN_PROGRESS -> COMPLETED -> VERIFIED).
- **Executive Analytics & Custom Charts:** Custom Swing `Graphics2D` charts (Pie, Bar, Line) for defect distribution, batch yield, and monthly quality trends.
- **7 Automated Quality Reports:** Production of formal audit reports in CSV and formatted TXT formats.
- **Security & Audit Logging:** Cryptographic PBKDF2WithHmacSHA256 password hashing, brute-force lockout, and immutable event logging.
- **Fail-Safe Offline Resilience:** In-memory caching fallback ensures the desktop and test suites remain operational during database maintenance.

---

## 6. Architecture & Design Patterns
The application follows a clean, decoupled **Model-View-Controller (MVC)** and **DAO (Data Access Object)** pattern:

```text
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│   Java Swing Desktop UI    │   Java Servlets / JSP Web │
└──────────────┬─────────────────────────┬───────────────┘
               │                         │
               ▼                         ▼
┌────────────────────────────────────────────────────────┐
│                     Service Layer                      │
│   AuthenticationService   │   ProductService           │
│   BatchService            │   InspectionService        │
│   AIPredictionService     │   QualityReportService     │
│   AuditService            │   CorrectiveActionService  │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                   Data Access Layer                    │
│   UserDAO                 │   ProductDAO               │
│   BatchDAO                │   InspectionDAO            │
│   DefectResultDAO         │   QualityReportDAO         │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                    Persistence Tier                    │
│           MySQL 8.x Database / PreparedStatements      │
│           (with In-Memory Thread-Safe Fallback)        │
└────────────────────────────────────────────────────────┘
```

### Applied Design Patterns:
- **Singleton:** `DBConnection`, `ThreadPoolManager`, `AuditService`
- **Factory & Strategy:** `QualityReportService` coordinating specialized report generators (`ReportGenerator` interface)
- **Polymorphism & Abstraction:** `AIInspectionService` interface implemented by `SimulatedAIInspectionService`
- **DAO Pattern:** Clear decoupling between persistence queries and business logic
- **Observer/MVC:** Separation of data models from Swing views and JSP presentation

---

## 7. Technology Stack
- **Language:** Java 17 (Java SE)
- **Desktop UI:** Java Swing (Pure `JFrame`, `JPanel`, `Graphics2D`, Custom `UITheme`)
- **Web UI:** Java Servlets 4.0, JSP 2.3, JSTL 1.2
- **Persistence:** JDBC 4.2 with `PreparedStatement`, MySQL Connector/J 8.3.0
- **Database:** MySQL 8.x (15 fully normalized tables with InnoDB foreign keys)
- **Concurrency:** `java.util.concurrent` (`ThreadPoolManager`, `ExecutorService`, `ConcurrentHashMap`)
- **Cryptography:** Java Cryptography Architecture (`SecretKeyFactory`, `PBKDF2WithHmacSHA256`, `SecureRandom`)
- **Build / Packaging:** Java Compiler (`javac`), `jar`, and Apache Maven (`pom.xml`)

---

## 8. Database Schema
The MySQL database `industrial_inspection` consists of 15 normalized tables:

1. `users` — User credentials, PBKDF2 hash, salt, roles, status, lockout counters
2. `products` — Automotive and aerospace part catalog specifications
3. `machines` — CNC milling machines, hydraulic presses, optical rigs
4. `operators` — Certified inspection staff, shifts, and contact details
5. `production_batches` — Manufacturing lots, allocated machinery, and shift runs
6. `product_images` — Inspection image captures, camera metadata, file paths
7. `defect_types` — Master classification taxonomy of visual industrial defects
8. `ai_predictions` — Simulated computer vision inference results and bounding boxes
9. `inspection_records` — Quality engineer evaluations and final PASS/FAIL audits
10. `defect_results` — Confirmed anomalies, severities, and coordinate positions
11. `corrective_actions` — CAPA non-conformance remediation tickets
12. `quality_reports` — Historical archive of generated compliance reports
13. `audit_log` — Security and transactional event history
14. `login_log` — Access tracking and brute-force detection
15. `system_settings` — Dynamic thresholds, tolerances, and inspection settings

---

## 9. Installation & Prerequisites
1. **Java Development Kit (JDK 17 or higher):** Ensure `java` and `javac` are available in your PATH.
2. **MySQL Server 8.0+:** Running locally on port 3306.
3. **Apache Tomcat 9.0+ / 10.x:** Required only for deploying the Servlet/JSP web application WAR.

---

## 10. MySQL Setup
1. Log in to MySQL via command line or MySQL Workbench:
   ```bash
   mysql -u root -p
   ```
2. Execute the included schema script:
   ```sql
   SOURCE database/industrial_inspection.sql;
   ```
3. Verify that all 15 tables and initial seed records are populated.

---

## 11. Configuration
Open `src/main/resources/db.properties` and verify your credentials:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/industrial_inspection?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=root
```
*(Note: If MySQL is offline, the system automatically runs in offline demo mode using thread-safe in-memory stores).*

---

## 12. Running the Desktop Application (Swing)
You can launch the Swing client immediately using the provided launcher:

### Windows Batch Launcher:
```cmd
run_desktop.bat
```

### Manual Command Line:
```cmd
javac -encoding UTF-8 -cp "lib\javax.servlet-api-4.0.1.jar;lib\mysql-connector-j-8.3.0.jar" -d "target\classes" src\main\java\com\industrialinspection\*.java
java -cp "target\classes;lib\javax.servlet-api-4.0.1.jar;lib\mysql-connector-j-8.3.0.jar" com.industrialinspection.Main
```

The application will display:
1. Industrial **Splash Screen** with asynchronous component initialization.
2. **Login Screen** with credentials verification.
3. Full **Industrial Operations Dashboard** with 18 navigable modules.

---

## 13. Running the Web Application (Servlets / JSP)
1. Build the production `.war` archive:
   ```cmd
   build_war.bat
   ```
2. The script generates:
   ```text
   target/IndustrialInspectionSystem.war
   ```
3. Copy `IndustrialInspectionSystem.war` into your Tomcat `webapps/` folder.
4. Start Apache Tomcat (`bin/startup.bat`).
5. Open your browser and navigate to:
   ```text
   http://localhost:8080/IndustrialInspectionSystem/
   ```

---

## 14. Default Login Credentials

| Username | Password | Full Name | Role | Access Scope |
| :--- | :--- | :--- | :--- | :--- |
| `admin` | `Admin@123` | Rajesh Sharma | `ADMINISTRATOR` | Full Administrative & System Security Access |
| `engineer` | `Pass@123` | Priya Patel | `QUALITY_ENGINEER` | Inspection Station, Defect Classification & CAPA |
| `supervisor` | `Pass@123` | Vikram Singh | `PRODUCTION_SUPERVISOR` | Batch Scheduling, Quality Dashboard & Yield Stats |
| `operator` | `Pass@123` | Kavita Roy | `INSPECTION_OPERATOR` | Optical Capture & Shift Inspection Submissions |

---

## 15. Running the Automated Test Suite
To verify all business services, password algorithms, and inspection workflows:

```cmd
run_tests.bat
```

The automated test suite runs 12 targeted tests:
- `[TEST 01]` Password Hashing & PBKDF2 Verification
- `[TEST 02]` User Authentication & Account Lockout
- `[TEST 03]` Polymorphic Role-Based Access Control (RBAC)
- `[TEST 04]` Product Catalog Registration & Specs
- `[TEST 05]` Production Batch Lifecycle & Tracking
- `[TEST 06]` Machine & Optical Metrology Station Ops
- `[TEST 07]` Operator Directory & Shift Assignment
- `[TEST 08]` Synthetic Image Synthesis & Physical Storage
- `[TEST 09]` Simulated AI Vision Inference & Bounding Boxes
- `[TEST 10]` Quality Inspection Decision Workflow
- `[TEST 11]` Corrective & Preventive Action (CAPA) Lifecycle
- `[TEST 12]` Executive Quality Report Generation (CSV/TXT)

---

## 16. Project Structure

```text
IndustrialInspectionSystem/
├── pom.xml                               # Maven Build Configuration
├── run_desktop.bat                       # Desktop Swing GUI Launcher
├── run_tests.bat                         # Automated Test Suite Launcher
├── build_war.bat                         # Tomcat WAR Packaging Script
├── database/
│   └── industrial_inspection.sql         # 15 Tables Schema + Comprehensive Seed Data
├── lib/
│   ├── javax.servlet-api-4.0.1.jar       # Servlet API
│   └── mysql-connector-j-8.3.0.jar       # Official MySQL JDBC Driver
├── src/
│   ├── main/
│   │   ├── java/com/industrialinspection/
│   │   │   ├── Main.java                 # Desktop Application Entry Point
│   │   │   ├── ai/                       # AI Metrology & Simulation Module
│   │   │   │   ├── AIInspectionException.java
│   │   │   │   ├── AIInspectionService.java
│   │   │   │   ├── AIPrediction.java
│   │   │   │   ├── PredictionResult.java
│   │   │   │   └── SimulatedAIInspectionService.java
│   │   │   ├── dao/                      # JDBC Data Access Object Layer
│   │   │   │   ├── DBConnection.java
│   │   │   │   ├── UserDAO.java / UserDAOImpl.java
│   │   │   │   ├── ProductDAO.java / ProductDAOImpl.java
│   │   │   │   ├── BatchDAO.java / BatchDAOImpl.java
│   │   │   │   ├── MachineDAO.java / MachineDAOImpl.java
│   │   │   │   ├── OperatorDAO.java / OperatorDAOImpl.java
│   │   │   │   ├── InspectionDAO.java / InspectionDAOImpl.java
│   │   │   │   ├── DefectResultDAO.java / DefectResultDAOImpl.java
│   │   │   │   ├── CorrectiveActionDAO.java / CorrectiveActionDAOImpl.java
│   │   │   │   ├── QualityReportDAO.java / QualityReportDAOImpl.java
│   │   │   │   ├── AuditLogDAO.java / AuditLogDAOImpl.java
│   │   │   │   └── SystemSettingsDAO.java / SystemSettingsDAOImpl.java
│   │   │   ├── model/                    # Domain Entity Objects
│   │   │   │   ├── User.java, Administrator.java, QualityEngineer.java, etc.
│   │   │   │   ├── Product.java, ProductionBatch.java, Machine.java, Operator.java
│   │   │   │   ├── InspectionRecord.java, DefectResult.java, CorrectiveAction.java
│   │   │   │   └── enums/                # Type-Safe Enums
│   │   │   ├── service/                  # Business Logic Layer
│   │   │   ├── servlet/                  # Java EE HTTP Controllers
│   │   │   ├── gui/                      # Swing Presentation & 18 UI Screens
│   │   │   │   ├── components/           # Custom Graphics2D Chart Panels
│   │   │   │   ├── UITheme.java          # Industrial Dark Design System
│   │   │   │   ├── MainDashboardFrame.java
│   │   │   │   └── ... (all 18 screen components)
│   │   │   ├── report/                   # 7 Specialized Report Strategy Classes
│   │   │   └── util/                     # Cryptography, Image IO, CSV, Concurrency
│   │   ├── resources/
│   │   │   └── db.properties             # Database Connection & File Paths
│   │   └── webapp/                       # JSP Views & Web Descriptor
│   │       ├── WEB-INF/web.xml
│   │       ├── assets/css/style.css      # Dark Industrial Web Theme
│   │       └── *.jsp                     # Modular Web View Pages
│   └── test/
│       └── java/com/industrialinspection/
│           └── SystemTestSuite.java      # Comprehensive System Verification
└── target/
    └── IndustrialInspectionSystem.war    # Deployable Web Application Archive
```

---

## 17. Future Enhancements
- Hardware edge integration with GigE Vision / GenICam optical inspection cameras.
- Real-time deep learning edge inference adapter replacing `SimulatedAIInspectionService`.
- Barcode and 2D DataMatrix direct laser scanner integration for lot traceability.

---

## 18. Authors & Academic Integrity
**ABC Precision Manufacturing Systems Team**  
Developed exclusively with Core Java SE, JDBC, Swing, and Java EE Servlets/JSP to demonstrate rigorous Object-Oriented Software Engineering and Enterprise System Design.
