package com.orbotix.calibration.api;

public interface CalibrationEventListener {
    /* Invoked when the Calibration has started. */
    public void onCalibrationBegan();

    /* Invoked when the Calibraiton has been updated. */
    public void onCalibrationChanged(final float angle);

    /* Invoked when the Calibration has stopped. */
    public void onCalibrationEnded();
}
