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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psychapp.ui.login.LoginActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NotificationReceiver extends BroadcastReceiver {
    private static int count = 56748;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("alarm")) {
            try {
                LoginActivity.loadUserInfo();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            LoginActivity.progress();

            Log.d("wtf","Sending Notification");
            trackProgress(context, intent);
        } else {
            Log.d("wtf","Intent action not recognized");
        }
    }

    public void trackProgress(Context context, Intent intent){
        if(PsychApp.isNetworkConnected(PsychApp.context)) {
            sendUserProgressUpdate();
        }

        if (LoginActivity.user.getAutomaticTermination()) {
            if (LoginActivity.user.getProgress() <= LoginActivity.user.getMaxProgress()) {
                sendNotification(context, intent);
            } else {
                Log.d("wtf", "Notification supressed");
            }
        } else {
            sendNotification(context, intent);
        }
    }

    public void sendNotification(Context context, Intent intent){
        Toast.makeText(context.getApplicationContext(), "Please answer some short questions", Toast.LENGTH_LONG).show();
        QuestionnaireActivity.setEnabled(true);

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

    private void sendUserProgressUpdate(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(PsychApp.context);
        String url = PsychApp.serverUrl + "users/progress/" + LoginActivity.user.getUserId();

        Map<String, String> params = new HashMap<>();
        params.put("progress", ""+LoginActivity.user.getProgress());
        Log.d("wtf", params.toString());
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(PsychApp.DEBUG)
                            Toast.makeText(PsychApp.context, "Progress sent to server successfully", Toast.LENGTH_LONG).show();
                        Log.d("wtf", "Updated user progress in server to "+ LoginActivity.user.getProgress());
                        Log.d("wtf", "Server responded with: "+ response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("wtf", "An Error occurred");
                    }
                });

        // add it to the RequestQueue
        queue.add(postRequest);
    }

    public void startService(Context context){
        Intent serviceintent = new Intent(context, ReminderService.class);
        context.startService(serviceintent);
    }
}