package orbotix.robot.widgets.calibration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import orbotix.robot.animation.VectorAnimation;
import orbotix.robot.base.CalibrateCommand;
import orbotix.robot.base.FrontLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RollCommand;
import orbotix.robot.widgets.Controller;
import orbotix.robot.widgets.WidgetGraphicView;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *     A WidgetGraphicView that helps a user visualize calibration by showing some fancy graphic output when the user
 *     does a rotation gesture.
 * </p>
 * <p>
 *     To use, add this as a View to your XML layout, or instantiate it manually and add it to a parent View in Java.
 *     To add it to a layout resource in XML use its package name as the element name:
 *     <pre>
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
 *     Inside the parent Activity for this View, you must override the Activity's
 *     {@link android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)}
 *     method, and call this View's {@link #interpretMotionEvent(android.view.MotionEvent)} method, passing the
 *     MotionEvent to it:
 *     <pre>{@code
 *
 *          public boolean dispatchTouchEvent(MotionEvent event) {
 *              mCalibrationView.interpretMotionEvent(event);
 *              return super.dispatchTouchEvent(event);
 *          }
 *     }
 *     </pre>
 * </p>
 * <p>
 *     If the CalibrationView is passed a Robot using the {@link #setRobot(orbotix.robot.base.Robot)} method, then
 *     the CalibrationView will calibrate the specified Robot when the user does a multi-touch gesture. If not, then
 *     it will still show the graphic, but will do nothing else. The Robot can be unset by passing null.
 * </p>
 * <p>
 *     Runnables can be set as callbacks in {@link #setOnStartRunnable(Runnable)}, {@link #setOnRotationRunnable(Runnable)},
 *     and {@link #setOnEndRunnable(Runnable)}. These will be run when calibration starts, durring calibration, or when
 *     it ends, respectively. These are great place to put sound effects. Additionally, you can extend this View and
 *     override the {@link #onRotationStarted(android.graphics.Point, android.graphics.Point)},
 *     {@link #onRotation(double, android.graphics.Point, android.graphics.Point)}, and
 *     {@link #onRotationEnded(double, android.graphics.Point, android.graphics.Point)} for even more control over these
 *     events.
 * </p>
 * <p>
 *     The CalibrationView can be disabled with {@link #disable()}, and enabled again with {@link #enable()}, and its
 *     visual style can be customized with {@link #setColor(Integer...)}, {@link #setCircleColor(Integer...)},
 *     {@link #setDotColor(Integer...)}, {@link #setDot1Color(Integer...)}, {@link #setDot2Color(Integer...)} and {@link #setDotSize(int)}
 * </p>
 *
 *
 * Created by Orbotix Inc.
 * Date: 11/22/11
 * @author Adam Williams
 */
public class CalibrationView extends WidgetGraphicView implements Controller {

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
    private int mBackgroundColor   = 0x00000000;
    private int mBackgroundOnColor = 0x88000000;
    private boolean mHandlesTouchEvent = false;
    private boolean mEnabled = true;

    //The Robot to calibrate
    private Robot mRobot = null;

    //Callbacks
    private Runnable mOnStartRunnable    = null;
    private Runnable mOnRotationRunnable = null;
    private Runnable mOnEndRunnable      = null;

    //Gesture detector
    private RotationGestureDetector mRotationGestureDetector = new RotationGestureDetector();

    //State
    private boolean mIsCalibrating = false;

    /**
     * Creates a new instance of CalibrationView on the provided Context. Identical to
     * {@link #CalibrationView(android.content.Context, android.util.AttributeSet)} with a
     * null AttributeSet.
     * @param context The Android Context
     * @see #CalibrationView(android.content.Context, android.util.AttributeSet)
     */
    public CalibrationView(Context context){
        this(context, null);
    }

    /**
     * Creates a new instance of CalibrationView on the provided Context, with the provided AttributeSet. Usually
     * used by the API when CalibrationView is in an XML layout. AttributeSet can be null.
     * @param context The Android Context
     * @param attrs The AttributeSet containing this View's attributes. Can be null.
     * @see #CalibrationView(android.content.Context)
     */
    public CalibrationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGlowingCircle = new CalibrationCircle(context);
        mInnerDashedCircle = new CalibrationCircle(context, 2, 10).setAsDashed();
        mOuterDashedCircle = new CalibrationCircle(context, 2, 10).setAsDashed();

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
        int[] colors = new int[]{0xffffff88, 0xffff3300};
        mGlowingCircle.setPosition(pos);
        mInnerDashedCircle.setPosition(pos);
        mOuterDashedCircle.setPosition(pos);
        setColor(colors[0], colors[1]);
        setDotSize(40);
    }

    /**
     * Sets the size of the dots. Sizes are in dips, and will be rescaled.
     * @param size The size, in dips
     */
    public void setDotSize(int size){

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
    public void setDotColor(Integer... colors){
        mDot1.setColor(colors);
        mDot2.setColor(colors);
    }

    /**
     * Sets the colors of the first dot.
     *
     * @param colors One or two colors, as integers
     * @see #setDotColor(Integer...)
     */
    public void setDot1Color(Integer... colors){
        mDot1.setColor(colors);
    }

    /**
     * Sets the colors of the second dot.
     * @param colors One or two colors, as integers
     * @see #setDotColor(Integer...)
     */
    public void setDot2Color(Integer... colors){
        mDot2.setColor(colors);
    }

    /**
     * Sets the colors of the circles. Accepts one or two integer values. If one value is passed, the
     * second value will automatically become a darker version of it.
     *
     * @param colors One or two colors, as integers.
     * @see #setColor(Integer...)
     */
    public void setCircleColor(Integer... colors){
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
    public void setColor(Integer... colors){
        setCircleColor(colors);
        setDotColor(colors);
    }

    /**
     * Sets the background colors that this will use.
     * @param color_off The color that the background will be when not showing the WidgetGraphic (default color 0x00000000)
     * @param color_on The color that the background will be when showing the WidgetGraphic (default 0x88000000)
     */
    public void setBackgroundColors(int color_off, int color_on){

        mBackgroundColor   = color_off;
        mBackgroundOnColor = color_on;
    }

    /**
     * Sets the provided Runnable as an optional Runnable to run when the WidgetGraphic starts showing. If set to
     * null, will clear the Runnable.
     * @param on_start a Runnable to run on start
     */
    public void setOnStartRunnable(Runnable on_start){
        mOnStartRunnable = on_start;
    }

    /**
     * Sets the provided Runnable as an optional Runnable to run when the WidgetGraphic ends showing. If set to
     * null, will clear the Runnable.
     * @param on_end a Runnable to run on end
     */
    public void setOnEndRunnable(Runnable on_end){
        mOnEndRunnable = on_end;
    }

    /**
     * Specifies a Runnable to run while the WidgetGraphic is rotating. Use this carefully, because this will run between
     * each frame of animation, and could dramatically slow down the animation.
     * @param on_rotation a Runnable to run on each rotation event
     */
    public void setOnRotationRunnable(Runnable on_rotation){
        mOnRotationRunnable = on_rotation;
    }

    @Override
    public void setEnabled(boolean val){
        super.setEnabled(val);
        mEnabled = val;
    }

    /**
     * Disables this CalibrationDialView. Will not show the calibration Widgets. Will not calibrate robot.
     * @see #enable()
     */
    @Override
    public void disable(){
        setEnabled(false);
    }

    /**
     * Enables this CalibrationDialView.
     * @see #disable()
     */
    @Override
    public void enable(){
        setEnabled(true);
    }

    /**
     * Indicates whether this CalibrationView is currently calibrating.
     *
     * @return True, if so
     */
    public boolean isCalibrating(){
        return mIsCalibrating;
    }

    /**
     * Sets the Robot for this CalibrationDialView. If not null, the CalibrationDialView will calibrate the
     * provided robot. If set to null, it will still display, but will not calibrate.
     *
     * @param robot A Robot to calibrate
     */
    public void setRobot(Robot robot){
        mRobot = robot;
    }

    /**
     * Interprets a MotionEvent, and determines whether it should show the view or not.
     * @param event The MotionEvent from the onTouch event
     */
    @Override
    public void interpretMotionEvent(MotionEvent event){

        //TODO: Correct coordinates for the position of the View
        mRotationGestureDetector.onTouchEvent(event);
    }

    //TODO: Handle onTouchEvent if the user has set mHandleTouchEvent

    private Point getCenterPoint(Point p1, Point p2){
        return new Point(((p1.x + p2.x) / 2), ((p1.y + p2.y) / 2));
    }

    private double getRadius(Point p1, Point p2){
        final int a = Math.abs(Math.abs(p1.y) - Math.abs(p2.y));
        final int b = Math.abs(Math.abs(p1.x) - Math.abs(p2.x));
        return Math.hypot(b, a) / 2f;
    }

    private float convertAngleToDegrees(double angleInRadians) {
        float angleInDegrees = (float)Math.toDegrees(angleInRadians);
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

    private void startRotation(Point p1, Point p2) {

        mIsCalibrating = true;

        mDot1.setPosition(p1);
        mDot2.setPosition(p2);

        mIntroAnimation.initialize((int)getRadius(p1, p2));
        mIntroAnimation.start();

        showAllWidgetParts();

        setBackgroundColor(mBackgroundOnColor);

        //If there is a robot, then zero its calibration and show the LED
        if(mRobot != null){
            //DriveControl.INSTANCE.stopDriving();
            FrontLEDOutputCommand.sendCommand(mRobot, 1f);
            CalibrateCommand.sendCommand(mRobot, 0f);
        }

        //Run any possibly overridden onRotationStart method
        onRotationStarted(new Point(p1), new Point(p2));

        //Run the optional start runnable, if it exists
        if(mOnStartRunnable != null){
            mOnStartRunnable.run();
        }
    }

    private void doRotation(double totalRotationAngle, Point p1, Point p2) {

        final Point center = getCenterPoint(p1, p2);
        final double radius = getRadius(p1, p2);
        final float angle   = convertAngleToDegrees(2.0 *totalRotationAngle);

        //Change WidgetGraphicPart properties
        mGlowingCircle.setPosition(center);
        mInnerDashedCircle.setPosition(center);
        mOuterDashedCircle.setPosition(center);
        mInnerDashedCircle.setAngle(angle);
        mOuterDashedCircle.setAngle(-angle);

        if(mIntroAnimation.isEnded()){
            mGlowingCircle.setSize((int)radius);
            mInnerDashedCircle.setSize((int)(radius * 0.9));
            mOuterDashedCircle.setSize((int)(radius * 1.1));
        }

        mDot1.setPosition(p1);
        mDot2.setPosition(p2);

        //If there is a robot, then rotate it
        if(mRobot != null){
            RollCommand.sendCommand(mRobot, angle, 0.0f, false);
        }

        //Run any possibly overridden onRotation method
        onRotation(totalRotationAngle, p1, p2);

        //Run the optional rotation runnable, if it exists
        if(mOnRotationRunnable != null){
            mOnRotationRunnable.run();
        }
    }

    private void endRotation(double finalAngle, Point p1, Point p2) {

        mIsCalibrating = false;

        mDot1.hide();
        mDot2.hide();

        mOutroAnimation.start();

        //If there is a robot, stop calibration
        if(mRobot != null){
            // I'm not sure why this line is here, it initializes the joystickDriveAlgorithm with a size of 0,0 if
            // it does not already exist. This causes problems if a joystick is used after this is called.
            // commenting out for now, but I don't think it is necessary at all.
            //DriveControl.INSTANCE.startDriving(this.getContext(), DriveControl.JOY_STICK);
            RollCommand.sendCommand(mRobot, (float)finalAngle, 0.0f, true);
            FrontLEDOutputCommand.sendCommand(mRobot, 0f);
            CalibrateCommand.sendCommand(mRobot, 0f);
        }

        //Run any possibly overridden onRotationEnded method
        onRotationEnded(finalAngle, p1, p2);

        if(mOnEndRunnable != null){
            mOnEndRunnable.run();
        }
    }

    /**
     * Override this method to implement functionality that should happen when rotation first starts. Does nothing
     * by default.
     *
     * @param p1 The Point of the first finger's position
     * @param p2 The Point of the second finger's position
     */
    protected void onRotationStarted(Point p1, Point p2){
        //Override to implement this method
    }

    /**
     * Override this method to implement functionality that should happen on each rotation event.
     * @param totalRotationAngle The angle of the current rotation, in radians
     * @param p1 The Point of the first finger's position
     * @param p2 The Point of the second finger's position
     */
    protected void onRotation(double totalRotationAngle, Point p1, Point p2){
        //Override to implement this method
    }

    /**
     * Override this method to implement functionality that should happen when rotation ends.
     * @param finalAngle The angle of the rotation, in radians
     * @param p1 The Point of the first finger's position
     * @param p2 The Point of the second finger's position
     */
    protected void onRotationEnded(double finalAngle, Point p1, Point p2){
        //Override to implement this method
    }

    @Override
    public void onDraw(Canvas canvas){

        for(VectorAnimation d : mAnimations){
            d.run(canvas, this);
        }

        super.onDraw(canvas);
    }

    /**
     * If set to false, will not show the "glow" behind the widget. Defaults to true.
     * @param val
     */
    public void setShowGlow(boolean val){

        mGlowingCircle.setShowGlow(val);
        mInnerDashedCircle.setShowGlow(val);
        mOuterDashedCircle.setShowGlow(val);
        mDot1.setShowGlow(val);
        mDot2.setShowGlow(val);
    }


    /**
     * The animation that shows the circles when the user starts the gesture
     */
    private class IntroAnimation extends VectorAnimation {

        private static final int sStartRadius = 300;

        private int   mTargetRadius;

        public IntroAnimation() {
            super(40, 250);
        }

        public void initialize(int radius){

            mTargetRadius = radius;

            mGlowingCircle.setSize(sStartRadius);
            mInnerDashedCircle.setSize(sStartRadius);
            mOuterDashedCircle.setSize(sStartRadius);
        }

        @Override
        protected Rect showFrame(Canvas canvas) {

            final float scale = getScale();

            final int diff = sStartRadius - mTargetRadius;

            mGlowingCircle.setSize(mTargetRadius + (int)(diff * (1f - scale)));

            mInnerDashedCircle.setSize((int)((mTargetRadius * 0.9f) + (diff * (1f - scale))));
            mOuterDashedCircle.setSize((int)((mTargetRadius * 1.1f) + (diff * (1f - scale))));

            return null;
        }
    }

    /**
     * The animation that removes the circles when the user is done with the gesture
     */
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

            if(mStartRadius == -1){
                mStartRadius = mGlowingCircle.getSize();
            }

            final float scale = getScale();
            final int diff = sTargetRadius - mStartRadius;

            mGlowingCircle.setSize(mStartRadius + (int)(diff * scale));

            mInnerDashedCircle.setSize((int)((mStartRadius * 0.9f) + (diff * scale)));
            mOuterDashedCircle.setSize((int)((mStartRadius * 1.1f) + (diff * scale)));

            return null;
        }
    }

    /**
     * A class that helps the CalibrationDialView detect the calibration gesture, and gets values it needs to
     * properly render the calibration widget.
     *
     * Authors: Brandon Dorris, Adam Williams
     * Date: 9/21/11
     * Time: 12:42 PM
     */
    private class RotationGestureDetector {
        private Point mP1, mP2, mCenterPoint;
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

        /**
         * To be called each time your {@link android.view.View} receives a touch event from
         * {@link android.view.View#onTouchEvent(android.view.MotionEvent)}. This will enable rotation gesture detection
         * on this view. If you would like the rotation gesture to be global, you can call this in your activity's
         * {@link android.app.Activity#dispatchTouchEvent(android.view.MotionEvent)}.
         * @param event the {@link android.view.MotionEvent} from the {@link android.view.View}'s
         * {@link android.view.View#onTouchEvent(android.view.MotionEvent)} method.
         * @return true if the event is consumed, false otherwise
         */
        public boolean onTouchEvent(MotionEvent event) {
            boolean consumed = false;
            int pointerIndex = event.getActionIndex();
            int pointerId = event.getPointerId(pointerIndex);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_POINTER_2_DOWN:
                case MotionEvent.ACTION_DOWN:
                    if(mEnabled){
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
                            consumed = true;
                        }
                    }

                    break;

                case MotionEvent.ACTION_MOVE:
                    if(mEnabled){
                        int pointers = event.getPointerCount();
                        for (int i = 0; i < pointers; i++) {
                            pointerId = event.getPointerId(i);
                            if (detectingP1 && pointerId == mP1Id) {
                                // point 1 moved
                                mP1 = new Point((int)event.getX(i), (int)event.getY(i));

                                if(mCenterPoint != null && !detectingP2){
                                    mP2 = getOppositePoint(mP1, mCenterPoint);
                                }
                            } else if (detectingP2 && pointerId == mP2Id) {
                                // point 2 moved
                                mP2 = new Point((int)event.getX(i), (int)event.getY(i));

                                if(mCenterPoint != null && !detectingP1){
                                    mP1 = getOppositePoint(mP2, mCenterPoint);
                                }
                            }
                        }

                        if (trackingTwoPoints) {
                            calculateRotation();
                            consumed = true;
                        }
                    }


                    break;

                case MotionEvent.ACTION_POINTER_1_UP:
                case MotionEvent.ACTION_POINTER_2_UP:
                case MotionEvent.ACTION_POINTER_3_UP:
                case MotionEvent.ACTION_UP:
                    if (detectingP1 && pointerId == mP1Id) {

                        if(trackingTwoPoints && mP1 != null && mP2 != null){
                            mCenterPoint = getCenterPoint(mP1, mP2);
                        }

                        mP1 = new Point((int)event.getX(pointerIndex), (int)event.getY(pointerIndex));
                        detectingP1 = false;
                        mP1Id = -1;


                    } else if (detectingP2 && pointerId == mP2Id) {

                        if(trackingTwoPoints && mP1 != null && mP2 != null){
                            mCenterPoint = getCenterPoint(mP1, mP2);
                        }

                        mP2 = new Point((int)event.getX(pointerIndex), (int)event.getY(pointerIndex));
                        detectingP2 = false;
                        mP2Id = -1;

                    } else {
                        // third point up?
                    }

                    if(!detectingP1 && !detectingP2 && trackingTwoPoints){
                        trackingTwoPoints = false;
                        mCenterPoint = null;
                        stopRotating();
                    }
                    break;
            }
            return consumed;
        }

        private Point getOppositePoint(Point point, Point center){

            //correct coordinates to zero center
            final Point c_point = new Point(point.x - center.x, point.y - center.y);

            //flip coord signs
            c_point.x = -c_point.x;
            c_point.y = -c_point.y;

            //revert coordinates to old center
            c_point.x = c_point.x+center.x;
            c_point.y = c_point.y+center.y;

            return c_point;
        }

        private void calculateRotation() {

            double angle = calculateAngle();
            if (rotating) {
                doRotation(angle - mStartingAngle, mP1, mP2);
            } else if (trackingTwoPoints) {
                // extra check, just in case. This is the specific state that should start the rotation gesture
                startRotation(mP1, mP2);
                mStartingAngle = angle;
                rotating = true;
            }
        }

        private double calculateAngle() {
            double numerator = (double)mP2.y - (double)mP1.y;
            double denominator = (double)mP2.x - (double)mP1.x;
            return Math.atan2(numerator, denominator);
        }

        private void stopRotating() {
            double angle = calculateAngle();
            endRotation(angle, mP1, mP2);
            rotating = false;
        }

    }

}
