package com.orbotix.calibration.utilities.widget;

/**
 * A type that contains WidgetParts

 *
 * @author Adam Williams
 */
public interface WidgetGraphic {

    /**
     * Adds a WidgetGraphicPart to this WidgetGraphic
     *
     * @param part
     */
    public void addWidgetPart(WidgetGraphicPart part);

    /** Sets all WidgetParts in this WidgetGraphic to show */
    public void showAllWidgetParts();

    /** Sets all WidgetParts in this WidgetGraphic to hide */
    public void hideAllWidgetsParts();
}
