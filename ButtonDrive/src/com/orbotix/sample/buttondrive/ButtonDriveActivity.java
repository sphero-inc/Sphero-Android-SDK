package com.orbotix.sample.buttondrive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RollCommand;

/**
 * Activity for controlling the Sphero with five control buttons.
 */
public class ButtonDriveActivity extends Activity
{
    /**
     * ID for starting the StartupActivity for result
     */
    private final static int STARTUP_ACTIVITY = 0;

    /**
     * Robot to control
     */
    private Robot mRobot;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    }

    /**
     * Connect to the robot when the Activity starts
     */
    @Override
    protected void onStart() {
        super.onStart();
        
        if(mRobot == null){

            //Connect to the Robot
            Intent i = new Intent(this, StartupActivity.class);
            startActivityForResult(i, STARTUP_ACTIVITY);
        }
    }

    /**
     * Get the robot id from the StartupActivity result, and use the RobotProvider singleton to 
     * get an instance of the connected robot
     * @param requestCode The request code from the returned Activity
     * @param resultCode The result code from the returned Activity
     * @param data The Intent containing the result data tuple. The robot id is under the key 
     *             in StartupActivity.EXTRA_ROBOT_ID
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(requestCode == STARTUP_ACTIVITY && resultCode == RESULT_OK){
            
            final String robot_id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);
            
            if(robot_id != null && !robot_id.equals("")){
                mRobot = RobotProvider.getDefaultProvider().findRobot(robot_id);
            }
        }
    }

    /**
     * When the user clicks "STOP", stop the Robot.
     * @param v The View that had been clicked
     */
    public void onStopClick(View v){

        if(mRobot != null){
            //Stop robot
            RollCommand.sendCommand(mRobot, 0f, 0f);
        }
    }

    /**
     * When the user clicks a control button, roll the Robot in that direction
     * @param v The View that had been clicked
     */
    public void onControlClick(View v){
        
        //Find the heading, based on which button was clicked
        final float heading;
        switch (v.getId()){
            
            case R.id.ninety_button:
                heading = 90f;
                break;
            
            case R.id.one_eighty_button:
                heading = 180f;
                break;
            
            case R.id.two_seventy_button:
                heading = 270f;
                break;

            default:
                heading = 0f;
                break;
        }
        
        //Set speed. 60% of full speed
        final float speed = 0.6f;
        
        //Roll robot
        RollCommand.sendCommand(mRobot, heading, speed);
    }

    /**
     * Disconnect from the robot when the Activity stops
     */
    @Override
    protected void onStop() {
        super.onStop();

        //disconnect robot
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }
}
