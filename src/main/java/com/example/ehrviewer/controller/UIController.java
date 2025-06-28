package com.example.ehrviewer.controller;

import com.example.ehrviewer.model.User;
import com.example.ehrviewer.model.UserType;
import com.example.ehrviewer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/ui")
public class UIController {
    @Autowired
    private UserService userService;

    // Show login page
    @GetMapping({"/login", "/"})
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "login";
    }

    // Handle login form
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        try {
            User user = userService.authenticate(username, password);
            if (user != null) {
                session.setAttribute("user", user);
                return "redirect:/ui/users";
            } else {
                model.addAttribute("error", "Invalid username or password");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Login failed");
            return "login";
        }
    }

    // Logout
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            try { userService.logLogoutEvent(user); } catch (Exception ignored) {}
        }
        session.invalidate();
        return "redirect:/ui/login";
    }

    // Show user list
    @GetMapping("/users")
    public String userList(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/ui/login";
        try {
            if (currentUser.getType() == UserType.PATIENT) {
                model.addAttribute("users", java.util.Collections.singletonList(currentUser));
            } else {
                Map<String, User> users = userService.getAllUsers(currentUser.getUserId());
                model.addAttribute("users", users.values());
            }
            return "user_list";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load users");
            return "user_list";
        }
    }

    // Show add user form
    @GetMapping("/add-user")
    public String addUserForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/ui/login";
        model.addAttribute("userTypes", UserType.values());
        return "add_user";
    }

    // Handle add user form
    @PostMapping("/add-user")
    public String addUser(@RequestParam String name,
                         @RequestParam String dob,
                         @RequestParam String address,
                         @RequestParam int age,
                         @RequestParam String type,
                         @RequestParam String gender,
                         @RequestParam String contactInfo,
                         @RequestParam String username,
                         @RequestParam String password,
                         HttpSession session,
                         Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/ui/login";
        try {
            String userId = UUID.randomUUID().toString();
            User newUser = new User(userId, name, dob, address, age, UserType.valueOf(type), gender, contactInfo, username, password);
            userService.addUser(newUser, currentUser.getUserId(), "Added via UI");
            return "redirect:/ui/users";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to add user: " + e.getMessage());
            model.addAttribute("userTypes", UserType.values());
            return "add_user";
        }
    }

    // Show user detail
    @GetMapping("/user/{userId}")
    public String userDetail(@PathVariable String userId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/ui/login";
        try {
            User user = userService.getUser(userId, currentUser.getUserId());
            model.addAttribute("user", user);
            return "user_detail";
        } catch (Exception e) {
            model.addAttribute("error", "User not found");
            return "redirect:/ui/users";
        }
    }

    // Remove user
    @PostMapping("/remove-user/{userId}")
    public String removeUser(@PathVariable String userId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/ui/login";
        try {
            userService.removeUser(userId, currentUser.getUserId(), "Removed via UI");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to remove user: " + e.getMessage());
        }
        return "redirect:/ui/users";
    }
} 