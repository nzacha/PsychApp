package com.example.psychapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ReminderService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        YourTask();
        return Service.START_STICKY;
    }

    private void YourTask(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


}