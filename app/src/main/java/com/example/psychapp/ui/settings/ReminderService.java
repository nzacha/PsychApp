package com.example.psychapp.ui.settings;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.psychapp.ui.main.MainActivity;
import com.example.psychapp.R;

import java.util.Calendar;

public class ReminderService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Tapping the notification will open the specified Activity.
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // This always shows up in the notifications area when this Service is running.
        // TODO: String localization
        Notification not = new Notification.Builder(this).
                setContentTitle(getText(R.string.app_name)).
                setContentInfo("Doing stuff in the background...").setSmallIcon(R.mipmap.ic_launcher).
                setContentIntent(pendingIntent).build();
        //startForeground(1, not);

        shceduleRepeatingAlarm();
        Log.d("wtf","Service started");
        return Service.START_STICKY;
    }

    private void shceduleRepeatingAlarm(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(this, NotificationReceiver.class);
        alarmIntent.setAction("alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 1000 * 10, pendingIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}