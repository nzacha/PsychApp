package com.example.psychapp.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private Integer id, researcherId;
    private String displayName;
    //... other data fields that may be accessible to the UI

    public LoggedInUserView(Integer id, String displayName, Integer researcherrId) {
        this.id = id;
        this.displayName = displayName;
        this.researcherId = researcherrId;
    }

    String getDisplayName() {
        return displayName;
    }
}