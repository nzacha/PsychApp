package com.example.psychapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.psychapp.ui.login.LoginActivity;

import java.util.Calendar;
import java.util.Locale;

public class PsychApp extends Application {
    //public static final String serverUrl = "http://192.168.0.17:5050/";
    //public static final String serverUrl = "http://109.110.252.231:5050/";
    public static final String serverUrl = "http://10.0.2.2:5050/";
    public static String user_name = "";
    public static Integer researcherId = LoginActivity.CODE_UNAVAILABLE, userId = LoginActivity.CODE_UNAVAILABLE;
    public static Integer NUMBER_OF_ALARMS = 1, ALARM_INTERVAL = 3, STUDY_LENGTH = 1;

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

    public void scheduleDailyNotification(Calendar calendar, int requestCode){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, NotificationReceiver.class);
        alarmIntent.setAction("alarm");
        alarmIntent.putExtra("code", requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, alarmIntent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        Log.d("wtf","Alarm set for "+String.format("%2d:%2d:%s", calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.AM_PM) == 0 ? "AM" : "PM")+" daily.");
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
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}