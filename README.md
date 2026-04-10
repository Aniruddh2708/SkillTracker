# SkillTracker 🎓
### Digital Skill Portfolio System for Rural Youth

> A Java-based desktop application that replaces paper certificates with verifiable digital portfolios for vocational trainees — built as part of JAVA-IV PBL (T-170).

---

## 📌 Problem Statement

Millions of rural youth in India complete NGO and government vocational training programs but end up with paper certificates that get lost, damaged, or simply can't be verified by employers. A skilled electrician or mechanic has no reliable way to prove their competency — their skills stay invisible.

**SkillTracker** (internal project name *SkillBridge*) solves this by providing a desktop-based, database-backed digital portfolio system that tracks skill development, monitors progress, and generates verifiable credentials.

---

## 🎯 Key Features

- 🔐 **Role-based login** — Separate portals for Trainers and Trainees
- 📋 **Trainee enrollment** — Trainers can register trainees and assign skills/courses
- 📈 **Progress tracking** — Monitor skill completion status in real time
- 🏅 **Digital credentials** — Issue verifiable digital certificates replacing paper
- 📁 **Portfolio export** — Trainees can export their skill profile as a digital record
- 🔒 **Secure data storage** — All records stored in a MySQL database via JDBC

---

## 🏛️ System Architecture
```
       USER ACCESS LAYER (Login / Registration)
                       ↓
       AUTHENTICATION & ROLE ASSIGNMENT SERVICE
                       ↓               ↓
    ┌───────────────────────┐   ┌───────────────────────┐
    │  TRAINEE PORTAL (UI)  │   │ TRAINER PORTAL (UI)   │
    └───────────┬───────────┘   └───────────┬───────────┘          
                └──────────┬────────────────┘
                           ↓
  [Data Management] ↔  CORE LOGIC CONTROLLER  ↔  [Business Logic]
      (MySQL DB)       (Java OOP Impl.)           (Encapsulation & Auth)
                           ↓
              CREDENTIAL GENERATION SERVICE
                    (Export Logic)
                           ↓
          DIGITAL PORTFOLIO & DASHBOARD OUTPUT
                   (Verified Profile)
```

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Core Language | Java |
| GUI Framework | JavaFX |
| Database | MySQL |
| DB Connectivity | JDBC |
| IDE | VS Code |
| DB Management | MySQL Workbench |

---

## 📐 Java Concepts Demonstrated

| Concept | Implementation |
|---|---|
| Abstract Classes & Inheritance | `User` → `Trainer` / `Trainee` hierarchy |
| Encapsulation | Private fields with public getters/setters in Models |
| Collections | `ArrayList<Skill>`, `HashMap` for trainee rosters |
| Exception Handling | Custom `AuthException`, JDBC error handling |
| JDBC | `DBConnection` singleton, `PreparedStatement` |
| JavaFX GUI | Event-driven UI with role-based navigation |
| Multithreading | `Task<T>` for background DB operations |
| File I/O & Serialization | Portfolio text export + object serialization |

---

## 🗂️ Project Structure
```
SkillTracker/
├── src/
│   ├── model/
│   │   ├── User.java              ← Abstract base class
│   │   ├── Trainer.java           ← Extends User
│   │   ├── Trainee.java           ← Extends User, Serializable
│   │   └── Skill.java             ← Skill/course model
│   ├── db/
│   │   ├── DBConnection.java      ← JDBC singleton
│   │   └── UserDAO.java           ← User data access object
│   ├── gui/
│   │   ├── LoginApp.java          ← JavaFX login screen
│   │   ├── TrainerDashboard.java  ← Trainer portal
│   │   └── TraineeDashboard.java  ← Trainee portal
│   ├── service/
│   │   ├── AuthService.java       ← Authentication logic
│   │   ├── PortfolioExporter.java ← File I/O export
│   │   └── AuthException.java     ← Custom exception
│   ├── LoginFlowTestRunner.java   ← CLI test runner
│   └── Main.java                  ← Application entry point
├── db/
│   └── skillbridge.sql            ← Database schema
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- Java JDK 17 or above
- JavaFX SDK 17+
- MySQL 8.0+
- VS Code with Java Extension Pack

### Setup Steps

**1. Clone the repository**
```bash
git clone https://github.com/Aniruddh2708/SkillTracker.git
cd SkillTracker
```

**2. Set up the database**
```bash
mysql -u root -p < db/skillbridge.sql
```

**3. Configure DB credentials**

Open `src/db/DBConnection.java` and update:
```java
private static final String URL = "jdbc:mysql://localhost:3306/skillbridge";
private static final String USER = "your_mysql_username";
private static final String PASSWORD = "your_mysql_password";
```

**4. Add JavaFX to your classpath**

In VS Code, add to `.vscode/settings.json`:
```json
{
  "java.project.referencedLibraries": [
    "lib/**/*.jar",
    "/path/to/javafx-sdk/lib/*.jar"
  ]
}
```

**5. Run the application**
```bash
# Using the bootstrap class
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp "bin;lib/*" Main
```

---

## 👥 Team

| Name | Roll Number | Role |
|---|---|---|
| Aniruddh Negi | 240212122 | Team Lead |
| Krishna Bhardwaj | 240211595 | Member |
| Sneha Thapliyal | 24022692 | Member |
| Parinita Dhyani | 240222099 | Member |

**Team:** ByteForge (T-170) | **Course:** JAVA-IV | **Institution:** GEU

---

## 📅 Milestones

| Milestone | Description | Status |
|---|---|---|
| M1 | Core data models and OOP structure | ✅ Completed |
| M2 | Certification and Progress logic | ✅ Completed |
| M3 | Data Persistence (JDBC + MySQL) | ✅ Completed |
| M4 | Skill visibility portal (JavaFX GUI) | ✅ Progress |

---

## 📦 Project Deliverables

- **Centralized Repository** — All certifications and badges stored in one system
- **Role-Based Access** — Trainees and trainers see only their relevant data
- **Verifiable Digital Portfolios** — Digital record replacing paper certificates
- **Operational Efficiency** — Real-time progress tracking for multiple students

---

## ⚙️ Assumptions

- Target machines run **Windows 7 or above** (supports JRE)
- Host machines have **basic GPU support** for JavaFX rendering

---