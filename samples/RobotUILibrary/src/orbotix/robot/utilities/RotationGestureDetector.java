package orbotix.robot.utilities;

import android.graphics.Point;
import android.view.MotionEvent;

/**
 * User: brandon
 * Date: 9/21/11
 * Time: 12:42 PM
 */
public class RotationGestureDetector {

    private OnRotationGestureListener mListener;
    private Point mInitialP1, mInitialP2, mP1, mP2;
    private int mP1Id, mP2Id;

    private double mStartingAngle;

    boolean detectingP1, detectingP2;

    public boolean rotating, trackingTwoPoints;

    /**
     * Basic constructor to be used to create a new {@link RotationGestureDetector}.
     */
    public RotationGestureDetector() {
        detectingP1 = detectingP2 = false;
        rotating = trackingTwoPoints = false;
    }

    public interface OnRotationGestureListener {
        /**
         * Called when the rotation gesture is first detected.
         * @param distance the distance between the two points.
         * @param p1 one of the touch points. (most likely the first touch that happened, but not guaranteed.)
         * @param p2 the other touch point used in the angle calculations from this point forward until another
         * {@link #onRotationGestureStarted(double, android.graphics.Point, android.graphics.Point)} is called.
         */
        public void onRotationGestureStarted(double distance, Point p1, Point p2);

        /**
         * Called when the rotation gesture has rotated since either an
         * {@link #onRotationGestureStarted(double, android.graphics.Point, android.graphics.Point)} call or the
         * previous {@link #onRotationGestureRotated(double, double, android.graphics.Point, android.graphics.Point)}
         * call.
         * @param totalRotationAngle the angle (in radians) between the initial set of points and the last touch points that caused
         * the rotation. This is the total amount of rotation since the gesture started NOT the amount of rotation
         * since the last call to
         * {@link #onRotationGestureRotated(double, double, android.graphics.Point, android.graphics.Point)}. The angle
         * will be between -pi/2 and pi/2.
         * @param distance the distance between the two points.
         * @param p1 one of the touch points. (most likely the first touch that happened, but not guaranteed.)
         * @param p2 the other touch point used in the angle calculations from this point forward until another
         * {@link #onRotationGestureStarted(double, android.graphics.Point, android.graphics.Point)} is called.
         */
        public void onRotationGestureRotated(double totalRotationAngle, double distance, Point p1, Point p2);

        /**
         * Called when the rotation gesture has ended and will no longer rotate.
         * @param finalAngle the final angle (in radians) observed between the points that started the rotation gesture and
         * the last known points.
         * @param distance the distance between the two points.
         * @param p1 one of the touch points. (most likely the first touch that happened, but not guaranteed.)
         * @param p2 the other touch point used in the angle calculations from this point forward until another
         * {@link #onRotationGestureStarted(double, android.graphics.Point, android.graphics.Point)} is called.
         */
        public void onRotationGestureEnded(double finalAngle, double distance, Point p1, Point p2);
    }

    /**
     * Used to set the {@link OnRotationGestureListener} that will receive the rotation events from this detector.
     * @param listener the {@link OnRotationGestureListener} to receive the events.
     */
    public void setOnRotationGestureListener(OnRotationGestureListener listener) {
        mListener = listener;
    }

    /**
     * To be called each time your {@link android.view.View} receives a touch event from
     * {@link android.view.View#onTouchEvent(android.view.MotionEvent)}. This will enable rotation gesture detection
     * on this view. Make sure you register your {@link OnRotationGestureListener} so that you can receive the
     * rotation events. If you would like the rotation gesture to be global, you can call this in your activity's
     * {@link android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)}.
     * @param event the {@link MotionEvent} from the {@link android.view.View}'s
     * {@link android.view.View#onTouchEvent(android.view.MotionEvent)} method.
     * @return true if the event is consumed, false otherwise
     */
    public boolean onTouchEvent(MotionEvent event) {
        boolean consumed = false;
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        switch (event.getAction()) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_2_DOWN:
            case MotionEvent.ACTION_DOWN:
                if (!detectingP1) {
                    mP1Id = pointerId;
                    mP1 = new Point((int)event.getX(pointerIndex), (int)event.getY(pointerIndex));
                    detectingP1 = true;
                } else if (!detectingP2) {
                    mP2Id = pointerId;
                    mP2 = new Point((int)event.getX(pointerIndex), (int)event.getY(pointerIndex));
                    detectingP2 = true;
                } else {
                    // three points?
                }

                if (!trackingTwoPoints && detectingP1 && detectingP2) {
                    trackingTwoPoints = true;
                    mInitialP1 = new Point(mP1);
                    mInitialP2 = new Point(mP2);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                int pointers = event.getPointerCount();
                for (int i = 0; i < pointers; i++) {
                    pointerId = event.getPointerId(i);
                    if (detectingP1 && pointerId == mP1Id) {
                        // point 1 moved
                        mP1 = new Point((int)event.getX(i), (int)event.getY(i));
                    } else if (detectingP2 && pointerId == mP2Id) {
                        // point 2 moved
                        mP2 = new Point((int)event.getX(i), (int)event.getY(i));
                    }
                }

                if (trackingTwoPoints) {
                    calculateRotation();
                    consumed = true;
                }

                break;

            case MotionEvent.ACTION_POINTER_1_UP:
            case MotionEvent.ACTION_POINTER_2_UP:
            case MotionEvent.ACTION_POINTER_3_UP:
            case MotionEvent.ACTION_UP:
                if (detectingP1 && pointerId == mP1Id) {
                    mP1 = new Point((int)event.getX(pointerIndex), (int)event.getY(pointerIndex));
                    detectingP1 = false;
                    mP1Id = -1;
                    trackingTwoPoints = false;
                    if (rotating) {
                        stopRotating();
                    }
                } else if (detectingP2 && pointerId == mP2Id) {
                    mP2 = new Point((int)event.getX(pointerIndex), (int)event.getY(pointerIndex));
                    detectingP2 = false;
                    mP2Id = -1;
                    trackingTwoPoints = false;
                    if (rotating) {
                        stopRotating();
                    }
                } else {
                    // third point up?
                }
                break;
        }
        return consumed;
    }

    private void calculateRotation() {
        if (mListener == null) {
            // why sing if no one is listening?
            return;
        }
        double angle = calculateAngle();
        if (rotating) {
            mListener.onRotationGestureRotated(angle - mStartingAngle, getDistance(), mP1, mP2);
        } else if (trackingTwoPoints) {
            // extra check, just in case. This is the specific state that should start the rotation gesture
            mListener.onRotationGestureStarted(getDistance(), mP1, mP2);
            mStartingAngle = angle;
            rotating = true;
        }
    }

    private double calculateAngle() {
        double numerator = (double)mP2.y - (double)mP1.y;
        double denominator = (double)mP2.x - (double)mP1.x;
        return Math.atan2(numerator, denominator);
    }

    private double getDistance() {
        return 0.0;
    }

    private void stopRotating() {
        if (mListener != null) {
            double angle = calculateAngle();
            mListener.onRotationGestureEnded(angle, getDistance(), mP1, mP2);
        }
        rotating = false;
    }

}
