package com.example.psychapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import com.example.psychapp.MainActivity;
import com.example.psychapp.R;
import com.example.psychapp.ui.login.ConsentActivity;
import com.example.psychapp.ui.login.LoginActivity;

public class LogoActivity extends AppCompatActivity {
    private Handler mHandler;
    private Runnable mNextActivityCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        mHandler = new Handler();
        mNextActivityCallback = new Runnable() {
            @Override
            public void run() {
                // Intent to jump to the next activity
                Intent intent= new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish(); // so the splash activity goes away
            }
        };
        mHandler.postDelayed(mNextActivityCallback, 3000L);
    }
}