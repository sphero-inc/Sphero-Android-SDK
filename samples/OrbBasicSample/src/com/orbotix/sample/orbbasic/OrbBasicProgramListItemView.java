package com.orbotix.sample.orbbasic;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Orbotix Inc.
 * Date: 4/17/12
 *
 * @author Mike DePhillips
 */
public class OrbBasicProgramListItemView extends RelativeLayout {

    private final TextView mTextView;

    public OrbBasicProgramListItemView(Context context) {
        this(context, null);
    }

    public OrbBasicProgramListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.orbbasic_list_item_view, this);

        mTextView = (TextView)findViewById(R.id.txt_title);

        if(attrs != null){}
    }

    /**
     * Sets the text of this view
     * @param text
     */
    public void setText(String text){
        mTextView.setText(text);
    }
    
}
