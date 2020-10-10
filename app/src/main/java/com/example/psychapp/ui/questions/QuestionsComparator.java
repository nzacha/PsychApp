package com.example.psychapp.ui.questions;

import com.example.psychapp.data.Question;

public class QuestionsComparator implements java.util.Comparator<Question> {

    @Override
    public int compare(Question question, Question t1) {
        return question.id - t1.id;
    }
}
