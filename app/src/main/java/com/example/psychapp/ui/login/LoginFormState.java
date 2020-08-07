package com.example.psychapp.ui.login;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class LoginFormState {
    public static final Integer invalid_code = 1;

    @Nullable
    private Integer codeError;
    private boolean isDataValid;

    LoginFormState(@Nullable Integer codeError) {
        this.codeError = codeError;
        this.isDataValid = false;
    }

    LoginFormState(boolean isDataValid) {
        this.codeError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getCodeError() {
        return codeError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}