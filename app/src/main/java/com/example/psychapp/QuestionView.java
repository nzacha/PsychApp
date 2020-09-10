package com.example.psychapp;


import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.Serializable;
import java.util.InputMismatchException;

import static com.example.psychapp.Question.QuestionType.MULTIPLE_CHOICE;
import static com.example.psychapp.Question.QuestionType.MULTIPLE_CHOICE_HORIZONTAL;
import static com.example.psychapp.Question.QuestionType.SLIDER;
import static com.example.psychapp.Question.QuestionType.SLIDER_DISCRETE;
import static com.example.psychapp.Question.QuestionType.TEXT;

public class QuestionView extends View{
    private Context context;

    public QuestionView(Context context){
        super(context);
        this.context = context;
    }

    public void inflateInto(ViewGroup parent,Question question, int index){
        init(parent, question, index);
    }

    private void init(ViewGroup parent, final Question question, final int index){
        ViewGroup inflatedViewRoot;
        View inflatedView;
        switch(question.type) {
            case TEXT:
                inflatedViewRoot = (ViewGroup) parent.inflate(context, R.layout.quiz_question, parent);
                inflatedView = inflatedViewRoot.getChildAt(index);

                EditText answerView = (EditText) inflatedView.findViewById(R.id.quiz_question_message);
                answerView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                        QuestionnaireActivity.questions.get(index).answer = editable.toString();
                        //Log.d("value", "text value changed to "+ question.answer);
                    }
                });
                break;
            case SLIDER:
                inflatedViewRoot = (ViewGroup) parent.inflate(context, R.layout.quiz_question_slider, parent);
                inflatedView = inflatedViewRoot.getChildAt(index);

                SeekBar bar = (SeekBar) inflatedView.findViewById(R.id.question_seekbar);
                bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        QuestionnaireActivity.questions.get(index).answer = ""+i;
                        //Log.d("value", "slider value changed to "+ question.answer);
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
                break;
            case SLIDER_DISCRETE:
                inflatedViewRoot = (ViewGroup) parent.inflate(context, R.layout.quiz_question_slider_discrete, parent);
                inflatedView = inflatedViewRoot.getChildAt(index);

                SeekBar bar_discrete = (SeekBar) inflatedView.findViewById(R.id.question_seekbar_discrete);
                bar_discrete.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        QuestionnaireActivity.questions.get(index).answer = ""+i;
                        //Log.d("value", "discrete slider value changed to "+ question.answer);
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
                bar_discrete.setMax(question.level-1);
                break;
            case MULTIPLE_CHOICE:
            case MULTIPLE_CHOICE_HORIZONTAL:
                inflatedViewRoot = (ViewGroup) parent.inflate(context, R.layout.quiz_question_multiple_choice, parent);
                inflatedView = inflatedViewRoot.getChildAt(index);

                RadioGroup radioGroup = (RadioGroup) inflatedView.findViewById(R.id.choice_group);
                if(question.type == Question.QuestionType.MULTIPLE_CHOICE_HORIZONTAL)
                    radioGroup.setOrientation(LinearLayout.HORIZONTAL);

                for(String option: question.options) {
                    RadioButton button = new RadioButton(context);
                    button.setText(option);
                    if(question.type == Question.QuestionType.MULTIPLE_CHOICE_HORIZONTAL) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                        button.setLayoutParams(params);
                    }
                    radioGroup.addView(button);
                }

                if(question.requestReason) {
                    RadioButton button = new RadioButton(context);
                    button.setText(context.getString(R.string.request_other));
                    if (question.type == Question.QuestionType.MULTIPLE_CHOICE_HORIZONTAL) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                        button.setLayoutParams(params);
                    }
                    radioGroup.addView(button);
                }

                final TextView reasoning = inflatedView.findViewById(R.id.reasoning_input);
                radioGroup.check(radioGroup.getChildAt(0).getId());
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
                            QuestionnaireActivity.questions.get(index).hint = editable.toString();
                            //Log.d("value", "hint value changed to "+ question.hint);
                        }
                    });
                }

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        int radioIndex = radioGroup.indexOfChild(radioGroup.findViewById(i));
                        QuestionnaireActivity.questions.get(index).answer = ""+radioIndex;
                        //Log.d("value", "choice value changed to "+ question.answer);
                        if (question.requestReason && radioIndex == radioGroup.getChildCount()-1){
                            reasoning.setVisibility(View.VISIBLE);
                        } else {
                            reasoning.setVisibility(View.GONE);
                        }
                        Log.d("questions", "check listener: "+radioIndex);
                    }
                });
                break;
            default:
                throw new InputMismatchException();
        }

        TextView title = (TextView) inflatedView.findViewById(R.id.quiz_question_title);
        title.setText(question.question);
    }
}