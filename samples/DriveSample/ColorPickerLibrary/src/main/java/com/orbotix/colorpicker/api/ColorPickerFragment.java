package com.orbotix.colorpicker.api;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.orbotix.colorpicker.R;
import com.orbotix.colorpicker.internal.HSBColorPickerView;
import com.orbotix.colorpicker.internal.OnColorChangedListener;

/**
 * Date: 4/4/14
 *
 * @author Adam Williams
 * @author Jack Thorp
 */
public class ColorPickerFragment extends Fragment {
    private final static String TAG = "OBX-ColorPickerFragment";

    /* Listener for Joystick Event notifications */
    private ColorPickerEventListener mEventListener = null;

    private HSBColorPickerView hsbPicker = null;
    private Button previousColorBox = null;
    private Button newColorBox = null;

    private static final int MAX_VALUE = 255;
    private static final float ROUNDED_CORNER_RADIUS = 15.0f;
    private static final float GLOSS_INTENSITY = 0.2f;
    private static final long DOUBLE_CLICK_DELAY = 200; // millis

    private SeekBar redSeekBar = null;
    private SeekBar greenSeekBar = null;
    private SeekBar blueSeekBar = null;

    // Colors
    private int mPreviousColor;
    private int red = 0;
    private int green = 0;
    private int blue = 0;

    // View Animator
    private ViewAnimator mViewAnimator;

    // RGB text views in hsb picker
    private TextView mRedText = null;
    private TextView mGreenText = null;
    private TextView mBlueText = null;

    // Preview boxes
    private ShapeDrawable mPreviousBoxBackground;
    private ShapeDrawable mNewBoxBackground;

    // RGB Conversion View
    private Handler handler = new Handler();
    private boolean rgbClickedOnce = false;


    // listener for the hsbColorPicker
    private OnColorChangedListener mColorChangedListener = new OnColorChangedListener() {

        @Override
        public void OnColorChanged(int newColor) {
            red = Color.red(newColor);
            green = Color.green(newColor);
            blue = Color.blue(newColor);
            mRedText.setText(Integer.toString(red));
            mGreenText.setText(Integer.toString(green));
            mBlueText.setText(Integer.toString(blue));
            redSeekBar.setProgress(red);
            greenSeekBar.setProgress(green);
            blueSeekBar.setProgress(blue);
            mNewBoxBackground.getPaint().setShader(getPreviewBackground(newColor, mNewBoxBackground.getBounds()));
            mPreviousBoxBackground.getPaint().setShader(getPreviewBackground(mPreviousColor, mPreviousBoxBackground.getBounds()));
            mNewBoxBackground.invalidateSelf();
            mPreviousBoxBackground.invalidateSelf();
            broadcastColorChange();
        }

    };

    /**
     * Returns a "glossy" background using the given color and the given bounds. Currently,
     * the gloss is applied vertically from top to bottom.
     *
     * @param color the color to be "glossed"
     * @param rect  the bounds of the area to be glossed
     * @return a {@link android.graphics.LinearGradient} to be used for the background of a color preview
     */
    private LinearGradient getPreviewBackground(int color, Rect rect) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] -= GLOSS_INTENSITY;
        hsv[2] += GLOSS_INTENSITY;
        int glossColor = Color.HSVToColor(hsv);
        return new LinearGradient(rect.left,
                rect.top,
                rect.left,
                rect.bottom,
                new int[] {color, glossColor, color},
                new float[] {0.0f, 0.5f, 0.50001f},
                Shader.TileMode.CLAMP);
    }

    /**
     * Used by the activity to set it's RGB values.
     *
     * @param r Red color component.
     * @param g Green color component.
     * @param b Blue color component.
     */
    public void setColor(int r, int g, int b) {
        red = r;
        green = g;
        blue = b;
    }

    /**
     * Accessor to the red color component.
     *
     * @return Value from 0 to 255.
     */
    public int getRed() {
        return red;
    }

    /**
     * Accessor to the green color component.
     *
     * @return Value from 0 to 255.
     */
    public int getGreen() {
        return green;
    }

    /**
     * Accessor to the blue color component.
     *
     * @return Value from 0 to 255.
     */
    public int getBlue() {
        return blue;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.hsb_color_picker, container, false);

        red = 100;
        green = 100;
        blue = 100;
        mPreviousColor = Color.rgb(red, green, blue);

        hsbPicker = (HSBColorPickerView) v.findViewById(R.id.ColorPickerBase);
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return hsbPicker.onTouchEvent(event);
            }
        });

        // preview boxes
        previousColorBox = (Button) v.findViewById(R.id.previous_color);
        newColorBox = (Button) v.findViewById(R.id.new_color);

        // rgb text views
        mRedText = (TextView) v.findViewById(R.id.RValueText);
        mGreenText = (TextView) v.findViewById(R.id.GValueText);
        mBlueText = (TextView) v.findViewById(R.id.BValueText);

        redSeekBar = (SeekBar) v.findViewById(R.id.RedSeekBar);
        greenSeekBar = (SeekBar) v.findViewById(R.id.GreenSeekBar);
        blueSeekBar = (SeekBar) v.findViewById(R.id.BlueSeekBar);

        setupRedSeekBar();
        setupGreenSeekBar();
        setupBlueSeekBar();

        mViewAnimator = (ViewAnimator) v.findViewById(R.id.ColorPickerViewAnimator);
        setupPickerItems();
        LinearLayout rgbView = (LinearLayout) v.findViewById(R.id.RGBConversionLayout);

        // setup double click on the rgb conversion view to switch to the rgb picker
        rgbView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (rgbClickedOnce) {
                    mViewAnimator.showNext();
                    rgbClickedOnce = false;
                } else {
                    rgbClickedOnce = true;
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            rgbClickedOnce = false;
                        }

                    }, DOUBLE_CLICK_DELAY);
                }

            }
        });
        
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setColorPickerEventListener(ColorPickerEventListener eventListener) {
        mEventListener = eventListener;
    }

    private void setupPickerItems() {
        hsbPicker.setNewColor(Color.rgb(red, green, blue));
        hsbPicker.setOnColorChangedListener(mColorChangedListener);

        // shapes for the backgrounds of the previews
        float[] cornerRadii = new float[] {ROUNDED_CORNER_RADIUS, ROUNDED_CORNER_RADIUS,
                ROUNDED_CORNER_RADIUS, ROUNDED_CORNER_RADIUS,
                ROUNDED_CORNER_RADIUS, ROUNDED_CORNER_RADIUS,
                ROUNDED_CORNER_RADIUS, ROUNDED_CORNER_RADIUS};
        // previous color preview
        RoundRectShape shape1 = new RoundRectShape(cornerRadii, null, null);
        mPreviousBoxBackground = new ShapeDrawable(shape1);
        previousColorBox.setBackgroundDrawable(mPreviousBoxBackground);
        View.OnClickListener previousListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tempColor = mPreviousColor;
                mPreviousColor = Color.rgb(red, green, blue);
                hsbPicker.setNewColor(tempColor);
                mColorChangedListener.OnColorChanged(tempColor);
            }
        };

        previousColorBox.setOnClickListener(previousListener);

        // new color preview
        RoundRectShape shape2 = new RoundRectShape(cornerRadii, null, null);
        mNewBoxBackground = new ShapeDrawable(shape2);
        newColorBox.setBackgroundDrawable(mNewBoxBackground);
    }

    private void setupRedSeekBar() {
        redSeekBar.setMax(MAX_VALUE);
        redSeekBar.setProgress(red);
        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                red = progress;
                mColorChangedListener.OnColorChanged(Color.rgb(red, green, blue));
                broadcastColorChange();
            }
        });
    }

    private void setupGreenSeekBar() {
        greenSeekBar.setMax(MAX_VALUE);
        greenSeekBar.setProgress(green);
        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                green = progress;
                mColorChangedListener.OnColorChanged(Color.rgb(red, green, blue));
                broadcastColorChange();
            }
        });
    }

    private void setupBlueSeekBar() {
        blueSeekBar.setMax(MAX_VALUE);
        blueSeekBar.setProgress(blue);
        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                blue = progress;
                mColorChangedListener.OnColorChanged(Color.rgb(red, green, blue));
                broadcastColorChange();
            }
        });
    }

    private void broadcastColorChange() {
        assert (mEventListener != null);
        if (mEventListener != null) {
            mEventListener.onColorPickerChanged(red, green, blue);
        }
    }
}