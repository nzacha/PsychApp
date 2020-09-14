package com.example.psychapp.ui.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;

import com.example.psychapp.R;
import com.example.psychapp.ui.login.WelcomeScreen;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class StartupNotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_notification);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent= new Intent(getApplicationContext(), WelcomeScreen.class);
        startActivity(intent);
    }
}