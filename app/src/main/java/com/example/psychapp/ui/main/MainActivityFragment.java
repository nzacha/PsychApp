package com.example.psychapp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.psychapp.QuestionnaireActivity;
import com.example.psychapp.R;
import com.example.psychapp.SettingsActivity;
import com.example.psychapp.SettingsTabbedActivity;
import com.example.psychapp.ui.login.ConsentActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public static MainActivityFragment newInstance(int index) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
        */
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        Button introduction = root.findViewById(R.id.introduction_button);
        introduction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openConsentActivity();
            }
        });

        Button questionnaireButton = root.findViewById(R.id.quiz_button);
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
                System.exit(0);
            }
        });

        return root;
    }

    public void openConsentActivity(){
        Intent intent = new Intent(getActivity(), ConsentActivity.class);
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