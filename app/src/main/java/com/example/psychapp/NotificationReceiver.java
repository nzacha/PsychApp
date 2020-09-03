package com.example.psychapp;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static int count = 56748;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("alarm")) {
            Toast.makeText(context.getApplicationContext(), "Please answer some short questions", Toast.LENGTH_LONG).show();
            QuestionnaireActivity.setEnabled(true);
            sendNotification(context, intent);
            Log.d("wtf","Sending Notification");
        } else {
            Log.d("wtf","Intent action not recognized");
        }
    }

    public void startService(Context context){
        Intent serviceintent = new Intent(context, ReminderService.class);
        context.startService(serviceintent);
    }

    public void sendNotification(Context context, Intent intent){
        Intent newIntent = new Intent(context, QuestionnaireActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(newIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(intent.getExtras().getInt("code"), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, PsychApp.CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_title))
                //.setContentText("Ignore this message " + count)
                .setContentIntent(pendingIntent)
                //.setStyle(new NotificationCompat.BigTextStyle()
                //        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        if(Build.VERSION.SDK_INT < 25) {
            builder.setSmallIcon(R.drawable.ic_stat_name);
        }else{
            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(count, builder.build());
        count++;

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