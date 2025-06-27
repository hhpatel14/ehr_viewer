# EHR Viewer

A simple Electronic Health Record (EHR) Viewer web application built with Spring Boot 2.7.18, Java 8, Thymeleaf, and an in-memory user store. All user actions are audit-logged using the enterprise-audit-library. The app demonstrates RBAC (Role-Based Access Control) and a basic HTML UI for login, logout, user management, and audit logging.

---

## Features
- **Login/Logout** with session management
- **Role-Based Access Control (RBAC):**
  - **Doctors, Nurses, Admins:** Can view all users
  - **Patients:** Can only view their own record
- **User Management:**
  - View user list (RBAC-restricted)
  - View user details
  - Add new user (staff only)
  - Remove user (staff only)
- **Audit Logging:** All actions (login, logout, add, remove, view) are logged to disk
- **Simple HTML UI** (Thymeleaf)

---

## Getting Started

### Prerequisites
- Java 8 or higher (Java 17+ also works)
- Maven 3.6+

### Build the Application
```sh
mvn clean package
```

### Run the Application
```sh
mvn spring-boot:run
```

The app will start on [http://localhost:8081](http://localhost:8081)

---

## Using the Web UI

### 1. **Login**
- Go to: [http://localhost:8081/ui/login](http://localhost:8081/ui/login)
- Use one of the sample users below:

| Username    | Password   | Type      |
|-------------|------------|-----------|
| johndoe     | password1  | PATIENT   |
| alicesmith  | password2  | DOCTOR    |
| nursebob    | password3  | NURSE     |
| adminjane   | adminpass  | ADMIN     |

### 2. **User List**
- After login, you will see the user list page.
- **Patients** see only their own record.
- **Doctors, nurses, admins** see all users.

### 3. **View User Details**
- Click "View" next to a user to see their details.

### 4. **Add a New User**
- Click "Add New User" (visible to staff roles).
- Fill out the form and submit.

### 5. **Remove a User**
- Click "Remove" next to a user (visible to staff roles).

### 6. **Logout**
- Click the "Logout" button to end your session and return to the login page.

---

## Audit Logging
- All actions are logged to disk in JSON format.
- Log files are stored in `ehr-audit-logs/audit.log` in the project directory.
- Each log entry includes timestamp, event type, action, resource, result, message, and details.

To view logs:
```sh
tail -f ehr-audit-logs/audit.log
```

---

## Project Structure
- `src/main/java/com/example/ehrviewer/model/` — User, UserType models
- `src/main/java/com/example/ehrviewer/service/` — UserService (business logic, audit logging)
- `src/main/java/com/example/ehrviewer/controller/` — REST and UI controllers
- `src/main/resources/templates/` — Thymeleaf HTML templates
- `src/main/resources/application.properties` — App config

---

## Notes
- **In-memory storage:** All users are stored in memory and reset on restart.
- **No registration:** Only staff can add users; patients cannot self-register.
- **No password hashing:** For demo purposes only. Do not use in production.
- **RBAC:** Enforced in the UI controller for user list and details.

---

## License
This project is for demonstration and educational purposes. 