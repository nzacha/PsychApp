package com.example.psychapp.data.model;

import android.util.Log;

import java.io.Serializable;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser implements Serializable {
    private int userId, projectId;
    private String displayName, code;
    private int study_length, tests_per_day, tests_time_interval, progress, maxProgress;
    private boolean allow_individual_times, allow_user_termination, automatic_termination;

    public LoggedInUser(int userId, String displayName, int projectId, int study_length, int tests_per_day, int tests_time_interval, boolean allow_individual_times, boolean allow_user_termination, boolean automatic_termination, int progress, String code) {
        this.userId = userId;
        this.displayName = displayName;
        this.projectId = projectId;
        this.study_length = study_length;
        this.tests_per_day = tests_per_day;
        this.tests_time_interval = tests_time_interval;
        this.allow_individual_times = allow_individual_times;
        this.allow_user_termination = allow_user_termination;
        this.automatic_termination = automatic_termination;
        this.progress = progress;
        this.maxProgress = study_length * tests_per_day;
        this.code = code;
    }

    public String getCode(){
        return code;
    }

    public int getUserId() {
        return userId;
    }

    public int getProjectId(){
        return projectId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStudyLength(){ return study_length; }

    public int getTestsTimeInterval(){ return tests_time_interval; }

    public int getTestsPerDay(){ return tests_per_day; }

    public boolean getAllowIndividualTimes(){ return allow_individual_times; }

    public boolean getAllowUserTermination(){ return allow_user_termination; }

    public boolean getAutomaticTermination(){ return automatic_termination; }

    public int getProgress(){ return progress; }

    public int getMaxProgress(){ return maxProgress; }

    public void progress(){
        progress += 1;
    }

    public String toString(){
        return String.format("code: %s, researcher_id: %s, progress: %s", code, projectId, progress);
    }

    public boolean isActive(){
        return progress < maxProgress;
    }
}