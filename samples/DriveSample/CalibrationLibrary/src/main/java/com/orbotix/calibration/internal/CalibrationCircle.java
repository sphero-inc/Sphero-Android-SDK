package com.orbotix.calibration.internal;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.Rect;

import com.orbotix.calibration.utilities.color.ColorTools;
import com.orbotix.calibration.utilities.widget.WidgetGraphicPart;

/**
 * One of the three circles in the CalibrationView that rotate around the calibration point
 * @author Adam Williams
 */
public class CalibrationCircle implements WidgetGraphicPart {

    private final int[] mColors = new int[] {0xffffff88, 0x666622};
    private final Point mPosition = new Point();

    private volatile int mRadius = 0;
    private float mAngle = 1f;
    private boolean mDashed = false;
    private boolean mShowing = false;
    private boolean mCleared = false;
    private boolean mShowGlow = true;
    private int mDirtyRadius = 0;

    private final Paint mInnerPaint = new Paint();
    private final Paint mOuterPaint = new Paint();

    //Display scale
    private final float mScale;

    //Base dash interval
    private final float[] mBaseDashInterval = new float[] {30f, 15f};

    //Base blur radius
    private final int mOuterBlurRadius;

    //Base stroke sizes
    private final int mInnerStrokeSize;
    private final int mOuterStrokeSize;

    //Base radius
    private final int mBaseRadius;


    /**
     * Creates a new CalibrationCircle using the standard widths
     *
     * @param context
     */
    public CalibrationCircle(Context context) {

        this(context, 5, 20);

    }

    public CalibrationCircle(Context context, int i_stroke_width){
        this(context, i_stroke_width, 20);
    }

    /**
     * Creates a new CalibrationCircle with the specified widths, in dps
     *
     * @param context
     * @param i_stroke_width
     * @param o_stroke_width
     */
    public CalibrationCircle(Context context, int i_stroke_width, int o_stroke_width) {

        if (o_stroke_width == 0) {
            setShowGlow(false);
        }

        mScale = context.getResources().getDisplayMetrics().density;

        mOuterBlurRadius = (int) (10f * mScale);

        mInnerStrokeSize = (int) (i_stroke_width * mScale);
        mOuterStrokeSize = (int) (o_stroke_width * mScale);

        mBaseRadius = (int) (100 * mScale);

        mBaseDashInterval[0] = mBaseDashInterval[0] * mScale;
        mBaseDashInterval[1] = mBaseDashInterval[1] * mScale;

        //Set paint basic settings
        mInnerPaint.setStyle(Paint.Style.STROKE);
        mInnerPaint.setAntiAlias(true);
        mOuterPaint.setStyle(Paint.Style.STROKE);

        //Set default colors and size
        setColor(mColors[0], mColors[1]);
        setSize(100);
    }

    /**
     * Makes this calibration circle dashed
     *
     * @return This instance, for convenience.
     */
    public CalibrationCircle setAsDashed() {

        mDashed = true;

        return this;
    }


    /**
     * Set the angle of the rotation of this CalibrationCircle, in degrees.
     *
     * @param degrees The angle, in degrees
     * @return this instance, for convenience
     */
    public CalibrationCircle setAngle(float degrees) {

        mAngle = degrees;
        return this;
    }

    /**
     * Sets the Color of the CalibrationCircle. Two colors are expected. Any more are ignored.
     * If only one color is provided, the second color is a color that is half its luminance
     *
     * @param colors One or two colors, as integers.
     */
    @Override
    public void setColor(Integer... colors) {

        if (colors != null && colors.length == 1) {

            mColors[1] = ColorTools.average(colors[0], ((colors[0] >>> 24 & 0xFF) << 24));
        }

        if (colors != null && colors.length > 0) {

            for (int i = 0; i < colors.length; i++) {
                mColors[i] = colors[i];
                if (i == 1) {
                    break;
                }
            }

            mInnerPaint.setColor(mColors[0]);
            mOuterPaint.setColor(mColors[1]);
        }
    }

    @Override
    public void setPosition(Point position) {
        mPosition.set(position.x, position.y);
    }

    @Override
    public void setSize(int size) {

        //mRadius = (int)((float )size * mScale);
        mRadius = size;

        final float factor = ((float) mRadius / mBaseRadius);

        //find stroke size and blur size based on the radius
        final int i_stroke = Math.max((int) ((float) mInnerStrokeSize * factor), 1);
        final int o_stroke;
        if (mShowGlow) {
            o_stroke = Math.max((int) ((float) mOuterStrokeSize * factor), 1);
        } else {
            o_stroke = 0;
        }

        final int o_blur = Math.max((int) ((float) mOuterBlurRadius * factor), 1);

        //Set stroke width
        mInnerPaint.setStrokeWidth(i_stroke);
        if (mShowGlow) {
            mOuterPaint.setStrokeWidth(o_stroke);
        }

        //If dashed, set the dash interval
        if (mDashed) {
            final float[] interval = new float[] {
                    Math.max(mBaseDashInterval[0] * factor, 1f),
                    Math.max(mBaseDashInterval[1] * factor, 1f)
            };

            PathEffect e = mInnerPaint.setPathEffect(new DashPathEffect(interval, mAngle));
            if (mShowGlow) {
                mOuterPaint.setPathEffect(e);
            }
        } else {
            mInnerPaint.setPathEffect(null);

            if (mShowGlow) {
                mOuterPaint.setPathEffect(null);
            }
        }

        //Set blur size
        if (mShowGlow) {
            mOuterPaint.setMaskFilter(new BlurMaskFilter(o_blur, BlurMaskFilter.Blur.NORMAL));
        }

        mDirtyRadius = mRadius + (int) (o_blur * 2.6f);
    }

    @Override
    public int getSize() {
        return mRadius;
    }

    @Override
    public void show() {
        mShowing = true;
        mCleared = false;
    }

    @Override
    public void hide() {
        mShowing = false;
    }


    @Override
    public Rect draw(Canvas canvas) {

        if (mShowing) {
            //Draw the two circles
            if (mShowGlow) {
                canvas.drawCircle(mPosition.x, mPosition.y, mRadius, mOuterPaint);
            }
            canvas.drawCircle(mPosition.x, mPosition.y, mRadius, mInnerPaint);
        }

        if (mShowing || !mCleared) {

            if (!mCleared) {
                mCleared = true;
            }

            return new Rect(
                    mPosition.x - mDirtyRadius,
                    mPosition.y - mDirtyRadius,
                    mPosition.x + mDirtyRadius,
                    mPosition.y + mDirtyRadius);
        }

        //Return empty rect if nothing has been done
        return new Rect();
    }

    /** @param val set this to true to show the "glow", or false to not. Defaults to true. */
    public void setShowGlow(boolean val) {
        mShowGlow = val;
    }
}
