package com.example.psychapp.data;


import java.io.Serializable;
import java.util.Calendar;

import static com.example.psychapp.data.Question.QuestionType.MULTIPLE_CHOICE;
import static com.example.psychapp.data.Question.QuestionType.MULTIPLE_CHOICE_HORIZONTAL;
import static com.example.psychapp.data.Question.QuestionType.SLIDER;
import static com.example.psychapp.data.Question.QuestionType.SLIDER_DISCRETE;
import static com.example.psychapp.data.Question.QuestionType.TEXT;

public class Question implements Serializable{
    public static final String VERTICAL = "Vertical", HORIZONTAL = "Horizontal";

    public enum QuestionType{
        TEXT,
        SLIDER,
        SLIDER_DISCRETE,
        MULTIPLE_CHOICE,
        MULTIPLE_CHOICE_HORIZONTAL,
    }

    public int id, userId, index;
    public String question = "Placeholder Question";
    public String answer = "", hint = null;
    public QuestionType type = TEXT;
    public Calendar date;
    public String[] options;
    public int level;
    public boolean requestReason, missing=false;

    public Question(int userId, int index, int id, String question, QuestionType type){
        this.userId = userId;
        this.index = index;
        this.id = id;
        this.question = question;
        this.type = type;
        this.date = Calendar.getInstance();
    }

    //constructor for multiple choice questions
    public Question(int userId, int index, int id, String question, String[] options, String orientation, boolean requestReason){
        this.userId = userId;
        this.index = index;
        this.id = id;
        this.question = question;
        if(orientation.equals(VERTICAL)) {
            this.type = MULTIPLE_CHOICE;
        } else if (orientation.equals(HORIZONTAL)){
            this.type = MULTIPLE_CHOICE_HORIZONTAL;
        }
        this.answer = "" + 0;
        this.options = options;
        this.requestReason = requestReason;
        this.date = Calendar.getInstance();
    }

    //constructor for slider
    public Question(int userId, int index, int id, String question, QuestionType type, int level){
        this.userId = userId;
        this.index = index;
        this.id = id;
        this.question = question;
        this.level = level;
        if(level <= 1){
            this.type =  SLIDER;
        }else{
            this.type =  SLIDER_DISCRETE;
        }
        this.date = Calendar.getInstance();
    }

    public String toString(){
        return id+") "+question+": "+type.name()+":"+level+" = "+answer+".";
    }
}