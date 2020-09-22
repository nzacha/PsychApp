package com.example.psychapp.ui.login;

import android.os.AsyncTask;

import java.net.URL;

public class LoginBackgroundTask extends AsyncTask<LoginViewModel, Integer, Long> {

    @Override
    protected Long doInBackground(LoginViewModel... loginViewModels) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(LoginViewModel loginViewModel : loginViewModels){
            loginViewModel.login();
        }
        return 100L;
    }

    @Override
    protected void onPostExecute(Long result) {

    }
}
