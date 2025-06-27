package com.example.ehrviewer.service;

import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.exception.AuditLoggingException;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.FileSystemAuditLogger;
import com.example.ehrviewer.model.User;
import com.example.ehrviewer.model.UserRequest;
import com.example.ehrviewer.model.UserType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer for user management with audit logging in EHR.
 */
@Service
public class UserService {
    private FileSystemAuditLogger auditLogger;
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws AuditLoggingException {
        // Initialize audit logger
        AuditConfiguration config = new AuditConfiguration();
        config.setLogDirectory("./ehr-audit-logs");
        config.setAutoCreateDirectory(true);
        auditLogger = new FileSystemAuditLogger(config);

        // Initialize with some sample users
        initializeSampleUsers();
    }

    @PreDestroy
    public void cleanup() throws AuditLoggingException {
        if (auditLogger != null) {
            auditLogger.close();
        }
    }

    private void initializeSampleUsers() {
        users.put("PATIENT-001", new User("PATIENT-001", "John Doe", "1980-01-01", "123 Main St", 44, UserType.PATIENT, "M", "555-1234", "johndoe", "password1"));
        users.put("DOCTOR-001", new User("DOCTOR-001", "Dr. Alice Smith", "1975-05-10", "456 Clinic Rd", 49, UserType.DOCTOR, "F", "555-5678", "alicesmith", "password2"));
        users.put("NURSE-001", new User("NURSE-001", "Nurse Bob", "1985-09-15", "789 Hospital Ave", 39, UserType.NURSE, "M", "555-9012", "nursebob", "password3"));
        users.put("ADMIN-001", new User("ADMIN-001", "Admin Jane", "1970-12-20", "101 Admin Blvd", 54, UserType.ADMIN, "F", "555-3456", "adminjane", "adminpass"));
    }

    /**
     * Add a new user.
     */
    public User addUser(User user, String requesterId, String reason) throws AuditLoggingException {
        if (users.containsKey(user.getUserId())) {
            auditLogger.logFailure(
                "USER_ADD",
                "ADD",
                "user/" + user.getUserId(),
                "User already exists: " + user.getUserId()
            );
            throw new IllegalArgumentException("User already exists: " + user.getUserId());
        }
        users.put(user.getUserId(), user);
        Map<String, Object> details = new HashMap<>();
        details.put("reason", reason);
        details.put("added_user_type", user.getType());
        AuditEvent auditEvent = AuditEvent.builder()
                .eventType("USER_ADD")
                .userId(requesterId)
                .sessionId(UUID.randomUUID().toString())
                .application("EHRViewer")
                .component("UserService")
                .action("ADD")
                .resource("user/" + user.getUserId())
                .result(AuditResult.SUCCESS)
                .message("Added user: " + user.getName())
                .details(details)
                .correlationId(UUID.randomUUID().toString())
                .build();
        auditLogger.logEvent(auditEvent);
        return user;
    }

    /**
     * Remove a user.
     */
    public User removeUser(String userId, String requesterId, String reason) throws AuditLoggingException {
        User user = users.get(userId);
        if (user == null) {
            auditLogger.logFailure(
                "USER_REMOVE",
                "REMOVE",
                "user/" + userId,
                "User not found: " + userId
            );
            throw new IllegalArgumentException("User not found: " + userId);
        }
        users.remove(userId);
        Map<String, Object> details = new HashMap<>();
        details.put("reason", reason);
        details.put("removed_user_type", user.getType());
        AuditEvent auditEvent = AuditEvent.builder()
                .eventType("USER_REMOVE")
                .userId(requesterId)
                .sessionId(UUID.randomUUID().toString())
                .application("EHRViewer")
                .component("UserService")
                .action("REMOVE")
                .resource("user/" + userId)
                .result(AuditResult.SUCCESS)
                .message("Removed user: " + user.getName())
                .details(details)
                .correlationId(UUID.randomUUID().toString())
                .build();
        auditLogger.logEvent(auditEvent);
        return user;
    }

    /**
     * Get user details.
     */
    public User getUser(String userId, String requesterId) throws AuditLoggingException {
        User user = users.get(userId);
        if (user == null) {
            auditLogger.logFailure(
                "USER_VIEW",
                "VIEW",
                "user/" + userId,
                "User not found: " + userId
            );
            throw new IllegalArgumentException("User not found: " + userId);
        }
        auditLogger.logSuccess(
            "USER_VIEW",
            "VIEW",
            "user/" + userId,
            "User " + requesterId + " viewed user: " + user.getName()
        );
        return user;
    }

    /**
     * Get all users.
     */
    public Map<String, User> getAllUsers(String requesterId) throws AuditLoggingException {
        auditLogger.logSuccess(
            "USER_VIEW_ALL",
            "VIEW_ALL",
            "user",
            "User " + requesterId + " viewed all users"
        );
        return new HashMap<>(users);
    }

    /**
     * Authenticate user by username and password.
     */
    public User authenticate(String username, String password) throws AuditLoggingException {
        for (User user : users.values()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                auditLogger.logSuccess(
                    "USER_LOGIN",
                    "LOGIN",
                    "user/" + user.getUserId(),
                    "User " + username + " logged in successfully"
                );
                return user;
            }
        }
        auditLogger.logFailure(
            "USER_LOGIN",
            "LOGIN",
            "user/unknown",
            "Failed login attempt for username: " + username
        );
        return null;
    }

    /**
     * Log a logout event for a user.
     */
    public void logLogoutEvent(User user) throws AuditLoggingException {
        auditLogger.logSuccess(
            "USER_LOGOUT",
            "LOGOUT",
            "user/" + user.getUserId(),
            "User " + user.getUsername() + " logged out successfully"
        );
    }
} 