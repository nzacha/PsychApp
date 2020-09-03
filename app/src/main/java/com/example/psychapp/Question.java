package com.example.psychapp;


import org.json.JSONArray;

import java.io.Serializable;

import static com.example.psychapp.Question.QuestionType.*;

public class Question implements Serializable {
    public static final String VERTICAL = "Vertical", HORIZONTAL = "Horizontal";
    public static final int View_ID = 100;
    public Question(String question, QuestionType type, JSONArray options) {
    }

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
    public Question(int id, String question, String[] options, String orientation, boolean requesReason){
        this.id = id;
        this.question = question;
        if(orientation.equals(VERTICAL)) {
            this.type = MULTIPLE_CHOICE;
        } else if (orientation.equals(HORIZONTAL)){
            this.type = MULTIPLE_CHOICE_HORIZONTAL;
        }
        this.answer = "" + 0;
        this.options = options;
        this.requestReason = requesReason;
    }

    //constructor for slider
    public Question(int id, String question, QuestionType type, int level){
        this.id = id;
        this.question = question;
        this.level = level;
        this.type = type;
    }

    public String toString(){
        return id+") "+question+": "+type.name()+":"+level+" = "+answer;
    }
}