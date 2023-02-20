package com.example.psychapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.psychapp.ExitActivity;
import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.ui.IntroductionActivity;
import com.example.psychapp.ui.login.LoginActivity;
import com.example.psychapp.ui.questions.QuestionnaireActivity;
import com.example.psychapp.R;
import com.example.psychapp.ui.settings.SettingsTabbedActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static MainActivityFragment newInstance(int index) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }


    private Button questionnaireButton;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        EditText welcomeText = root.findViewById(R.id.welcome_text);
        welcomeText.setText(PsychApp.getContext().getString(R.string.welcome_no_questionmark) + ' ' + LoginActivity.user.getCode() + "!");

        Button introduction = root.findViewById(R.id.introduction_button);
        introduction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openIntroductionActivity();
            }
        });

        questionnaireButton = root.findViewById(R.id.quiz_button);
        questionnaireButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openQuestionnaireActivity();
            }
        });

        Button settingsButton = root.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openSettingsActivity();
            }
        });

        Button exitButton = root.findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                ExitActivity.exitApplication(getActivity());
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        questionnaireButton.setEnabled(QuestionnaireActivity.isActive());
    }

    public void openIntroductionActivity(){
        Intent intent = new Intent(getActivity(), IntroductionActivity.class);
        startActivity(intent);
    }

    public void openQuestionnaireActivity(){
        Intent intent = new Intent(getActivity(), QuestionnaireActivity.class);
        startActivity(intent);
    }

    public void openSettingsActivity(){
        Intent intent = new Intent(getActivity(), SettingsTabbedActivity.class);
        startActivity(intent);
    }
}