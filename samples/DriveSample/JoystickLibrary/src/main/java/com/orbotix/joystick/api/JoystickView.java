package com.orbotix.joystick.api;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.orbotix.joystick.R;
import com.orbotix.joystick.internal.JoystickPuck;
import com.orbotix.joystick.internal.JoystickWheel;
import com.orbotix.joystick.utilities.control.Controller;
import com.orbotix.joystick.utilities.math.Value;

/**
 * View that displays the Joystick, and handles the user's interactions with it.
 *
 * Created by Orbotix Inc.
 * @author Adam Williams
 */
public class JoystickView extends View implements Controller {
    private static final String TAG = "OBX-JoystickView";

    /* Listener for Joystick Event notifications */
    private JoystickEventListener mEventListener = null;

    private final JoystickPuck puck;
    private final JoystickWheel mWheel;

    private int puck_radius  = 25;
    private int wheel_radius = 75;
    private int puck_edge_overlap = 30;

    private final Point center_point = new Point();

    private double mJoystickPadCenterX = 0;
    private double mJoystickPadCenterY = 0;

    private boolean mEnabled = true;
	private volatile boolean draggingPuck = false;
	private int		draggingPuckPointerId;

    /** The Joystick's Angle from 0 degrees */
    private double mJoystickAngleDegrees;

    /** The Joystick's Distance from the center */
    private double mJoystickDistanceFromCenter;

    private Runnable mOnStartRunnable;
    private Runnable mOnDragRunnable;
    private Runnable mOnEndRunnable;

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.puck = new JoystickPuck();
        this.mWheel = new JoystickWheel();

        if(attrs != null){
            TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.JoystickView);

            //Get puck size
            this.puck_radius = (int)a.getDimension(R.styleable.JoystickView_puck_radius, 25);

            //Get alpha
            this.setAlpha(a.getFloat(R.styleable.JoystickView_alpha, 1));

            //Get edge overlap
            this.puck_edge_overlap = (int)a.getDimension(R.styleable.JoystickView_edge_overlap, 10);

            setWheelColor(a.getColor(R.styleable.JoystickView_color_wheel, 0xff454545));

            a.recycle();
        }
    }

    public void setJoystickEventListener(JoystickEventListener eventListener)
    {
        mEventListener = eventListener;
    }

    public void setAlpha(float alpha){
        alpha = (alpha > 1)? 1: alpha;
        alpha = (alpha < 0)? 0: alpha;

        alpha = (255 * alpha);

        this.puck.setAlpha((int)alpha);
        this.mWheel.setAlpha((int) alpha);
    }

    public void setWheelColor(int color){
        mWheel.setColor(color);
    }

    /**
     * Sets the radius of the puck to the provided radius, in pixels
     * @param radius
     */
    public void setPuckRadius(int radius){
        this.puck_radius = radius;

        this.puck.setRadius(radius);
    }

    /**
     * Resets the puck's position to the middle of the wheel
     */
    public void resetPuck(){
        this.puck.setPosition(new Point(this.center_point));
    }

    public void setOnStartRunnable(Runnable runnable){
        mOnStartRunnable = runnable;
    }

    public void setOnDragRunnable(Runnable runnable){
        mOnDragRunnable = runnable;
    }

    public void setOnEndRunnable(Runnable runnable){
        mOnEndRunnable = runnable;
    }

    @Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {

        this.center_point.set(this.getMeasuredWidth() / 2, this.getMeasuredHeight() / 2);

        if(this.getMeasuredWidth() > this.getMeasuredHeight()){
            this.wheel_radius = (this.getMeasuredWidth() / 2) - this.puck_edge_overlap - 2;
        }else{
            this.wheel_radius = (this.getMeasuredHeight()  / 2) - this.puck_edge_overlap - 2;
        }

        //Check that the puck and wheel are within reasonable limits
        this.wheel_radius = (this.wheel_radius < 3)?3:this.wheel_radius;
        this.puck_radius = (this.puck_radius < this.wheel_radius)? this.puck_radius : (this.wheel_radius / 3);

        this.mWheel.setRadius(this.wheel_radius);
        this.setPuckRadius(this.puck_radius);

        this.mWheel.setPosition(this.center_point);
        this.puck.setPosition(this.center_point);

        // Adjust the Joystick Pad Size
        mJoystickPadCenterX = (this.mWheel.getBounds().width() / 2.0);
        mJoystickPadCenterY = (this.mWheel.getBounds().height() / 2.0);
    }

    @Override
    public void onDraw(Canvas canvas){

        this.mWheel.draw(canvas);
        this.puck.draw(canvas);
    }

    /**
     * Indicates whether the user is currently dragging the puck
     * @return True, if so
     */
    public boolean getIsDraggingPuck(){
        return draggingPuck;
    }

    /**
     * From a provided current_position Point, returns an Point that contains
     * coordinates that are within the area of the puck wheel.
     *
     * @param current_position
     * @return an Point containing the puck's position
     */
    private Point getValidPuckPosition(Point current_position){

        Point pointer = new Point(current_position);
        Point wheel_center = this.mWheel.getPosition();
        Point adj_pointer = new Point(pointer);

        //Set the puck position to within the bounds of the wheel
        if(pointer.x != wheel_center.x || pointer.y != wheel_center.y){

            //reset the drive coords to be the zeroed pointer coords
            adj_pointer.set(pointer.x, pointer.y);

            //Use the wheel center to zero the pointer coords
            adj_pointer.x = adj_pointer.x - wheel_center.x;
            adj_pointer.y = adj_pointer.y - wheel_center.y;

            double a = Math.abs(adj_pointer.y);
            double b = Math.abs(adj_pointer.x);

            double hyp = Math.hypot(a, b);

            final double radius = (wheel_radius - (puck_radius - puck_edge_overlap));
            
            if(hyp > radius){
                final double factor = radius / hyp;

                pointer.x = (int)(adj_pointer.x * factor) + wheel_center.x;
                pointer.y = (int)(adj_pointer.y * factor) + wheel_center.y;
            }
        }

        return pointer;
    }

    /**
     * From a provided Point containing the puck's current position, returns an Point containing
     * a valid coordinate for use with the DriveControl's joystick area.
     *
     * @param current_position
     * @return an Point containing the clipped coordinates
     */
    private Point getDrivePuckPosition(Point current_position){

        Point drive_coord = new Point(current_position);
        Rect bounds = this.mWheel.getBounds();

        drive_coord.x = drive_coord.x - bounds.left;
        drive_coord.y = drive_coord.y - bounds.top;

        if(drive_coord.x < 0){
            drive_coord.x = 0;
        }else if(drive_coord.x > bounds.width()){
            drive_coord.x = bounds.width();
        }

        if(drive_coord.y < 0){
            drive_coord.y = 0;
        }else if(drive_coord.y > bounds.height()){
            drive_coord.y = bounds.height();
        }

        return drive_coord;
    }

    /**
     * Gets a point corrected for this Views position
     * @param p a Point to correct
     * @return a Point, corrected for the View's position
     */
    private Point getCorrectedPoint(Point p){
        
        final Point ret = new Point(p.x, p.y);
        
        ret.x -= getLeft();
        ret.y -= getTop();
        
        return ret;
    }

    /**
     * Converts an x,y position in the joy stick control's area into a length and angle.
     */
    public void convert(double x, double y) {
        double x_length = x - mJoystickPadCenterX;
        double y_length = mJoystickPadCenterY - y;

        // Need to scale x_length and y_length to make sure the coordinates are polar
        // instead of elliptical
        if (mJoystickPadCenterX > mJoystickPadCenterY)
        {
            x_length *= mJoystickPadCenterY / mJoystickPadCenterX;
        }
        else if (mJoystickPadCenterX < mJoystickPadCenterY)
        {
            y_length *= mJoystickPadCenterX / mJoystickPadCenterY;
        }

        if (mJoystickPadCenterX > 0.0 && mJoystickPadCenterY > 0.0)
        {
            mJoystickDistanceFromCenter = (Math.sqrt(x_length * x_length + y_length * y_length) /
                                           Math.min(mJoystickPadCenterX, mJoystickPadCenterY));
            mJoystickDistanceFromCenter = Value.clamp(mJoystickDistanceFromCenter, 0.0, 1.0);
        }
        else
        {
            mJoystickDistanceFromCenter = 0.0;
        }

        // Calculate the angle
        mJoystickAngleDegrees = Math.atan2(x_length, y_length);

        // Adjust for range between 0 and 2
        if (mJoystickAngleDegrees < 0.0)
        {
            mJoystickAngleDegrees += 2.0 * Math.PI;
        }

        // Convert to degrees
        mJoystickAngleDegrees *= 180.0 / Math.PI;
    }

    @Override
	public void interpretMotionEvent(MotionEvent event) {

		boolean handled = false;

        int pointer_Point = event.getActionIndex();
        int pointer_id = event.getPointerId(pointer_Point);
        final Point global_point = new Point((int)event.getX(), (int)event.getY());
        final Point local_point  = getCorrectedPoint(global_point);
        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                if(mEnabled){
                    if (puck.getBounds().contains(local_point.x, local_point.y)) {
                        draggingPuck = true;
                        draggingPuckPointerId = pointer_id;
                        handled = true;
                    }

                    // Notify our listener, if we have one, that the Joystick has started
                    if(mEventListener != null)
                    {
                        mEventListener.onJoystickBegan();
                    }

                    if(mOnStartRunnable != null){
                        mOnStartRunnable.run();
                    }
                }
                break;

		case MotionEvent.ACTION_MOVE:
			if (mEnabled && draggingPuck && draggingPuckPointerId == pointer_id) {


                //Adjust drive coordinates for driving
                Point drive_coord = this.getDrivePuckPosition(local_point);

                // Convert the Joystick coordinates into our global length / angle vector elements
                convert(drive_coord.x, drive_coord.y);

                // Notify our listener, if we have one, that the Joystick has moved
                if(mEventListener != null)
                {
                    mEventListener.onJoystickMoved(mJoystickDistanceFromCenter, mJoystickAngleDegrees);
                }

                //Set the puck position to within the bounds of the wheel
                final Point i = getValidPuckPosition(local_point);
                local_point.set(i.x, i.y);
                this.puck.setPosition(new Point(local_point.x, local_point.y));
                this.invalidate();

                if(mOnDragRunnable != null)
                {
                    mOnDragRunnable.run();
                }

                handled = true;
            }
            break;

        case MotionEvent.ACTION_UP:
            if (draggingPuck && draggingPuckPointerId == pointer_id) {
                this.resetPuck();
                invalidate();

                draggingPuck = false;
                handled = true;

                // Notify our listener, if we have one, that the Joystick has stopped
                if(mEventListener != null)
                {
                    mEventListener.onJoystickEnded();
                }

                if(mOnEndRunnable != null)
                {
                    mOnEndRunnable.run();
                }
            }
            break;

        default:
            break;
        }
	}

    @Override
    public void setEnabled(boolean val){
        super.setEnabled(val);
        mEnabled = val;
    }

    @Override
    public void enable() {
        setEnabled(true);
    }

    @Override
    public void disable() {
        setEnabled(false);
    }

    public boolean getJoystickInUse() {
        return draggingPuck;
    }

    public double getJoystickDistanceFromCenter() {
        return mJoystickDistanceFromCenter;
    }

    public double getJoystickAngleDegrees() {
        return mJoystickAngleDegrees;
    }


}
