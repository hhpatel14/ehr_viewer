package com.example.ehrviewer.controller;

import com.example.ehrviewer.model.User;
import com.example.ehrviewer.model.UserRequest;
import com.example.ehrviewer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for user management operations in EHR.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Get all users.
     */
    @GetMapping
    public ResponseEntity<Map<String, User>> getAllUsers(@RequestParam(defaultValue = "system") String requesterId) {
        try {
            Map<String, User> users = userService.getAllUsers(requesterId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get a specific user.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId, @RequestParam(defaultValue = "system") String requesterId) {
        try {
            User user = userService.getUser(userId, requesterId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add a new user.
     */
    @PostMapping("/add")
    public ResponseEntity<User> addUser(@RequestBody UserRequest request, @RequestBody User user) {
        try {
            User addedUser = userService.addUser(user, request.getRequesterId(), request.getReason());
            return ResponseEntity.ok(addedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Remove a user.
     */
    @PostMapping("/remove")
    public ResponseEntity<User> removeUser(@RequestBody UserRequest request) {
        try {
            User removedUser = userService.removeUser(request.getUserId(), request.getRequesterId(), request.getReason());
            return ResponseEntity.ok(removedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Login endpoint.
     */
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestParam String username, @RequestParam String password) {
        try {
            User user = userService.authenticate(username, password);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(401).build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Logout endpoint.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String username) {
        try {
            User user = null;
            for (User u : userService.getAllUsers("system").values()) {
                if (u.getUsername().equals(username)) {
                    user = u;
                    break;
                }
            }
            if (user != null) {
                // Log the logout event using the service method
                userService.logLogoutEvent(user);
                return ResponseEntity.ok("Logout successful");
            } else {
                return ResponseEntity.status(404).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Logout failed");
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("EHR User Management Service is running!");
    }
} 