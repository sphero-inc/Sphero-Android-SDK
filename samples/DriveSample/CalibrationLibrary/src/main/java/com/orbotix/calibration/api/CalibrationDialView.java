package com.orbotix.calibration.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.orbotix.calibration.R;

/**
 * Widget for displaying a calibration wheel. Moving the displayed knob sends rotations
 * from 0.0 to 360.0 degrees to the CalibrationEventListener. Tapping on the knob will send an onCalibrationStop
 * call with calibrate equal false, in order to cancel the calibration.
 */
public class CalibrationDialView extends View {
	private Bitmap wheel;
	private Bitmap knob;

	private float[] wheelPosition = new float[2];
	private RectF wheelFrame;
	private float[] knobPosition = new float[2];
	private RectF originalKnobFrame;
    private RectF actualKnobFrame;
	private float radius;

	private GestureDetector gestureDetector;
	private Matrix scrollTransform = new Matrix();
	private Matrix knobTransform = new Matrix();

	private CalibrationEventListener calibrationListener;
	private boolean calibrating = false;

	public interface CalibrationEventListener {
		/**
		 * Sent to the listener to indicate that the user has touched down on the
		 * knob.
		 */
		public void onCalibrationStart();
		/**
		 * Sent to the listener when the user has removed their finger off of the knob. Tapping on
		 * the knob indicates a cancel calibration.
		 * @param calibrate true to indicate that the listener should send the user the calibration message.
		 * false to abort the calibration.
		 */
		public void onCalibrationStop(boolean calibrate);
		/**
		 * Sent as the knob is turned giving an angle from 0 to 360 for the new heading for the ball.
		 */
		public void onHeadingRotation(float heading);
	}

	/**
	 * Constructor for archiving from a layout file.
	 * @param context The context for the view.
	 * @param attrs The specified in the layout file.
	 */
	public CalibrationDialView(Context context, AttributeSet attrs) {
		super(context, attrs);
		wheel = BitmapFactory.decodeResource(getResources(), R.drawable.calibrate_wheel);
		knob = BitmapFactory.decodeResource(getResources(), R.drawable.calibrate_knob);

		gestureDetector = new GestureDetector(context, new GestureListener());
	}

	/**
	 * Method to set the CalibrationEventListener object for this view.
	 * @param listener
	 */
	public void setCalibrationEventListener(CalibrationEventListener listener) {
		calibrationListener = listener;
	}

    private void resizeBitmaps(double resizingFactor) {
        resizeWheel(resizingFactor);
        resizeKnob(resizingFactor);
    }

    private void resizeWheel(double resizingFactor) {
        int newWidth = (int)Math.floor(wheel.getWidth() * resizingFactor);
        int newHeight = (int)Math.floor(wheel.getHeight() * resizingFactor);
        wheel = Bitmap.createScaledBitmap(wheel, newWidth, newHeight, true);
    }

    private void resizeKnob(double resizingFactor) {
        int newWidth = (int)Math.floor(knob.getWidth() * resizingFactor);
        int newHeight = (int)Math.floor(knob.getHeight() * resizingFactor);
        knob = Bitmap.createScaledBitmap(knob, newWidth, newHeight, true);
    }

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

        // make sure this view fits in its allotted space
        double resizingFactor = 1.0;
        if (wheel.getHeight() > getHeight()) {
            resizingFactor = (double)getHeight() / (double)wheel.getHeight();
            resizeBitmaps(resizingFactor);
        }
        // if the width is still too big after resizing based on the height,
        // we will make it even smaller so that the width fits.
        if (wheel.getWidth() > getWidth()) {
            resizingFactor = (double)getWidth() / (double)wheel.getWidth();
            resizeBitmaps(resizingFactor);
        }

		wheelPosition[0] = (getWidth() - wheel.getWidth())/2;
		wheelPosition[1] = getHeight() - wheel.getHeight();
		wheelFrame = new RectF(wheelPosition[0], wheelPosition[1],
				wheelPosition[0] + wheel.getWidth(), wheelPosition[1] + wheel.getHeight());

		knobPosition[0] = (getWidth() - knob.getWidth())/2;
		knobPosition[1] = (float) (wheelPosition[1] + 40.0);
		originalKnobFrame = new RectF(knobPosition[0], knobPosition[1],
				knobPosition[0] + knob.getWidth(), knobPosition[1] + knob.getHeight());
        actualKnobFrame = new RectF(originalKnobFrame);
		radius = wheelFrame.centerY() - originalKnobFrame.centerY();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(wheel, wheelPosition[0], wheelPosition[1], null);

		float transformed_pos[] = new float[2];
		knobTransform.mapPoints(transformed_pos, knobPosition);
		canvas.drawBitmap(knob, transformed_pos[0], transformed_pos[1], null);
	}

	/**
	 * Called by the android framework.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
        if (calibrating && event.getAction() == MotionEvent.ACTION_UP) {
			// Finish the calibration
			//calibrating = false;
			//knobTransform.reset();
			//scrollTransform.reset();
			//invalidate();
			//if (calibrationListener != null) {
			//	calibrationListener.onCalibrationStop(true);
			//}
            actualKnobFrame = new RectF();
            knobTransform.mapRect(actualKnobFrame, originalKnobFrame);
			return true;
		}
		return gestureDetector.onTouchEvent(event);
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			if (actualKnobFrame.contains(e.getX(), e.getY())) {
				// The user touched down on the knob so handle events
				calibrating = true;
				if (calibrationListener != null) {
					calibrationListener.onCalibrationStart();
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// Calculate the angle from the center and from the positive y axis.
			scrollTransform.postTranslate(-distanceX, -distanceY);
			RectF knob_frame = new RectF();
			scrollTransform.mapRect(knob_frame, originalKnobFrame);
			float x = knob_frame.centerX() - wheelFrame.centerX();
			float y = wheelFrame.centerY() - knob_frame.centerY();
			float angle = (float) Math.atan2(x, y);
			if (calibrationListener != null)  {
				// Convert angle from the range -pi...pi to 0....360
				float heading = 0.0f;
				if (angle >= 0.0) {
					heading = (float) (angle * 180.0/Math.PI);
				} else {
					heading = (float) (360.0 + angle * 180.0/Math.PI);
				}
                // Inform the listener of the new heading.
                calibrationListener.onHeadingRotation(heading);
            }

			// Calculate the final knob translation. (clamped to the radius)
			knobTransform.setTranslate( (float)(radius * Math.sin(angle)),
					(float)(radius * (1.0 - Math.cos(angle))));
			invalidate();
			return true;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			/*calibrating = false;
			if (calibrationListener != null) {
				calibrationListener.onCalibrationStop(false);
			}*/
			return true;
		}
	}
}
