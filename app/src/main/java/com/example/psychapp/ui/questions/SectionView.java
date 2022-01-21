package com.example.psychapp.ui.questions;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.psychapp.R;
import com.example.psychapp.data.Section;

public class SectionView extends View{
    private Context context;

    public SectionView(Context context){
        super(context);
        this.context = context;
    }

    public void inflateInto(ViewGroup parent, Section section, int s_index, int t_index){
        init(parent, section, s_index, t_index);
    }

    private void init(ViewGroup parent, final Section section, final int s_index, final int t_index){
        ViewGroup inflatedViewRoot = (ViewGroup) parent.inflate(context, R.layout.quiz_section, parent);
        View inflatedView = inflatedViewRoot.getChildAt(t_index);

        TextView title = (TextView) inflatedView.findViewById(R.id.quiz_section_title);
        title.setText(section.name);

        TextView description = (TextView) inflatedView.findViewById(R.id.quiz_section_description);
        description.setText(section.description);
    }
}