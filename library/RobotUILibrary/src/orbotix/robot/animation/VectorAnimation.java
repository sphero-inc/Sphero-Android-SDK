package orbotix.robot.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Looper;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * A Vector animation that uses an interpolated mScale from 0 to 1 to aide with displaying animation progress
 *
 * Author: Adam Williams
 */
public abstract class VectorAnimation implements DrawnAnimation {

	protected long mStartTime = 0;
	protected long mNextFrame = 0;
	protected long mEndTime = 0;
	protected int mTotalFrames = 0;
	protected int mFrameDuration = 0;
	protected int mDuration = 0;
	protected int mCurrentFrame = -1;
	protected float mScale = 0.0f;
	protected int mRepeats = 0;
	private boolean mHasStarted = false;
	private boolean mHasEnded = false;
    private boolean mFullInvalidation = false;
    private Interpolator mInterpolator;
    private Runnable mOnStartCallback;
    private Runnable mOnEndCallback;

    /**
     * Creates an instance of VectorAnimation with the provided frames per second, and that lasts the
     * provided duration, in milliseconds
     * @param frames The number of frames per second this animation will attempt to achieve
     * @param duration The amount of time this animation will last, in milliseconds
     */
	public VectorAnimation(int frames, int duration)
	{
		if(frames < 1 || duration < 1)
		{
			throw new IllegalArgumentException("Animation must have a number of frames and a duration greater than 0");
		}
		this.mTotalFrames = (int)(frames * ((float)duration / 1000f));
		if(this.mTotalFrames < 1) this.mTotalFrames = 1;
		this.mFrameDuration = duration / this.mTotalFrames;
		this.mDuration = (this.mTotalFrames * this.mFrameDuration);
	}

    /**
     * Sets an Interpolator for this animation to use. An Interpolator modifies the scale returned
     * by getScale, depending on the Interpolator used.
     * @param interpolator
     */
    public void setInterpolator(Interpolator interpolator){
        mInterpolator = interpolator;
    }

    /**
     * Sets an optional Runnable to run when this animation starts. A null value clears it.
     * @param runnable An optional Runnable to run when this animation starts.
     */
    public void setStartCallback(Runnable runnable){
        mOnStartCallback = runnable;
    }

    /**
     * Sets an optional Runnable to run when this animation ends. A null value clears it.
     * @param runnable An optional Runnable to run when this animation ends.
     */
    public void setEndCallback(Runnable runnable){
        mOnEndCallback = runnable;
    }

    /**
     * Sets whether or not this animation will invalidate the *entire* canvas or not. The default is false.
     * If set, the entire canvas will be refreshed every frame. If not set, only the "dirty" area of the
     * animation returned by showFrame will be refreshed each frame.
     *
     * Set this to true if you're experiencing tearing. May result in a hit to the framerate.
     *
     * @param val Pass true if you want to use full invalidation
     */
    public void setFullInvalidation(boolean val){

        mFullInvalidation = val;
    }

    /**
     * Indicates whether this animation is supposed to invalidate the entire View's area instead of
     * just the dirty area each frame.
     * @return True, if so.
     * @see #setFullInvalidation(boolean)
     */
    public boolean getIsFullInvalidation(){
        return mFullInvalidation;
    }
	
	private void setScale(long time)
	{
		this.mScale = ((float)(this.mCurrentFrame + 1) / (float)this.mTotalFrames);
	}

    /**
     * Gets the current scale of this animation. The scale is a float from 0 to 1 that indicates this
     * animation's progress through the duration of the animation. This number should be used
     * in the animation's showFrame method to affect the presentation of the animation.
     *
     * @return the current scale, as a float
     */
	protected float getScale()
	{

		return this.mScale;
	}

    /**
     * Gets the current scale of this animation. The scale is a float from 0 to 1 that indicates this
     * animation's progress through the duration of the animation. This number should be used
     * in the animation's showFrame method to affect the presentation of the animation.
     *
     * The scale is affected by an Interpolator that can be optionally set by setInterpolator.
     *
     * @return the current scale, as a float, modified by this animation's Interpolator
     */
    protected float getInterpolatedScale(){
        if(mInterpolator != null){
            return mInterpolator.getInterpolation(mScale);
        }
		return this.mScale;
    }
	
	protected float getAdjustedScale(int add_to_current_frame, int added_frames)
	{
		int adj_frames = (this.mTotalFrames + added_frames);
		if(adj_frames == 0)
		{
			return 0;
		}
		return (float)(this.mCurrentFrame + 1 + add_to_current_frame) / (float)adj_frames;
	}
	
	private void setNextFrameTime()
	{
		this.mNextFrame = this.mStartTime + ((this.mCurrentFrame +1)*this.mFrameDuration);
	}

    /**
     * A run method that also handles the invalidation of the provided View. If you don't use this method in the
     * onDraw of the View, and instead use run(Canvas), you will need to ensure that the View invalidates the
     * dirty area, or the entire View, if this is set to full invalidation.
     *
     * @param canvas
     * @param view
     * @return A Rect containing the dirty area of this frame of animation.
     */
    public Rect run(Canvas canvas, View view){

        Rect dirty_area = run(canvas);

        if(dirty_area == null){
            dirty_area = new Rect();
        }
        if(mFullInvalidation
                || dirty_area.isEmpty()){
            view.invalidate();
        }else{
            view.invalidate(dirty_area);
        }

        return dirty_area;
    }
	
	@Override
	public Rect run(Canvas canvas)
	{
		

        if(isStarted() && !isEnded()){
            long current_time = System.currentTimeMillis();

            if(this.mStartTime == 0)
            {
                this.mStartTime = current_time;
                this.mCurrentFrame = 0;
            }else
            {
                if(current_time > this.mNextFrame)
                {
                    int play_time = (int)(current_time - this.mStartTime);
                    this.mCurrentFrame = (play_time / this.mFrameDuration);
                }
            }


            if(this.mCurrentFrame < (this.mTotalFrames))
            {
                this.setNextFrameTime();
                this.setScale(current_time);

                return this.showFrame(canvas);

            }else
            {
                if(this.mRepeats > 0)
                {
                    this.mRepeats--;
                    this.restart();

                    return this.showFrame(canvas);

                }else
                {
                    this.stop();

                    if(mOnEndCallback != null){
                        mOnEndCallback.run();
                    }
                }
            }
        }


		return new Rect();
	}

    /**
     * Override this method to implement an initialization.
     * @param context
     */
	public void initialize(Context context){
        //Override to implement this method
    }

    /**
     * This method shows a frame of the animation on the provided Canvas. Does the actual drawing. Use the
     * getScale method to get a number from 0 to 1 to indicate how far along this animation is in its duration.
     * If an Interpolator has been set,
     * @param canvas
     * @return
     */
	protected abstract Rect showFrame(Canvas canvas);
	
	@Override
	public boolean isStarted()
	{
		return this.mHasStarted;
	}
	
	@Override
	public boolean isEnded()
	{
		return this.mHasEnded;
	}
	
	@Override
	public void start()
	{

        if(this.mCurrentFrame == -1 && !this.mHasStarted)
        {
            this.mStartTime = 0;
            this.mEndTime = (this.mStartTime + (this.mDuration));
            this.mHasStarted = true;
            this.mHasEnded = false;
        }

        if(mOnStartCallback != null){
            mOnStartCallback.run();
        }
		
	}
	
	
	@Override
	public void reset()
	{
		this.mStartTime = 0;
		this.mCurrentFrame = -1;
		this.mHasStarted = false;
		this.mHasEnded = false;
	}
	
	@Override
	public void restart()
	{
		this.reset();
		this.start();
	}
	
	@Override
	public void stop()
	{
		this.reset();
		this.mHasEnded = true;


	}
	
	@Override
	public int getDuration()
	{
		return this.mDuration;
	}
}
