package com.example.ehrviewer.service;

import com.enterprise.audit.logging.config.AuditConfiguration;
import com.enterprise.audit.logging.model.AuditContext;
import com.enterprise.audit.logging.model.AuditEvent;
import com.enterprise.audit.logging.model.AuditResult;
import com.enterprise.audit.logging.service.StreamableAuditLogger;
import com.example.ehrviewer.model.User;
import com.example.ehrviewer.model.UserRequest;
import com.example.ehrviewer.model.UserType;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer for user management with audit logging in EHR.
 */
@Service
public class UserService {
    private StreamableAuditLogger auditLogger;
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Initialize audit logger with environment-based configuration (like inventory_management)
        AuditConfiguration config = new AuditConfiguration();
        config.setStreamHost(System.getenv().getOrDefault("AUDIT_STREAM_HOST", "localhost"));
        config.setStreamPort(Integer.parseInt(System.getenv().getOrDefault("AUDIT_STREAM_PORT", "5000")));
        config.setStreamProtocol(System.getenv().getOrDefault("AUDIT_STREAM_PROTOCOL", "tcp"));
        auditLogger = new StreamableAuditLogger(config);

        // Initialize with some sample users
        initializeSampleUsers();
    }

    @PreDestroy
    public void cleanup() {
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
    public User addUser(User user, String requesterId, String reason) {
        if (users.containsKey(user.getUserId())) {
            logAuditEvent("USER_ADD", "ADD", "user/" + user.getUserId(), AuditResult.FAILURE, 
                         "User already exists: " + user.getUserId(), requesterId);
            throw new IllegalArgumentException("User already exists: " + user.getUserId());
        }
        users.put(user.getUserId(), user);
        Map<String, Object> details = new HashMap<>();
        details.put("reason", reason);
        details.put("added_user_type", user.getType().toString());
        logAuditEvent("USER_ADD", "ADD", "user/" + user.getUserId(), AuditResult.SUCCESS, 
                     "Added user: " + user.getName(), requesterId, details);
        return user;
    }

    /**
     * Remove a user.
     */
    public User removeUser(String userId, String requesterId, String reason) {
        User user = users.get(userId);
        if (user == null) {
            logAuditEvent("USER_REMOVE", "REMOVE", "user/" + userId, AuditResult.FAILURE, 
                         "User not found: " + userId, requesterId);
            throw new IllegalArgumentException("User not found: " + userId);
        }
        users.remove(userId);
        Map<String, Object> details = new HashMap<>();
        details.put("reason", reason);
        details.put("removed_user_type", user.getType().toString());
        logAuditEvent("USER_REMOVE", "REMOVE", "user/" + userId, AuditResult.SUCCESS, 
                     "Removed user: " + user.getName(), requesterId, details);
        return user;
    }

    /**
     * Get user details.
     */
    public User getUser(String userId, String requesterId) {
        User user = users.get(userId);
        if (user == null) {
            logAuditEvent("USER_VIEW", "VIEW", "user/" + userId, AuditResult.FAILURE, 
                         "User not found: " + userId, requesterId);
            throw new IllegalArgumentException("User not found: " + userId);
        }
        logAuditEvent("USER_VIEW", "VIEW", "user/" + userId, AuditResult.SUCCESS, 
                     "User " + requesterId + " viewed user: " + user.getName(), requesterId);
        return user;
    }

    /**
     * Get all users.
     */
    public Map<String, User> getAllUsers(String requesterId) {
        logAuditEvent("USER_VIEW_ALL", "VIEW_ALL", "user", AuditResult.SUCCESS, 
                     "User " + requesterId + " viewed all users", requesterId);
        return new HashMap<>(users);
    }

    /**
     * Authenticate user by username and password.
     */
    public User authenticate(String username, String password) {
        for (User user : users.values()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                logAuditEvent("USER_LOGIN", "LOGIN", "user/" + user.getUserId(), AuditResult.SUCCESS, 
                             "User " + username + " logged in successfully", user.getUserId());
                return user;
            }
        }
        logAuditEvent("USER_LOGIN", "LOGIN", "user/unknown", AuditResult.FAILURE, 
                     "Failed login attempt for username: " + username, "unknown");
        return null;
    }

    /**
     * Log a logout event for a user.
     */
    public void logLogoutEvent(User user) {
        logAuditEvent("USER_LOGOUT", "LOGOUT", "user/" + user.getUserId(), AuditResult.SUCCESS, 
                     "User " + user.getUsername() + " logged out successfully", user.getUserId());
    }

    /**
     * Helper method to log audit events using the new v2.0.0 API.
     */
    private void logAuditEvent(String eventType, String action, String resource, AuditResult result, 
                              String message, String userId) {
        logAuditEvent(eventType, action, resource, result, message, userId, null);
    }

    private void logAuditEvent(String eventType, String action, String resource, AuditResult result, 
                              String message, String userId, Map<String, Object> details) {
        try {
            AuditContext.setUserId(userId);
            AuditContext.setApplication("EHRViewer");
            AuditContext.setComponent("UserService");
            AuditContext.setCorrelationId(UUID.randomUUID().toString());
            
            AuditEvent auditEvent = AuditContext.fromContext(
                eventType, action, resource, result, message, details
            );
            
            auditLogger.logEventAsync(auditEvent);
        } catch (Exception e) {
            // Log error but don't throw to avoid breaking the main functionality
            System.err.println("Failed to log audit event: " + e.getMessage());
        } finally {
            AuditContext.clear();
        }
    }
} 