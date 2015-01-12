package com.orbotix.joystick.utilities.math;

/** Provides methods to constrain numerical values. */
public class Value {
    /**
     * Clamps a value between a minimum and maximum value.
     *
     * @param value The value to clamp.
     * @param min   The minimum value.
     * @param max   The maximum value.
     */
    public static float clamp(float value, float min, float max) {
        if (value > max)
            return max;
        else if (value < min)
            return min;
        else
            return value;
    }

    /**
     * Clamps a value between a minimum and maximum value.
     *
     * @param value The value to clamp.
     * @param min   The minimum value.
     * @param max   The maximum value.
     */
    public static double clamp(double value, double min, double max) {
        if (value > max)
            return max;
        else if (value < min)
            return min;
        else
            return value;
    }

    /**
     * Clamps a value between a minimum and maximum value.
     *
     * @param value The value to clamp.
     * @param min   The minimum value.
     * @param max   The maximum value.
     */
    public static int clamp(int value, int min, int max) {
        if (value > max)
            return max;
        else if (value < min)
            return min;
        else
            return value;
    }

    /**
     * Keeps a value at a fixed value if it is within some range of it.
     *
     * @param value       The value to window.
     * @param windowValue A value that the value should be if it is within some delta of it.
     * @param delta       The delta used to calculate the range within the windowValue.
     * @return A value constrained to the window value.
     */
    public static double window(double value, double windowValue, double delta) {
        if (Math.abs(value) > Math.abs(windowValue) - delta && Math.abs(value) < Math.abs(windowValue) + delta) {
            return windowValue;
        }
        return value;
    }
}
