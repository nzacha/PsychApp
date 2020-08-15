package com.example.psychapp.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.psychapp.MainActivity;
import com.example.psychapp.PsychApp;
import com.example.psychapp.R;
import com.example.psychapp.data.model.LoggedInUser;

public class LoginActivity extends AppCompatActivity {
    public static final Integer CODE_UNAVAILABLE= -1;
    public static final String LOGIN_INFO = "login_info";

    public static LoginActivity instance;
    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.instance = this;

        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText codeEditText = findViewById(R.id.code_text);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_INFO, MODE_PRIVATE);
        Integer code = sharedPreferences.getInt("login_code", CODE_UNAVAILABLE);

        //user exists
        if(code != CODE_UNAVAILABLE){
            PsychApp.userId = code;
            String name = sharedPreferences.getString("login_name","user");
            Integer researcherId = sharedPreferences.getInt("login_researcherId", -1);
            PsychApp.researcherId = researcherId;

            updateUiWithUser(new LoggedInUser(code, name, researcherId), false);
            setResult(Activity.RESULT_OK);
            finish();
            return;
        }

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getCodeError() != null) {
                    codeEditText.setError(getString(loginFormState.getCodeError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }

                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                } else if (loginResult.getSuccess() != null) {
                    LoggedInUser user = loginResult.getSuccess();
                    storeUserInfo(user);
                    updateUiWithUser(user, true);

                    //Complete and destroy login activity once successful
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(codeEditText.getText().toString());
            }
        };
        codeEditText.addTextChangedListener(afterTextChangedListener);

        /*
        nameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(codeEditText.getText().toString());
                }
                return false;
            }
        });
        */

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.setCode(codeEditText.getText().toString());
                LoginBackgroundTask loginTAsk = (LoginBackgroundTask) new LoginBackgroundTask().execute(loginViewModel);
            }
        });
    }

    public void startNextActivity(boolean showConsent){
        Intent intent;
        if(showConsent) {
            intent = new Intent(this, ConsentActivity.class);
        }else{
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
    }

    private void updateUiWithUser(LoggedInUser model, boolean showConsent) {
        PsychApp.userId = model.getUserId();
        PsychApp.researcherId = model.getResearcherId();
        String welcome;
        if(model.getDisplayName() == null) {
            welcome = "Welcome!";
        }else{
            welcome = "Welcome " + model.getDisplayName() + "!";
        }
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();

        startNextActivity(showConsent);
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void storeUserInfo(LoggedInUser user){
        SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_INFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("login_code", user.getUserId());
        editor.putString("login_name", user.getDisplayName());
        editor.putInt("login_researcherId", user.getResearcherId());
        editor.apply();
    }
}