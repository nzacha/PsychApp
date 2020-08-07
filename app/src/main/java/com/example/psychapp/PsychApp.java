package com.example.psychapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import java.util.Calendar;

public class PsychApp extends Application {;
    public static final String serverUrl = "http://10.0.2.2:5050/";
    public static int researcherId = -1, userId = -1;

    public static PsychApp instance;
    public static Context context;
    public static String CHANNEL_ID = "PsychAppNotifications";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = this;

        createNotificationChannel();

        //scheduleNotification(1000);
        //long timeInSeconds = 10;
        //scheduleNotification(timeInSeconds);
        //scheduleRepeatingNotification();
    }

    public void scheduleDailtNotification(long timeInSeconds){
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + (timeInSeconds * 1000L), alarmIntent);
    }

    public void scheduleDailyNotification(Calendar calendar){
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
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