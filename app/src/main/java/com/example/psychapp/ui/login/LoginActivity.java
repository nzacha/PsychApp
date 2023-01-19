package com.example.psychapp.ui.login;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psychapp.data.Exceptions;
import com.example.psychapp.data.Result;
import com.example.psychapp.ui.IntroductionActivity;
import com.example.psychapp.ui.main.MainActivity;
import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.ui.questions.QuestionnaireActivity;
import com.example.psychapp.R;
import com.example.psychapp.data.LoggedInUser;
import com.example.psychapp.ui.settings.NotificationReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import static com.example.psychapp.ui.IntroductionActivity.DESCRIPTION;

public class LoginActivity extends AppCompatActivity {
    public static final String USER_INFO = "User_info";
    public static LoggedInUser user = null;

    private LoginViewModel loginViewModel = new LoginViewModel("");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //power permission request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent power_intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                power_intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                power_intent.setData(Uri.parse("package:" + packageName));
                startActivity(power_intent);
            }
        }

        try {
            loadUserInfo();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        final boolean savedData = (user != null);

        //send local answers
        if(QuestionnaireActivity.answersExist() && PsychApp.isNetworkConnected(PsychApp.getContext())){
            Log.d("wtf","sending local answers");
            try {
                QuestionnaireActivity.sendLocalAnswers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //setLocale("el");

        final EditText codeEditText = findViewById(R.id.code_text);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

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
                        QuestionnaireActivity.setEnabled(false);
                        finishAffinity();
                    }
                } else if (loginResult.getSuccess() != null) {
                    //value is correct occurred
                    if(user!=null) {
                        LoggedInUser newData = loginResult.getSuccess();
                        user = new LoggedInUser(user.getUserId(), user.getDisplayName(), newData.getProjectId(), newData.getStudyLength(), newData.getTestsPerDay(), newData.getTestsTimeInterval(), newData.getAllowIndividualTimes(), newData.getAllowUserTermination(), newData.getAutomaticTermination(), user.getProgress(), user.getCode(), newData.isActive(), user.getToken());
                    } else {
                        user = loginResult.getSuccess();
                    }
                    PsychApp.clearNotifications();
                    try {
                        saveUserInfo(user);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    updateUiWithUser(user, (!savedData && user != null));
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });

        //user exists
        if(savedData){
            Log.d("wtf","code available");

            codeEditText.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
            loadingProgressBar.setVisibility(View.VISIBLE);

            if(PsychApp.isNetworkConnected(this)){
                Log.d("wtf", "fetching user data from server");
                NotificationReceiver.sendUserProgressUpdate();
                loginViewModel.setCode(""+user.getCode());
                LoginBackgroundTask loginTask = (LoginBackgroundTask) new LoginBackgroundTask().execute(loginViewModel);
            } else {
//                Log.d("wtf", "fetching user data from local storage");
//                try {
//                    loadUserInfo();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }

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
        Log.d("wtf", "Login was successfull");
//        QuestionnaireActivity.retrieveQuestions(model.getProjectId());

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.getBoolean("notification_origin")){
                Intent newIntent = new Intent(PsychApp.getContext(), QuestionnaireActivity.class);
                TaskStackBuilder.create(PsychApp.getContext())
                        .addNextIntentWithParentStack(newIntent)
                        .startActivities();

                //Complete and destroy login activity once successful
                setResult(Activity.RESULT_OK);
                finish();
                return;
            }
        }

        //String welcome = "Welcome " + model.getDisplayName() + "!";
        String welcome = "Welcome!";

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
        PsychApp.getContext().deleteFile(USER_INFO);
        PsychApp.getContext().deleteFile(DESCRIPTION);
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
        FileOutputStream fos = PsychApp.getContext().openFileOutput(USER_INFO, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(user);
        os.close();
        fos.close();
        Log.d("wtf", "User info saved on Phone");
    }

    public static void progress(){
        //Log.d("wtf", "before progress: "+user.getProgress());
        user.progress();
        Log.d("wtf", "progress is updating from / to: "+user.getProgress()+"/"+user.getMaxProgress());
        try {
            saveUserInfo(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("wtf", "after save progress: "+user.getProgress());

        try {
            loadUserInfo();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //Log.d("wtf", "after load  progress: "+user.getProgress());
    }

    public static void loadUserInfo() throws IOException, ClassNotFoundException {
        File file = PsychApp.getContext().getFileStreamPath(USER_INFO);
        if(!file.exists())
            return;
        FileInputStream fis = PsychApp.getContext().openFileInput(USER_INFO);
        ObjectInputStream is = new ObjectInputStream(fis);
        user = (LoggedInUser) is.readObject();
        is.close();
        fis.close();
        Log.d("wtf", "User info loaded from Phone");
    }

    public static void loadUserInfo(Context context) throws IOException, ClassNotFoundException {
        File file = context.getFileStreamPath(USER_INFO);
        if(!file.exists())
            return;
        FileInputStream fis = context.openFileInput(USER_INFO);
        ObjectInputStream is = new ObjectInputStream(fis);
        user = (LoggedInUser) is.readObject();
        is.close();
        fis.close();
        Log.d("wtf", "User info loaded from Phone");
    }
}