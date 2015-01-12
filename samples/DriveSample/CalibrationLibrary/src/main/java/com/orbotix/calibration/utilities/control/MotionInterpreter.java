package com.orbotix.calibration.utilities.control;

import android.view.MotionEvent;

/**
 * A type that can interpret a MotionEvent
 *
 * @author Adam Williams
 */
interface MotionInterpreter {

    /**
     * Interprets the provided MotionEvent
     *
     * @param event a MotionEvent to interpret
     */
    public void interpretMotionEvent(MotionEvent event);
}
