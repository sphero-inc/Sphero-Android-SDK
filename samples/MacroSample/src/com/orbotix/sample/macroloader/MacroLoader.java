package com.orbotix.sample.macroloader;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.os.Handler;
import android.view.View;
import android.widget.Button;
//import android.widget.ImageButton;
import orbotix.macro.MacroObject;
import orbotix.macro.MacroObject.MacroObjectMode;
import orbotix.macro.RGB;
import orbotix.macro.Roll;
import orbotix.macro.RollSD1;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.AbortMacroCommand;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RollCommand;
//import orbotix.robot.base.RobotControl;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.StabilizationCommand;
//import com.orbotix.sample.helloworld.FileManager;
import com.orbotix.sample.macroloader.R;

/**
 * Connects to an available Sphero robot, and then flashes its LED.
 */
public class MacroLoader extends Activity
{
    /**
     * ID for launching the StartupActivity for result to connect to the robot
     */
    private final static int STARTUP_ACTIVITY = 0;

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
        
        //Normal Macro
        //Smaller macros can be used with Normal Marco

    	
    	//Change Colors:
    	//Chunky Macro are large macros files 

    	



    //Shape Macros:
    //Chunky Macro are large macros files 
    public void commandClicked(View v) {
        //Aborts Previous Macro Command
    	AbortMacroCommand.sendCommand(mRobot);
        StabilizationCommand.sendCommand(mRobot, true);
        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
        //Checks if Robot is Null
    	if(mRobot != null){
        	FileManager files= new FileManager();
            MacroObject macro= null;
            try {
           	 //opens the Macro Binary dance1
				 macro = files.getMacro(v.getContext(), "symboll.sphero");//Fade
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
    	
    public void smalldanceClicked(View v) {
    	//Aborts Previous Macro Command
        AbortMacroCommand.sendCommand(mRobot);
        StabilizationCommand.sendCommand(mRobot, true);
        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
        
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
        
    public void strobeClicked(View v) {
        //Aborts Previous Macro Command
    	AbortMacroCommand.sendCommand(mRobot);
        StabilizationCommand.sendCommand(mRobot, true);
        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
        //Checks if Robot is Null
    	if(mRobot != null){
        	FileManager files= new FileManager();
            MacroObject macro= null;
            try {
           	 //opens the Macro Binary dance1
				 macro = files.getMacro(v.getContext(), "strobelight.sphero");// Large Dance
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
    
	//Large Dance
	//Chunky Macro are large macros files 
    public void largedanceClicked(View v) {
        //Aborts Previous Macro Command
    	AbortMacroCommand.sendCommand(mRobot);
        StabilizationCommand.sendCommand(mRobot, true);
        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
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
    
    	//Abort Macro Commands:
    public void stopClicked(View v) {
        AbortMacroCommand.sendCommand(mRobot);//abort command
        StabilizationCommand.sendCommand(mRobot, true); //turn on stabilization
        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);//make Sphero White
    }
    
    
    @Override
    protected void onStart() {
    	super.onStart();

    	//Launch the StartupActivity to connect to the robot
        Intent i = new Intent(this, StartupActivity.class);
        startActivityForResult(i, STARTUP_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(requestCode == STARTUP_ACTIVITY && resultCode == RESULT_OK){

            //Get the connected Robot
            final String robot_id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);
            if(robot_id != null && !robot_id.equals("")){
                mRobot = RobotProvider.getDefaultProvider().findRobot(robot_id);
            }
            
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mRobot = null;

        //Disconnect Robot
        RobotProvider.getDefaultProvider().removeAllControls();
    }

    }

