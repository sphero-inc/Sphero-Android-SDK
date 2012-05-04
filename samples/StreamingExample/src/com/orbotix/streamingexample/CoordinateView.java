package com.orbotix.streamingexample;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Orbotix Inc.
 * Date: 4/30/12
 *
 * @author Adam Williams
 */
public class CoordinateView extends RelativeLayout {

    private TextView mTitle;
    private TextView mXvalue;
    private TextView mYvalue;
    private TextView mZvalue;

    public CoordinateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.coordinate_view, this);

        mTitle = (TextView)findViewById(R.id.title_text);
        mXvalue = (TextView)findViewById(R.id.x_value);
        mYvalue = (TextView)findViewById(R.id.y_value);
        mZvalue = (TextView)findViewById(R.id.z_val);

        if(attrs != null){

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CoordinateView);

            if(a.hasValue(R.styleable.CoordinateView_android_text)){
                setTitleText(a.getString(R.styleable.CoordinateView_android_text));
            }
        }
    }

    public void setTitleText(String text){

        mTitle.setText(text);
    }

    public void setX(String x){
        mXvalue.setText(x);
    }

    public void setY(String y){
        mYvalue.setText(y);
    }

    public void setZ(String z){
        mZvalue.setText(z);
    }
}
