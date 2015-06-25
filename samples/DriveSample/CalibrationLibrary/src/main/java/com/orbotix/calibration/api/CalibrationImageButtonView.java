package com.orbotix.calibration.api;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.orbotix.calibration.R;

/**
 * Created by Orbotix Inc.
 * Date: 1/31/13
 *
 */
public class CalibrationImageButtonView extends ImageButton implements View.OnTouchListener {

    private int mColorBackground = 0xff1e90ff;
    private int mColorForeground = Color.WHITE;

    private CalibrationView mCalibrationView;

    private Point mCenterPoint = null;
    private int mCalibrationRadius = 100;

    /**
     * Define the custom orientations
     */
    public static final int NONE = 0;
    public static final int ABOVE = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int BELOW = 4;

    private CalibrationView.CalibrationCircleLocation mOrientation =
        CalibrationView.CalibrationCircleLocation.NONE;
    private Drawable mSpecifiedDrawable;

    public CalibrationImageButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener(this);

        scaleCalibrationRadius();

        if (attrs != null) {
            applyAttributeSet(attrs);
        }

    }

    private void scaleCalibrationRadius() {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        mCalibrationRadius *= scale;
    }

    private void applyAttributeSet(AttributeSet attrs) {
        final Context context = getContext();
        TypedArray a =
            context.obtainStyledAttributes(attrs, R.styleable.CalibrationImageButtonView);

        applySpecifiedDrawableAttributes(a);
        applyBackgroundColorsAttributes(a);
        applyCalibrationRadiusAttributes(a);
        applyOrientationAttributes(a);
        applyCalibrationViewReferenceAttribute(a);

        if(a.hasValue(R.styleable.CalibrationImageButtonView_calibration_center_x) &&
            a.hasValue(R.styleable.CalibrationImageButtonView_calibration_center_y)){

            mCenterPoint = new Point();
            mCenterPoint.x = a.getDimensionPixelOffset(R.styleable.CalibrationImageButtonView_calibration_center_x, 0);
            mCenterPoint.y = a.getDimensionPixelOffset(R.styleable.CalibrationImageButtonView_calibration_center_y, 0);
        }


        a.recycle();
    }

    private void applyCalibrationViewReferenceAttribute(TypedArray a) {
        int ref = a.getResourceId(R.styleable.CalibrationImageButtonView_calibration_view, 0);
        if(ref != 0){
            CalibrationView view = (CalibrationView)findViewById(ref);
            setCalibrationView(view);
        }
    }

    private void applyOrientationAttributes(TypedArray a) {
        final int orientation =
            a.getInt(R.styleable.CalibrationImageButtonView_widget_orientation, NONE);
        setOrientation(getCalibrationCircleLocationFromId(orientation));
    }

    private void applyCalibrationRadiusAttributes(TypedArray a) {
        mCalibrationRadius =
            (int)a.getDimension(
                R.styleable.CalibrationImageButtonView_calibration_circle_radius,
                mCalibrationRadius);
    }

    private void applyBackgroundColorsAttributes(TypedArray a) {
        if(!isDrawableSpecified()){
            mColorBackground = a.getColor(
                R.styleable.CalibrationImageButtonView_background_color,
                mColorBackground);
            mColorForeground = a.getColor(
                R.styleable.CalibrationImageButtonView_foreground_color,
                mColorForeground);
        }
    }

    private void applySpecifiedDrawableAttributes(TypedArray a) {
        mSpecifiedDrawable = a.getDrawable(R.styleable.CalibrationImageButtonView_android_src);

        if(isDrawableSpecified()){
            setImageDrawable(mSpecifiedDrawable);
        }
    }

    private boolean isDrawableSpecified() {
        return mSpecifiedDrawable != null;
    }

    public boolean isCalibrating() {
        return mCalibrationView.isCalibrating();
    }

    /**
     * Sets the {@link CalibrationView} that this CalibrationImageButtonView uses to display the calibration.
     *
     * @param view The {@link CalibrationView} to set
     */
    public void setCalibrationView(CalibrationView view) {
        mCalibrationView = view;
    }

    private CalibrationView.CalibrationCircleLocation getCalibrationCircleLocationFromId(int id) {
        CalibrationView.CalibrationCircleLocation ret;
        if (id == ABOVE) {
            ret = CalibrationView.CalibrationCircleLocation.ABOVE;
        } else if (id == LEFT) {
            ret = CalibrationView.CalibrationCircleLocation.LEFT;
        } else if (id == RIGHT) {
            ret = CalibrationView.CalibrationCircleLocation.RIGHT;
        } else if (id == BELOW){
            ret = CalibrationView.CalibrationCircleLocation.BELOW;
        } else {
            ret = CalibrationView.CalibrationCircleLocation.NONE;
        }

        return ret;
    }

    /**
     * Set background color of the button
     */
    public void setBackgroundColor(int color) {
        mColorBackground = color;
        generateButtonDrawable();
    }

    /**
     * Set foreground color of the button
     */
    public void setForegroundColor(int color) {
        mColorForeground = color;
        generateButtonDrawable();
    }

    /**
     * Set the draw orientation
     *
     * @param orientation an enum of the CalibrationCircleLocation constant
     */
    public void setOrientation(CalibrationView.CalibrationCircleLocation orientation) {
        mOrientation = orientation;
        generateButtonDrawable();
    }

    /**
     * Called when we need to change the default image colors
     * And combine them into one image for the button
     */
    private void generateButtonDrawable() {

        if(isDrawableSpecified()){
            return;
        }
        Drawable drawable = getColoredDrawable();
        Bitmap rotatedBitmap = getRotatedBitmap(drawable);
        setImageBitmap(rotatedBitmap);
    }

    private Drawable getColoredDrawable() {

        Drawable background = getResources().getDrawable(R.drawable.calibrate_background);
        Drawable foreground = getResources().getDrawable(R.drawable.calibrate_foreground);

        return getLayeredDrawable(background, foreground);
    }

    private Drawable getLayeredDrawable(Drawable background, Drawable foreground) {
        background.setColorFilter(mColorBackground, PorterDuff.Mode.MULTIPLY);
        foreground.setColorFilter(mColorForeground, PorterDuff.Mode.MULTIPLY);

        return getLayerDrawableFromLayers(background, foreground);
    }

    private LayerDrawable getLayerDrawableFromLayers(Drawable background, Drawable foreground) {
        Drawable[] layers = new Drawable[2];
        layers[0] = background;
        layers[1] = foreground;
        return new LayerDrawable(layers);
    }

    private Bitmap getRotatedBitmap(Drawable drawable) {

        Matrix matrix = new Matrix();
        float angle = mOrientation.getAngle();
        matrix.postRotate(angle);
        Bitmap bitmap = drawableToBitmap(drawable);
        Bitmap rotatedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return rotatedBitmap;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap =
            Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (mCalibrationView != null) {
            int[] location = new int[2];
            getLocationOnScreen(location);

            if(mCenterPoint != null) {
                Point center = new Point(mCenterPoint);
                if(center == null){
                    center = new Point();
                    center.x = location[0] + (getWidth() / 2);
                    center.y = location[1] + (getHeight() / 2);
                }else {

                    center.x = location[0];
                    center.y = location[1];
                    center.x += mCenterPoint.x;
                    center.y += mCenterPoint.y;

                    float touchX = location[0] + (getWidth() / 2);
                    float touchY = location[1] + (getHeight() / 2);

                    float length = Math.abs(center.x - touchX);
                    float height = Math.abs(center.y- touchY);

                    mCalibrationRadius = (int)Math.hypot(length, height);
                }

                mCalibrationView.startSingleTouchCalibration(
                        mOrientation,
                        center,
                        mCalibrationRadius,
                        motionEvent);
            }
        }

        return false;
    }
}
