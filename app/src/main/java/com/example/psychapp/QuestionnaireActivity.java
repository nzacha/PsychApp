package com.example.psychapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class QuestionnaireActivity extends AppCompatActivity {
    String[] titles = {"Are you happy?", "Are you sad?", "Do you like dancing?", "Do you sing?", "Do you play poker?", "Do you speak English?", "Do you speak French?", "how is the weather like?"};
    String[] messages = {"Yes", "No", "Yes", "Yes", "No", "Yes", "No", "I don't know"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        ArrayList<Question> questions = new ArrayList<Question>();
        QuizAdapter adapter = new QuizAdapter(this, questions);
        ListView quizQuestionList = findViewById(R.id.quiz_question_list);
        quizQuestionList.setAdapter(adapter);
        for(int i=0; i<titles.length; i++){
            questions.add(new Question(titles[i],messages[i]));
            Log.d("wtf", "help?:"+i);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    class QuizAdapter extends ArrayAdapter<Question>{
        public QuizAdapter(@NonNull Context context, ArrayList<Question> questions) {
            super(context, 0, questions);
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent){

            Question question = getItem(position);
            Log.i("wtf",""+question+":"+position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.quiz_question, parent, false);
            }

            TextView title = convertView.findViewById(R.id.quiz_question_title);
            TextView message = convertView.findViewById(R.id.quiz_question_message);

            title.setText(titles[position]);
            message.setText(messages[position]);

            return convertView;
        }
    }
}