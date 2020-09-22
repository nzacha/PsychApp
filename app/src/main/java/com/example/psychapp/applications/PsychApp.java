package com.example.psychapp.applications;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import com.example.psychapp.ui.settings.NotificationReceiver;
import com.example.psychapp.R;

import java.util.Calendar;

public class PsychApp extends Application {
    //public static final String serverUrl = "http://192.168.0.17:5050/";
    public static final String serverUrl = "http://109.110.252.231:5050/";
    //public static final String serverUrl = "http://10.0.2.2:5050/";

    public static Context context;
    public static String CHANNEL_ID = "PsychAppNotifications";
    public static boolean DEBUG = true;

    public static NotificationChannel channel;

    public static PsychApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = this;

        createNotificationChannel();
    }

    public void scheduleDailyNotification(Calendar calendar, int requestCode){
        if(calendar.get(Calendar.HOUR_OF_DAY) < Calendar.getInstance().get(Calendar.HOUR_OF_DAY) && calendar.get(Calendar.DATE) <= Calendar.getInstance().get(Calendar.DATE))
            calendar.add(Calendar.DATE,1);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, NotificationReceiver.class);
        alarmIntent.setAction("alarm");
        alarmIntent.putExtra("code", requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, alarmIntent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        //Log.d("wtf", calendar.toString());
        Log.d("wtf","Alarm set for "+String.format("%02d:%02d:%s (%d, %02d)", calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.AM_PM) == 0 ? "AM" : "PM", calendar.get(Calendar.DAY_OF_YEAR), calendar.get(Calendar.HOUR_OF_DAY))+" daily.");
    }

    public void scheduleNotificationReminder(int requestCode){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, NotificationReceiver.class);
        alarmIntent.setAction("reminder");
        alarmIntent.putExtra("code", requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, alarmIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d("wtf","Reminder set for "+String.format("%02d:%02d:%s", calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.AM_PM) == 0 ? "AM" : "PM"));

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
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

    public static void clearNotifications(){
        NotificationManager notificationManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(PsychApp.channel);
            notificationManager.cancelAll();
        }
    }
}