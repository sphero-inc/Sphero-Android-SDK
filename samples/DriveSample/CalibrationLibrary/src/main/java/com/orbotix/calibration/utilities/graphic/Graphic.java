package com.orbotix.calibration.utilities.graphic;

import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * A Type that is drawable to a provided Canvas
 * <p/>
 * <p/>
 * Author: Adam Williams
 * Date: 11/22/11
 * Time: 8:35 AM
 */
public interface Graphic {

    /**
     * Draws this Graphic to the provided Canvas
     *
     * @param canvas
     * @return a Rect containing only the area of the Canvas used by this Graphic.
     */
    public Rect draw(Canvas canvas);
}
