package com.orbotix.joystick.utilities.control;


/**
 * A type that is used to specify a generic controller.
 *
 * @author Adam Williams
 */
public interface Controller extends MotionInterpreter {
    /** Enables the Controller. */
    public void enable();

    /** Disables the Controller */
    public void disable();
}
