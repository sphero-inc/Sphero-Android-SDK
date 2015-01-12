package com.orbotix.calibration.utilities.animation;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.io.Serializable;

/**
 * An animation that draws directly to a canvas
 *
 * Author: Adam Williams
 */
public interface DrawnAnimation extends Serializable {

    /**
     * Starts the animation. The animation's run method will then actually draw to the Canvas
     */
	public void start();

    /**
     * Runs the animation on the provided Canvas. This should be called on every draw pass, regardless
     * of whether the animation should be actually drawing or not. The start, stop, restart, and reset
     * methods will then determine whether the run method does anything.
     *
     * @param canvas
     * @return a Rect with the affected, "dirty" area, or an empty Rect, for no area.
     */
	public Rect run(Canvas canvas);

    /**
     * Indicates whether this animation is started.
     * @see #start()
     * @return True, if so
     */
	public boolean isStarted();

    /**
     * Indicates whether this animation has ended.
     * @see #stop()
     * @return True, if so
     */
	public boolean isEnded();

    /**
     * Resets this animation and then starts it again.
     * @see #reset()
     */
	public void restart();

    /**
     * Resets the values of this animation to its beginning. Doesn't start the animation after that,
     * though. To do that use restart().
     * @see #restart()
     */
	public void reset();

    /**
     * Stops this animation. Resets its values, and then marks it as having ended.
     * @see #reset()
     * @see #isEnded()
     */
	public void stop();

    /**
     * Gets the duration of this animation, in milliseconds
     * @return The duration, in milliseconds
     */
	public int getDuration();
	
}
