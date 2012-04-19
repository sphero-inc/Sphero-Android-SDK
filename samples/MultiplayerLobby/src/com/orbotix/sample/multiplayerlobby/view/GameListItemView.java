package com.orbotix.sample.multiplayerlobby.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.orbotix.sample.multiplayerlobby.R;

/**
 * Created by Orbotix Inc.
 * Date: 4/17/12
 *
 * @author Adam Williams
 */
public class GameListItemView extends RelativeLayout {

    private final TextView mTextView;
    
    public GameListItemView(Context context) {
        this(context, null);
    }

    public GameListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.games_list_item, this);

        mTextView = (TextView)findViewById(R.id.text);
        
        if(attrs != null){

        }
    }

    /**
     * Sets the text of this view
     * @param text
     */
    public void setText(String text){
        mTextView.setText(text);
    }
    
}
