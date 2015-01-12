package com.orbotix.joystick.api;

public interface JoystickEventListener {
    /* Invoked when the Joystick has stopped moving. */
    public void onJoystickBegan();

    /* Invoked when the Joystick has moved. */
    public void onJoystickMoved(final double distanceFromCenter, final double angle);

    /* Invoked when the Joystick has stopped moving. */
    public void onJoystickEnded();
}
