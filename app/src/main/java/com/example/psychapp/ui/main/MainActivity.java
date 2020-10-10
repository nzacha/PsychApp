package com.example.psychapp.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.psychapp.R;
import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.ui.login.LoginActivity;
import com.example.psychapp.ui.main.SectionsPagerAdapter;
import com.example.psychapp.ui.questions.QuestionnaireActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getString(R.string.main_activity_help), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //QuestionnaireActivity.setEnabled(true);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 1);
        PsychApp.instance.scheduleDailyNotification(cal,145);
    }
}