package com.example.psychapp.ui.settings;

import static com.example.psychapp.ui.questions.QuestionnaireActivity.setEnabled;

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
import com.example.psychapp.R;
import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.data.LoggedInUser;
import com.example.psychapp.data.Question;
import com.example.psychapp.data.Section;
import com.example.psychapp.ui.login.LogoActivity;
import com.example.psychapp.ui.questions.QuestionnaireActivity;
import com.example.psychapp.ui.login.LoginActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NotificationReceiver extends BroadcastReceiver {
    private static int notification_code = 56748, reminder_code = 9876;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            LoginActivity.loadUserInfo();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (intent.getAction().equals("alarm")) {
            setEnabled(context, true);
            Toast.makeText(context, context.getString(R.string.notification_title), Toast.LENGTH_LONG).show();
            LoginActivity.progress();
            trackProgress(context, intent);
        } else if (intent.getAction().equals("reminder")) {
            Toast.makeText(context, context.getString(R.string.notification_reminder), Toast.LENGTH_LONG).show();
            sendReminder(context, intent);
        } else {
            Log.d("wtf", "Intent action not recognized");
        }
    }

    public void trackProgress(Context context, Intent intent) {
        if (PsychApp.isNetworkConnected(PsychApp.getContext())) {
            sendUserProgressUpdate();
        }

        Log.d("wtf", "automatic termination is: "+ LoginActivity.user.getAutomaticTermination());
        if (LoginActivity.user.isActive() && (LoginActivity.user.getAutomaticTermination() ? LoginActivity.user.getProgress() <= LoginActivity.user.getMaxProgress() : true)) {
//            ArrayList<Section> sections = QuestionnaireActivity.sections;
//            Log.d("wtf", "Answers Missing");
//            if (PsychApp.isNetworkConnected(PsychApp.getContext())) {
//                for(Section section: sections) {
//                    for (Question question : section.questions) {
//                        question.missing = true;
//                        question.date = Calendar.getInstance();
//                    }
//                    QuestionnaireActivity.sendAllAnswersToServer(section.questions, x->null);
//                }
//            }else{
//                for(Section section: sections)
//                    for(Question question: section.questions) {
//                        question.missing = true;
//                }
//                try {
//                    QuestionnaireActivity.saveAnswers(sections);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }

            sendNotification(context, intent);
        } else {
            Log.d("wtf", "Notification suppressed");
            sendDeactivationNotification(context, intent);
        }
    }

    public void sendReminder(Context context, Intent intent) {
        if (!QuestionnaireActivity.isActive(PsychApp.getContext())) {
            Log.d("wtf", "Reminder suppressed");
            return;
        }
        Log.d("wtf", "Sending Reminder");

        Intent newIntent = new Intent(context, LoginActivity.class);
        newIntent.putExtra("notification_origin", true);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(newIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(intent.getExtras().getInt("code"), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, PsychApp.CHANNEL_ID)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_reminder))
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
        notificationManager.notify(reminder_code, builder.build());

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    public void sendNotification(Context context, Intent intent) {
        PsychApp.clearNotifications();
        Log.d("wtf", "Sending Notification");

        Intent newIntent = new Intent(context, LoginActivity.class);
        newIntent.putExtra("notification_origin", true);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(newIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(intent.getExtras().getInt("code"), PendingIntent.FLAG_UPDATE_CURRENT);

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
        notificationManager.notify(notification_code, builder.build());

//        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//        // Vibrate for 500 milliseconds
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
//        } else {
//            //deprecated in API 26
//            v.vibrate(500);
//        }

        PsychApp.instance.scheduleNotificationReminder(reminder_code);
    }

    public static void sendUserProgressUpdate() {
        // Instantiate the RequestQueue.
        String url = PsychApp.serverUrl + "participant/" + LoginActivity.user.getUserId();
        Log.d("wtf", url);

        Map<String, String> params = new HashMap<>();
        params.put("progress", "" + LoginActivity.user.getProgress());
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.PATCH, url, new JSONObject(params),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //Toast.makeText(PsychApp.context, "Progress sent to server successfully", Toast.LENGTH_LONG).show();
                    Log.d("wtf", "Updated user progress in server to " + LoginActivity.user.getProgress());
                    Log.d("wtf", "Server responded with: " + response.toString());
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error.networkResponse != null)
                        Log.e("wtf", "An Error occurred: " + error.networkResponse.statusCode + ": " + error.networkResponse.data);
                    else
                        Log.e("wtf", "An Error occurred: " + error);
                }
            }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-access-token", LoginActivity.user.getToken());
                return params;
            }
        };;

        // add it to the RequestQueue
        PsychApp.queue.add(postRequest);
    }

    public void startService(Context context) {
        Intent serviceintent = new Intent(context, ReminderService.class);
        context.startService(serviceintent);
    }

    public void sendDeactivationNotification(Context context, Intent intent) {
        PsychApp.clearNotifications();
        setEnabled(false);
        LoginActivity.user.deactivate();
        LoginActivity.clearInfo();
        PsychApp.cancelAlarmNotifications();

        Log.d("wtf", "Sending Deactivation Notification");

        Intent newIntent = new Intent(context, LogoActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(newIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(intent.getExtras().getInt("code"), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(context, PsychApp.CHANNEL_ID)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.deactivation_title))
                .setContentIntent(pendingIntent)
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.app_logo);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notification_code, builder.build());

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