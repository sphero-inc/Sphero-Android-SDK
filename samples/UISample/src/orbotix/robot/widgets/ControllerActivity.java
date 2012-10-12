package orbotix.robot.widgets;

import android.app.Activity;
import android.view.MotionEvent;
import orbotix.robot.base.Robot;

import java.util.ArrayList;
import java.util.List;

/**
 * An Activity that contains Controllers and can use them to control the Robot. Extend this class to 
 * more easily attach Controllers to your application.
 *
 * Created by Orbotix Inc.
 * Date: 12/13/11
 *
 * @author Adam Williams
 */
public class ControllerActivity extends Activity {

    private List<Controller> mControllers = new ArrayList<Controller>();

    /**
     * Adds a Controller to this Activity
     *
     * @param controller a Controller to add to this Activity
     */
    public void addController(Controller controller){
        mControllers.add(controller);
    }

    /**
     * Enables all Controllers
     */
    public void enableControllers(){

        for(Controller controller : mControllers){
            controller.enable();
        }
    }

    /**
     * Disables all Controllers
     */
    public void disableControllers(){

        for(Controller controller : mControllers){
            controller.disable();
        }
    }

    /**
     * Sets the provided robot to all Controllers
     * @param robot the Robot to set on all Controllers
     */
    public void setRobot(final Robot robot){
        for(Controller controller : mControllers){
            controller.setRobot(robot);
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        
        for(Controller controller : mControllers){

            controller.interpretMotionEvent(ev);
        }
        
        return super.dispatchTouchEvent(ev);
    }
}
