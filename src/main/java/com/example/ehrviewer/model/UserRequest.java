package com.example.ehrviewer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model for user operations in EHR.
 */
public class UserRequest {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("requester_id")
    private String requesterId; // who is making the request

    @JsonProperty("action")
    private String action; // add, remove, update

    @JsonProperty("reason")
    private String reason;

    // Default constructor for JSON deserialization
    public UserRequest() {}

    public UserRequest(String userId, String requesterId, String action, String reason) {
        this.userId = userId;
        this.requesterId = requesterId;
        this.action = action;
        this.reason = reason;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @Override
    public String toString() {
        return "UserRequest{" +
                "userId='" + userId + '\'' +
                ", requesterId='" + requesterId + '\'' +
                ", action='" + action + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
} 