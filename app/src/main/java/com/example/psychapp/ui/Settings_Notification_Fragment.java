package com.example.psychapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.psychapp.PsychApp;
import com.example.psychapp.R;
import com.example.psychapp.ui.login.LoginActivity;

import java.util.Calendar;

public class Settings_Notification_Fragment extends Fragment {
    public static int hour = 12, minutes = 00;

    private TimePicker datePicker;
    private TextView notificationTime;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_notification_fragment, container, false);
        datePicker = root.findViewById(R.id.notificationTimePicker);
        datePicker.setHour(hour);
        datePicker.setMinute(minutes);

        notificationTime = root.findViewById(R.id.notificationTime);
        notificationTime.setText(String.format("%02d:%02d", hour, minutes));

        Button notificationTimeButton = root.findViewById(R.id.notificationTimeButton);
        notificationTimeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                hour = datePicker.getHour();
                minutes = datePicker.getMinute();

                notificationTime.setText(String.format("%02d:%02d", hour, minutes));

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minutes);

                PsychApp.instance.shceduleBackgroundService(calendar);
                //PsychApp.instance.scheduleDailyNotification(calendar);
                Log.i("Notification","reminder set for "+calendar);
                getActivity().finish();
            }
        });
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("hour", hour);
        savedInstanceState.putInt("minutes", minutes);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
        hour = savedInstanceState.getInt("hour");
        minutes = savedInstanceState.getInt("minutes");
    }
}