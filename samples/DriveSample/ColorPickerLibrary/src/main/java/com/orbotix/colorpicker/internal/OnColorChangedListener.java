package com.orbotix.colorpicker.internal;

/**
 * Date: 4/4/14
 *
 * @author Jack Thorp
 */
public interface OnColorChangedListener {
    /**
     * Called when the color changes withing a color picker view.
     *
     * @param newColor the new color selected in the color picker view.
     */
    public void OnColorChanged(int newColor);
}
