package com.example.psychapp.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {
    private Integer userId, researcherId;
    private String displayName;

    public LoggedInUser(Integer userId, String displayName, Integer researcherId) {
        this.userId = userId;
        this.displayName = displayName;
        this.researcherId = researcherId;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getResearcherId(){
        return researcherId;
    }

    public String getDisplayName() {
        return displayName;
    }
}