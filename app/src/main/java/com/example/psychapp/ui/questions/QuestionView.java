package com.example.psychapp.ui.questions;


import android.content.Context;
import android.opengl.Visibility;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.psychapp.R;
import com.example.psychapp.data.Question;

import java.util.InputMismatchException;

public class QuestionView extends View{
    private Context context;

    public QuestionView(Context context){
        super(context);
        this.context = context;
    }

    public void inflateInto(ViewGroup parent, Question question, int s_index, int q_index, int t_index){
        init(parent, question, s_index, q_index, t_index);
    }

    private void init(ViewGroup parent, final Question question, final int s_index, final int q_index, final int t_index){
        ViewGroup inflatedViewRoot;
        View inflatedView;
        switch(question.type) {
            case Text:
                inflatedViewRoot = (ViewGroup) parent.inflate(context, R.layout.quiz_question, parent);
                inflatedView = inflatedViewRoot.getChildAt(t_index);

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
                        question.answer = editable.toString();
                        //Log.d("value", "text value changed to "+ question.answer);
                    }
                });
                break;
            case Slider:
                inflatedViewRoot = (ViewGroup) parent.inflate(context, R.layout.quiz_question_slider, parent);
                inflatedView = inflatedViewRoot.getChildAt(t_index);

                SeekBar bar = (SeekBar) inflatedView.findViewById(R.id.question_seekbar);
                final TextView marker = (TextView) inflatedView.findViewById(R.id.marker);
                bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        question.answer = ""+i;
                        marker.setText(i+"%");
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
            case Slider_Discrete:
                inflatedViewRoot = (ViewGroup) parent.inflate(context, R.layout.quiz_question_slider_discrete, parent);
                inflatedView = inflatedViewRoot.getChildAt(t_index);

                String[] options = question.options;
                final LinearLayout optionLabels = (LinearLayout) inflatedView.findViewById(R.id.options);
                optionLabels.setWeightSum(options.length);
                for(int i=0; i<options.length; i++){
                    TextView view = new TextView(context);
                    view.setText(options[i]);
                    view.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1
                    ));
                    view.setVisibility(INVISIBLE);
                    view.setGravity(Gravity.CENTER);
                    optionLabels.addView(view);
                }
                SeekBar bar_discrete = (SeekBar) inflatedView.findViewById(R.id.question_seekbar_discrete);
                if(options.length > 2) {
                    TextView left = (TextView) inflatedView.findViewById(R.id.slider_left);
                    left.setText(options[0]);
                    TextView right = (TextView) inflatedView.findViewById(R.id.slider_right);
                    right.setText(options[options.length -1]);

                }
                bar_discrete.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        question.answer = ""+i;
                        for(int j=0; j<optionLabels.getChildCount(); j++){
                            View view = optionLabels.getChildAt(j);
                            if(i!=j) view.setVisibility(INVISIBLE);
                            else view.setVisibility(VISIBLE);
                        }
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
            case Multiple_Choice:
            case Multiple_Choice_Horizontal:
                inflatedViewRoot = (ViewGroup) parent.inflate(context, R.layout.quiz_question_multiple_choice, parent);
                inflatedView = inflatedViewRoot.getChildAt(t_index);

                RadioGroup radioGroup = (RadioGroup) inflatedView.findViewById(R.id.choice_group);
                if(question.type == Question.QuestionType.Multiple_Choice_Horizontal)
                    radioGroup.setOrientation(LinearLayout.HORIZONTAL);

                for(int i=question.options.length-1; i>=0; i--){
                    RadioButton button = new RadioButton(context);
                    button.setText(question.options[i]);
                    if(question.type == Question.QuestionType.Multiple_Choice_Horizontal) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                        button.setLayoutParams(params);
                    }
                    radioGroup.addView(button);
                }

                if(question.requestReason) {
                    RadioButton button = new RadioButton(context);
                    button.setText(context.getString(R.string.request_other));
                    if (question.type == Question.QuestionType.Multiple_Choice_Horizontal) {
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
                            question.hint = editable.toString();
                            //Log.d("value", "hint value changed to "+ question.hint);
                        }
                    });
                }

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        int radioIndex = radioGroup.indexOfChild(radioGroup.findViewById(i));
                        question.answer = ""+radioIndex;
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