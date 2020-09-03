package com.example.psychapp.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {
    private Integer userId, researcherId;
    private String displayName;
    private Integer study_length, tests_per_day, tests_time_interval;

    public LoggedInUser(Integer userId, String displayName, Integer researcherId, Integer study_length, Integer tests_per_day, Integer tests_time_interval) {
        this.userId = userId;
        this.displayName = displayName;
        this.researcherId = researcherId;
        this.study_length = study_length;
        this.tests_per_day = tests_per_day;
        this.tests_time_interval = tests_time_interval;
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

    public Integer getStudyLength(){ return study_length; }

    public Integer getTestsTimeInterval(){ return tests_time_interval; }

    public Integer getTestsPerDay(){ return tests_per_day; }
}