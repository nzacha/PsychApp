package com.example.psychapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;

public class NotificationReceiver extends BroadcastReceiver {
    private static int count = 56748;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("alarm")) {
            Toast.makeText(context.getApplicationContext(), "Alarm Manager just ran", Toast.LENGTH_LONG).show();
            sendNotification(context, intent);
            Log.d("wtf","Sending Notification");
        } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            startService(context);
            Log.d("wtf","Starting service - BOOT");
        } else if (intent.getAction().equals("service.start")){
            startService(context);
            Log.d("wtf","Starting service");
        } else{
            Log.d("wtf","Intent action not recognized");
        }
    }

    public void startService(Context context){
        Intent serviceintent = new Intent(context, ReminderService.class);
        context.startService(serviceintent);
    }

    public void sendNotification(Context context, Intent intent){
        intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, count, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PsychApp.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("this is a Title")
                .setContentText("this is a Message, with no: " + count)
                //.setAutoCancel(true)
                //.setStyle(new NotificationCompat.BigTextStyle()
                //        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(count, builder.build());
        count++;
    }
}