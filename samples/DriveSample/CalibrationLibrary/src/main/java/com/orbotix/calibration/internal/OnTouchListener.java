package com.orbotix.calibration.internal;

import android.view.MotionEvent;

/**
 * Detects a touch, and does something when the touch happens
 * <p/>
 * <p/>
 * Date: 2/5/13
 *
 * @author Adam Williams
 * @author Jack Thorp
 */
public interface OnTouchListener {

    /**
     * Runs on each touch event
     *
     * @param event
     */
    public void onTouch(MotionEvent event);
}
