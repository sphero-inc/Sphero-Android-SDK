package orbotix.robot.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import orbotix.uisample.R;
import orbotix.view.calibration.CalibrationView;

/**
 * A type of {@link ImageButton} that uses the {@link CalibrationView} to calibrate Sphero with
 * a single touch, rather than a multi-touch rotation gesture.
 *

 * Date: 1/31/13
 *
 * @author Adam Williams
 */
public class CalibrationImageButtonView extends ImageButton implements View.OnTouchListener {

    private int mColorBackground = 0xff1e90ff;
    private int mColorForeground = Color.WHITE;

    private CalibrationView mCalibrationView;

    private Point mCenterPoint = new Point(-1, -1);
    private int mCalibrationRadius = 100;

    /**
     * Define the custom orientations
     */
    public static final int ABOVE = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int BELOW = 4;

    private CalibrationView.CalibrationCircleLocation mOrientation =
        CalibrationView.CalibrationCircleLocation.ABOVE;

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

        mColorBackground = a.getColor(
            R.styleable.CalibrationImageButtonView_background_color,
            mColorBackground);
        mColorForeground = a.getColor(
            R.styleable.CalibrationImageButtonView_foreground_color,
            mColorForeground);

        mCalibrationRadius =
            (int)a.getDimension(
                R.styleable.CalibrationImageButtonView_calibration_circle_radius,
                mCalibrationRadius);

        final int orientation =
            a.getInt(R.styleable.CalibrationImageButtonView_widget_orientation, ABOVE);
        setOrientation(getCalibrationCircleLocationFromId(orientation));

        a.recycle();
    }

    /**
     * Sets the {@link orbotix.view.calibration.CalibrationView} that this CalibrationImageButtonView uses to display the calibration.
     *
     * @param view The {@link orbotix.view.calibration.CalibrationView} to set
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
        } else {
            ret = CalibrationView.CalibrationCircleLocation.BELOW;
        }

        return ret;
    }

    /**
     * Set background color of the button
     */
    public void setBackgroundColor(int color) {
        mColorBackground = color;
        updateButtonImages();
    }

    /**
     * Set foreground color of the button
     */
    public void setForegroundColor(int color) {
        mColorForeground = color;
        updateButtonImages();
    }

    /**
     * Set the draw orientation
     *
     * @param orientation an enum of the CalibrationCircleLocation constant
     */
    public void setOrientation(CalibrationView.CalibrationCircleLocation orientation) {
        mOrientation = orientation;
        updateButtonImages();
    }

    /**
     * Called when we need to change the default image colors
     * And combine them into one image for the button
     */
    private void updateButtonImages() {

        Drawable drawable = getColoredDrawable();
        Bitmap rotatedBitmap = getRotatedBitmap(drawable);
        setImageBitmap(rotatedBitmap);
    }

    private Drawable getColoredDrawable() {

        Drawable background = getResources().getDrawable(R.drawable.calibrate_background);
        Drawable foreground = getResources().getDrawable(R.drawable.calibrate_foreground);

        // Create drawables of desired colors
        background.setColorFilter(mColorBackground, PorterDuff.Mode.MULTIPLY);
        foreground.setColorFilter(mColorForeground, PorterDuff.Mode.MULTIPLY);

        // Combine drawables into one for the image button
        Drawable[] layers = new Drawable[2];
        layers[0] = background;
        layers[1] = foreground;
        LayerDrawable layerDrawable = new LayerDrawable(layers);

        return layerDrawable;
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

            mCenterPoint.x = getLeft() + (getWidth() / 2);
            mCenterPoint.y = getTop() + (getHeight() / 2);

            mCalibrationView.startSingleTouchCalibration(
                mOrientation,
                mCenterPoint,
                mCalibrationRadius,
                motionEvent);
        }

        return false;
    }

    /**
     * Sets the radius of the CalibrationView's "calibration circle", in pixels
     * @param radius The radius, in pixels
     */
    public void setRadius(int radius) {
        mCalibrationRadius = radius;
    }
}
