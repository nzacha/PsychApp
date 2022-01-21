package com.example.psychapp.ui.settings;

import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.R;
import com.example.psychapp.ui.login.LoginActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class Settings_Notification_Fragment extends Fragment {
    private static final String Settings_State = "settings_State";
    public static int DEFAULT_HOUR = 8, DEFAULT_MINUTES = 0, MINIMUM_HOUR = 4, MAXIMUM_HOUR = 22;
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
        for (int i = 0; i < LoginActivity.user.getTestsPerDay(); i++){
            Integer hour = sharedPreferences.getInt("hour_value_" + i, DEFAULT_HOUR + (LoginActivity.user.getTestsTimeInterval() * i));
            Integer minute = sharedPreferences.getInt("minute_value_" + i, DEFAULT_MINUTES);
            hours.add(hour);
            minutes.add(minute);
        }

        datePicker = root.findViewById(R.id.notificationTimePicker);
        final NumberPicker hourSpinner = (NumberPicker) datePicker.findViewById(Resources.getSystem().getIdentifier("hour", "id", "android"));
        hourSpinner.setWrapSelectorWheel(false);
        NumberPicker minuteSpinner = (NumberPicker) datePicker.findViewById(Resources.getSystem().getIdentifier("minute", "id", "android"));
        minuteSpinner.setMaxValue(3);
        minuteSpinner.setMinValue(0);
        minuteSpinner.setDisplayedValues(new String[]{"00","15","30","45"});
        minuteSpinner.setValue(1);

        notificationLabel = root.findViewById(R.id.notificationTimeTitle);
        notificationTime = root.findViewById(R.id.notification_time);
        notificationTimes = root.findViewById(R.id.notifications);

        datePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                if(LoginActivity.user.getTestsPerDay() > 1  && LoginActivity.user.getAllowIndividualTimes()) {
                    RadioButton radio = (RadioButton) root.findViewById(notificationTimes.getCheckedRadioButtonId());
                    radio.setText(String.format("%02d:%02d", datePicker.getHour(), datePicker.getMinute() * MINUTE_INTERVAL));
                }else{
                    notificationTime.setText(String.format("%02d:%02d", datePicker.getHour(), datePicker.getMinute() * MINUTE_INTERVAL));
                }
            }
        });

        Log.d("wtf", ""+LoginActivity.user.getTestsPerDay());
        if (LoginActivity.user.getTestsPerDay() > 1  && LoginActivity.user.getAllowIndividualTimes()) {
            notificationLabel.setText(R.string.notification_time_interval);
            String modifiedReminderText = notificationLabel.getText().toString();
            modifiedReminderText.replace("${1}", ""+LoginActivity.user.getTestsPerDay()).replace("${2}", ""+LoginActivity.user.getTestsTimeInterval());
            notificationLabel.setText(modifiedReminderText);

            notificationTime.setVisibility(View.INVISIBLE);

            RadioButton radio;
            for (int i = 0; i < LoginActivity.user.getTestsPerDay(); i++){
                radio = new RadioButton(getContext());
                radio.setText(String.format("%02d:%02d", hours.get(i), minutes.get(i)));
                notificationTimes.addView(radio);
            }
            notificationTimes.check(notificationTimes.getChildAt(0).getId());

            radio = (RadioButton) root.findViewById(notificationTimes.getCheckedRadioButtonId());
            Integer hour = Integer.parseInt(radio.getText().toString().split(":")[0]);
            Integer minute = Integer.parseInt(radio.getText().toString().split(":")[1]);
            datePicker.setHour(hour);
            datePicker.setMinute(minute / MINUTE_INTERVAL);
            datePicker.setIs24HourView(true);

            notificationTimes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    RadioButton radio = (RadioButton) root.findViewById(radioGroup.getCheckedRadioButtonId());
                    int index = radioGroup.indexOfChild(radio);
                    int hourMin, hourMax, hour = Integer.parseInt(radio.getText().toString().split(":")[0]);
                    int minutes = Integer.parseInt(radio.getText().toString().split(":")[1]);
                    hourMin = hour;
                    hourMax = hour;

                    RadioButton tempRadio;
                    if (index == 0) {
                        tempRadio = (RadioButton) radioGroup.getChildAt(index + 1);
                        hourMin = MINIMUM_HOUR;
                        hourMax = Integer.parseInt(tempRadio.getText().toString().split(":")[0]) - LoginActivity.user.getTestsTimeInterval();
                    } else if (index == LoginActivity.user.getTestsPerDay() -1) {
                        tempRadio = (RadioButton) radioGroup.getChildAt(index -1);
                        hourMin = Integer.parseInt(tempRadio.getText().toString().split(":")[0]) + LoginActivity.user.getTestsTimeInterval();
                        hourMax = MAXIMUM_HOUR;
                    } else {
                        tempRadio = (RadioButton) radioGroup.getChildAt(index -1);
                        hourMin = Integer.parseInt(tempRadio.getText().toString().split(":")[0]) + LoginActivity.user.getTestsTimeInterval();
                        tempRadio = (RadioButton) radioGroup.getChildAt(index +1);
                        hourMax = Integer.parseInt(tempRadio.getText().toString().split(":")[0]) - LoginActivity.user.getTestsTimeInterval();
                    }

                    hourSpinner.setMinValue(hourMin);
                    hourSpinner.setMaxValue(hourMax);

                    datePicker.setHour(hour);
                    datePicker.setMinute(minutes / MINUTE_INTERVAL);
                }
            });

            Button notificationTimeButton = root.findViewById(R.id.notificationTimeButton);
            notificationTimeButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    hours.clear();
                    minutes.clear();
                    for (int i = 0; i < LoginActivity.user.getTestsPerDay(); i++){
                        RadioButton radio = (RadioButton) notificationTimes.getChildAt(i);
                        hours.add(Integer.parseInt(radio.getText().toString().split(":")[0]));
                        minutes.add(Integer.parseInt(radio.getText().toString().split(":")[1]));

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        calendar.set(Calendar.HOUR_OF_DAY, hours.get(i));
                        calendar.set(Calendar.MINUTE, minutes.get(i));

                        PsychApp.instance.scheduleDailyNotification(calendar, 2612 + i);
                    }
                    getActivity().finish();
                }
            });
        } else {
            // else (LoginActivity.user.getTestsPerDay() == 1) {
            notificationTimes.setVisibility(View.INVISIBLE);
            notificationTime.setText(String.format("%02d:%02d", hours.get(0), minutes.get(0) * MINUTE_INTERVAL));

            Integer hour = Integer.parseInt(notificationTime.getText().toString().split(":")[0]);
            Integer minute = Integer.parseInt(notificationTime.getText().toString().split(":")[1]);
            datePicker.setHour(hour);
            datePicker.setMinute(minute / MINUTE_INTERVAL);
            datePicker.setIs24HourView(true);

            Button notificationTimeButton = root.findViewById(R.id.notificationTimeButton);
            notificationTimeButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    hours.clear();
                    minutes.clear();

                    hours.add(datePicker.getHour());
                    minutes.add(datePicker.getMinute());

                    if (LoginActivity.user.getTestsPerDay() == 1) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        calendar.set(Calendar.HOUR_OF_DAY, hours.get(0));
                        calendar.set(Calendar.MINUTE, minutes.get(0) * MINUTE_INTERVAL);

                        PsychApp.instance.scheduleDailyNotification(calendar, 2612);
                    } else {
                        for (int i = 0; i < LoginActivity.user.getTestsPerDay(); i++){
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            calendar.set(Calendar.HOUR_OF_DAY, hours.get(0) + LoginActivity.user.getTestsTimeInterval()*i);
                            calendar.set(Calendar.MINUTE, minutes.get(0) * MINUTE_INTERVAL);

                            PsychApp.instance.scheduleDailyNotification(calendar, 2612+i);
                        }
                    }

                    storeValues();

                    getActivity().finish();
                }
            });
        }
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        storeValues();
    }

    public void storeValues() {
        if(LoginActivity.user == null) {
            Log.d("wtf", "Can't store values, user is null");
            return;
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Settings_State, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int maxValue = LoginActivity.user.getTestsPerDay();
        if(!LoginActivity.user.getAllowIndividualTimes())
            maxValue =1;
        for(int i=0; i < maxValue; i++) {
            editor.putInt("hour_value_" + i, hours.get(i));
            editor.putInt("minute_value_" + i, minutes.get(i));
        }
        editor.apply();
    }
}