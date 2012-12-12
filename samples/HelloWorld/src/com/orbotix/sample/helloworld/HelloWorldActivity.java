package com.orbotix.sample.helloworld;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RobotProvider.OnRobotConnectedListener;
import orbotix.robot.base.RobotProvider.OnRobotDisconnectedListener;

/**
 * Connects to an available Sphero robot, and then flashes its LED.
 */
public class HelloWorldActivity extends Activity
{
    /**
     * The Sphero Robot
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
     * Called when the user comes back to this app
     */
    @Override 
    protected void onResume() {
    	super.onResume();
    	
        // Tell the Robot Provider to find all the paired robots
        // Only do this if the bluetooth adapter is enabled
        if( RobotProvider.getDefaultProvider().isAdapterEnabled() ) {
        	RobotProvider.getDefaultProvider().findRobots();
        }
        // Obtain the list of paired Spheros
        ArrayList<Robot> robots = RobotProvider.getDefaultProvider().getRobots();
        // Connect to first available robot (only works if 1 or more robots are paired)
        if( robots.size() > 0 ) {
        	RobotProvider.getDefaultProvider().control(robots.get(0));
        	RobotProvider.getDefaultProvider().connectControlledRobots();
        }
        
        // Set the Listener for when the robot has successfully connected
        RobotProvider.getDefaultProvider().setOnRobotConnectedListener( new OnRobotConnectedListener() {
			@Override
			public void onRobotConnected(Robot robot) {
				// Remember the connected robot reference
				mRobot = robot;
				// Blink the robot's LED
				HelloWorldActivity.this.blink(false);
			}
		});
        
        // Register to be notified when Sphero disconnects (out of range, battery dead, sleep, etc.)
        RobotProvider.getDefaultProvider().setOnRobotDisconnectedListener(new OnRobotDisconnectedListener() {
			@Override
			public void onRobotDisconnected(Robot robot) {
				Toast.makeText(HelloWorldActivity.this, "Sphero Disconnected", Toast.LENGTH_SHORT).show();
			}
		});
    }
    
    /**
     * Called when the user presses the back or home button
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	// Disconnect Robot properly
    	RobotProvider.getDefaultProvider().disconnectControlledRobots();
    	mRobot = null;
    }

    /**
     * Causes the robot to blink once every second.
     * @param lit
     */
    private void blink(final boolean lit){
        
        if(mRobot != null){
            
            //If not lit, send command to show blue light, or else, send command to show no light
            if(lit){
                RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 0);
            }else{
                RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 255);
            }
            
            //Send delayed message on a handler to run blink again
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    blink(!lit);
                }
            }, 1000);
        }
    }
}
