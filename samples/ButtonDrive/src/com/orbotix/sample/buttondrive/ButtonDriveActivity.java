package com.orbotix.sample.buttondrive;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import orbotix.robot.base.*;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;

/**
 * Activity for controlling the Sphero with five control buttons.
 */
public class ButtonDriveActivity extends Activity
{
    /**
     * Robot to control
     */
    private Robot mRobot;

    /**
     * The Sphero Connection View
     */
    private SpheroConnectionView mSpheroConnectionView;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
        // Set the connection event listener 
        mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
        	// If the user clicked a Sphero and it failed to connect, this event will be fired
			@Override
			public void onRobotConnectionFailed(Robot robot) {}
			// If there are no Spheros paired to this device, this event will be fired
			@Override
			public void onNonePaired() {}
			// The user clicked a Sphero and it successfully paired.
			@Override
			public void onRobotConnected(Robot robot) {
				mRobot = robot;
				// Skip this next step if you want the user to be able to connect multiple Spheros
				mSpheroConnectionView.setVisibility(View.GONE);
			}
		});
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
        mSpheroConnectionView.shutdown();
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }
}
