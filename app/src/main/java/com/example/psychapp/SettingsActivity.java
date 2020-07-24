package com.example.psychapp;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    public static int hour = 12, minutes = 00;

    private TimePicker datePicker;
    private TextView notificationTime;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        datePicker = findViewById(R.id.notificationTimePicker);
        datePicker.setHour(hour);
        datePicker.setMinute(minutes);

        notificationTime = findViewById(R.id.notificationTime);
        notificationTime.setText(String.format("%02d:%02d", hour, minutes));

        Button notificationTimeButton = findViewById(R.id.notificationTimeButton);
        notificationTimeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                hour = datePicker.getHour();
                minutes = datePicker.getMinute();

                notificationTime.setText(String.format("%02d:%02d", hour, minutes));

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minutes);
                PsychApp.instance.scheduleDailyNotification(calendar);
                Log.i("Notification","reminder set for "+calendar);
                finish();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("hour", hour);
        savedInstanceState.putInt("minutes", minutes);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        hour = savedInstanceState.getInt("hour");
        minutes = savedInstanceState.getInt("minutes");
    }
}