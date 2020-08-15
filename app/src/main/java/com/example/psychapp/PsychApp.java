package com.example.psychapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.psychapp.ui.login.LoginActivity;

import java.util.Calendar;

public class PsychApp extends Application {;
    public static final String serverUrl = "http://10.0.2.2:5050/";
    public static Integer researcherId = LoginActivity.CODE_UNAVAILABLE, userId = LoginActivity.CODE_UNAVAILABLE;

    public static PsychApp instance;
    public static Context context;
    public static String CHANNEL_ID = "PsychAppNotifications";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = this;

        createNotificationChannel();
    }

    public void shceduleBackgroundService(Calendar calendar){
        /*
        Intent serviceintent = new Intent(context, ReminderService.class);
        context.startService(serviceintent);
        */

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, NotificationReceiver.class);
        alarmIntent.setAction("alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 89467, alarmIntent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 1000 * 10, AlarmManager.INTERVAL_DAY, pendingIntent);

        Log.d("wtf","Alarm`set");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}