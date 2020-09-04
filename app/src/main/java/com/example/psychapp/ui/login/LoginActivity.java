package com.example.psychapp.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.example.psychapp.IntroductionActivity;
import com.example.psychapp.MainActivity;
import com.example.psychapp.PsychApp;
import com.example.psychapp.QuestionnaireActivity;
import com.example.psychapp.R;
import com.example.psychapp.data.model.LoggedInUser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import static com.example.psychapp.PsychApp.context;

public class LoginActivity extends AppCompatActivity {
    public static final String USER_INFO = "User_info";
    public static LoggedInUser user = null;

    private LoginViewModel loginViewModel = new LoginViewModel("");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //setLocale("el");

        final EditText codeEditText = findViewById(R.id.code_text);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        try {
            loadUserInfo();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }

                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    //error occurred
                    showLoginFailed(loginResult.getError());
                    //stored user info is wrong
                    if(user!=null && loginResult.getError() == R.string.user_inactive){
                        LoginActivity.clearInfo();
                        finishAffinity();
                        QuestionnaireActivity.setEnabled(false);
                    }
                } else if (loginResult.getSuccess() != null) {
                    //value is correct occurred
                    user = loginResult.getSuccess();
                    try {
                        saveUserInfo(user);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    updateUiWithUser(user, user == null);
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });

        //user exists
        if(user != null){
            Log.d("wtf","code available");
            codeEditText.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
            loadingProgressBar.setVisibility(View.VISIBLE);

            if(PsychApp.isNetworkConnected(this)){
                loginViewModel.setCode(""+user.getUserId());
                LoginBackgroundTask loginTask = (LoginBackgroundTask) new LoginBackgroundTask().execute(loginViewModel);
            } else {
                try {
                    loadUserInfo();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                updateUiWithUser(user, false);
                setResult(Activity.RESULT_OK);
                finish();
            }
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
        user = null;
        context.deleteFile(USER_INFO);
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

    private static void saveUserInfo(LoggedInUser user) throws IOException {
        FileOutputStream fos = PsychApp.context.openFileOutput(USER_INFO, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(user);
        os.close();
        fos.close();
        Log.d("wtf", "User info saved on Phone");
    }

    public static void progress(){
        Log.d("wtf", "before progress: "+user.getProgress());
        user.progress();
        Log.d("wtf", "progress: "+user.getProgress());
        try {
            saveUserInfo(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("wtf", "after save progress: "+user.getProgress());

        try {
            loadUserInfo();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("wtf", "after load  progress: "+user.getProgress());
    }

    public static void loadUserInfo() throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(USER_INFO);
        ObjectInputStream is = new ObjectInputStream(fis);
        user = (LoggedInUser) is.readObject();
        Log.d("wtf", user.toString());
        is.close();
        fis.close();
        Log.d("wtf", "User info loaded from Phone");
    }
}