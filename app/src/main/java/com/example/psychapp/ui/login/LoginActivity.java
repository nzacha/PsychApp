package com.example.psychapp.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psychapp.IntroductionActivity;
import com.example.psychapp.MainActivity;
import com.example.psychapp.PsychApp;
import com.example.psychapp.QuestionnaireActivity;
import com.example.psychapp.R;
import com.example.psychapp.data.Result;
import com.example.psychapp.data.model.LoggedInUser;
import com.example.psychapp.ui.ConsentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    public static final Integer CODE_UNAVAILABLE= -1;
    public static final String LOGIN_INFO = "login_info";

    public static LoginActivity instance;
    private LoginViewModel loginViewModel = new LoginViewModel("");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.instance = this;
        setContentView(R.layout.activity_login);

        //setLocale("el");

        final EditText codeEditText = findViewById(R.id.code_text);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        SharedPreferences sharedPreferences = getSharedPreferences(LOGIN_INFO, MODE_PRIVATE);
        Integer code = sharedPreferences.getInt("login_code", CODE_UNAVAILABLE);

        //user exists
        if(code != CODE_UNAVAILABLE){
            if(PsychApp.isNetworkConnected(this)){
                loginViewModel = new LoginViewModel(""+code);
                loginViewModel.login();
                try {
                    QuestionnaireActivity.sendLocalAnswers(PsychApp.userId);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            loadUserData();

            updateUiWithUser(new LoggedInUser(PsychApp.userId, PsychApp.user_name,  PsychApp.researcherId, PsychApp.STUDY_LENGTH, PsychApp.NUMBER_OF_ALARMS, PsychApp.ALARM_INTERVAL), false);
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
                    codeEditText.setError("Error");
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
                    storeResearcherInfo(user.getStudyLength(), user.getTestsPerDay(), user.getTestsTimeInterval());
                    updateUiWithUser(user, true);
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
                LoginBackgroundTask loginTask = (LoginBackgroundTask) new LoginBackgroundTask().execute(loginViewModel);
            }
        });
    }

    public static void loadUserData(){
        SharedPreferences sharedPreferences = PsychApp.context.getSharedPreferences(LOGIN_INFO, MODE_PRIVATE);

        PsychApp.userId = sharedPreferences.getInt("login_code", CODE_UNAVAILABLE);
        PsychApp.user_name = sharedPreferences.getString("login_name","user");
        PsychApp.researcherId = sharedPreferences.getInt("login_researcherId", -1);
        PsychApp.STUDY_LENGTH = sharedPreferences.getInt("study_length", 1);
        PsychApp.NUMBER_OF_ALARMS = sharedPreferences.getInt("tests_per_day", 1);
        PsychApp.ALARM_INTERVAL = sharedPreferences.getInt("tests_time_interval", 3);
    }

    public void startNextActivity(boolean showConsent){
        Intent intent;
        if(showConsent) {
            intent = new Intent(this, IntroductionActivity.class);
            intent.putExtra("new_user", true);
        }else{
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
    }

    private void updateUiWithUser(LoggedInUser model, boolean showConsent) {
        //String welcome = "Welcome " + model.getDisplayName() + "!";
        String welcome = "Welcome!";

        QuestionnaireActivity.retrieveQuestions(getApplicationContext(), model.getResearcherId());

        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        startNextActivity(showConsent);

        //Complete and destroy login activity once successful
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    public static void clearInfo(){
        SharedPreferences sharedPreferences = PsychApp.context.getSharedPreferences(LoginActivity.LOGIN_INFO, PsychApp.context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("login_code");
        editor.remove("login_name");
        editor.remove("login_researcherId");
        editor.remove("study_length");
        editor.remove("tests_per_day");
        editor.remove("tests_time_interval");
        editor.apply();
    }

    private static void storeResearcherInfo(int study_length, int tests_per_day, int tests_time_interval){
        SharedPreferences sharedPreferences = PsychApp.context.getSharedPreferences(LOGIN_INFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("study_length", study_length);
        editor.putInt("tests_per_day", tests_per_day);
        editor.putInt("tests_time_interval", tests_time_interval);
        editor.apply();

        PsychApp.STUDY_LENGTH = study_length;
        PsychApp.NUMBER_OF_ALARMS = tests_per_day;
        PsychApp.ALARM_INTERVAL = tests_time_interval;
    }

    private static void storeUserInfo(LoggedInUser user){
        SharedPreferences sharedPreferences = PsychApp.context.getSharedPreferences(LOGIN_INFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("login_code", user.getUserId());
        editor.putString("login_name", user.getDisplayName());
        editor.putInt("login_researcherId", user.getResearcherId());
        editor.apply();

        PsychApp.userId = user.getUserId();
        PsychApp.researcherId = user.getResearcherId();
    }

    public void setLocale(String language) {
        Log.d("wtf","language changed");
        String languageToLoad  = language; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        this.setContentView(R.layout.activity_login);
    }
}