package com.example.psychapp.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.psychapp.PsychApp;
import com.example.psychapp.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.InputMismatchException;

public class Settings_Notification_Fragment extends Fragment {
    private static final String Settings_State = "settings_Stae";
    public static int DEFAULT_HOUR = 8, DEFAULT_MINUTES = 0;
    public ArrayList<Integer> hours = new ArrayList<Integer>();
    public ArrayList<Integer> minutes = new ArrayList<Integer>();

    private TimePicker datePicker;
    private NumberPicker hourPicker, minutePicker;
    private TextView notificationLabel, notificationTime;
    private RadioGroup notificationTimes;

    private static final int MINUTE_INTERVAL = 15;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.settings_notification_fragment, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Settings_State, getActivity().MODE_PRIVATE);
        for (int i = 0; i < PsychApp.NUMBER_OF_ALARMS; i++){
            Integer hour = sharedPreferences.getInt("hour_value_" + i, DEFAULT_HOUR + 6);
            Integer minute = sharedPreferences.getInt("minute_value_" + i, DEFAULT_MINUTES);
            hours.add(hour);
            minutes.add(minute);
        }

        datePicker = root.findViewById(R.id.notificationTimePicker);
        final NumberPicker hourSpinner = (NumberPicker) datePicker.findViewById(Resources.getSystem().getIdentifier("hour", "id", "android"));
        NumberPicker minuteSpinner = (NumberPicker) datePicker.findViewById(Resources.getSystem().getIdentifier("minute", "id", "android"));
        minuteSpinner.setMaxValue(3);
        minuteSpinner.setMinValue(0);
        minuteSpinner.setDisplayedValues(new String[]{"00","15","30","45"});
        minuteSpinner.setValue(1);


        notificationLabel = root.findViewById(R.id.notification_label);
        notificationTime = root.findViewById(R.id.notification_time);
        notificationTimes = root.findViewById(R.id.notifications);

        if (PsychApp.NUMBER_OF_ALARMS == 1) {
            notificationTimes.setVisibility(View.INVISIBLE);
            notificationTime.setText(String.format("%02d:%02d", hours.get(0), minutes.get(0)));

            Button notificationTimeButton = root.findViewById(R.id.notificationTimeButton);
            notificationTimeButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    hours.clear();
                    minutes.clear();
                    hours.add(datePicker.getHour());
                    minutes.add(datePicker.getMinute());

                    notificationTime.setText(String.format("%02d:%02d", hours.get(0), minutes.get(0)));

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, hours.get(0));
                    calendar.set(Calendar.MINUTE, minutes.get(0) * MINUTE_INTERVAL);

                    PsychApp.instance.scheduleDailyNotification(calendar, 2612);

                    Log.i("Notification","reminder set for "+calendar);
                    getActivity().finish();
                }
            });
        } else if (PsychApp.NUMBER_OF_ALARMS > 1) {
            notificationTime.setVisibility(View.INVISIBLE);
            notificationLabel.setVisibility(View.INVISIBLE);
            notificationTimes.check(notificationTimes.getChildAt(0).getId());

            RadioButton radio;
            for (int i = 0; i < PsychApp.NUMBER_OF_ALARMS; i++){
                radio = (RadioButton) notificationTimes.getChildAt(i);
                radio.setText(String.format("%02d:%02d", hours.get(i), minutes.get(i)));
            }
            radio = (RadioButton) root.findViewById(notificationTimes.getCheckedRadioButtonId());
            Integer hour = Integer.parseInt(radio.getText().toString().split(":")[0]);
            Integer minute = Integer.parseInt(radio.getText().toString().split(":")[1]);
            datePicker.setHour(hour);
            datePicker.setMinute(minute * MINUTE_INTERVAL);
            datePicker.setIs24HourView(true);

            notificationTimes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    RadioButton radio = (RadioButton) root.findViewById(radioGroup.getCheckedRadioButtonId());
                    int index = radioGroup.indexOfChild(radio);
                    Integer hourMin, hourMax, hour = Integer.parseInt(radio.getText().toString().split(":")[0]);
                    Integer minutes = Integer.parseInt(radio.getText().toString().split(":")[1]);
                    hourMin = hour;
                    hourMax = hour;

                    RadioButton tempRadio;
                    if(index > 0){
                        tempRadio = (RadioButton) radioGroup.getChildAt(index-1);
                        hourMin = Integer.parseInt(tempRadio.getText().toString().split(":")[0]) + 3;
                    }
                    if(index < PsychApp.NUMBER_OF_ALARMS-1){
                        tempRadio = (RadioButton) radioGroup.getChildAt(index+1);
                        hourMax = Integer.parseInt(tempRadio.getText().toString().split(":")[0]) - 3;
                    }

                    hourSpinner.setMinValue(hourMin);
                    hourSpinner.setMaxValue(hourMax);

                    datePicker.setHour(hour);
                    datePicker.setMinute(minutes * MINUTE_INTERVAL);
                }
            });

            datePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                @Override
                public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                    RadioButton radio = (RadioButton) root.findViewById(notificationTimes.getCheckedRadioButtonId());
                    radio.setText(String.format("%02d:%02d", datePicker.getHour(), datePicker.getMinute() * MINUTE_INTERVAL));

                    for (int j=0; j<PsychApp.NUMBER_OF_ALARMS -1; j++) {
                        RadioButton button1 = (RadioButton) notificationTimes.getChildAt(j);
                        RadioButton button2 = (RadioButton) notificationTimes.getChildAt(j+1);

                    }
                }
            });

            Button notificationTimeButton = root.findViewById(R.id.notificationTimeButton);
            notificationTimeButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    hours.clear();
                    minutes.clear();
                    for (int i = 0; i < PsychApp.NUMBER_OF_ALARMS; i++){
                        RadioButton radio = (RadioButton) notificationTimes.getChildAt(i);
                        hours.add(Integer.parseInt(radio.getText().toString().split(":")[0]));
                        minutes.add(Integer.parseInt(radio.getText().toString().split(":")[1]));

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        calendar.set(Calendar.HOUR_OF_DAY, hours.get(i));
                        calendar.set(Calendar.MINUTE, minutes.get(i));

                        PsychApp.instance.scheduleDailyNotification(calendar, 2612 + i);

                        Log.i("Notification","reminder set for " + calendar);
                    }
                    getActivity().finish();
                }
            });
        } else {
            throw new InputMismatchException();
        }
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Settings_State, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i=0; i < PsychApp.NUMBER_OF_ALARMS; i++) {
            editor.putInt("hour_value_" + i, hours.get(i));
            editor.putInt("minute_value_" + i, minutes.get(i));
        }
        editor.apply();
    }
}