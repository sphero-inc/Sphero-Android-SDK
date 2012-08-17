package com.orbotix.sample.macrosample;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import orbotix.macro.Calibrate;
import orbotix.macro.Delay;
import orbotix.macro.Fade;
import orbotix.macro.LoopEnd;
import orbotix.macro.LoopStart;
import orbotix.macro.MacroCommand;
import orbotix.macro.MacroObject;
import orbotix.macro.Roll;
import orbotix.macro.RotateOverTime;
import orbotix.macro.MacroObject.MacroObjectMode;
import orbotix.macro.RGB;
import orbotix.macro.RawMotor;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.AbortMacroCommand;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotControl;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RollCommand;
import orbotix.robot.base.StabilizationCommand;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.orbotix.sample.macrosample.R;

/**
 * Connects to an available Sphero robot, and then flashes its LED.
 */
public class MacroSample extends Activity
{
    /**
     * ID for launching the StartupActivity for result to connect to the robot
     */
    private final static int STARTUP_ACTIVITY = 0;
    public static int speedValue=0;
    public static int delayValue=0;
    public static int loopValue=0;

    

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
   
        
        //Change Text and Value depending on slider
        final TextView robotspeedlabel = (TextView) findViewById(R.id.speedlabel);
        final TextView robotdelaylabel = (TextView) findViewById(R.id.delaylabel);
        final TextView robotlooplabel = (TextView) findViewById(R.id.looplabel);

        
        //Set up SeekBar
        SeekBar robotspeedBar = (SeekBar)findViewById(R.id.speedBar);
        robotspeedBar.setMax(10);
        robotspeedBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
   
 
   public void onStopTrackingTouch(SeekBar robotspeedBar) {
   }
   

   public void onStartTrackingTouch(SeekBar robotspeedBar) {
    // TODO Auto-generated method stub
   }
   
 
   public void onProgressChanged(SeekBar robotspeedBar, int progress,
     boolean fromUser) {
    robotspeedlabel.setText(+progress);
    speedValue = (progress);

   }
  });
     
        
        //Set up SeekBar
        SeekBar robotdelayBar = (SeekBar)findViewById(R.id.delayBar);
        robotdelayBar.setMax(10000);
        robotdelayBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
   
 
   public void onStopTrackingTouch(SeekBar robotdelayBar) {
   }
   

   public void onStartTrackingTouch(SeekBar robotdelayBar) {
    // TODO Auto-generated method stub
   }
   

   public void onProgressChanged(SeekBar robotdelayBar, int progress,
     boolean fromUser) {
	    //pass delayBar's value to delayValue
	   delayValue = progress;
	   robotdelaylabel.setText(+progress);
   }
  });
        
        
        
        //Set up LoopBar
        SeekBar robotloopBar = (SeekBar)findViewById(R.id.loopBar);
        //Set value 1-10
        robotloopBar.setMax(10);
        robotloopBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
   

   public void onStopTrackingTouch(SeekBar robotloopBar) {
   }
   

   public void onStartTrackingTouch(SeekBar robotloopBar) {
    // TODO Auto-generated method stub
   }
   

   public void onProgressChanged(SeekBar robotloopBar, int progress,
     boolean fromUser) {
    robotlooplabel.setText(+progress);
    //pass loopBar's value to loopValue
	 loopValue = progress;

   }
  });
        
    }

    public void squareClicked(View v) {
	       //Send Abort Macro
 	    	//Mention Bad states and changing to default
 	    	AbortMacroCommand.sendCommand(mRobot);
 	        StabilizationCommand.sendCommand(mRobot, true);
 	        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
 	        //Stop Commmand
 	    	
 	    	if(mRobot != null){
                 //MacroObject macro= null;
      
                 	//Create a new macro object to send to Sphero
                     MacroObject squareMacro = new MacroObject();
                     //Change Color
                     squareMacro.addCommand(new RGB(0, 255, 0, 255));
                     //Sphero drives forward in the 0 angle
                     squareMacro.addCommand(new Roll(speedValue, 0, delayValue));
                     //Have Sphero to come to stop to make sharp turn
                     squareMacro.addCommand(new Roll(0.0f,0,255));
                     //Change Color
                     squareMacro.addCommand(new RGB(0, 0, 255, 255));
                     //Sphero drives forward in the 90 angle
                     squareMacro.addCommand(new Roll(speedValue, 90, delayValue));
                     //Have Sphero to come to stop to make sharp turn
                     squareMacro.addCommand(new Roll(0.0f,90,255));
                     //Change Color
                     squareMacro.addCommand(new RGB(255, 255, 0, 255));
                     //Sphero drives forward in the 180 angle
                     squareMacro.addCommand(new Roll(speedValue, 180, delayValue));
                     //Have Sphero to come to stop to make sharp turn
                     squareMacro.addCommand(new Roll(0.0f,180,255));
                     //Change Color
                     squareMacro.addCommand(new RGB(255, 0, 0, 255));
                     //Sphero drives forward in the 270 angle
                     squareMacro.addCommand(new Roll(speedValue, 270, delayValue));
                     //Have Sphero to come to stop to make sharp turn
                     squareMacro.addCommand(new Roll(0.0f,270,255));
                     //Change Color
                     squareMacro.addCommand(new RGB(255, 255, 255, 255));        
                     squareMacro.addCommand(new Roll(0.0f,0,255));
                     squareMacro.setMode(MacroObject.MacroObjectMode.Normal);
                     squareMacro.setRobot(mRobot);
                     squareMacro.playMacro();
 	    	}
    }
    
    public void shapeClicked(View v) {
	    	AbortMacroCommand.sendCommand(mRobot);
 	        StabilizationCommand.sendCommand(mRobot, true);
 	        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
 	    	
 	    	if(mRobot != null){
                // MacroObject macro= null;
     
                 	//Create a new macro object to send to Sphero
                    MacroObject shapeMacro = new MacroObject();
                    //Sets loop from slider value
                    shapeMacro.addCommand(new LoopStart(loopValue));
                    //Change Color
                    shapeMacro.addCommand(new RGB(0, 0, 255, 255));
                    for (int i=0; i< loopValue; ++i){
                    
                    shapeMacro.addCommand(new Calibrate(i*(360 / loopValue), 255));
                    //Set new calibrated heading to Zero 
                    shapeMacro.addCommand(new Calibrate(0, 255));
                    //Change Color
                    shapeMacro.addCommand(new RGB(0, 255, 0, 255));
                    //Come to Stop
                   shapeMacro.addCommand(new Roll(speedValue,i*(360 / loopValue),delayValue));
                   shapeMacro.addCommand(new Roll(0.0f,0,255));  
                    }
                    
                   //Loop End
                   shapeMacro.addCommand(new LoopEnd());
                   //Set Macro size
                   shapeMacro.setMode(MacroObject.MacroObjectMode.Normal);
                   shapeMacro.setRobot(mRobot);
                   //Send Macro
                   shapeMacro.playMacro();

             }
    }
    
    public void figureeightClicked(View v) {
	    	AbortMacroCommand.sendCommand(mRobot);
 	        StabilizationCommand.sendCommand(mRobot, true);
 	        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
 	    	
 	    	if(mRobot != null){
                 //MacroObject macro= null;
     
                 	//Create a new macro object to send to Sphero
                    MacroObject figureMacro = new MacroObject();
                    //Tell Robot to look forward and to start driving
                    figureMacro.addCommand(new Roll(speedValue, 0, 1000));
                    //Start loop without slowing down
                    figureMacro.addCommand(new LoopStart(loopValue));
                    ///Tell Robot to perform 1st turn in the postive direction.
                    figureMacro.addCommand(new RotateOverTime(360, delayValue));
                    //Add delay to allow the rotateovertime command to perform.
                    figureMacro.addCommand(new Delay(delayValue));
                    //Rotate to perform the 2nd turn in the negitive direction
                    figureMacro.addCommand(new RotateOverTime(-360, delayValue));
                    //Add delay to allow the rotateovertime command to perform.
                    figureMacro.addCommand(new Delay(delayValue));
                    //End Loop
                    figureMacro.addCommand(new LoopEnd());
                    //Come to Stop
                    figureMacro.addCommand(new Roll(0.0f,0,255));
                    figureMacro.setMode(MacroObject.MacroObjectMode.Normal);
                   figureMacro.setRobot(mRobot);
                   figureMacro.playMacro();

             }
    }
    
    
    public void fadeClicked(View v) {
    	 //Abort Macro
        AbortMacroCommand.sendCommand(mRobot);
        StabilizationCommand.sendCommand(mRobot, true);
        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
        
    	if(mRobot != null){
            //MacroObject macro= null;

            	//Create a new macro object to send to Sphero
                MacroObject fadeMacro = new MacroObject();
                fadeMacro.addCommand(new LoopStart(loopValue));
                fadeMacro.addCommand(new Fade(255, 0, 0, delayValue));
                fadeMacro.addCommand(new Fade(0, 0, 255, delayValue));
                fadeMacro.addCommand(new Fade(0, 255, 0, delayValue));
                fadeMacro.addCommand(new LoopEnd());
                fadeMacro.setMode(MacroObject.MacroObjectMode.Normal);
                fadeMacro.setRobot(mRobot);
                fadeMacro.playMacro();

        }
    }
    
    
    public void stopClicked(View v) {
    	AbortMacroCommand.sendCommand(mRobot);
        StabilizationCommand.sendCommand(mRobot, true);
        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
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
