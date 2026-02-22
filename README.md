# 🎓 Student Management System

A modern, secure, and feature-rich Student Management System built with **Spring Boot**, **Spring Security**, and a high-performance **Vanilla JS** frontend. This application provides a comprehensive platform for Administrators, Teachers, and Students to manage academic data efficiently.

## 🚀 Key Features

### 🛡️ Secure Administration
- **Environment-Driven Security:** Admin credentials can be configured via environment variables.
- **Auto-Upsert Admin:** System ensures a secure admin account is always present with the password `Viswa@2025` by default.
- **RESTful Role-Based Access:** Strict authentication for all API endpoints using Spring Security.

### 📚 Academic Management
- **Year-Based Enrollment:** Strict logic that ensures students are only eligible for subjects matching their academic year.
- **Dynamic Subject Filtering:** Admin panel automatically filters subjects in real-time based on the student's selected year.
- **Teacher Assignment:** Easy mapping of teachers to specific subjects.

### 📊 Advanced Analytics & Dashboard
- **Grouped Top Performers:** View top students categorized by academic year (1st Year, 2nd Year, etc.).
- **Performance Metrics:** Subject-wise averages, pass percentages, and teacher performance scoring.
- **Visual Progress:** Dynamic progress bars and medal rankings (🥇, 🥈, 🥉) for top achievers.

### 👩‍🎓 User Experience
- **Personalized Access:** Users see their actual names instead of emails in the sidebar and dashboard.
- **Professional Table View:** Student results are presented in a clean, grouped-by-subject table with grade calculations.
- **Responsive Design:** Dark-themed modern UI optimized for all screen sizes.

---

## 🛠️ Technology Stack

- **Backend:** Java 17, Spring Boot 3.x, Spring Data JPA, Spring Security
- **Database:** H2 Database (Default) / MySQL / PostgreSQL (Configurable)
- **Frontend:** Vanilla JavaScript, CSS3 (Glassmorphism), HTML5
- **Build Tool:** Maven

---

## ⚙️ Getting Started

### Prerequisites
- JDK 17 or higher
- Maven installed (or use included `./mvnw`)

### Installation & Run
1. **Clone the repository:**
   ```bash
   git clone https://github.com/Viswa2708/Student_Management_System.git
   cd Student_Management_System
   ```

2. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access the App:**
   Open `http://localhost:8080` in your browser.

### Default Credentials
- **Role:** Admin
- **Username:** `admin`
- **Password:** `Viswa@2025`

---

## ☁️ Deployment
For production deployment, set the following environment variables for security:
- `ADMIN_USERNAME`: Your custom admin username
- `ADMIN_PASSWORD`: Your strong admin password

---

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
