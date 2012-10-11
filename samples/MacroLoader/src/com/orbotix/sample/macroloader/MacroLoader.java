package com.orbotix.sample.macroloader;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import orbotix.macro.MacroObject;
import orbotix.macro.MacroObject.MacroObjectMode;
import orbotix.robot.base.AbortMacroCommand;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.StabilizationCommand;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;
import com.orbotix.sample.macroloader.R;

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
    private Robot mRobot;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        
        //Normal Macro
        //Smaller macros can be used with Normal Marco
    	Button macrobutton1 = (Button) findViewById(R.id.button1);  
    	macrobutton1.setOnClickListener(new View.OnClickListener() { 
    		
    	    public void onClick(View v) {  
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
    	});
    
    	
    	//Change Colors:
    	//Chunky Macro are large macros files 
    	Button macrobutton2 = (Button) findViewById(R.id.button2);  
    	macrobutton2.setOnClickListener(new View.OnClickListener() { 
    		
    	    public void onClick(View v) {  
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
    	});
    	
    	
    	//Shape Macros:
    	//Chunky Macro are large macros files 
    	Button macrobutton3 = (Button) findViewById(R.id.button4);  
    	macrobutton2.setOnClickListener(new View.OnClickListener() { 
    		
    	    public void onClick(View v) {  
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
    	});
    	
    	//Large Dance
    	//Chunky Macro are large macros files 
    	Button macrobutton4 = (Button) findViewById(R.id.button5);  
    	macrobutton2.setOnClickListener(new View.OnClickListener() { 		
    	    public void onClick(View v) {  
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
    	});
    	
    	//Abort Macro Commands:
    	Button stopbutton = (Button) findViewById(R.id.button3);  
    	stopbutton.setOnClickListener(new View.OnClickListener() { 
    		
    	    public void onClick(View v) {  
    	        AbortMacroCommand.sendCommand(mRobot);//abort command
    	        StabilizationCommand.sendCommand(mRobot, true); //turn on stabilization
    	        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);//make Sphero White
    	    }  
    	});

    	// Grab Connection View
    	mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
    	// Listen to Robot Connection Events
    	mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
			@Override
			public void onRobotConnectionFailed(Robot arg0) {}
			@Override
			public void onNonePaired() {}
			
			@Override
			public void onRobotConnected(Robot arg0) {
				mRobot = arg0;
				mSpheroConnectionView.setVisibility(View.GONE);
			}
		});
    	
    }

    @Override
    protected void onStop() {
        super.onStop();

        mRobot = null;

		// Shutdown Sphero connection view
		mSpheroConnectionView.shutdown();
        //Disconnect Robot
        RobotProvider.getDefaultProvider().removeAllControls();
    }

    }

