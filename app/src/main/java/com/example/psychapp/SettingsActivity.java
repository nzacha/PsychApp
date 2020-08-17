package com.example.psychapp;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.psychapp.ui.login.LoginActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    public static int DEFAULT_HOUR = 12, DEFAULT_MINUTES = 00;
    public ArrayList<Integer> hours = new ArrayList<Integer>();
    public ArrayList<Integer> minutes = new ArrayList<Integer>();

    private TimePicker datePicker;
    private TextView notificationLabel, notificationTime;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_tabbed);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        datePicker = findViewById(R.id.notificationTimePicker);
        notificationLabel = findViewById(R.id.notification_label);
        notificationTime = findViewById(R.id.notification_time);

        datePicker.setHour(DEFAULT_HOUR);
        datePicker.setMinute(DEFAULT_MINUTES);

        notificationTime.setText(String.format("%02d:%02d", DEFAULT_HOUR, DEFAULT_MINUTES));

        Button notificationTimeButton = findViewById(R.id.notificationTimeButton);
        notificationTimeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                DEFAULT_HOUR = datePicker.getHour();
                DEFAULT_MINUTES = datePicker.getMinute();

                notificationTime.setText(String.format("%02d:%02d", DEFAULT_HOUR, DEFAULT_MINUTES));

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, DEFAULT_HOUR);
                calendar.set(Calendar.MINUTE, DEFAULT_MINUTES);

                PsychApp.instance.scheduleDailyNotification(calendar, 51421);
                //PsychApp.instance.scheduleDailyNotification(calendar);
                Log.i("Notification","reminder set for "+calendar);
                finish();
            }
        });

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.LOGIN_INFO, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("login_code");
                editor.remove("login_name");
                editor.remove("login_researcherId");
                editor.apply();
                finishAffinity();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("hour", DEFAULT_HOUR);
        savedInstanceState.putInt("minutes", DEFAULT_MINUTES);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //hour = savedInstanceState.getInt("hour");
        //minute = savedInstanceState.getInt("minutes");
    }
}