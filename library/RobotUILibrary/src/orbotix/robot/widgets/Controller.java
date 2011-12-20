package orbotix.robot.widgets;

import orbotix.robot.base.Robot;

/**
 * A type that controls the Robot in real time.
 *
 * Created by Orbotix Inc.
 * Date: 12/13/11
 *
 * @author Adam Williams
 */
public interface Controller extends MotionInterpreter {


    /**
     * Enables the Controller.
     */
    public void enable();

    /**
     * Disables the Controller
     */
    public void disable();

    /**
     * Sets the robot for this Controller to control
     * @param robot the Robot to control
     */
    public void setRobot(Robot robot);
}
