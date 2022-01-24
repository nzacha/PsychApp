package com.example.psychapp.ui.main;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.psychapp.R;
import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.ui.login.LoginActivity;
import com.example.psychapp.ui.main.SectionsPagerAdapter;
import com.example.psychapp.ui.questions.QuestionnaireActivity;
import com.example.psychapp.ui.settings.NotificationReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
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

//        QuestionnaireActivity.setEnabled(true);
//        test();
    }

    private void test(){
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendTest(true);
            }
        }, 3000);
    }

    private void sendTest(boolean sendNotification){
        LoginActivity.progress();
        if (PsychApp.isNetworkConnected(PsychApp.context)) {
            NotificationReceiver.sendUserProgressUpdate();
        }

        if(!sendNotification)
            return;

        if ((!LoginActivity.user.isActive() || (LoginActivity.user.getAutomaticTermination() ? LoginActivity.user.getProgress() > LoginActivity.user.getMaxProgress() : false))) {
            Log.d("wtf", "user was deactivated");
            PsychApp.clearNotifications();
            QuestionnaireActivity.setEnabled(false);
            LoginActivity.user.deactivate();
            LoginActivity.clearInfo();
            PsychApp.cancelAlarmNotifications();
            finishAffinity();
            return;
        }
        Context context = PsychApp.context;
        PsychApp.clearNotifications();
        Log.d("wtf", "Sending Notification");

        Intent newIntent = new Intent(context, LoginActivity.class);
        newIntent.putExtra("notification_origin", true);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(newIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(13452, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, PsychApp.CHANNEL_ID)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_title))
                .setContentIntent(pendingIntent)
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.app_logo)
                .setTimeoutAfter(10800000);
        /*
        if(Build.VERSION.SDK_INT < 25) {
            builder.setSmallIcon(R.drawable.ic_stat_name);
        }else{
            builder.setSmallIcon(R.drawable.ic_launcher);
        }
        */

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1345, builder.build());

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }
}