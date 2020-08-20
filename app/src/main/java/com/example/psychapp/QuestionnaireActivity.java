package com.example.psychapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ExitActivity;
import com.example.psychapp.Question.QuestionType;
import com.example.psychapp.ui.login.LoginActivity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;

import static com.example.psychapp.PsychApp.context;

public class QuestionnaireActivity extends AppCompatActivity {
    private static ArrayList<Question> questions = new ArrayList<Question>();
    private static final String QUESTIONNAIRE_STATE = "Questionnaire_State", QUESTIONS = "Questions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        //toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Answer the questions and press send to finish", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button sendAnswersButton = (Button) findViewById(R.id.send_answers_button);
        sendAnswersButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                for(Question question: questions) {
                    sendAnswerToServer(question, PsychApp.userId);
                }
                clearQuestions();
                ExitActivity.exitApplication(PsychApp.context);
            }
        });

        if(PsychApp.userId == LoginActivity.CODE_UNAVAILABLE)
            LoginActivity.loadUserData();
        retrieveQuestions(this, PsychApp.researcherId);
        QuizAdapter adapter = new QuizAdapter(this, questions);
        ListView quizQuestionList = findViewById(R.id.quiz_question_list);
        quizQuestionList.setAdapter(adapter);
    }

    private void sendAnswerToServer(Question question, int userId){
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = PsychApp.serverUrl + "answers/" + question.id + "/" + userId;

        Map<String, String> params = new HashMap<>();
        String answer = null;
        switch(question.type) {
            case TEXT:
                EditText questionText = findViewById(R.id.quiz_question_message);
                answer = questionText.getText().toString();
                break;
            case SLIDER:
            case SLIDER_DISCRETE:
                SeekBar seekbar = findViewById(R.id.question_seekbar);
                answer = ""+seekbar.getProgress();
                break;
            case MULTIPLE_CHOICE:
                RadioGroup radioGroup = findViewById(R.id.choice_group);
                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(selectedId);
                answer = ""+radioButton.getText();
                break;
            default:
                throw new InputMismatchException();
        }
        params.put("text", answer);

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });

        // add it to the RequestQueue
        queue.add(postRequest);
    }

    public static void retrieveQuestions(Context context, int researcherId){
        if(isNetworkConnected(context)){
            retrieveQuestionsFromServer(context, researcherId);
        } else {
            Log.d("wtf","Internet connection not available");
            try {
                loadQuestions();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static void retrieveQuestionsFromServer(Context context, int researcherId){
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(context);
        String url = PsychApp.serverUrl + "questions/" + researcherId;

        // prepare the Request
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        for( int i=0; i< response.length(); i++){
                            int id = -1;
                            String  question = null;
                            String type = null;
                            JSONArray options = null;
                            int level = 1;
                            try {
                                JSONObject questionObj = response.getJSONObject(i);
                                id = Integer.parseInt(questionObj.get("id").toString());
                                question = questionObj.get("question_text").toString();
                                type = questionObj.get("question_type").toString().toUpperCase();
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
                                questions.add(new Question(id, question, optionsList));
                            }//sliders
                            else if (type.equals(QuestionType.SLIDER.name()) || type.equals(QuestionType.SLIDER_DISCRETE.name())){
                                if(level == 1) {
                                    questions.add(new Question(id, question, QuestionType.SLIDER, level));
                                }else{
                                    questions.add(new Question(id, question, QuestionType.SLIDER_DISCRETE, level));
                                }
                            }//text
                            else{
                                questions.add(new Question(id, question, QuestionType.TEXT));
                            }
                        }
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

        // add it to the RequestQueue
        queue.add(getRequest);
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
        questions.clear();

        SharedPreferences sharedPreferences = context.getSharedPreferences(QUESTIONNAIRE_STATE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("status", false);
        editor.apply();

        try {
            saveQuestions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enable(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(QUESTIONNAIRE_STATE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("status", true);
        editor.commit();
        editor.apply();
    }

    public static boolean isActive(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(QUESTIONNAIRE_STATE, context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("status", false);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    class QuizAdapter extends ArrayAdapter<Question>{
        public QuizAdapter(@NonNull Context context, ArrayList<Question> questions) {
            super(context, 0, questions);
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent){
            final Question question = getItem(position);

            switch(question.type) {
                case TEXT:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_question, parent, false);
                    EditText message = convertView.findViewById(R.id.quiz_question_message);
                    message.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void afterTextChanged(Editable s) {
                            question.answer = s.toString();
                        }
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    });
                    message.setText(question.answer);
                    break;
                case SLIDER:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_question_slider, parent, false);
                    SeekBar continuousSeekBar = convertView.findViewById(R.id.question_seekbar);
                    continuousSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            question.answer = ""+i;
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                    continuousSeekBar.setMax(100);
                    try {
                        continuousSeekBar.setProgress(Integer.parseInt(question.answer));
                    } catch (NumberFormatException nfe) {
                        question.answer = "" + 0;
                        continuousSeekBar.setProgress(Integer.parseInt(question.answer));
                    }
                    break;
                case SLIDER_DISCRETE:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_question_slider_discrete, parent, false);
                    SeekBar discreteSeekBar = convertView.findViewById(R.id.question_seekbar);
                    discreteSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            question.answer = ""+i;
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
                    discreteSeekBar.setMax(question.level-1);
                    try {
                        discreteSeekBar.setProgress(Integer.parseInt(question.answer));
                    } catch (NumberFormatException nfe) {
                        question.answer = "" + 0;
                        discreteSeekBar.setProgress(Integer.parseInt(question.answer));
                    }
                    break;
                case MULTIPLE_CHOICE:
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_question_multiple_choice, parent, false);
                    RadioGroup optionsGroup = convertView.findViewById(R.id.choice_group);
                    optionsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            RadioButton radio = radioGroup.findViewById(i);
                            question.answer = "" + radioGroup.indexOfChild(radio);
                        }
                    });
                    for(int i=0; i < question.options.length; i++){
                        RadioButton newButton = new RadioButton( getContext());
                        newButton.setText(question.options[i]);
                        optionsGroup.addView(newButton);
                    }
                    try {
                        optionsGroup.check(optionsGroup.getChildAt(Integer.parseInt(question.answer)).getId());
                    } catch (NumberFormatException nfe) {
                        question.answer = "" + 0;
                        optionsGroup.check(optionsGroup.getChildAt(Integer.parseInt(question.answer)).getId());
                    }
                    break;
                default:
                    throw new InputMismatchException();
            }

            TextView title = convertView.findViewById(R.id.quiz_question_title);
            title.setText(question.question);
            return convertView;
        }
    }
}