package com.orbotix.calibration.utilities.graphic;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.orbotix.calibration.utilities.color.ColorTools;
import com.orbotix.calibration.utilities.widget.WidgetGraphicPart;

/**
 * A WidgetGraphicPart that represents a glowing point of light
 *
 * @author Adam Williams
 */
public class GlowingDot implements WidgetGraphicPart {

    private final int[] mColors = new int[] {0xffffffff, 0xff888888};
    private final Point mPosition = new Point();
    private int mRadius = 0;
    private int mInnerRadius = 0;
    private int mOuterRadius = 0;
    private int mInnerBlur = 0;
    private int mOuterBlur = 0;
    private boolean mShowing = false;
    private boolean mCleared = false;
    private boolean mShowGlow = true;

    private final Paint mInnerPaint = new Paint();
    private final Paint mOuterPaint = new Paint();

    //Base values
    private final int mBaseRadius = 100;
    private final int mBaseInnerBlur;
    private final int mBaseOuterBlur;
    private final float mScale;

    public GlowingDot(Context context) {

        mScale = context.getResources().getDisplayMetrics().density;

        mBaseInnerBlur = (int) (1 * mScale);
        mBaseOuterBlur = (int) (25 * mScale);

        mInnerPaint.setStyle(Paint.Style.FILL);
        mOuterPaint.setStyle(Paint.Style.FILL);
        mInnerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        mOuterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));

        setColor(mColors[0], mColors[1]);
        setPosition(new Point(150, 150));
        setSize(mBaseRadius);
    }

    @Override
    public void setColor(Integer... colors) {

        if (colors != null && colors.length == 1) {

            mColors[1] = ColorTools.average(colors[0], (Color.alpha(colors[0]) << 24));
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

        mRadius = size;
        mInnerRadius = (int) (mRadius * 0.1f);
        mOuterRadius = (int) (mRadius * 1f);

        //Find the blur size
        final float factor = (float) mRadius / (float) mBaseRadius;
        mInnerBlur = Math.max((int) (mBaseInnerBlur * factor), 1);
        mOuterBlur = Math.max((int) (mBaseOuterBlur * factor), 1);

        mInnerPaint.setMaskFilter(new BlurMaskFilter(mInnerBlur, BlurMaskFilter.Blur.NORMAL));
        mOuterPaint.setMaskFilter(new BlurMaskFilter(mOuterBlur, BlurMaskFilter.Blur.NORMAL));
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

        if (!mShowing && mCleared) {
            return new Rect();
        }

        if (mShowing) {

            if (mShowGlow) {
                canvas.drawCircle(mPosition.x, mPosition.y, mOuterRadius, mOuterPaint);
            }
            canvas.drawCircle(mPosition.x, mPosition.y, mInnerRadius, mInnerPaint);
        } else {
            mCleared = true;
        }


        final int dirty_radius = mOuterRadius + (mOuterBlur * 2);

        return new Rect(
                mPosition.x - dirty_radius,
                mPosition.y - dirty_radius,
                mPosition.x + dirty_radius,
                mPosition.y + dirty_radius);
    }

    /** @param val set this to true to show the "glow", or false to not. Defaults to true. */
    public void setShowGlow(boolean val) {
        mShowGlow = val;
    }
}
