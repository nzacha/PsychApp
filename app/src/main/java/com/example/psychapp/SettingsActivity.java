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