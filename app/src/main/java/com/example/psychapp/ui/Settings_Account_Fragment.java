package com.example.psychapp.ui;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.psychapp.QuestionnaireActivity;
import com.example.psychapp.R;
import com.example.psychapp.ui.login.LoginActivity;

public class Settings_Account_Fragment extends Fragment {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_account_fragment, container, false);

        Button logoutButton = root.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            LoginActivity.clearInfo();
            getActivity().finishAffinity();
            QuestionnaireActivity.setEnabled(false);
            }
        });

        return root;
    }
}