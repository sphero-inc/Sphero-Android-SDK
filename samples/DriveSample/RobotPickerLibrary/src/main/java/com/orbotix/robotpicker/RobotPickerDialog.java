package com.orbotix.robotpicker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class RobotPickerDialog extends Dialog {
    public interface RobotPickerListener {
        public void onRobotPicked(RobotPicked robotPicked);
    }

    private RobotPickerListener _pickerListener;

    public enum RobotPicked {
        Sphero(0),
        Ollie(1);

        private int value;
        private RobotPicked(int value) {
            this.value = value;
        }

        public int getValue() { return value; }
    }

    public RobotPickerDialog(Context context) {
        super(context);
        commonInit(null);
    }

    public RobotPickerDialog(Context context, RobotPickerListener listener) {
        super(context);
        commonInit(listener);
    }

    private void commonInit(RobotPickerListener listener) {
        _pickerListener = listener;
        setCancelable(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.robot_picker);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = WindowManager.LayoutParams.FILL_PARENT;
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        getWindow().setAttributes(params);

        findViewById(R.id.ollie_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_pickerListener != null) _pickerListener.onRobotPicked(RobotPicked.Ollie);
            }
        });

        findViewById(R.id.sphero_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_pickerListener != null) _pickerListener.onRobotPicked(RobotPicked.Sphero);
            }
        });
    }

    public void setPickerListener(RobotPickerListener pickerListener) {
        _pickerListener = pickerListener;
    }
}