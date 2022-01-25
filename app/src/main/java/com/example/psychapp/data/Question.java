package com.example.psychapp.data;


import java.io.Serializable;
import java.util.Calendar;


public class Question implements Serializable{
    public static final String VERTICAL = "Vertical", HORIZONTAL = "Horizontal";

    public enum QuestionType{
        Text,
        Slider,
        Slider_Discrete,
        Multiple_Choice,
        Multiple_Choice_Horizontal,
    }

    public int id, userId, index;
    public String question = "Placeholder Question";
    public String answer = "", hint = null;
    public QuestionType type = QuestionType.Text;
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
            this.type = QuestionType.Multiple_Choice;
        } else if (orientation.equals(HORIZONTAL)){
            this.type = QuestionType.Multiple_Choice_Horizontal;
        }
        this.answer = "" + 0;
        this.options = options;
        this.requestReason = requestReason;
        this.date = Calendar.getInstance();
    }

    //constructor for slider
    public Question(int userId, int index, int id, String question, QuestionType type, String[] options){
        this.userId = userId;
        this.index = index;
        this.id = id;
        this.question = question;
        this.options = options;
        this.level = options.length;
        if(level <= 1){
            this.type = QuestionType.Slider;
        }else{
            this.type = QuestionType.Slider_Discrete;
        }
        this.date = Calendar.getInstance();
    }

    public String toString(){
        return "["+id+") "+question+": "+type.name()+":"+level+" = "+answer+"]";
    }
}