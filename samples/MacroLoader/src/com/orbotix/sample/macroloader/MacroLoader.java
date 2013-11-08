package com.orbotix.sample.macroloader;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import orbotix.macro.MacroObject;
import orbotix.macro.MacroObject.MacroObjectMode;
import orbotix.robot.base.AbortMacroCommand;
import orbotix.robot.base.BackLEDOutputCommand;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RollCommand;
import orbotix.robot.base.StabilizationCommand;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

/**
 * Connects to an available Sphero robot, and then flashes its LED.
 */
public class MacroLoader extends Activity
{
	/**
	 * Sphero Connection View
	 */
	private SpheroConnectionView mSpheroConnectionView;

	/**
	 * The Sphero Robot
	 */
	private Sphero mRobot;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Grab Connection View
		mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
		// Listen to Robot Connection Events
        mSpheroConnectionView.addConnectionListener(new ConnectionListener() {

            @Override
            public void onConnected(Robot robot) {
                mRobot = (Sphero)robot;
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                // let the SpheroConnectionView handle or hide it and do something here...
            }

            @Override
            public void onDisconnected(Robot sphero) {
                mSpheroConnectionView.startDiscovery();
            }
        });
	}
	
    /**
     * Called when the user comes back to this app
     */
    @Override
    protected void onResume() {
    	super.onResume();
        // Refresh list of Spheros
        mSpheroConnectionView.startDiscovery();
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
	 * Called when the large dance button is clicked
	 * @param v
	 */
	public void largeDanceClicked(View v) {
		returnSpheroToStableState();
		//Checks if Robot is Null
		if(mRobot != null){
			FileManager files= new FileManager();
			MacroObject macro= null;
			try {
				//opens the Macro Binary dance1
				macro = files.getMacro(v.getContext(), "bigdance.sphero"); //Shape
				//Sets the macro size
				macro.setMode(MacroObjectMode.Chunky);
				//Set Robot
				macro.setRobot(mRobot);
				//Send Macro to Sphero
				macro.playMacro(); 
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Called when the command button is clicked
	 * @param v
	 */
	public void commandClicked(View v) {
		returnSpheroToStableState();
		//Checks if Robot is Null
		if(mRobot != null){
			FileManager files= new FileManager();
			MacroObject macro= null;
			try {
				//opens the Macro Binary dance1
				macro = files.getMacro(v.getContext(), "symboll.sphero");//Fade
				//Sets the macro size
				macro.setMode(MacroObjectMode.Normal);
				//Set Robot
				macro.setRobot(mRobot);
				//Send Macro to Sphero
				macro.playMacro(); 
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Called when the strobe button is clicked
	 * @param v
	 */
	public void strobeClicked(View v) {
		returnSpheroToStableState();
		//Checks if Robot is Null
		if(mRobot != null){
			FileManager files= new FileManager();
			MacroObject macro= null;
			try {
				//opens the Macro Binary dance1
				macro = files.getMacro(v.getContext(), "strobelight.sphero");// Strobe light
				//Sets the macro size
				macro.setMode(MacroObjectMode.Normal);
				//Set Robot
				macro.setRobot(mRobot);
				//Send Macro to Sphero
				macro.playMacro(); 
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Called when the smalle dance button is clicked
	 */
	public void smallDanceClicked(View v) {
		returnSpheroToStableState();
		//Checks if Robot is Null
		if(mRobot != null){
			FileManager files= new FileManager();
			MacroObject macro= null;
			try {
				//opens the Macro Binary dance1
				macro = files.getMacro(v.getContext(), "dance1.sphero"); //Small Dance
				//Sets the macro size
				macro.setMode(MacroObjectMode.Normal);
				//Set Robot
				macro.setRobot(mRobot);
				//Send Macro to Sphero
				macro.playMacro(); 
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Called when the stop button is clicked
	 * @param v
	 */
	public void stopClicked(View v) { 
		returnSpheroToStableState();
	}

	/**
	 * Puts Spheros back into their default state
	 */
	private void returnSpheroToStableState() {
		AbortMacroCommand.sendCommand(mRobot); // abort command
		StabilizationCommand.sendCommand(mRobot, true); // turn on stabilization
		RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255); // make Sphero White
		BackLEDOutputCommand.sendCommand(mRobot, 0.0f);  // Turn off tail light
		RollCommand.sendStop(mRobot);  // Stop rolling
	}
}

