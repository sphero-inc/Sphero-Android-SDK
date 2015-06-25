package com.orbotix.speedslider.api;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Orbotix Inc.
 * Date: 1/22/14
 *
 * @author Adam Williams
 * @author Jack Thorp
 */
public class SpeedSliderView extends View {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Bitmap mOriginalBitmap;
    private Paint mBitmapPaint = new Paint();
    private Paint mArcPaint = new Paint();
    private float mSliderRatio;
    private float mMaxSpeed;
    private float mMinSpeed;
    private float mArcSweepStart;
    private float mArcSweepEnd;
    private float mCurrentSpeed;
    private final float mOvalSizeYAdjustment;
    private final float mKnobLineOuterRadiusPadding;
    private final float mKnobLineInnerRadiusPadding;
    private final float mKnobLineThickness;
    private PointF mTouchPoint = new PointF();
    private PointF outerPoint = new PointF();
    private PointF innerPoint = new PointF();
    public boolean leftHandMode = false;


    //Values that are going to be deleted
    float angleInDegrees;

    private OnTouchGestureChangedListener mOnTouchGestureChangedListener;
    private OnSpeedChangedListener mOnSpeedChangedListener;
    private Paint mKnobPaint;

    public SpeedSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setMaxSpeed(1f);
        setMinSpeed(0.1f);

        if(attrs != null){

            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SpeedSliderView);

            int bitmapId = a.getResourceId(R.styleable.SpeedSliderView_android_src, 0);
            mOriginalBitmap = BitmapFactory.decodeResource(getResources(), bitmapId);

            setJoystickDriveDirection(a.getBoolean(R.styleable.SpeedSliderView_leftHandDriveMode, false));

            if (leftHandMode){
                setSweepStartAngle(a.getFloat(R.styleable.SpeedSliderView_sweepStartAngle, 4f));
                setSweepFinishAngle(a.getFloat(R.styleable.SpeedSliderView_sweepEndAngle, 52f));
            }
            else{
                setSweepStartAngle(a.getFloat(R.styleable.SpeedSliderView_sweepStartAngle, 178.0f));
                setSweepFinishAngle(a.getFloat(R.styleable.SpeedSliderView_sweepEndAngle, -48.5f));
            }

            setMaxSpeed(a.getFloat(R.styleable.SpeedSliderView_maxSpeed, 1f));
            setMinSpeed(a.getFloat(R.styleable.SpeedSliderView_minSpeed, 0.1f));
        }

        setSliderRatio(mMaxSpeed);
        initializePaints();

        if (leftHandMode){
            mKnobLineOuterRadiusPadding = 0.08f;
            mKnobLineInnerRadiusPadding = 0.25f;
        }
        else{
            mKnobLineOuterRadiusPadding = 0.135f;
            mKnobLineInnerRadiusPadding = 0.30f;
        }

        mOvalSizeYAdjustment = 0.029f;
        mKnobLineThickness = getContext().getResources().getDimension(R.dimen.knob_thickness);
    }

    private void initializePaints() {
        initializeBitmapPaint();
        initializeArcPaint();
        initializeKnobPaint();
    }

    private void initializeKnobPaint() {
        mKnobPaint = new Paint();
        mKnobPaint.setFilterBitmap(false);
        mKnobPaint.setAntiAlias(true);
        mKnobPaint.setColor(0xff00ff00);
        mKnobPaint.setStyle(Paint.Style.STROKE);
        mKnobPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void initializeBitmapPaint() {
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setFilterBitmap(true);
    }

    private void initializeArcPaint() {
        mArcPaint.setFilterBitmap(false);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setColor(0x00000000);
        mArcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
    }

    public void setOnSpeedChangedListener(OnSpeedChangedListener onSpeedChangedListener) {
        mOnSpeedChangedListener = onSpeedChangedListener;
    }

    public void setOnTouchGestureChangedListener(OnTouchGestureChangedListener onTouchGestureChangedListener) {
        mOnTouchGestureChangedListener = onTouchGestureChangedListener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if(changed ||  mBitmap == null){
            Rect size = new Rect(l, t, r, b);
            initializeBitmaps(size);
            scaleKnobLineThickness(size);
        }
    }

    private void initializeBitmaps(Rect bounds) {
        if(mBitmap != null){
            setBackgroundDrawable(null);
            mBitmap.recycle();
        }

        RectF bitmapBounds = getBitmapBounds(bounds);
        mBitmap = Bitmap.createBitmap((int)bitmapBounds.width(), (int)bitmapBounds.height(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        BitmapDrawable drawable = new BitmapDrawable(mBitmap);
        drawable.setTileModeXY(Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        setBackgroundDrawable(drawable);
        drawSlider(bounds);
    }

    private RectF getBitmapBounds(Rect viewBounds) {
        float scale = getResizeScale(viewBounds);
        return getBitmapBounds(scale);
    }

    private RectF getBitmapBounds(float scale) {
        float width = mOriginalBitmap.getWidth() * scale;
        float height = mOriginalBitmap.getHeight() * scale;
        return new RectF(0,0, width, height);
    }

    private float getResizeScale(Rect bounds) {
        float widthScale = bounds.width() / (float)mOriginalBitmap.getWidth();
        float heightScale = bounds.height() / (float)mOriginalBitmap.getHeight();
        return (widthScale < heightScale)? widthScale : heightScale;
    }

    private void scaleKnobLineThickness(Rect size) {
        float scale = getResizeScale(size);
        float thickness = mKnobLineThickness * scale;
        mKnobPaint.setStrokeWidth(thickness);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE){
            mTouchPoint.set(event.getX(), event.getY());
            setSpeedByTouchPoint(getViewBounds());
            invalidate();
        }
        return true;
    }

    private void drawSlider() {
        drawSlider(getViewBounds());
    }

    private Rect getViewBounds() {
        return new Rect(0,0,getWidth(), getHeight());
    }

    private void drawSlider(Rect viewBounds) {
        RectF bitmapSize = getBitmapBounds(viewBounds);
        RectF o = getOvalRect(bitmapSize);
        float sweep = mArcSweepEnd * (1 - mSliderRatio);

        mCanvas.drawRect(viewBounds, mArcPaint);
        mCanvas.drawBitmap(mOriginalBitmap, null, bitmapSize, mBitmapPaint);
        mCanvas.drawArc(o, mArcSweepStart, sweep, true, mArcPaint);
//        float[] sweepAngles = new float[2];
//        sweepAngles = SetSweepAngleForKnob(o);
//        ClampSliderKnobAngles(sweepAngles[0], sweepAngles[1], sweep, o);
    }

    private void CheckIfPlayerHasPressedTheScreenYet(RectF o){
        if (mTouchPoint == null){
            float knobDiff = mArcSweepEnd - mArcSweepStart;
            float knobStartingAngle = ((knobDiff * mCurrentSpeed) + mArcSweepStart);
            double[] coordinate = GetClampedKnobCoordinate(o, knobStartingAngle);
            mTouchPoint.x = (float) coordinate[0];
            mTouchPoint.y = (float) coordinate[1];
        }
    }

    private float[] SetSweepAngleForKnob(RectF o) {
        float sweepEnd;
        float sweepStart;
        float deltaY = mTouchPoint.y;

        CheckIfPlayerHasPressedTheScreenYet(o);

        if (leftHandMode){
            float deltaX = mTouchPoint.x;
            angleInDegrees = (float) (Math.atan2(deltaY, deltaX) * 180 / Math.PI);
            sweepEnd = mArcSweepEnd;
            sweepStart = mArcSweepStart;
        }
        else{
            float deltaX = (o.width() / 2) - mTouchPoint.x;
            angleInDegrees = (float) (Math.atan2(deltaY, deltaX) * 180 / Math.PI);
            sweepEnd = 180 - (mArcSweepStart + mArcSweepEnd);
            sweepStart = 180 - mArcSweepStart;
        }

        float[] results = new float[2];
        results[0] = sweepEnd;
        results[1] = sweepStart;

        return results;
    }

    private void ClampSliderKnobAngles(float sweepStart, float sweepEnd, float sweep, RectF o){
        float tx = 0;
        float ty = 0;

        if (leftHandMode)
        {
            if (angleInDegrees < sweepEnd || angleInDegrees > sweepStart){
                if (angleInDegrees  < sweepEnd){
                    double[] coordinate = GetClampedKnobCoordinate(o, sweepEnd);
                    tx = (float) coordinate[0];
                    ty = (float) coordinate[1];
                }
                if (angleInDegrees > sweepStart){
                    double[] coordinate = GetClampedKnobCoordinate(o, sweepStart);
                    tx = (float) coordinate[0];
                    ty = (float) coordinate[1];
                }
            }
            else{
                    tx = mTouchPoint.x;
                    ty = mTouchPoint.y;
            }
        }
        else{
            if (angleInDegrees > sweepStart || angleInDegrees < sweepEnd){
                if (angleInDegrees  < sweepStart){
                    double[] coordinate = GetClampedKnobCoordinate(o, sweepEnd);
                    tx = (float) coordinate[0];
                    ty = (float) coordinate[1];
                }
                if (angleInDegrees > sweepEnd){
                    double[] coordinate = GetClampedKnobCoordinate(o, sweepStart);
                    tx = (float) coordinate[0];
                    ty = (float) coordinate[1];
                }
            }
            else{
                    tx = o.centerX() - mTouchPoint.x;
                    ty = mTouchPoint.y - o.centerY();
            }
        }

        DrawKnobCanvas(tx, ty, o);
    }

    private double[] GetClampedKnobCoordinate(RectF o, float Angle) {
        float hypot = (o.width() / 2);

        double radians = (double) Angle / (180 / Math.PI);
        double adj = Math.cos(radians) * hypot;
        double opp = Math.sqrt(Math.pow(hypot, 2) - Math.pow(adj, 2));

        double[] coordinatePosition = new double[2];
        coordinatePosition[0] = adj;
        coordinatePosition[1] = opp;

        return coordinatePosition;
    }

    private void DrawKnobCanvas(float tx, float ty, RectF o) {
        float a = Math.abs(tx);
        float b = Math.abs(-ty);
        double hypot = Math.hypot(a, b);

        outerPoint = getKnobLineSegmentPoint(o, a, b, hypot, mKnobLineOuterRadiusPadding);
        innerPoint = getKnobLineSegmentPoint(o, a, b, hypot, mKnobLineInnerRadiusPadding);
        mCanvas.drawLine(outerPoint.x, outerPoint.y + o.centerY(), innerPoint.x, innerPoint.y + o.centerY(), mKnobPaint);
    }

    private PointF getKnobLineSegmentPoint(RectF o, float a, float b, double hypot, float padding) {
        float radius = (o.width() / 2);
        radius -= (radius * padding);
        PointF point = new PointF();
        point = getRatioForKnobSegment(o, a, b, radius, hypot, point);

        return point;
    }

    private PointF getRatioForKnobSegment(RectF o, float a, float b, float radius, double hypot, PointF point){
        double ratio = radius / hypot;
        a *= ratio;
        b *= ratio;

        if (leftHandMode){
            point.x = (o.centerX() + a);
        }
        else{

            point.x = (o.centerX() - a);
        }

        point.y = b;
        return point;
    }

    private void setSpeedByTouchPoint(Rect viewBounds) {
        RectF bitmapSize = getBitmapBounds(viewBounds);
        RectF oval = getOvalRect(bitmapSize);

        double touchAngle = getTouchAngle(oval);

        if (leftHandMode){
            touchAngle -= (0 + mArcSweepStart);
        }
        else{
            touchAngle -= (180 - mArcSweepStart);
        }

        setSpeedByAngle(touchAngle);
    }

    private void setSpeedByAngle(double touchAngle) {
        float speed = getSpeedFromAngle(touchAngle);
        setSliderRatio(speed);

        sendSpeedListenerEvent();
    }

    private float getSpeedFromAngle(double touchAngle) {
        return 1 - (float) (touchAngle / Math.abs(mArcSweepEnd));
    }

    private float getClampedSpeed(float speed) {
        float diff = mMaxSpeed - mMinSpeed;
        speed = (diff * speed) + mMinSpeed;
        return speed;
    }

    public void setSpeed(float speed){
        speed = (speed < mMinSpeed)?mMinSpeed:speed;
        speed = (speed > mMaxSpeed)?mMaxSpeed:speed;
        mCurrentSpeed = speed;

        float diff = mMaxSpeed - mMinSpeed;

        setSliderRatio((diff * speed) + mMinSpeed);
    }

    private void setSliderRatio(float sliderRatio) {
        mSliderRatio = sliderRatio;
        mSliderRatio = (mSliderRatio > 1f)?1f: mSliderRatio;
        mSliderRatio = (mSliderRatio < 0f)?0f: mSliderRatio;
        sendSpeedListenerEvent();
        sendTouchPointListenerEvent();
    }

    private void sendSpeedListenerEvent() {
        if(mOnSpeedChangedListener != null){
            float speed = getClampedSpeed(mSliderRatio);
            mOnSpeedChangedListener.onSpeedChanged(speed);
        }
    }

    private void sendTouchPointListenerEvent() {
        if(mOnTouchGestureChangedListener != null){
            mOnTouchGestureChangedListener.onTouchGestureChanged(mTouchPoint, angleInDegrees);
        }
    }

    /**
     * Set the maximum speed scale. This is the speed that will be set when the slider is full.
     * @param maxSpeed The maximum speed, between 0 and 1
     */
    public void setMaxSpeed(float maxSpeed) {
        mMaxSpeed = maxSpeed;
        mMaxSpeed = (mMaxSpeed > 1f)?1f:mMaxSpeed;
        mMaxSpeed = (mMaxSpeed < 0f)?0f:mMaxSpeed;
    }

    /**
     * Set the minimum speed scale. This is the speed that will be set when the slider is empty.
     * @param minSpeed The minimum speed, between 0 and 1
     */
    public void setMinSpeed(float minSpeed) {
        mMinSpeed = minSpeed;
        mMinSpeed = (mMinSpeed > 1f)?1f:mMinSpeed;
        mMinSpeed = (mMinSpeed < 0f)?0f:mMinSpeed;
    }

    public void setSweepStartAngle(float sweepAngle){
        mArcSweepStart = sweepAngle;
    }

    public void setSweepFinishAngle(float sweepAngle){
        mArcSweepEnd = sweepAngle;
    }

    public void setJoystickDriveDirection(boolean driveModeLeft){
        leftHandMode = driveModeLeft;
    }

    private double getTouchAngle(RectF oval) {
        return getRadianTouchAngle(oval) * (180 / Math.PI);
    }

    private double getRadianTouchAngle(RectF oval) {
        float opposite;
        float adjacent = getCenterOfOval(oval);

        if (leftHandMode){
             opposite = mTouchPoint.y;
         }
        else{
             opposite = mTouchPoint.y - oval.centerY();
         }

        return Math.atan2(opposite, adjacent);
    }

    private float getCenterOfOval(RectF oval){
        if (leftHandMode){
            return mTouchPoint.x;
        }
        else{
            return oval.centerX() - mTouchPoint.x;
        }
    }

    private RectF getOvalRect(RectF bitmapSize) {
        RectF oval = new RectF(bitmapSize);
        float additionalTimesTheWidth = 2.1f;
        float ovalSideSize = oval.width() + (oval.width() * additionalTimesTheWidth);
        float yAdjustment = ovalSideSize * mOvalSizeYAdjustment;
        oval.top -= (ovalSideSize / 2);
        oval.bottom = ovalSideSize + oval.top + yAdjustment;

        if (leftHandMode){
            oval.left = oval.right - ovalSideSize;
        }
        else{
            oval.right = ovalSideSize + oval.left;
        }

        return oval;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawSlider();

        super.onDraw(canvas);
    }

    /**
     * Listener that is updated when the speed is changed.
     */
    public interface OnSpeedChangedListener {

        public void onSpeedChanged(float speed);
    }

    public interface OnTouchGestureChangedListener {

        public void onTouchGestureChanged(PointF touchPoint, float angle);
    }
}
