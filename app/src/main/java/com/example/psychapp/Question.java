package com.example.psychapp;


import java.io.Serializable;

import static com.example.psychapp.Question.QuestionType.MULTIPLE_CHOICE;
import static com.example.psychapp.Question.QuestionType.MULTIPLE_CHOICE_HORIZONTAL;
import static com.example.psychapp.Question.QuestionType.SLIDER;
import static com.example.psychapp.Question.QuestionType.SLIDER_DISCRETE;
import static com.example.psychapp.Question.QuestionType.TEXT;

public class Question implements Serializable{
    public static final String VERTICAL = "Vertical", HORIZONTAL = "Horizontal";

    public enum QuestionType{
        TEXT,
        SLIDER,
        SLIDER_DISCRETE,
        MULTIPLE_CHOICE,
        MULTIPLE_CHOICE_HORIZONTAL,
    }

    public int id;
    public String question = "Placeholder Question";
    public String answer = "", hint = null;
    public QuestionType type = TEXT;
    public String[] options;
    public int level;
    public boolean requestReason;

    public Question(int id, String question, QuestionType type){
        this.id = id;
        this.question = question;
        this.type = type;
    }

    //constructor for multiple choice questions
    public Question(int id, String question, String[] options, String orientation, boolean requestReason){
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
    }

    //constructor for slider
    public Question(int id, String question, QuestionType type, int level){
        this.id = id;
        this.question = question;
        this.level = level;
        if(level <= 1){
            this.type =  SLIDER;
        }else{
            this.type =  SLIDER_DISCRETE;
        }
    }

    public String toString(){
        return id+") "+question+": "+type.name()+":"+level+" = "+answer+".";
    }
}