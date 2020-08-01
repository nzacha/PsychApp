package com.example.psychapp;

import org.json.JSONArray;

public class Question {
    public static final String Placeholder = "Type your answer here";

    public Question(String question, QuestionType type, JSONArray options) {
    }

    public enum QuestionType{
        TEXT,
        SLIDER,
        SLIDER_DISCRETE,
        MULTIPLE_CHOICE
    }

    public int id;
    public String question = "Placeholder Question";
    public QuestionType type = QuestionType.TEXT;
    public String[] options;
    public int level;

    public Question(int id, String question, QuestionType type){
        this.id = id;
        this.question = question;
        this.type = type;
    }

    //constructor for multiple choice questions
    public Question(int id, String question, String[] options){
        this.id = id;
        this.question = question;
        this.type = QuestionType.MULTIPLE_CHOICE;
        this.options = options;
    }

    //constructor for slider
    public Question(int id, String question, QuestionType type, int level){
        this.id = id;
        this.question = question;
        this.level = level;
        this.type = type;
    }

    public String toString(){
        return question+": "+type.name();
    }
}