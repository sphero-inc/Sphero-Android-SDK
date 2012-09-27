package orbotix.robot.widgets;

import android.view.MotionEvent;

/**
 * A type that can interpret a MotionEvent
 *
 * Created by Orbotix Inc.
 * Date: 12/13/11
 *
 * @author Adam Williams
 */
public interface MotionInterpreter {

    /**
     * Interprets the provided MotionEvent
     * @param event a MotionEvent to interpret
     */
    public void interpretMotionEvent(MotionEvent event);
}
