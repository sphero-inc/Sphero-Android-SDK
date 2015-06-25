package com.orbotix.calibration.utilities.widget;

import android.graphics.Point;

import com.orbotix.calibration.utilities.graphic.Graphic;

/**
 * A part of a WidgetGraphic that can be drawn to a View.
 *
 * @author Adam Williams
 */
public interface WidgetGraphicPart extends Graphic {

    /**
     * Sets this WidgetGraphicPart's colors to the provided colors
     *
     * @param colors The colors of this WidgetGraphicPart, as integers
     */
    public void setColor(Integer... colors);

    /**
     * Sets the position of this WidgetGraphicPart to the provided Point within its parent.
     *
     * @param position the position of this WidgetGraphicPart
     */
    public void setPosition(Point position);

    /**
     * Sets the size of this WidgetGraphicPart
     *
     * @param size the size to set for this WidgetGraphicPart
     */
    public void setSize(int size);

    /**
     * Gets the size of this WidgetGraphicPart
     *
     * @return the size, as an int
     */
    public int getSize();

    /** Shows this WidgetGraphicPart */
    public void show();

    /** Hides this WidgetGraphicPart */
    public void hide();
}
