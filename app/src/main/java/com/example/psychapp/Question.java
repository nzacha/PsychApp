package com.example.psychapp;

public class Question {
    public String question;
    public String answer;

    public Question(String question, String answer){
        this.question = question;
        this.answer = answer;
    }

    public String toString(){
        return question+": "+answer;
    }
}
