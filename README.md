# CAMPUS PLACEMENT MANAGEMENT SYSTEM
### Advanced Java Desktop Application | JavaFX 21/17 | JDBC | MySQL 8 | Multithreading

---

## 1. Project Overview

The **Campus Placement Management System** is a full-featured, enterprise-grade desktop software engineered for colleges and universities to automate and manage the entire campus placement lifecycle.

Built using **Core Java, JavaFX, JDBC, MySQL, and Java Concurrency Utilities**, the application eliminates manual placement record-keeping, streamlines company recruitment drives, automates eligibility screening, logs recruitment status changes, and generates official placement audit reports in **CSV and PDF** formats.

---

## 2. Key Modules & Features

### 🔐 1. Authentication & Role-Based Access Control
- Secure login backed by database authentication.
- Passwords stored as **SHA-256 cryptographic hashes**.
- 3 distinct user roles:
  - **Administrator:** Full system control (Students, Companies, Drives, Applications, Reports).
  - **Placement Officer:** Student evaluations, drive scheduling, application status updates, interview scheduling.
  - **Student:** Profile viewing, drive discovery, automated eligibility checking, 1-click application submission, status tracking, notifications.
- Automatic routing to administrative dashboard or student portal based on authenticated role.

### 📊 2. Dynamic Placement Dashboard
- Real-time KPI statistics loaded directly from MySQL:
  - Total Students, Recruiting Companies, Active Drives, Total Applications.
  - Selected Candidates, Rejected Applications, Overall Placement Rate (%).
  - Average Package (LPA) and Highest Package (LPA).
- **Four Dynamic JavaFX Charts:**
  1. *Department-Wise Student Distribution* (Interactive Pie Chart).
  2. *Application Status Distribution* (Interactive Pie Chart).
  3. *Recruiter Placement Count* (Bar Chart).
  4. *Top Offered Packages Comparison* (Bar Chart).

### 🎓 3. Student Management
- Comprehensive student directory with JavaFX `TableView`.
- Add, Edit, Delete, and Search operations.
- Dynamic search filter (by Name, Roll Number, Skills) and Department filtering.
- Input validation:
  - Unique Roll Number constraint.
  - Unique & valid Email address format.
  - Valid 10-digit Phone number.
  - CGPA bound between 0.00 and 10.00.
  - Non-negative backlogs.

### 🏢 4. Corporate Recruiting Partner Management
- Complete recruiting company directory.
- Industry domain categorization, location tracking, official website links.
- Corporate HR contact person names and email addresses.
- Relational integrity connected to placement drives with foreign key cascades.

### 🚀 5. Placement Drive & Eligibility Engine
- Schedule on-campus and virtual recruitment drives.
- Specify package (in LPA), job description, application deadline, and drive status (`OPEN`, `IN_PROGRESS`, `CLOSED`).
- **Automated Student Eligibility Screening:**
  - Evaluates Student CGPA vs Drive Cutoff.
  - Evaluates Student Department vs Eligible Departments.
  - Evaluates Active Backlogs vs Allowed Threshold.
  - Checks if application deadline is active.
  - Displays instant, explanatory feedback on why a candidate is eligible or ineligible.
- "View Eligible Students" feature allows placement officers to see all qualifying candidates with 1 click.

### 📝 6. Application Workflow & Audit Trail
- Multi-stage recruitment lifecycle:
  `Applied` ➔ `Under Review` ➔ `Shortlisted` ➔ `Interview Scheduled` ➔ `Selected` / `Rejected`.
- Duplicate application prevention (composite unique constraint on `drive_id` + `student_id`).
- Atomic **Database Transactions** (`setAutoCommit(false)`, `commit()`, `rollback()`):
  - Updates application status.
  - Inserts immutable audit history into `application_history`.
  - Automatically sends system notifications to the candidate.
- View entire status audit trail with timestamps, stage notes, and officer names.

### 🔔 7. Internal Notification System
- System notifications for new placement drives, deadline warnings, and application updates.
- Real-time unread notification badge indicator in the sidebar.

### 📄 8. Asynchronous Report Generation (CSV & PDF)
- Complete student lists, company summaries, and selection/rejection rosters.
- Formatted **PDF Document** generation using **Apache PDFBox** with official headers, tables, and timestamps.
- **CSV Spreadsheet** export compatible with Excel and Google Sheets.

---

## 3. Genuine Java Multithreading Architecture

The project implements **3 genuine multithreading features** solving real-world performance problems:

| Feature | Concurrency Technology | Purpose & Implementation |
|---|---|---|
| **1. Background Database Operations** | `ExecutorService` & `javafx.concurrent.Task` | Offloads heavy student queries and dashboard KPI computations to background worker threads. JavaFX UI never freezes. Results delivered safely via `setOnSucceeded()`. |
| **2. Background Report Generation** | `ExecutorService` & `JavaFX Task` | Executes database reads, PDF table layout calculations, and disk I/O in the background. Shows live `ProgressBar` and progress messages while the user continues navigating the UI. |
| **3. Automatic Deadline Monitoring** | `ScheduledExecutorService` | Standalone background daemon checking drive deadlines every 30 seconds. Detects drives approaching deadline within 48 hours, prevents duplicate alerts, auto-closes expired drives, and updates UI on `Platform.runLater()`. |

---

## 4. Technology Stack & Architecture

- **Language:** Java 21 / 17 (Core Java, OOP, Collections, Concurrency)
- **UI Toolkit:** JavaFX (Controls, TableView, Charts, Dialogs, CSS, Properties)
- **Database:** MySQL 8.0+ (InnoDB, Foreign Keys, Checks, Indexes, Transactions)
- **Connectivity:** Direct JDBC (`java.sql.*`, `PreparedStatement`, `DriverManager`)
- **Reporting:** Apache PDFBox 2.0.31 (PDF Rendering)
- **Build Tool:** Apache Maven
- **Architecture:** Clean, direct, non-overengineered architecture without MVC or DAO layers. Business logic and direct JDBC statements are encapsulated inside descriptive management classes.

---

## 5. Project Directory Structure

```
CampusPlacementSystem/
│
├── src/main/java/com/campusplacement/
│   ├── MainApp.java                       # Main JavaFX Application & Scene Router
│   ├── DBConnection.java                  # Centralized JDBC Connection & Schema Seeder
│   │
│   ├── model/                             # Data Model Classes
│   │   ├── Student.java
│   │   ├── Company.java
│   │   ├── PlacementDrive.java
│   │   ├── PlacementApplication.java
│   │   └── Notification.java
│   │
│   ├── management/                        # Application Logic & Direct JDBC Operations
│   │   ├── LoginManagement.java
│   │   ├── StudentManagement.java
│   │   ├── CompanyManagement.java
│   │   ├── DriveManagement.java
│   │   ├── ApplicationManagement.java
│   │   └── ReportManagement.java
│   │
│   ├── screens/                           # JavaFX User Interface Screens
│   │   ├── LoginScreen.java
│   │   ├── DashboardScreen.java
│   │   ├── StudentScreen.java
│   │   ├── CompanyScreen.java
│   │   ├── DriveScreen.java
│   │   ├── ApplicationScreen.java
│   │   ├── ReportScreen.java
│   │   └── StudentDashboardScreen.java
│   │
│   ├── concurrency/                       # Genuine Multithreading Framework
│   │   ├── BackgroundTasks.java           # Thread Pool & JavaFX Task Factory
│   │   └── DeadlineMonitor.java           # ScheduledExecutorService Deadline Watchdog
│   │
│   └── utils/                             # Utilities
│       ├── ValidationUtil.java            # Input validators & SHA-256 Hasher
│       └── AlertUtil.java                 # JavaFX Dialog and Confirmation Helpers
│
├── src/test/java/com/campusplacement/
│   └── SystemIntegrationTest.java         # 9-Module Automated Integration Test Suite
│
├── src/main/resources/
│   ├── style.css                          # Modern Blue & White JavaFX Stylesheet
│   ├── db.properties                      # MySQL host, port, credentials configuration
│   └── database/
│       └── schema.sql                     # Full MySQL 8 DDL Schema & Sample Records
│
├── compile.bat                            # 1-Click Compilation Script
├── run.bat                                # 1-Click Application Launch Script
├── test.bat                               # 1-Click Automated Test Runner
├── pom.xml                                # Maven Project Configuration
├── README.md                              # Complete System Documentation
└── VIVA_NOTES.md                          # Comprehensive Viva Examination Guide
```

---

## 6. Database Setup & Configuration

### Option A: MySQL Server (Recommended for College Lab / Viva)
1. Ensure MySQL Server is running on your machine (via MySQL Service or XAMPP).
2. Open `src/main/resources/db.properties` and verify your MySQL credentials:
   ```properties
   db.host=localhost
   db.port=3306
   db.name=campus_placement
   db.user=root
   db.password=
   db.allowFallback=true
   ```
3. Import `src/main/resources/database/schema.sql` into MySQL Workbench, phpMyAdmin, or the MySQL CLI:
   ```bash
   mysql -u root -p < src/main/resources/database/schema.sql
   ```
   *(Note: If you run the application without manual import, `DBConnection.java` will automatically create the database tables and insert sample records on first run!)*

### Option B: Zero-Config Offline Fallback
If MySQL server is not installed or offline, `DBConnection.java` detects the absence of MySQL and automatically initializes an embedded local database (`campus_placement.db`). This guarantees that your project **will never crash during an evaluation or viva demonstration**!

---

## 7. Demo Login Credentials

The database comes pre-seeded with accounts for all roles:

| Role | Username | Password | Linked Profile / Permissions |
|---|---|---|---|
| **Administrator** | `admin` | `admin123` | Full administrative access to all modules |
| **Placement Officer** | `officer` | `officer123` | Student records, Drives, Status Updates, Reports |
| **Student (Placed)** | `priya` | `priya123` | Priya Patel (IT, CGPA 9.10) - Placed at Google |
| **Student (Active)** | `student` | `student123` | Rahul Sharma (CSE, CGPA 8.75) - Shortlisted |

*(Tip: On the login screen, click the quick demo buttons at the bottom to auto-fill credentials instantly!)*

---

## 8. How to Compile & Run

### Method 1: Using 1-Click Batch Scripts (Fastest on Windows)
- **Compile:** Double-click `compile.bat`
- **Run:** Double-click `run.bat`
- **Run Tests:** Double-click `test.bat`

### Method 2: Using Maven
```bash
# 1. Compile the application
mvn clean compile

# 2. Run automated tests
mvn test

# 3. Launch JavaFX Application
mvn javafx:run
```

### Method 3: Using Command Prompt / PowerShell
```powershell
# Compile
javac -cp "C:\Users\roopa\.m2\repository\org\openjfx\javafx-base\17.0.10\javafx-base-17.0.10-win.jar;C:\Users\roopa\.m2\repository\org\openjfx\javafx-controls\17.0.10\javafx-controls-17.0.10-win.jar;C:\Users\roopa\.m2\repository\org\openjfx\javafx-graphics\17.0.10\javafx-graphics-17.0.10-win.jar;C:\Users\roopa\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar;C:\Users\roopa\.m2\repository\org\apache\pdfbox\pdfbox\2.0.31\pdfbox-2.0.31.jar;C:\Users\roopa\.m2\repository\org\apache\pdfbox\fontbox\2.0.31\fontbox-2.0.31.jar;C:\Users\roopa\.m2\repository\commons-logging\commons-logging\1.2\commons-logging-1.2.jar;C:\Users\roopa\.m2\repository\org\xerial\sqlite-jdbc\3.45.3.0\sqlite-jdbc-3.45.3.0.jar;C:\Users\roopa\.m2\repository\org\slf4j\slf4j-api\2.0.13\slf4j-api-2.0.13.jar;src\main\resources" -d target\classes (Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })

# Run
java -cp "target\classes;C:\Users\roopa\.m2\repository\org\openjfx\javafx-base\17.0.10\javafx-base-17.0.10-win.jar;C:\Users\roopa\.m2\repository\org\openjfx\javafx-controls\17.0.10\javafx-controls-17.0.10-win.jar;C:\Users\roopa\.m2\repository\org\openjfx\javafx-graphics\17.0.10\javafx-graphics-17.0.10-win.jar;C:\Users\roopa\.m2\repository\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar;C:\Users\roopa\.m2\repository\org\apache\pdfbox\pdfbox\2.0.31\pdfbox-2.0.31.jar;C:\Users\roopa\.m2\repository\org\apache\pdfbox\fontbox\2.0.31\fontbox-2.0.31.jar;C:\Users\roopa\.m2\repository\commons-logging\commons-logging\1.2\commons-logging-1.2.jar;C:\Users\roopa\.m2\repository\org\xerial\sqlite-jdbc\3.45.3.0\sqlite-jdbc-3.45.3.0.jar;C:\Users\roopa\.m2\repository\org\slf4j\slf4j-api\2.0.13\slf4j-api-2.0.13.jar" com.campusplacement.MainApp
```

---

## 9. Verification & Automated Testing

The project includes an end-to-end integration test suite (`SystemIntegrationTest.java`) that verifies all 9 core subsystems:
1. **Database Connection & Schema Init**
2. **Password Hashing & Authentication**
3. **Student CRUD & Duplicate Constraints**
4. **Company Management & Search**
5. **Placement Drive & Eligibility Logic**
6. **Application Workflow, Transactions & History**
7. **Multithreading (ExecutorService & Task)**
8. **Multithreading (Scheduled Deadline Monitor)**
9. **CSV & PDF Report Generation**

Run `test.bat` to verify all 9 tests pass.
