package com.example.ehrviewer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Model representing a user in the EHR system (patient, doctor, nurse, etc.).
 */
public class User {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("dob")
    private String dob; // ISO date string (YYYY-MM-DD)

    @JsonProperty("address")
    private String address;

    @JsonProperty("age")
    private int age;

    @JsonProperty("type")
    private UserType type; // patient, doctor, nurse, etc.

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("contact_info")
    private String contactInfo;

    @JsonProperty("username")
    private String username;

    @JsonIgnore // Do not expose password in JSON responses
    private String password;

    // Default constructor for JSON deserialization
    public User() {}

    public User(String userId, String name, String dob, String address, int age, UserType type, String gender, String contactInfo, String username, String password) {
        this.userId = userId;
        this.name = name;
        this.dob = dob;
        this.address = address;
        this.age = age;
        this.type = type;
        this.gender = gender;
        this.contactInfo = contactInfo;
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", dob='" + dob + '\'' +
                ", address='" + address + '\'' +
                ", age=" + age +
                ", type='" + type + '\'' +
                ", gender='" + gender + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
} 