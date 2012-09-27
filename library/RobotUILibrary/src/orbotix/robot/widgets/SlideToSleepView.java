package orbotix.robot.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import orbotix.robot.app.R;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RunMacroCommand;

/**
 * A widget that allows the user to put a robot to sleep by sliding a knob to the right.
 *
 * @author Orbotix Inc.
 * @since 0.12.0
 */
public class SlideToSleepView extends View implements Controller {

    private OnSleepListener mListener;
    private Bitmap mSliderBackground;
    private Bitmap mSliderBall;
    private Bitmap mTempBall;

    private Point mBackgroundPosition;
    private Point mBallPosition;

    private boolean dragging = false;
    private boolean resetting = false;
    private boolean scoring = false;
    private boolean unScoring = false;

    private float resetStep = 0.0f;
    private float scoreStep = 0.0f;
    
    private boolean mEnabled           = true;
    private boolean mHandleTouchEvents = false;
    private Robot mRobot = null;


    private Handler mHandler = new Handler();
    private Runnable mInvalidateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    /**
     */
    public interface OnSleepListener {
        /**
         */
        public void onSleep();
    }

    private void notifyListener() {
        if (mListener != null) {
            mListener.onSleep();
        }
    }

    public SlideToSleepView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSliderBackground = BitmapFactory.decodeResource(getResources(), R.drawable.slidetosleep_background);
        mSliderBall = BitmapFactory.decodeResource(getResources(), R.drawable.slidetosleep_arrow);
    }

    /**
     * Sets the {@link SlideToSleepView.OnSleepListener} for this view to be called when the view is slid to the score position.
     *
     * @param listener the listener to be notified when this view is slid to the score position.
     */
    public void setOnSleepListener(OnSleepListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // since we know the resources that will be in this view and, for now, we only want this view to be one size
        // we will just set the dimensions to be the max width and max height of the view elements.
        setMeasuredDimension(mSliderBackground.getWidth(), mSliderBall.getHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int height = bottom - top;
        int width = right - left;
        int widthMiddle = (width - mSliderBackground.getWidth()) / 2;
        int heightMiddle = (height - mSliderBackground.getHeight()) / 2;
        mBackgroundPosition = new Point(widthMiddle, heightMiddle);
        heightMiddle = (height - mSliderBall.getHeight()) / 2;
        if (mBallPosition == null) {
            mBallPosition = new Point(mBackgroundPosition.x, heightMiddle);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mSliderBackground, mBackgroundPosition.x, mBackgroundPosition.y, null);
        if (resetting) {
            resetStep *= 1.08f;
            int newX = (int)(mBallPosition.x - resetStep);
            if (newX < mBackgroundPosition.x) {
                resetting = false;
                newX = mBackgroundPosition.x;
            }
            mBallPosition = new Point(newX, mBallPosition.y);
            mHandler.postDelayed(mInvalidateRunnable, 1);
        } else if (scoring) {
            scoreStep *=0.95f;
            if (mSliderBall.getWidth() <= 0.5 * mTempBall.getWidth()) {
                scoring = false;
                scoreStep = 0.5f;
                notifyListener();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        unScoring = true;
                        invalidate();
                    }
                }, 1000);
            }
            int width = (int)((float)mTempBall.getWidth() * scoreStep);
            int height = (int)((float)mTempBall.getHeight() * scoreStep);
            mSliderBall = Bitmap.createScaledBitmap(mTempBall, width, height, true);
            mBallPosition = new Point((int)(mBallPosition.x), (getHeight() - mSliderBall.getHeight()) / 2);
            mHandler.postDelayed(mInvalidateRunnable, 1);
        } else if (unScoring) {
            scoreStep /= 0.95f;
            if (mSliderBall.getHeight() >= mTempBall.getHeight()) {
                unScoring = false;
                mSliderBall = mTempBall.copy(Bitmap.Config.ARGB_8888, true);
                reset();
                scoreStep = 1.0f;
            }
            int width = (int)((float)mTempBall.getWidth() * scoreStep);
            int height = (int)((float)mTempBall.getHeight() * scoreStep);
            mSliderBall = Bitmap.createScaledBitmap(mTempBall, width, height, true);
            mBallPosition = new Point((int)(mBallPosition.x), (getHeight() - mSliderBall.getHeight()) / 2);
            mHandler.postDelayed(mInvalidateRunnable, 1);
        }

        canvas.drawBitmap(mSliderBall, mBallPosition.x, mBallPosition.y, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        if(mEnabled && mHandleTouchEvents){
            final Point p = new Point((int)event.getX(), (int)event.getY());
            return doTouchEvent(event.getAction(), p);
        }
        return false;
    }

    @Override
    public void enable() {
        mEnabled = true;
    }

    @Override
    public void disable() {
        mEnabled = false;
    }

    @Override
    public void setRobot(Robot robot) {
        mRobot = robot;
    }

    public void show(){
        Animation inAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.dropdown_in);
        setVisibility(View.VISIBLE);
        startAnimation(inAnimation);
        enable();
    }

    public void hide(){
        Animation outAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.dropdown_out);
        outAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(View.INVISIBLE);
                disable();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // do nothing
            }
        });
        startAnimation(outAnimation);
    }

    @Override
    public void interpretMotionEvent(MotionEvent event) {

        if(mEnabled && !mHandleTouchEvents){

            //Get corrected coords for the point
            final Point p = new Point((int)event.getX(), (int)event.getY());
            p.x -= getLeft();
            p.y -= getTop();

            doTouchEvent(event.getAction(), p);
        }
    }

    /**
     * Sets whether the SlideToSleepView will handle onTouchEvent, or work through interpretMotionEvent. If
     * set to false, onTouchEvent will do nothing and always return false. If true, onTouchEvent will evaluate
     * the MotionEvent and interpetMotionEvent will do nothing.
     * @param val true, or false
     */
    public void setHandleTouchEvents(boolean val){
        mHandleTouchEvents = val;
    }
    
    private boolean doTouchEvent(int action, Point point){
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (touchInsideObject(point, mBallPosition, mSliderBall)) {
                    dragging = true;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    updateBallPosition(point.x);
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                dragging = false;
                if (mBallPosition.x > (mBackgroundPosition.x + mSliderBackground.getWidth() - (1.5 * mSliderBall.getWidth()))) {
                    score();
                } else {
                    reset();
                }
                return true;
        }
        return false;
    }

    private void score() {
        notifyListener();
        reset();
        hide();

        //Put robot to sleep
        if(mRobot != null){
            RunMacroCommand.sendCommand(mRobot, (byte) 2);
        }
    }

    private void reset() {
        resetStep = 1.0f;
        resetting = true;
        invalidate();
    }

    private void updateBallPosition(float touchX) {
        float newXPosition = touchX - (mSliderBall.getWidth() / 2);
        if (newXPosition < mBackgroundPosition.x) {
            mBallPosition.x = mBackgroundPosition.x;
        } else if (newXPosition > (mBackgroundPosition.x + mSliderBackground.getWidth()) - mSliderBall.getWidth()) {
            mBallPosition.x = (mBackgroundPosition.x + mSliderBackground.getWidth()) - mSliderBall.getWidth();
        } else {
            mBallPosition = new Point((int)newXPosition, mBallPosition.y);
        }
    }

    private boolean touchInsideObject(Point p, Point objectPosition, Bitmap object) {
        final float x = p.x;
        final float y = p.y;
        if (x >= objectPosition.x && x <= (objectPosition.x + object.getWidth())) {
            if (y >= objectPosition.y && y <= (objectPosition.y + object.getHeight())) {
                return true;
            }
        }
        return false;
    }

}
