package com.orbotix.colorpicker.api;

/**
 * Date: 4/4/14
 *
 * @author Jack Thorp
 */
public interface ColorPickerEventListener {
    /* Invoked when the Color has changed. */
    public void onColorPickerChanged(int red, int green, int blue);
}
