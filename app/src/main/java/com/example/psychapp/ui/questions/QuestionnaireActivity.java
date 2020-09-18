package com.example.psychapp.ui.questions;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psychapp.ExitActivity;
import com.example.psychapp.data.Exceptions;
import com.example.psychapp.data.LoggedInUser;
import com.example.psychapp.data.Result;
import com.example.psychapp.ui.questions.Question.QuestionType;
import com.example.psychapp.R;
import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.ui.login.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;

import static com.example.psychapp.applications.PsychApp.context;

public class QuestionnaireActivity extends AppCompatActivity {
    public static ArrayList<Question> questions = new ArrayList<Question>();
    private static final String QUESTIONNAIRE_STATE = "Questionnaire_State", QUESTIONS = "Questions", ANSWERS = "Answers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(LoginActivity.user == null){
            try {
                LoginActivity.loadUserInfo();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        PsychApp.clearNotifications();

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            if(extras.getBoolean("notification_origin"))
                updateUserData();
        }
        setContentView(R.layout.activity_questionnaire);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getString(R.string.questionnaire_help), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button sendAnswersButton = (Button) findViewById(R.id.send_answers_button);
        sendAnswersButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(PsychApp.isNetworkConnected(context)) {
                    for (Question question : questions) {
                        Log.d("value", "sent to server: "+question.toString());
                        sendAnswerToServer(question, LoginActivity.user.getUserId());
                    }
                    Log.d("wtf", "answers sent to server");
                } else {
                    try {
                        saveAnswers();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                clearQuestions();
                retrieveQuestions(context, LoginActivity.user.getProjectId());
                PsychApp.clearNotifications();
                ExitActivity.exitApplication(context);
            }
        });

        retrieveQuestions(this, LoginActivity.user.getProjectId());
        /*
        QuizAdapter adapter = new QuizAdapter(this, questions);
        ListView quizQuestionList = findViewById(R.id.quiz_question_list);
        quizQuestionList.setAdapter(adapter);
        */

        LinearLayout quizQuestionList = findViewById(R.id.quiz_question_layout);
        populateList(quizQuestionList, questions);
    }

    private static void sendAnswerToServer(final Question question, int userId){
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(context);
        String url = PsychApp.serverUrl + "answers/" + question.id + "/" + userId;
        Log.d("wtf", PsychApp.serverUrl + "answers/" + question.id + "/" + userId);
        
        Map<String, String> params = new HashMap<>();
        String answer = question.answer;

        switch (question.type){
            case SLIDER:
            case SLIDER_DISCRETE:
                int temp;
                try {
                    temp = Integer.parseInt(answer);
                } catch (NumberFormatException nfe){
                    temp = 0;
                }
                if (question.type == QuestionType.SLIDER_DISCRETE)
                    temp++;
                answer = ""+ (temp);
                break;
            case MULTIPLE_CHOICE:
            case MULTIPLE_CHOICE_HORIZONTAL:
                int index ;
                try {
                    index = Integer.parseInt(answer);
                    answer = question.options[index];
                } catch (NumberFormatException nfe){
                    answer = nfe.getMessage();
                } catch (ArrayIndexOutOfBoundsException nfe){
                    answer = question.hint;
                }
                break;
            case TEXT:
                if(answer == "")
                    answer = "No answer given";
                break;
        }
        params.put("text", ""+answer);
        params.put("progress", ""+LoginActivity.user.getProgress());
        final String finalAnswer = answer;
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("wtf", "Sent to server: "+question);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("wtf", "Answer: "+ finalAnswer + ", gave server response: "+error.toString());
                }
            });

        // add it to the RequestQueue
        queue.add(postRequest);
    }

    public static void retrieveQuestions(Context context, int researcherId){
        if(PsychApp.isNetworkConnected(context)){ // && !questionsExist()
            retrieveQuestionsFromServer(context, researcherId);
        } else {
            try {
                loadQuestions();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean questionsExist(){
        File file = context.getFileStreamPath(QUESTIONS);
        return file.exists();
    }

    private static void retrieveQuestionsFromServer(final Context context, int researcherId){
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(context);
        String url = PsychApp.serverUrl + "questions/" + researcherId;

        // prepare the Request
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>(){
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONArray response) {
                        questions.clear();
                        for( int i=0; i< response.length(); i++){
                            int id = -1;
                            String  question = null, type = null, orientation = null;
                            JSONArray options = null;
                            boolean requestReason = true;
                            int level = 1;
                            try {
                                JSONObject questionObj = response.getJSONObject(i);
                                id = questionObj.getInt("id");
                                question = questionObj.getString("question_text");
                                type = questionObj.getString("question_type").toUpperCase();
                                orientation = questionObj.getString("orientation");
                                requestReason = questionObj.getBoolean("request_reason");
                                if(questionObj.has("question_options"))
                                    options = questionObj.getJSONArray("question_options");
                                if(questionObj.has("levels"))
                                    level = Integer.parseInt(questionObj.get("levels").toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //multiple choice
                            if(options != null && options.length() > 1) {
                                String[] optionsList = new String[options.length()];
                                for(int j = 0; j < options.length(); j++){
                                    try {
                                        optionsList[j] = options.getJSONObject(j).get("option").toString();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                questions.add(new Question(id, question, optionsList, orientation, requestReason));
                            }
                            //sliders
                            else if (type.equals(QuestionType.SLIDER.name()) || type.equals(QuestionType.SLIDER_DISCRETE.name())){
                                questions.add(new Question(id, question, QuestionType.SLIDER, level));
                            }
                            //text
                            else{
                                questions.add(new Question(id, question, QuestionType.TEXT));
                            }
                        }

                        Collections.sort(questions, new QuestionsComparator());

                        try {
                            saveQuestions();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );

        Log.d("wtf","Retrieved questions from server");
        // add it to the RequestQueue
        queue.add(getRequest);
    }

    public static void sendLocalAnswers(int userId) throws IOException, ClassNotFoundException {
        File file = context.getFileStreamPath(ANSWERS);
        if(!file.exists())
            return;
        FileInputStream fis = context.openFileInput(ANSWERS);
        ObjectInputStream is = new ObjectInputStream(fis);
        ArrayList<Question> answers = (ArrayList<Question>) is.readObject();
        is.close();
        fis.close();

        if(answers.size() > 0) {
            Log.d("wtf", "Answers loaded from Phone");

            for (Question question : answers) {
                sendAnswerToServer(question, userId);
            }

            context.deleteFile(ANSWERS);

            Log.d("wtf", "Local answers sent to server");
        }else{
            Log.d("wtf", "No answers found locally");
        }
    }

    private static void saveAnswers() throws IOException, ClassNotFoundException {
        ArrayList<Question> answers = new ArrayList<Question>();
        File file = context.getFileStreamPath(ANSWERS);
        if(file.exists()) {
            FileInputStream fis = context.openFileInput(ANSWERS);
            ObjectInputStream is = new ObjectInputStream(fis);
            answers = (ArrayList<Question>) is.readObject();
            is.close();
            fis.close();
        }
        //Log.d("wtf", "before: "+answers.size());
        for(Question question: questions)
            answers.add(question);
        //Log.d("wtf", "after: "+answers.size());
        FileOutputStream fos = context.openFileOutput(ANSWERS, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(answers);
        os.close();
        fos.close();
        Log.d("wtf", "Answers saved on Phone");
    }

    private static void saveQuestions() throws IOException {
        FileOutputStream fos = context.openFileOutput(QUESTIONS, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(questions);
        os.close();
        fos.close();
        Log.d("wtf", "Questions saved on Phone");
    }

    private static void loadQuestions() throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(QUESTIONS);
        ObjectInputStream is = new ObjectInputStream(fis);
        questions = (ArrayList<Question>) is.readObject();
        is.close();
        fis.close();
        Log.d("wtf", "Questions loaded from Phone");
    }

    public static void clearQuestions(){
        for(Question question: questions){
            question.answer = "";
            question.hint ="";
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(QUESTIONNAIRE_STATE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("status", false);
        editor.apply();
    }

    public static void setEnabled(boolean val){
        SharedPreferences sharedPreferences = context.getSharedPreferences(QUESTIONNAIRE_STATE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("status", val);
        editor.commit();
        editor.apply();
    }

    public static boolean isActive(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(QUESTIONNAIRE_STATE, context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("status", false);
    }

    private void populateList(LinearLayout layout, ArrayList<Question> questions){
        int index = 0;
        for(Question question: questions){
            QuestionView questionView = new QuestionView(PsychApp.context);
            questionView.inflateInto(layout, question, index++);
        }
    }

    private void updateUserData(){
        if(!PsychApp.isNetworkConnected(context))
            return;
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(PsychApp.context);
        String url = PsychApp.serverUrl + "users/" + LoginActivity.user.getCode();

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            QuestionnaireActivity.sendLocalAnswers(Integer.parseInt(response.get("id").toString()));
                            JSONObject project = response.getJSONObject("project");
                            LoginActivity.user = new LoggedInUser(Integer.parseInt(response.get("id").toString()), response.get("name").toString(), response.getInt("projectId"), project.getInt("study_length"), project.getInt("tests_per_day"), project.getInt("tests_time_interval"), project.getBoolean("allow_individual_times"), project.getBoolean("allow_user_termination"), project.getBoolean("automatic_termination"), response.getInt("progress"), response.getString("code"), response.getBoolean("isActive"));
                            if(!LoginActivity.user.isActive()) {
                                setEnabled(false);
                                clearQuestions();
                                LoginActivity.clearInfo();
                                finishAffinity();
                            }
                        } catch (JSONException e) {
                            try {
                                throw new IOException("Error with JSONObject", e);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        switch(error.networkResponse.statusCode){
                            case 400:
                                try {
                                    throw new Exceptions.UserInactive();
                                } catch (Exceptions.UserInactive userInactive) {
                                    userInactive.printStackTrace();
                                }
                                break;
                            case 404:
                                try {
                                    throw new Exceptions.InvalidCredentials();
                                } catch (Exceptions.InvalidCredentials invalidCredentials) {
                                    invalidCredentials.printStackTrace();
                                }
                                break;
                            default:
                                Log.d("wtf",error.networkResponse.statusCode+": "+error.networkResponse.data);
                                try {
                                    throw new Exception();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                });

        // add it to the RequestQueue
        queue.add(postRequest);
    }

    class QuizAdapter extends ArrayAdapter<Question>{
        public QuizAdapter(@NonNull Context context, ArrayList<Question> questions) {
            super(context, 0, questions);
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent){
            //This is a bad implementation but at least i only have to make one adapter :)

            final Question question = getItem(position);

            switch(question.type) {
                case TEXT:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_question, parent, false);

                    EditText answer = (EditText) convertView.findViewById(R.id.quiz_question_message);
                    answer.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            question.answer = editable.toString();
                        }
                    });
                    answer.setText(question.answer);
                    break;
                case SLIDER:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_question_slider, parent, false);

                    SeekBar bar = (SeekBar) convertView.findViewById(R.id.question_seekbar);
                    bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            question.answer = ""+i;
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    bar.setMax(100);

                    int progress;
                    try {
                        progress = Integer.parseInt(question.answer);
                    } catch (NumberFormatException nfe){
                        progress = 0;
                    }
                    bar.setProgress(progress);

                    break;
                case SLIDER_DISCRETE:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_question_slider_discrete, parent, false);

                    SeekBar bar2 = (SeekBar) convertView.findViewById(R.id.question_seekbar);
                    bar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            question.answer = ""+i;
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    bar2.setMax(question.level-1);

                    int progress2;
                    try {
                        progress2 = Integer.parseInt(question.answer);
                    } catch (NumberFormatException nfe){
                        progress2 = 0;
                    }
                    bar2.setProgress(progress2);
                    break;
                case MULTIPLE_CHOICE:
                case MULTIPLE_CHOICE_HORIZONTAL:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_question_multiple_choice, parent, false);

                    RadioGroup radioGroup = (RadioGroup) convertView.findViewById(R.id.choice_group);
                    if(question.type == QuestionType.MULTIPLE_CHOICE_HORIZONTAL)
                        radioGroup.setOrientation(LinearLayout.HORIZONTAL);

                    for(String option: question.options) {
                        RadioButton button = new RadioButton(context);
                        button.setText(option);
                        if(question.type == QuestionType.MULTIPLE_CHOICE_HORIZONTAL) {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                            button.setLayoutParams(params);
                        }
                        radioGroup.addView(button);
                    }

                    if(question.requestReason) {
                        RadioButton button = new RadioButton(context);
                        button.setText(getString(R.string.request_other));
                        if (question.type == QuestionType.MULTIPLE_CHOICE_HORIZONTAL) {
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                            button.setLayoutParams(params);
                        }
                        radioGroup.addView(button);
                    }

                    final TextView reasoning = convertView.findViewById(R.id.reasoning_input);
                    Log.d("questions", "question adapter before: "+question.answer);
                    try {
                        radioGroup.check(radioGroup.getChildAt(Integer.parseInt(question.answer)).getId());
                        if (question.requestReason && Integer.parseInt(question.answer) == radioGroup.getChildCount()-1){
                            reasoning.setVisibility(View.VISIBLE);
                        } else {
                            reasoning.setVisibility(View.GONE);
                        }
                    } catch (NumberFormatException nfe) {
                        question.answer = "" + 0;
                        radioGroup.check(radioGroup.getChildAt(Integer.parseInt(question.answer)).getId());
                    }
                    Log.d("questions", "question adapter after: "+question.answer);

                    if(question.requestReason) {
                        if (question.hint != null && !question.hint.equals(""))
                            reasoning.setText(question.hint);
                        reasoning.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }
                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }
                            @Override
                            public void afterTextChanged(Editable editable) {
                                question.hint = editable.toString();
                            }
                        });
                    }

                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            int index = radioGroup.indexOfChild(radioGroup.findViewById(i));
                            question.answer = ""+index;
                            if (question.requestReason && index == radioGroup.getChildCount()-1){
                                reasoning.setVisibility(View.VISIBLE);
                            } else {
                                reasoning.setVisibility(View.GONE);
                            }
                            notifyDataSetChanged();
                            Log.d("questions", "check listener: "+index);
                        }
                    });
                    break;
                default:
                    throw new InputMismatchException();
            }

            TextView title = (TextView) convertView.findViewById(R.id.quiz_question_title);
            title.setText(question.question);
            return convertView;
        }
    }
}