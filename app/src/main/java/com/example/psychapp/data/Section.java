package com.example.psychapp.data;

import java.io.Serializable;
import java.util.ArrayList;

public class Section implements Serializable {
    public ArrayList<Question> questions = new ArrayList<Question>();
    public String name;
    public String description;

    public Section(String name, String description){
        this.name = name;
        this.description = description;
    }

    public void add(Question question){
        questions.add(question);
    }
}
