package com.example.psychapp.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.psychapp.R;
import com.example.psychapp.data.LoginRepository;
import com.example.psychapp.data.Result;
import com.example.psychapp.data.model.LoggedInUser;

public class LoginViewModel extends ViewModel {
    public static LoginViewModel instance;

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;
    private String code;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
        instance = this;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login() {
        // can be launched in a separate asynchronous job
        loginRepository.login(code);
    }

    public void authenticateResult(Result result){
        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.postValue(new LoginResult(data));
        } else {
            loginResult.postValue(new LoginResult(R.string.login_failed));
        }
    }

    public void setCode(String code){
        this.code = code;
    }

    public void loginDataChanged(String code) {
        if (!isCodeValid(code)) {
            loginFormState.setValue(new LoginFormState(LoginFormState.invalid_code));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isCodeValid(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        /*
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
        */
        return true;
    }
}