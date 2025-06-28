# EHR Viewer

A modern Electronic Health Record (EHR) Viewer web application built with Spring Boot 3.2.0, Java 21, Thymeleaf, and an in-memory user store. All user actions are audit-logged using the enterprise-audit-library v2.0.0 with streaming capabilities. The app demonstrates RBAC (Role-Based Access Control) and a basic HTML UI for login, logout, user management, and audit logging.

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
- **Modern Audit Logging:** All actions are logged using the enterprise-audit-library v2.0.0 with streaming capabilities
- **Simple HTML UI** (Thymeleaf)
- **Spring Boot 3.2.0** with Java 21 support

---

## Getting Started

### Prerequisites
- **Java 21** (required for Spring Boot 3.2.0)
- Maven 3.6+

### 1. Start Logstash (Required)

The EHR viewer requires Logstash to be running for audit logging. Start Logstash first:

```bash
# Using Podman (recommended)
podman run -d --name logstash-ehr -p 5000:5000 \
  -v $(pwd)/logstash.conf:/usr/share/logstash/pipeline/logstash.conf \
  -v $(pwd)/logstash.yml:/usr/share/logstash/config/logstash.yml \
  docker.elastic.co/logstash/logstash:8.11.0

# Using Docker (alternative)
docker run -d --name logstash-ehr -p 5000:5000 \
  -v $(pwd)/logstash.conf:/usr/share/logstash/pipeline/logstash.conf \
  -v $(pwd)/logstash.yml:/usr/share/logstash/config/logstash.yml \
  docker.elastic.co/logstash/logstash:8.11.0
```

**Note:** The application will fail to start if Logstash is not available. This is intentional for production environments where audit logging is critical.

### 2. Build the Application
```sh
mvn clean package
```

### 3. Run the Application
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

## Audit Logging (v2.0.0)

The application uses the enterprise-audit-library v2.0.0 with streaming capabilities:

- **Streaming Architecture:** Audit events are streamed over TCP to a configured endpoint (default: localhost:5044)
- **Async Logging:** Non-blocking audit logging using virtual threads (Java 21)
- **Structured JSON:** Line-delimited JSON events for easy ingestion into ELK stack
- **Cloud-Native Ready:** Designed for containers, Kubernetes, and distributed systems

### Configuration
The audit logger is configured to stream events to:
- **Host:** localhost (configurable via `AUDIT_STREAM_HOST` environment variable)
- **Port:** 5000 (configurable via `AUDIT_STREAM_PORT` environment variable)
- **Protocol:** TCP (configurable via `AUDIT_STREAM_PROTOCOL` environment variable)

### Logstash Integration
To receive audit events, configure Logstash with:
```conf
input {
  tcp {
    port => 5000
    codec => json
  }
}
output {
  stdout {
    codec => json
  }
  file {
    path => "/tmp/ehr-audit-events.log"
    codec => json
  }
}
```

---

## Project Structure
- `src/main/java/com/example/ehrviewer/model/` — User, UserType models
- `src/main/java/com/example/ehrviewer/service/` — UserService (business logic, audit logging)
- `src/main/java/com/example/ehrviewer/controller/` — REST and UI controllers
- `src/main/resources/templates/` — Thymeleaf HTML templates
- `src/main/resources/application.properties` — App config

---

## Technology Stack
- **Spring Boot:** 3.2.0
- **Java:** 21
- **Audit Library:** enterprise-audit-library 2.0.0
- **Template Engine:** Thymeleaf
- **Build Tool:** Maven 3.11.0

---

## Notes
- **In-memory storage:** All users are stored in memory and reset on restart.
- **No registration:** Only staff can add users; patients cannot self-register.
- **No password hashing:** For demo purposes only. Do not use in production.
- **RBAC:** Enforced in the UI controller for user list and details.
- **Streaming Audit:** Requires a log aggregation service (like Logstash) to receive audit events.

---

## License
This project is for demonstration and educational purposes. 