package com.example.psychapp.ui.questions;

public class QuestionsComparator implements java.util.Comparator<Question> {

    @Override
    public int compare(Question question, Question t1) {
        return question.id - t1.id;
    }
}
