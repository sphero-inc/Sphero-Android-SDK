package com.orbotix.calibration.api;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.orbotix.calibration.R;
import com.orbotix.calibration.internal.CalibrationCircle;
import com.orbotix.calibration.internal.CalibrationGestureDetector;
import com.orbotix.calibration.utilities.animation.VectorAnimation;
import com.orbotix.calibration.utilities.control.Controller;
import com.orbotix.calibration.utilities.graphic.GlowingDot;
import com.orbotix.calibration.utilities.widget.WidgetGraphicView;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A WidgetGraphicView that helps a user visualize calibration by showing some fancy graphic output when the user
 * does a rotation gesture.
 * </p>
 * <p>
 * To use, add this as a View to your XML layout, or instantiate it manually and add it to a parent View in Java.
 * To add it to a layout resource in XML use its package name as the element name:
 * <pre>
 *         {@code
 *
 *              <orbotix.robot.widgets.calibration.CalibrationView
 *                  android:id="@+id/calibration_widget"
 *                  android:layout_width="fill_parent"
 *                  android:layout_height="fill_parent"
 *                  />
 *         }
 *     </pre>
 * </p>
 * <p>
 * Inside the parent Activity for this View, you must override the Activity's
 * {@link android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)}
 * method, and call this View's {@link #interpretMotionEvent(android.view.MotionEvent)} method, passing the
 * MotionEvent to it:
 * <pre>{@code
 *
 *          public boolean dispatchTouchEvent(MotionEvent event) {
 *              mCalibrationView.interpretMotionEvent(event);
 *              return super.dispatchTouchEvent(event);
 *          }
 *     }
 *     </pre>
 * </p>
 * <p>
 * If the CalibrationView is passed a Robot using the {@link #setRobot(orbotix.robot.base.Robot)} method, then
 * the CalibrationView will calibrate the specified Robot when the user does a multi-touch gesture. If not, then
 * it will still show the graphic, but will do nothing else. The Robot can be unset by passing null.
 * </p>
 * <p>
 * Runnables can be set as callbacks in {@link #setOnStartRunnable(Runnable)}, {@link #setOnRotationRunnable(Runnable)},
 * and {@link #setOnEndRunnable(Runnable)}. These will be run when calibration starts, durring calibration, or when
 * it ends, respectively. These are great place to put sound effects. Additionally, you can extend this View and
 * override the {@link #onRotationStarted(android.graphics.Point, android.graphics.Point)},
 * {@link #onRotation(double, android.graphics.Point, android.graphics.Point)}, and
 * {@link #onRotationEnded(double, android.graphics.Point, android.graphics.Point)} for even more control over these
 * events.
 * </p>
 * <p>
 * The CalibrationView can be disabled with {@link #disable()}, and enabled again with {@link #enable()}, and its
 * visual style can be customized with {@link #setColor(Integer...)}, {@link #setCircleColor(Integer...)},
 * {@link #setDotColor(Integer...)}, {@link #setDot1Color(Integer...)}, {@link #setDot2Color(Integer...)} and {@link #setDotSize(int)}
 * </p>
 *
 *
 *
 * Date: 11/22/11
 *
 * @author Adam Williams
 * @author Jack Thorp
 */
public class CalibrationView extends WidgetGraphicView implements Controller {

    public final static String TAG = "CalibrationView";

    /* Listener for Calibration Event notifications */
    public CalibrationEventListener mEventListener = null;

    //The calibration graphic parts
    private final CalibrationCircle mGlowingCircle;
    private final CalibrationCircle mInnerDashedCircle;
    private final CalibrationCircle mOuterDashedCircle;
    private final GlowingDot mDot1;
    private final GlowingDot mDot2;

    //Animations
    private final List<VectorAnimation> mAnimations = new ArrayList<VectorAnimation>();
    private final IntroAnimation mIntroAnimation = new IntroAnimation();
    private final OutroAnimation mOutroAnimation = new OutroAnimation();

    //Settings
    private int mBackgroundColor = 0x00000000;
    private int mBackgroundOnColor = 0x88000000;
    private boolean mHandlesTouchEvent = false;
    private boolean mEnabled = true;
    //Whether or not the view is in the middle of a single-touch calibration
    private boolean mSingleTouch = false;
    private boolean mShowsBothDots = true;

    // This is a hack to fix the rotation scale, since I am not sure how it works entirely
    private static final float ROTATION_SCALE_FACTOR = 100;

    //Callbacks
    private Runnable mOnStartRunnable = null;
    private Runnable mOnRotationRunnable = null;
    private Runnable mOnEndRunnable = null;

    //Gesture detector
    private RotationGestureDetector mRotationGestureDetector = new RotationGestureDetector();

    //Touch detector
    private TouchGestureDetector mTouchGestureDetector = new TouchGestureDetector();

    //State
    private boolean mIsCalibrating = false;
    private boolean mIsTwoFingerEnabled = true;

    /**
     * Creates a new instance of CalibrationView on the provided Context. Identical to
     * {@link #CalibrationView(android.content.Context, android.util.AttributeSet)} with a
     * null AttributeSet.
     *
     * @param context The Android Context
     * @see #CalibrationView(android.content.Context, android.util.AttributeSet)
     */
    public CalibrationView(Context context) {
        this(context, null);
    }

    /**
     * Creates a new instance of CalibrationView on the provided Context, with the provided AttributeSet. Usually
     * used by the API when CalibrationView is in an XML layout. AttributeSet can be null.
     *
     * @param context The Android Context
     * @param attrs   The AttributeSet containing this View's attributes. Can be null.
     * @see #CalibrationView(android.content.Context)
     */
    public CalibrationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGlowingCircle = new CalibrationCircle(context, 5, 20);
        mInnerDashedCircle = new CalibrationCircle(context, 2).setAsDashed();
        mOuterDashedCircle = new CalibrationCircle(context, 2).setAsDashed();

        mIntroAnimation.setFullInvalidation(true);
        mOutroAnimation.setFullInvalidation(true);
        mAnimations.add(mIntroAnimation);
        mAnimations.add(mOutroAnimation);

        mDot1 = new GlowingDot(context);
        mDot2 = new GlowingDot(context);

        addWidgetPart(mInnerDashedCircle);
        addWidgetPart(mOuterDashedCircle);
        addWidgetPart(mGlowingCircle);
        addWidgetPart(mDot1);
        addWidgetPart(mDot2);

        Point pos = new Point(200, 150);
        int[] colors = new int[] {0xffffffff, 0xff888888};
        mGlowingCircle.setPosition(pos);
        mInnerDashedCircle.setPosition(pos);
        mOuterDashedCircle.setPosition(pos);
        setColor(colors[0], colors[1]);
        setDotSize(40);

        setShowCircleGlow(false);
        setShowDotGlow(true);

        if(attrs != null){
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CalibrationView);
            setIsTwoFingerEnabled(a.getBoolean(R.styleable.CalibrationView_twoFingerEnabled, true));
        }

        // In order for the Calibration Glow to work properly, we must disable 3D Hardware Acceleration
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }


    public void setCalibrationEventListener(CalibrationEventListener eventListener)
    {
        mEventListener = eventListener;
    }

    /**
     * Sets the size of the dots. Sizes are in dips, and will be rescaled.
     *
     * @param size The size, in dips
     */
    public void setDotSize(int size) {

        mDot1.setSize(size);
        mDot2.setSize(size);
    }

    /**
     * Sets the colors of the dots. Accepts one or two integer values. If one value is passed, the
     * second value will automatically become a darker version of it.
     *
     * @param colors One or two colors, as integers.
     * @see #setColor(Integer...)
     */
    public void setDotColor(Integer... colors) {
        mDot1.setColor(colors);
        mDot2.setColor(colors);
    }

    /**
     * Sets the colors of the first dot.
     *
     * @param colors One or two colors, as integers
     * @see #setDotColor(Integer...)
     */
    public void setDot1Color(Integer... colors) {
        mDot1.setColor(colors);
    }

    /**
     * Sets the colors of the second dot.
     *
     * @param colors One or two colors, as integers
     * @see #setDotColor(Integer...)
     */
    public void setDot2Color(Integer... colors) {
        mDot2.setColor(colors);
    }

    /**
     * Sets whether both dots will show. If this is set to false, only dot 1 will show, and dot 2
     * will be hidden.
     *
     * @param show
     */
    public void setShowBothDots(boolean show) {

        mShowsBothDots = show;
    }

    /**
     * Sets the colors of the circles. Accepts one or two integer values. If one value is passed, the
     * second value will automatically become a darker version of it.
     *
     * @param colors One or two colors, as integers.
     * @see #setColor(Integer...)
     */
    public void setCircleColor(Integer... colors) {
        mGlowingCircle.setColor(colors);
        mInnerDashedCircle.setColor(colors);
        mOuterDashedCircle.setColor(colors);
    }

    /**
     * Sets the colors of this WidgetGraphic. Accepts one or two integer color values. The first color is the "inner"
     * color, and the second is an "outer" glow color. If no second color is specified, sets that color to half
     * the luminance of the first.
     *
     * @param colors One or two colors, as integers.
     */
    public void setColor(Integer... colors) {
        setCircleColor(colors);
        setDotColor(colors);
    }

    /**
     * Sets the background colors that this will use.
     *
     * @param color_off The color that the background will be when not showing the WidgetGraphic (default color 0x00000000)
     * @param color_on  The color that the background will be when showing the WidgetGraphic (default 0x88000000)
     */
    public void setBackgroundColors(int color_off, int color_on) {

        mBackgroundColor = color_off;
        mBackgroundOnColor = color_on;
    }

    /**
     * Sets the provided Runnable as an optional Runnable to run when the WidgetGraphic starts showing. If set to
     * null, will clear the Runnable.
     *
     * @param on_start a Runnable to run on start
     */
    public void setOnStartRunnable(Runnable on_start) {
        mOnStartRunnable = on_start;
    }

    /**
     * Sets the provided Runnable as an optional Runnable to run when the WidgetGraphic ends showing. If set to
     * null, will clear the Runnable.
     *
     * @param on_end a Runnable to run on end
     */
    public void setOnEndRunnable(Runnable on_end) {
        mOnEndRunnable = on_end;
    }

    /**
     * Specifies a Runnable to run while the WidgetGraphic is rotating. Use this carefully, because this will run between
     * each frame of animation, and could dramatically slow down the animation.
     *
     * @param on_rotation a Runnable to run on each rotation event
     */
    public void setOnRotationRunnable(Runnable on_rotation) {
        mOnRotationRunnable = on_rotation;
    }

    @Override
    public void setEnabled(boolean val) {
        super.setEnabled(val);
        mEnabled = val;
    }

    /**
     * Disables this CalibrationDialView. Will not show the calibration Widgets. Will not calibrate robot.
     *
     * @see #enable()
     */
    @Override
    public void disable() {
        setEnabled(false);
    }

    /**
     * Enables this CalibrationDialView.
     *
     * @see #disable()
     */
    @Override
    public void enable() {
        setEnabled(true);
    }

    /**
     * Indicates whether this CalibrationView is currently calibrating.
     *
     * @return True, if so
     */
    public boolean isCalibrating() {
        return mIsCalibrating;
    }

    public void setIsTwoFingerEnabled(boolean isTwoFingerEnabled) {
        mIsTwoFingerEnabled = isTwoFingerEnabled;
    }

    private boolean getTwoFingerControllable () {
        return mIsTwoFingerEnabled && !mSingleTouch;
    }

    /**
     * Interprets a MotionEvent, and determines whether it should show the view or not.
     *
     * @param event The MotionEvent from the onTouch event
     */
    @Override
    public void interpretMotionEvent(MotionEvent event) {
        mTouchGestureDetector.onTouch(event);
        if(getTwoFingerControllable()){
            mRotationGestureDetector.onTouch(event);
        }
    }

    /**
     * Starts calibration with a single touch instead of from a rotation gesture, such as from a button press.
     *
     * @param location The position of the 'circle', relative to the location of the touch
     * @param center   The center of the touch. Usually the center of the view touched. If null, it will get it from the event.
     * @param radius   The radius of the 'circle' to be shown
     * @param event    The MotionEvent from the touch
     */
    public void startSingleTouchCalibration(CalibrationCircleLocation location,
                                            Point center, int radius, MotionEvent event) {

        if (!mEnabled) {
            return;
        }

        mSingleTouch = true;

        mTouchGestureDetector.startRotating(center, location, radius, event);
    }

    /**
     * Sets the current radius of the circles
     *
     * @param radius
     */
    private void setRadius(int radius) {
        mGlowingCircle.setSize((radius));
        mInnerDashedCircle.setSize((int) ((radius) * 0.9));
        mOuterDashedCircle.setSize((int) ((radius) * 1.1));
    }


    private float convertAngleToDegrees(double angleInRadians) {
        float angleInDegrees = (float) Math.toDegrees(angleInRadians);
        if (angleInDegrees >= 0.0 && angleInDegrees < 360.0) {
            return angleInDegrees;
        } else if (angleInDegrees < 0.0) {
            return convertAngleToDegrees(angleInRadians + (2.0 * Math.PI));
        } else if (angleInDegrees > 360.0) {
            return convertAngleToDegrees(angleInRadians - (2.0 * Math.PI));
        } else {
            return Math.abs(angleInDegrees);
        }
    }

    private void startRotation(Point p1, Point p2, int radius) {

        mIsCalibrating = true;

        mDot1.setPosition(p1);
        mDot2.setPosition(p2);

        mIntroAnimation.initialize(radius);
        mIntroAnimation.start();

        showAllWidgetParts();

        if (!mShowsBothDots) {
            mDot2.hide();
        }

        setBackgroundColor(mBackgroundOnColor);

        // Check if we have a listener and notify it of a new event
        if(mEventListener != null)
        {
            mEventListener.onCalibrationBegan();
        }

        //Run any possibly overridden onRotationStart method
        onRotationStarted(new Point(p1), new Point(p2));

        //Run the optional start runnable, if it exists
        if (mOnStartRunnable != null) {
            mOnStartRunnable.run();
        }
    }


    private void doRotation(double totalRotationAngle, Point p1, Point p2, Point center, double radius) {

        final float angle = convertAngleToDegrees(totalRotationAngle);

        //Change WidgetGraphicPart properties
        mGlowingCircle.setPosition(center);
        mInnerDashedCircle.setPosition(center);
        mOuterDashedCircle.setPosition(center);
        mInnerDashedCircle.setAngle((angle / 2) * (mGlowingCircle.getSize() / ROTATION_SCALE_FACTOR));
        mOuterDashedCircle.setAngle(-((angle / 2) * (mGlowingCircle.getSize() / ROTATION_SCALE_FACTOR)));

        if (mIntroAnimation.isEnded()) {
            setRadius((int) radius);
        }

        mDot1.setPosition(p1);
        mDot2.setPosition(p2);

        // Check if we have a listener and notify it of a new event
        if(mEventListener != null)
        {
            mEventListener.onCalibrationChanged(angle);
        }

        //Run any possibly overridden onRotation method
        onRotation(totalRotationAngle, p1, p2);

        //Run the optional rotation runnable, if it exists
        if (mOnRotationRunnable != null) {
            mOnRotationRunnable.run();
        }
    }

    private void endRotation(double finalAngle, Point p1, Point p2) {

        mIsCalibrating = false;

        mDot1.hide();
        mDot2.hide();

        mOutroAnimation.start();

        // Check if we have a listener and notify it of a new event
        if(mEventListener != null)
        {
            mEventListener.onCalibrationEnded();
        }

        //Run any possibly overridden onRotationEnded method
        onRotationEnded(finalAngle, p1, p2);

        if (mOnEndRunnable != null) {
            mOnEndRunnable.run();
        }

        mSingleTouch = false;
    }

    /**
     * Override this method to implement functionality that should happen when rotation first starts. Does nothing
     * by default.
     *
     * @param p1 The Point of the first finger's position
     * @param p2 The Point of the second finger's position
     */
    protected void onRotationStarted(Point p1, Point p2) {
        //Override to implement this method
    }

    /**
     * Override this method to implement functionality that should happen on each rotation event.
     *
     * @param totalRotationAngle The angle of the current rotation, in radians
     * @param p1                 The Point of the first finger's position
     * @param p2                 The Point of the second finger's position
     */
    protected void onRotation(double totalRotationAngle, Point p1, Point p2) {
        //Override to implement this method
    }

    /**
     * Override this method to implement functionality that should happen when rotation ends.
     *
     * @param finalAngle The angle of the rotation, in radians
     * @param p1         The Point of the first finger's position
     * @param p2         The Point of the second finger's position
     */
    protected void onRotationEnded(double finalAngle, Point p1, Point p2) {
        //Override to implement this method
    }

    @Override
    public void onDraw(Canvas canvas) {

        for (VectorAnimation d : mAnimations) {
            d.run(canvas, this);
        }

        super.onDraw(canvas);
    }

    /**
     * If set to false, will not show the "glow" behind the widget. Defaults to false.
     *
     * @param val
     */
    public void setShowGlow(boolean val) {
        setShowDotGlow(val);
        setShowCircleGlow(val);
    }

    /**
     * If set to false, will not show a glow behind the dots. Defaults to true.
     *
     * @param val
     */
    public void setShowDotGlow(boolean val) {
        mDot1.setShowGlow(val);
        mDot2.setShowGlow(val);
    }

    /**
     * If set to false, will not show a glow behind the circle. Defaultys to true.
     *
     * @param val
     */
    public void setShowCircleGlow(boolean val) {
        mGlowingCircle.setShowGlow(val);
    }

    /** The animation that shows the circles when the user starts the gesture */
    private class IntroAnimation extends VectorAnimation {

        private static final int sStartRadius = 300;

        private int mTargetRadius;

        public IntroAnimation() {
            super(40, 250);
        }

        public void initialize(int radius) {

            mTargetRadius = radius;

            setRadius(sStartRadius);
        }

        @Override
        protected Rect showFrame(Canvas canvas) {

            final float scale = getScale();

            final int diff = sStartRadius - mTargetRadius;

            setRadius(mTargetRadius + (int) (diff * (1f - scale)));

            return null;
        }
    }

    /** The animation that removes the circles when the user is done with the gesture */
    private class OutroAnimation extends VectorAnimation {

        private static final int sTargetRadius = 300;

        private volatile int mStartRadius = -1;

        public OutroAnimation() {
            super(40, 150);

            //Hide all parts when the animation is over.
            setEndCallback(new Runnable() {
                @Override
                public void run() {
                    mStartRadius = -1;
                    hideAllWidgetsParts();
                    setBackgroundColor(mBackgroundColor);
                }
            });
        }

        @Override
        protected Rect showFrame(Canvas canvas) {

            if (mStartRadius == -1) {
                mStartRadius = mGlowingCircle.getSize();
            }

            final float scale = getScale();
            final int diff = sTargetRadius - mStartRadius;

            mGlowingCircle.setSize(mStartRadius + (int) (diff * scale));

            mInnerDashedCircle.setSize((int) ((mStartRadius * 0.9f) + (diff * scale)));
            mOuterDashedCircle.setSize((int) ((mStartRadius * 1.1f) + (diff * scale)));

            return null;
        }
    }

    /**
     * A class that helps the CalibrationDialView detect the calibration gesture, and gets values it needs to
     * properly render the calibration widget.
     * <p/>
     * Authors: Brandon Dorris, Adam Williams
     * Date: 9/21/11
     * Time: 12:42 PM
     */
    private class RotationGestureDetector extends CalibrationGestureDetector implements CalibrationGestureDetector.RotationEventListener {

        private int mPoint1Id;
        private int mPoint2Id;
        private double mStartingAngle;
        private boolean mDetectingP1;
        private boolean mDetectingP2;
        private boolean mTrackingTwoPoints;

        /** Basic constructor to be used to create a new {@link RotationGestureDetector}. */
        public RotationGestureDetector() {
            mDetectingP1 = false;
            mDetectingP2 = false;

            setOnRotationStartedListener(this);
            setOnRotationListener(this);
            setOnRotationEndedListener(this);
        }

        @Override
        public void onRotationEnded(double angle, Point point1, Point point2, Point centerPoint) {
            endRotation(angle, point1, point2);
            mDetectingP1 = false;
            mDetectingP2 = false;
            mTrackingTwoPoints = false;
        }

        @Override
        public void onRotation(double angle, Point point1, Point point2, Point centerPoint) {

            setCenterPoint(getCenterPoint(getPoint1(), getPoint2()));
            doRotation(angle - mStartingAngle, point1, point2, centerPoint, getRadius());
        }

        @Override
        public void onRotationStarted(Point point1, Point point2, Point centerPoint) {

            setCenterPoint(getCenterPoint(getPoint1(), getPoint2()));
            startRotation(point1, point2, (int) getRadius());
            mStartingAngle = getAngle();
        }

        /**
         * To be called each time your {@link android.view.View} receives a touch event from
         * {@link android.view.View#onTouchEvent(android.view.MotionEvent)}. This will enable rotation gesture detection
         * on this view. If you would like the rotation gesture to be global, you can call this in your activity's
         * {@link android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)}.
         *
         * @param event the {@link android.view.MotionEvent} from the {@link android.view.View}'s
         *              {@link android.view.View#onTouchEvent(android.view.MotionEvent)} method.
         * @return true if the event is consumed, false otherwise
         */
        public void onTouch(MotionEvent event) {
            int pointerIndex = event.getActionIndex();
            int pointerId = event.getPointerId(pointerIndex);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_POINTER_2_DOWN:
                case MotionEvent.ACTION_DOWN:
                    if (mEnabled) {
                        if (!mDetectingP1) {
                            mPoint1Id = pointerId;
                            setPoint1(new Point((int) event.getX(pointerIndex), (int) event.getY(pointerIndex)));
                            mDetectingP1 = true;


                        } else if (!mDetectingP2) {
                            mPoint2Id = pointerId;
                            setPoint2(new Point((int) event.getX(pointerIndex), (int) event.getY(pointerIndex)));
                            mDetectingP2 = true;


                        } else {
                            // three points?
                        }

                        if (mDetectingP1 && mDetectingP2 && !mTrackingTwoPoints) {
                            mTrackingTwoPoints = true;
                            setCenterPoint(getCenterPoint(getPoint1(), getPoint2()));
                            //startRotating();
                        }
                    }

                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mEnabled) {
                        int pointers = event.getPointerCount();
                        for (int i = 0; i < pointers; i++) {
                            int id = event.getPointerId(i);
                            if (mDetectingP1 && id == mPoint1Id) {
                                // point 1 moved
                                //DLog.d(TAG, "Pointer 1 moved");
                                setPoint1(new Point((int) event.getX(i), (int) event.getY(i)));

                                if (getCenterPoint() != null && !mDetectingP2) {
                                    setPoint2(getOppositePoint(getPoint1(), getCenterPoint()));
                                }
                            } else if (mDetectingP2 && id == mPoint2Id) {
                                // point 2 moved
                                //DLog.d(TAG, "Pointer 2 moved");
                                setPoint2(new Point((int) event.getX(i), (int) event.getY(i)));

                                if (getCenterPoint() != null && !mDetectingP1) {
                                    setPoint1(getOppositePoint(getPoint2(), getCenterPoint()));
                                }
                            }
                        }

                        if (mTrackingTwoPoints) {
                            if (!isRotating()) {
                                startRotating();
                            } else {
                                rotate();
                            }
                        }
                    }


                    break;

                case MotionEvent.ACTION_POINTER_1_UP:
                case MotionEvent.ACTION_POINTER_2_UP:
                case MotionEvent.ACTION_POINTER_3_UP:
                case MotionEvent.ACTION_UP:
                    if (mDetectingP1 && pointerId == mPoint1Id) {

                        if (mTrackingTwoPoints && getPoint1() != null && getPoint2() != null) {
                            setCenterPoint(getCenterPoint(getPoint1(), getPoint2()));
                        }

                        setPoint1(new Point((int) event.getX(pointerIndex), (int) event.getY(pointerIndex)));
                        mDetectingP1 = false;
                        mPoint1Id = -1;


                    } else if (mDetectingP2 && pointerId == mPoint2Id) {

                        if (mTrackingTwoPoints && getPoint1() != null && getPoint2() != null) {
                            setCenterPoint(getCenterPoint(getPoint1(), getPoint2()));
                        }

                        setPoint2(new Point((int) event.getX(pointerIndex), (int) event.getY(pointerIndex)));
                        mDetectingP2 = false;
                        mPoint2Id = -1;

                    } else {
                        // third point up?
                    }

                    if (!mDetectingP1 && !mDetectingP2 && mTrackingTwoPoints) {
                        mTrackingTwoPoints = false;
                        stopRotating();
                        setCenterPoint(null);
                    }
                    break;
            }
        }
    }

    /**
     * A class that helps the CalibrationDialView detect the calibration gesture, and gets values it needs to
     * properly render the calibration widget.
     * <p/>
     * Date: 10/23/12
     * Time: 12:42 PM
     */
    private class TouchGestureDetector extends CalibrationGestureDetector implements CalibrationGestureDetector.RotationEventListener {
        private int mPoint1Id;
        boolean detectingP1;
        private int mRadius = 0;
        private boolean mPreviousBothDotsValue = true;
        private double mStartingAngle = 0;

        /** Basic constructor to be used to create a new {@link TouchGestureDetector}. */
        public TouchGestureDetector() {
            detectingP1 = false;

            setOnRotationStartedListener(this);
            setOnRotationListener(this);
            setOnRotationEndedListener(this);
        }

        @Override
        public void onRotationEnded(double angle, Point point1, Point point2, Point centerPoint) {
            endRotation(angle, point1, point2);
            setShowBothDots(mPreviousBothDotsValue);
            mSingleTouch = false;
        }

        @Override
        public void onRotation(double angle, Point point1, Point point2, Point centerPoint) {

            doRotation(angle - mStartingAngle, point1, point2, getCenterPoint(), mRadius);
        }

        @Override
        public void onRotationStarted(Point point1, Point point2, Point centerPoint) {

            mInnerDashedCircle.setPosition(centerPoint);
            mOuterDashedCircle.setPosition(centerPoint);
            mGlowingCircle.setPosition(centerPoint);

            setRadius(mRadius);

            mPreviousBothDotsValue = mShowsBothDots;
            setShowBothDots(false);

            setPoint2(getOppositePoint(getPoint1(), getCenterPoint()));

            startRotation(getPoint1(), getPoint2(), mRadius);

            mStartingAngle = getAngle();
        }

        public void startRotating(Point center, CalibrationCircleLocation location, int radius, MotionEvent event) {

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && !detectingP1) {

                mRadius = radius;

                int shiftFactorX = location.getShiftFactorX();
                int shiftFactorY = location.getShiftFactorY();

                Point c = new Point(center);
                c.x += shiftFactorX * (radius);
                c.y += shiftFactorY * (radius);

                setCenterPoint(c);

                int pointerIndex = event.getActionIndex();
                int pointerId = event.getPointerId(pointerIndex);
                Point p = new Point((int) event.getRawX(), (int) event.getRawY());
                p = getConstrainedPoint(p, radius);
                setPoint1(p);

                mPoint1Id = pointerId;
                detectingP1 = true;

                startRotating();
            }
        }

        /**
         * To be called each time your {@link android.view.View} receives a touch event from
         * {@link android.view.View#onTouchEvent(android.view.MotionEvent)}. This will enable rotation gesture detection
         * on this view. If you would like the rotation gesture to be global, you can call this in your activity's
         * {@link android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)}.
         *
         * @param event the {@link android.view.MotionEvent} from the {@link android.view.View}'s
         *              {@link android.view.View#onTouchEvent(android.view.MotionEvent)} method.
         * @return true if the event is consumed, false otherwise
         */
        public void onTouch(MotionEvent event) {

            if (mSingleTouch) {
                int pointerIndex = event.getActionIndex();
                int pointerId = event.getPointerId(pointerIndex);

                switch (event.getActionMasked()) {

                    case MotionEvent.ACTION_MOVE:
                        if (mEnabled && getCenterPoint() != null) {
                            int pointers = event.getPointerCount();
                            for (int i = 0; i < pointers; i++) {
                                pointerId = event.getPointerId(i);
                                // Get the movement of the the touch event we process in ACTION_DOWN
                                if (detectingP1 && pointerId == mPoint1Id) {
                                    // point 1 moved
                                    Point p = new Point((int) event.getX(i), (int) event.getY(i));
                                    p = getConstrainedPoint(p, mGlowingCircle.getSize());
                                    setPoint1(p);
                                    setPoint2(getOppositePoint(p, getCenterPoint()));
                                    rotate();
                                }
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (detectingP1 && pointerId == mPoint1Id) {
                            Point p = new Point((int) event.getX(pointerIndex), (int) event.getY(pointerIndex));
                            p = getConstrainedPoint(p, mGlowingCircle.getSize());
                            setPoint1(p);
                            setPoint2(getOppositePoint(p, getCenterPoint()));
                            detectingP1 = false;
                            mPoint1Id = -1;

                            // Disable Center Point and Stop Rotating
                            stopRotating();
                        }
                        break;
                }
            }
        }
    }

    /** Used to define different draw locations of the calibration circle */
    public enum CalibrationCircleLocation {
        /** Calibration circle will be drawn left of the button */
        LEFT(-1, 0, -90f),

        /** Calibration circle will be drawn above the button */
        ABOVE(0, -1, 0f),

        /** Calibration circle will be drawn right of the button */
        RIGHT(1, 0, 90f),

        /** Calibration circle will be drawn below the button */
        BELOW(0, 1, 180f),

        NONE(0,0,0);


        private final int mShiftFactorX;
        private final int mShiftFactorY;
        private final float mAngle;

        private CalibrationCircleLocation(int shiftFactorX, int shiftFactorY, float angle) {
            mShiftFactorX = shiftFactorX;
            mShiftFactorY = shiftFactorY;
            mAngle = angle;
        }

        /**
         * The shift factor of the x placement of this orientation, used to place the calibration widget
         *
         * @return
         */
        int getShiftFactorX() {
            return mShiftFactorX;
        }

        /**
         * The shift factor of the y placement of this orientation, used to place the calibration widget
         *
         * @return
         */
        int getShiftFactorY() {
            return mShiftFactorY;
        }

        /**
         * The angle adjustment of this orientation
         *
         * @return
         */
        public float getAngle() {
            return mAngle;
        }
    }
}
