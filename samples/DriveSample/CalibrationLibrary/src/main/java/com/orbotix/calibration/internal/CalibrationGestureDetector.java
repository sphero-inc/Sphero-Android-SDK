package com.orbotix.calibration.internal;

import android.graphics.Point;

/**
 * An {@link OnTouchListener} that handles calibration gestures
 * <p/>
 * <p/>
 * Date: 2/5/13
 *
 * @author Adam Williams
 */
public abstract class CalibrationGestureDetector implements OnTouchListener {

    //Event blocks
    private OnRotationStartedListener mOnRotationStartedListener;
    private OnRotationListener mOnRotationListener;
    private OnRotationEndedListener mOnRotationEndedListener;


    private Point mCenterPoint;
    private boolean mRotating;
    private Point mPoint1;
    private Point mPoint2;


    public void setOnRotationStartedListener(OnRotationStartedListener onRotationStartedListener) {
        mOnRotationStartedListener = onRotationStartedListener;
    }

    public void setOnRotationListener(OnRotationListener onRotationListener) {
        mOnRotationListener = onRotationListener;
    }

    public void setOnRotationEndedListener(OnRotationEndedListener onRotationEndedListener) {
        mOnRotationEndedListener = onRotationEndedListener;
    }

    public void setCenterPoint(Point centerPoint) {
        mCenterPoint = centerPoint;
    }

    public void setPoint1(Point point1) {
        mPoint1 = point1;
    }

    public void setPoint2(Point point2) {
        mPoint2 = point2;
    }

    public Point getCenterPoint() {
        return mCenterPoint;
    }

    public boolean isRotating() {
        return mRotating;
    }

    public Point getPoint1() {
        return mPoint1;
    }

    public Point getPoint2() {
        return mPoint2;
    }

    public void startRotating() {

        mRotating = true;

        if (mOnRotationStartedListener != null) {
            mOnRotationStartedListener.onRotationStarted(mPoint1, mPoint2, mCenterPoint);
        }
    }

    // Calculate and translate angle of
    public void rotate() {

        if (mRotating) {
            final double angle = getAngle();
            if (mOnRotationListener != null) {
                mOnRotationListener.onRotation(angle, mPoint1, mPoint2, mCenterPoint);
            }
        }

        //TODO: use the block
        //doRotation(angle, mPoint1, getOppositePoint(mPoint1, mCenterPoint));
    }

    /**
     * Calculates the angle between the touch x and y and the center of the widget
     *
     * @return
     */
    public double getAngle() {
        double numerator = (double) mCenterPoint.x - (double) mPoint1.x;
        double denominator = (double) mCenterPoint.y - (double) mPoint1.y;
        return -Math.atan2(numerator, denominator);
    }

    /** Called to end the rotation on a touch up event */
    public void stopRotating() {
        if (mRotating) {
            mRotating = false;
            final double angle = getAngle();

            if (mOnRotationEndedListener != null) {
                mOnRotationEndedListener.onRotationEnded(angle, mPoint1, mPoint2, mCenterPoint);
            }
        }

        //TODO: use the block
        //endRotation(angle, mPoint1, getOppositePoint(mPoint1, mCenterPoint));
    }

    /**
     * Constrains the provided point to the provided radius
     *
     * @param point
     * @return
     */
    protected Point getConstrainedPoint(Point point, int radius) {

        point = getCenteredPoint(point);
        final double current_radius = Math.hypot(point.x, point.y);

        if (current_radius == 0) {
            point.x = radius;
            point.y = 0;
        } else {
            final double ratio = radius / current_radius;
            point.x = (int) (point.x * ratio);
            point.y = (int) (point.y * ratio);
        }

        point = getUnCenteredPoint(point);

        return point;
    }

    private Point getCenteredPoint(Point point) {
        Point p = new Point(point);
        p.x -= mCenterPoint.x;
        p.y -= mCenterPoint.y;
        return p;
    }

    private Point getUnCenteredPoint(Point point) {
        Point p = new Point(point);
        p.x += mCenterPoint.x;
        p.y += mCenterPoint.y;
        return p;
    }

    protected Point getOppositePoint(Point point, Point center) {

        //correct coordinates to zero center
        final Point c_point = new Point(point.x - center.x, point.y - center.y);

        //flip coord signs
        c_point.x = -c_point.x;
        c_point.y = -c_point.y;

        //revert coordinates to old center
        c_point.x = c_point.x + center.x;
        c_point.y = c_point.y + center.y;

        return c_point;
    }

    protected Point getCenterPoint(Point p1, Point p2) {
        return new Point(((p1.x + p2.x) / 2), ((p1.y + p2.y) / 2));
    }

    protected double getRadius(Point p1, Point p2) {
        final int a = Math.abs(Math.abs(p1.y) - Math.abs(p2.y));
        final int b = Math.abs(Math.abs(p1.x) - Math.abs(p2.x));
        return Math.hypot(b, a) / 2f;
    }

    protected double getRadius() {
        return getRadius(mPoint1, mPoint2);
    }

    /** Runs when the rotation has started */
    public interface OnRotationStartedListener {

        public void onRotationStarted(Point point1, Point point2, Point centerPoint);
    }

    /** Runs on each rotation event */
    public interface OnRotationListener {


        public void onRotation(double angle, Point point1, Point point2, Point centerPoint);
    }

    /** Runs when the rotation has ended */
    public interface OnRotationEndedListener {

        public void onRotationEnded(double angle, Point point1, Point point2, Point centerPoint);
    }

    /** Convenience Interface for all listeners in one */
    public interface RotationEventListener extends OnRotationStartedListener, OnRotationListener, OnRotationEndedListener {

    }

}
