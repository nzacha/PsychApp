package com.example.psychapp.ui.questions;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psychapp.ExitActivity;
import com.example.psychapp.data.Question;
import com.example.psychapp.data.Question.QuestionType;
import com.example.psychapp.R;
import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.data.Section;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class QuestionnaireActivity extends AppCompatActivity {
    public static ArrayList<Section> sections = new ArrayList<Section>();
    public static final String QUESTIONNAIRE_STATE = "Questionnaire_State", QUESTIONS = "Questions", ANSWERS = "Answers";

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
                if(PsychApp.isNetworkConnected(PsychApp.getContext())) {
                    for (Section section : sections) {
                        for (Question question : section.questions) {
                            Log.d("value", "sent to server: " + question.toString());
                            sendAnswerToServer(question, Calendar.getInstance());
                        }
                    }
                } else {
                    try {
                        saveAnswers(sections);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                clearQuestions();
                retrieveQuestions(LoginActivity.user.getProjectId());
                PsychApp.clearNotifications();
                ExitActivity.exitApplication(PsychApp.getContext());
            }
        });

        //retrieveQuestions(this, LoginActivity.user.getProjectId());
        /*
        QuizAdapter adapter = new QuizAdapter(this, questions);
        ListView quizQuestionList = findViewById(R.id.quiz_question_list);
        quizQuestionList.setAdapter(adapter);
        */

        LinearLayout quizQuestionList = findViewById(R.id.quiz_question_layout);
//        for(Section s: sections){
//            Log.d("wtf", s.toString());
//        }
        populateList(quizQuestionList, sections);
    }

    public static void sendAnswerToServer(final Question question, Calendar date){
        // Instantiate the RequestQueue.
        String url = PsychApp.serverUrl + "answer/";
        Log.d("info", url);
        
        Map<String, String> params = new HashMap<>();
        String answer = question.answer;

        if(question.missing){
            answer = "No answer given";
        }else {
            switch (question.type) {
                case Slider:
                case Slider_Discrete:
                    int temp;
                    try {
                        temp = Integer.parseInt(answer);
                    } catch (NumberFormatException nfe) {
                        temp = 0;
                    }
                    if (question.type == QuestionType.Slider_Discrete)
                        temp++;
                    answer = "" + (temp);
                    break;
                case Multiple_Choice:
                case Multiple_Choice_Horizontal:
                    int index;
                    try {
                        index = Integer.parseInt(answer);
                        answer = question.options[index];
                    } catch (NumberFormatException nfe) {
                        answer = nfe.getMessage();
                    } catch (ArrayIndexOutOfBoundsException nfe) {
                        answer = question.hint;
                    }
                    break;
                case Text:
                    if (answer == "")
                        answer = "No answer given";
                    break;
            }
        }
        params.put("answer", ""+answer);
        params.put("index", ""+question.index);
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String formatted = simpledateformat.format(date.getTime());
        params.put("date", formatted);
        params.put("question_id", ""+question.id);
        params.put("participant_id", ""+question.userId);

        final String finalAnswer = answer;
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("info", "Sent to server: "+question);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("wtf", "Answer: "+ finalAnswer + ", gave server response: "+error.toString());
                }
            }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-access-token", LoginActivity.user.getToken());
                return params;
            }
        };

        // add it to the RequestQueue
        PsychApp.queue.add(postRequest);
    }

    public static void retrieveQuestions(int projectId){
        if(PsychApp.isNetworkConnected(PsychApp.getContext())){ // && !questionsExist()
            retrieveQuestionsFromServer(projectId);
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
        File file = PsychApp.getContext().getFileStreamPath(QUESTIONS);
        return file.exists();
    }

    private static void retrieveQuestionsFromServer(int projectId){
        // Instantiate the RequestQueue.
        String url = PsychApp.serverUrl + "project/quiz/" + projectId;
        Log.i("wtf", url);
        // prepare the Request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,  null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //make model from data
                    try {
                        sections.clear();
                        JSONObject data = response.getJSONObject("data").getJSONObject("response");
                        Log.d("wtf", data.toString());

                        JSONArray quizSections = data.getJSONArray("quiz_sections");
                        for(int qs=0; qs<quizSections.length(); qs++) {
                            JSONObject quizSection = quizSections.getJSONObject(qs);
                            int qs_id = quizSection.getInt("section_id");
                            //Log.d("wtf", "section: "+qs_id);
                            String qs_name = quizSection.getString("name");
                            String qs_description = quizSection.getString("description");
                            JSONArray quizQuestions = quizSection.getJSONArray("quiz_questions");

                            Section _section = new Section(qs_name, qs_description);
                            for (int qq = 0; qq < quizQuestions.length(); qq++) {
                                JSONObject question = quizQuestions.getJSONObject(qq);
                                int q_id = question.getInt("question_id");
                                //Log.d("wtf", "question: "+q_id);
                                String q_question = question.getString("question");
                                String q_type = question.getString("type");
                                String q_alignment = question.getString("alignment");
                                boolean q_request_reason = question.getBoolean("request_reason");
                                JSONArray question_options = question.getJSONArray("question_options");
                                String[] q_options = new String[question_options.length()];
                                int q_levels = q_options.length;
                                for (int qo = 0; qo < question_options.length(); qo++){
                                    JSONObject question_option = question_options.getJSONObject(qo);
                                    int go_id = question_option.getInt("question_option_id");
                                    //Log.d("wtf", "option: "+go_id);
                                    q_options[qo] = question_option.getString("option");
                                }

                                Question _question;
                                if(q_type.equals(QuestionType.Text.name())) {
                                    if (q_options.length > 0) {
                                        _question = new Question(LoginActivity.user.getUserId(), LoginActivity.user.getProgress(), q_id, q_question, q_options, q_alignment, q_request_reason);
                                    } else {
                                        _question = new Question(LoginActivity.user.getUserId(), LoginActivity.user.getProgress(), q_id, q_question, QuestionType.Text);
                                    }
                                }else{
                                    _question = new Question(LoginActivity.user.getUserId(), LoginActivity.user.getProgress(), q_id, q_question, QuestionType.Slider, q_options);
                                }
                                _section.add(_question);
                            }
                            sections.add(_section);
                        }

                        try {
                            saveQuestions();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Log.d("wtf","Retrieved questions from server");
                    } catch (JSONException e) {
                        Log.e("wtf", e.getMessage());
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("wtf", error.getMessage() != null ? error.getMessage() : error.toString());
                }
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("x-access-token", LoginActivity.user.getToken());
                    return params;
                }
            };
        // add it to the RequestQueue
        PsychApp.queue.add(request);
    }

    public static void sendLocalAnswers() throws IOException, ClassNotFoundException {
        File file = PsychApp.getContext().getFileStreamPath(ANSWERS);
        if(!file.exists())
            return;
        FileInputStream fis = PsychApp.getContext().openFileInput(ANSWERS);
        ObjectInputStream is = new ObjectInputStream(fis);
        ArrayList<Question> answers = (ArrayList<Question>) is.readObject();
        is.close();
        fis.close();

        if(answers.size() > 0) {
            Log.d("wtf", "Answers loaded from Phone ("+answers.size()+")");

            for (Question question : answers) {
                try {
                    sendAnswerToServer(question, question.date);
                    PsychApp.getContext().deleteFile(ANSWERS);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            Log.d("wtf", "Local answers sent to server");
        }else{
            Log.d("wtf", "No answers found locally");
        }
    }

    public static void saveAnswers(ArrayList<Section> sections) throws IOException, ClassNotFoundException {
        ArrayList<Question> answers = new ArrayList<Question>();
        FileInputStream fis;
        ObjectInputStream is;
        if (answersExist()) {
            fis = PsychApp.getContext().openFileInput(ANSWERS);
            is = new ObjectInputStream(fis);
            answers = (ArrayList<Question>) is.readObject();
            is.close();
            fis.close();
        }

        Log.d("wtf", "before: " + answers.size());
        for (Section section : sections){
            for (Question question : section.questions) {
                question.date = Calendar.getInstance();
                question.index = LoginActivity.user.getProgress();
                answers.add(question);
            }
        }
        Log.d("wtf", "after: "+answers.size());
        FileOutputStream fos = PsychApp.getContext().openFileOutput(ANSWERS, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(answers);
        os.close();
        fos.close();
        Log.d("wtf", "Answers saved on Phone");
    }

    public static boolean answersExist(){
        File file = PsychApp.getContext().getFileStreamPath(ANSWERS);
        if(file.exists()) return true;
        return false;
    }

    private static void saveQuestions() throws IOException {
        FileOutputStream fos = PsychApp.getContext().openFileOutput(QUESTIONS, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(sections);
        os.close();
        fos.close();
        Log.d("wtf", "Questions saved on Phone");
    }

    private static void loadQuestions() throws IOException, ClassNotFoundException {
        FileInputStream fis = PsychApp.getContext().openFileInput(QUESTIONS);
        ObjectInputStream is = new ObjectInputStream(fis);
        sections = (ArrayList<Section>) is.readObject();
        is.close();
        fis.close();
        Log.d("wtf", "Sections loaded from Phone (" +sections.size()+")");
    }

    public static void clearQuestions(){
        for(Section section: sections){
            for(Question question: section.questions) {
                question.answer = "";
                question.hint = "";
            }
        }

        SharedPreferences sharedPreferences = PsychApp.getContext().getSharedPreferences(QUESTIONNAIRE_STATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("status", false);
        editor.apply();
    }

    public static void setEnabled(boolean val){
        SharedPreferences sharedPreferences = PsychApp.getContext().getSharedPreferences(QUESTIONNAIRE_STATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("status", val);
        editor.commit();
        editor.apply();
    }

    public static boolean isActive(){
        SharedPreferences sharedPreferences = PsychApp.getContext().getSharedPreferences(QUESTIONNAIRE_STATE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("status", false);
    }

    private void populateList(LinearLayout layout, ArrayList<Section> sections){
        int s_index = 0;
        int q_index = 0;
        int t_index = 0;
        for(Section section: sections) {
            SectionView sectionView = new SectionView(PsychApp.getContext());
            sectionView.inflateInto(layout, section, s_index,  t_index++);
            for (Question question : section.questions) {
                QuestionView questionView = new QuestionView(PsychApp.getContext());
                questionView.inflateInto(layout, question, s_index, q_index++, t_index++);
            }
            s_index++;
        }
    }

//    class QuizAdapter extends ArrayAdapter<Question>{
//        public QuizAdapter(@NonNull Context context, ArrayList<Question> questions) {
//            super(context, 0, questions);
//        }
//
//        @Override
//        public View getView(int position, View convertView, @NonNull ViewGroup parent){
//            //This is a bad implementation but at least i only have to make one adapter :)
//
//            final Question question = getItem(position);
//
//            switch(question.type) {
//                case Text:
//                    convertView = LayoutInflater.from(PsychApp.getContext()).inflate(R.layout.quiz_question, parent, false);
//
//                    EditText answer = (EditText) convertView.findViewById(R.id.quiz_question_message);
//                    answer.addTextChangedListener(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable editable) {
//                            question.answer = editable.toString();
//                        }
//                    });
//                    answer.setText(question.answer);
//                    break;
//                case Slider:
//                    convertView = LayoutInflater.from(PsychApp.getContext()).inflate(R.layout.quiz_question_slider, parent, false);
//
//                    SeekBar bar = (SeekBar) convertView.findViewById(R.id.question_seekbar);
//                    bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                        @Override
//                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                            question.answer = ""+i;
//                        }
//                        @Override
//                        public void onStartTrackingTouch(SeekBar seekBar) {
//                        }
//                        @Override
//                        public void onStopTrackingTouch(SeekBar seekBar) {
//                        }
//                    });
//                    bar.setMax(100);
//
//                    int progress;
//                    try {
//                        progress = Integer.parseInt(question.answer);
//                    } catch (NumberFormatException nfe){
//                        progress = 0;
//                    }
//                    bar.setProgress(progress);
//
//                    break;
//                case Slider_Discrete:
//                    convertView = LayoutInflater.from(PsychApp.getContext()).inflate(R.layout.quiz_question_slider_discrete, parent, false);
//
//                    SeekBar bar2 = (SeekBar) convertView.findViewById(R.id.question_seekbar);
//                    bar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                        @Override
//                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                            question.answer = ""+i;
//                        }
//                        @Override
//                        public void onStartTrackingTouch(SeekBar seekBar) {
//                        }
//                        @Override
//                        public void onStopTrackingTouch(SeekBar seekBar) {
//                        }
//                    });
//                    bar2.setMax(question.level-1);
//
//                    int progress2;
//                    try {
//                        progress2 = Integer.parseInt(question.answer);
//                    } catch (NumberFormatException nfe){
//                        progress2 = 0;
//                    }
//                    bar2.setProgress(progress2);
//                    break;
//                case Multiple_Choice:
//                case Multiple_Choice_Horizontal:
//                    convertView = LayoutInflater.from(PsychApp.getContext()).inflate(R.layout.quiz_question_multiple_choice, parent, false);
//
//                    RadioGroup radioGroup = (RadioGroup) convertView.findViewById(R.id.choice_group);
//                    if(question.type == QuestionType.Multiple_Choice_Horizontal)
//                        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
//
//                    for(String option: question.options) {
//                        RadioButton button = new RadioButton(PsychApp.getContext());
//                        button.setText(option);
//                        if(question.type == QuestionType.Multiple_Choice_Horizontal) {
//                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
//                            button.setLayoutParams(params);
//                        }
//                        radioGroup.addView(button);
//                    }
//
//                    if(question.requestReason) {
//                        RadioButton button = new RadioButton(PsychApp.getContext());
//                        button.setText(getString(R.string.request_other));
//                        if (question.type == QuestionType.Multiple_Choice_Horizontal) {
//                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
//                            button.setLayoutParams(params);
//                        }
//                        radioGroup.addView(button);
//                    }
//
//                    final TextView reasoning = convertView.findViewById(R.id.reasoning_input);
//                    Log.d("questions", "question adapter before: "+question.answer);
//                    try {
//                        radioGroup.check(radioGroup.getChildAt(Integer.parseInt(question.answer)).getId());
//                        if (question.requestReason && Integer.parseInt(question.answer) == radioGroup.getChildCount()-1){
//                            reasoning.setVisibility(View.VISIBLE);
//                        } else {
//                            reasoning.setVisibility(View.GONE);
//                        }
//                    } catch (NumberFormatException nfe) {
//                        question.answer = "" + 0;
//                        radioGroup.check(radioGroup.getChildAt(Integer.parseInt(question.answer)).getId());
//                    }
//                    Log.d("questions", "question adapter after: "+question.answer);
//
//                    if(question.requestReason) {
//                        if (question.hint != null && !question.hint.equals(""))
//                            reasoning.setText(question.hint);
//                        reasoning.addTextChangedListener(new TextWatcher() {
//                            @Override
//                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                            }
//                            @Override
//                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                            }
//                            @Override
//                            public void afterTextChanged(Editable editable) {
//                                question.hint = editable.toString();
//                            }
//                        });
//                    }
//
//                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                        @Override
//                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                            int index = radioGroup.indexOfChild(radioGroup.findViewById(i));
//                            question.answer = ""+index;
//                            if (question.requestReason && index == radioGroup.getChildCount()-1){
//                                reasoning.setVisibility(View.VISIBLE);
//                            } else {
//                                reasoning.setVisibility(View.GONE);
//                            }
//                            notifyDataSetChanged();
//                            Log.d("questions", "check listener: "+index);
//                        }
//                    });
//                    break;
//                default:
//                    throw new InputMismatchException();
//            }
//
//            TextView title = (TextView) convertView.findViewById(R.id.quiz_question_title);
//            title.setText(question.question);
//            return convertView;
//        }
//    }
}