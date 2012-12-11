package com.orbotix.sample.macrosample;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import orbotix.macro.BackLED;
import orbotix.macro.Delay;
import orbotix.macro.Fade;
import orbotix.macro.LoopEnd;
import orbotix.macro.LoopStart;
import orbotix.macro.MacroObject;
import orbotix.macro.RawMotor;
import orbotix.macro.Roll;
import orbotix.macro.RotateOverTime;
import orbotix.macro.RGB;
import orbotix.macro.Stabilization;
import orbotix.robot.base.AbortMacroCommand;
import orbotix.robot.base.FrontLEDOutputCommand;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RollCommand;
import orbotix.robot.base.StabilizationCommand;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;

import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.orbotix.sample.macrosample.R;

/**
 * Connects to an available Sphero robot, and then flashes its LED.
 */
public class MacroSample extends Activity
{
	/**
	 * Slider values
	 */
	private int speedValue = 0;
	private int delayValue = 0;
	private int loopValue = 0;

	/**
	 * The Sphero Robots
	 */
	private ArrayList<Robot> mRobots = new ArrayList<Robot>();

	/**
	 * The SpheroConnectionView
	 */
	private SpheroConnectionView mSpheroConnectionView;
	private Button mDoneButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Change Text and Value depending on slider
		final TextView robotspeedlabel = (TextView) findViewById(R.id.speedlabel);
		final TextView robotdelaylabel = (TextView) findViewById(R.id.delaylabel);
		final TextView robotlooplabel = (TextView) findViewById(R.id.looplabel);

		// Set up SeekBar with range from 0 to 10
		SeekBar robotspeedBar = (SeekBar)findViewById(R.id.speedBar);
		robotspeedBar.setMax(10);
		robotspeedBar.setProgress(5);
		robotspeedBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar robotspeedBar) {}
			public void onStartTrackingTouch(SeekBar robotspeedBar) {}

			public void onProgressChanged(SeekBar robotspeedBar, int progress,
					boolean fromUser) {
				// Display the speed percentage and set the speed value
				robotspeedlabel.setText((progress*10) + "%");
				speedValue = progress;
			}
		});


		// Set up SeekBar with range from 0 to 100000
		SeekBar robotdelayBar = (SeekBar)findViewById(R.id.delayBar);
		robotdelayBar.setMax(10000);
		robotspeedBar.setProgress(5000);
		robotdelayBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar robotdelayBar) {}
			public void onStartTrackingTouch(SeekBar robotdelayBar) {}

			public void onProgressChanged(SeekBar robotdelayBar, int progress,
					boolean fromUser) {
				// pass delayBar's value to delayValue
				delayValue = progress;
				robotdelaylabel.setText(progress + "ms");
			}
		});

		// Set up SeekBar with range from 0 to 10
		SeekBar robotloopBar = (SeekBar)findViewById(R.id.loopBar);
		robotloopBar.setMax(10);
		robotspeedBar.setProgress(5);
		robotloopBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar robotloopBar) {}
			public void onStartTrackingTouch(SeekBar robotloopBar) {}

			public void onProgressChanged(SeekBar robotloopBar, int progress,
					boolean fromUser) {
				robotlooplabel.setText(progress+"");
				//pass loopBar's value to loopValue
				loopValue = progress;
			}
		});

		// Set the done button to make the connection view go away
		mDoneButton = (Button)findViewById(R.id.done_button);
		mDoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSpheroConnectionView.setVisibility(View.GONE);
				mDoneButton.setVisibility(View.GONE);
				findViewById(R.id.connection_layout).setVisibility(View.GONE);
			}
		});

		// Find Sphero Connection View from layout file
		mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
		// This event listener will notify you when these events occur, it is up to you what you want to do during them
		mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
			@Override
			public void onRobotConnectionFailed(Robot arg0) {}
			@Override
			public void onNonePaired() {}

			@Override
			public void onRobotConnected(Robot arg0) {
				// Add the robot
				mRobots.add(arg0);
			}
			
			@Override
			public void onBluetoothNotEnabled() {
				// See ButtonDrive Sample on how to show BT settings screen, for now just notify user
				Toast.makeText(MacroSample.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
	}

	/**
	 * Called when the square button is clicked
	 * Drives Spheros in a square
	 * @param v
	 */
	public void squareMacroClicked(View v) {
		//Send Abort Macro
		//Mention Bad states and changing to default
		returnSpheroToStableState();

		if(mRobots.size() > 0){
			for( Robot mRobot : mRobots ) {
				float speed = ((float)(speedValue))/10.0f;
				//Create a new macro object to send to Sphero
				MacroObject squareMacro = new MacroObject();
				//Change Color
				squareMacro.addCommand(new RGB(0, 255, 0, 255));
				//Sphero drives forward in the 0 angle
				squareMacro.addCommand(new Roll(speed, 0, 0));
				//You must delay after a roll command, since the roll command does not itself
				squareMacro.addCommand(new Delay(delayValue));
				//Have Sphero to come to stop to make sharp turn
				squareMacro.addCommand(new Roll(0.0f,0,255));
				//Change Color
				squareMacro.addCommand(new RGB(0, 0, 255, 255));
				//Sphero drives forward in the 90 angle
				squareMacro.addCommand(new Roll(speed, 90, 0));
				squareMacro.addCommand(new Delay(delayValue));
				//Have Sphero to come to stop to make sharp turn
				squareMacro.addCommand(new Roll(0.0f,90,255));
				//Change Color
				squareMacro.addCommand(new RGB(255, 255, 0, 255));
				//Sphero drives forward in the 180 angle
				squareMacro.addCommand(new Roll(speed, 180, 0));
				squareMacro.addCommand(new Delay(delayValue));
				//Have Sphero to come to stop to make sharp turn
				squareMacro.addCommand(new Roll(0.0f,180,255));
				//Change Color
				squareMacro.addCommand(new RGB(255, 0, 0, 255));
				//Sphero drives forward in the 270 angle
				squareMacro.addCommand(new Roll(speed, 270, 0));
				squareMacro.addCommand(new Delay(delayValue));
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
	}

	/**
	 * Called when fade button is clicked
	 * Fades Spheros between colors
	 * @param v
	 */
	public void fadeMacroClicked(View v) {
		returnSpheroToStableState();

		if(mRobots.size() > 0){
			for( Robot mRobot : mRobots ) {
				//Create a new macro object to send to Sphero
				MacroObject fadeMacro = new MacroObject();
				fadeMacro.addCommand(new LoopStart(loopValue));
				// Fade the color from the current to purple
				fadeMacro.addCommand(new Fade(0, 255, 255, delayValue));
				// You must delay because, like the roll command, this does not delay for you
				fadeMacro.addCommand(new Delay(delayValue));
				// Fade the color from purple to yellow
				fadeMacro.addCommand(new Fade(255, 0, 255, delayValue));
				fadeMacro.addCommand(new Delay(delayValue));
				// Fade the color from yellow to whatever this next color is
				fadeMacro.addCommand(new Fade(255, 255, 0, delayValue));
				fadeMacro.addCommand(new Delay(delayValue));
				fadeMacro.addCommand(new LoopEnd());
				fadeMacro.setMode(MacroObject.MacroObjectMode.Normal);
				fadeMacro.setRobot(mRobot);
				fadeMacro.playMacro();
			}
		}
	}

	/**
	 * Called when the shapes button is called
	 * Sphero drives a shape with sides equal to the number of loops slider
	 * @param v
	 */
	public void shapesMacroClicked(View v) {
		returnSpheroToStableState();

		if( loopValue == 0 ) return;

		if(mRobots.size() > 0){
			for( Robot mRobot : mRobots ) {
				float speed = ((float)(speedValue))/10.0f;
				//Create a new macro object to send to Sphero
				MacroObject shapeMacro = new MacroObject();
				//Change Color
				shapeMacro.addCommand(new RGB(0, 0, 255, 255));

				for (int i = 0; i < loopValue; ++i) {
					//Change Color
					shapeMacro.addCommand(new RGB(0, 255, 0, 255));
					// Change direciton to roll in incraments of a 360
					shapeMacro.addCommand(new Roll(speed,i*(360 / loopValue),0));
					shapeMacro.addCommand(new Delay(delayValue));
					//Come to Stop
					shapeMacro.addCommand(new Roll(0.0f,i*(360 / loopValue),255));  
				}
				shapeMacro.addCommand(new Roll(0.0f,0,255)); 
				
				//Set Macro size
				shapeMacro.setMode(MacroObject.MacroObjectMode.Normal);
				shapeMacro.setRobot(mRobot);
				//Send Macro
				shapeMacro.playMacro();
			}
		}
	}

	/**
	 * Called when the figure 8 button is clicked
	 * Drives Spheros in a figure 8 pattern
	 * @param v
	 */
	public void figure8MacroClicked(View v) {
		returnSpheroToStableState();

		if(mRobots.size() > 0){
			for( Robot mRobot : mRobots ) {
				float speed = ((float)(speedValue))/10.0f;
				//Create a new macro object to send to Sphero
				MacroObject figure8Macro = new MacroObject();
				//Tell Robot to look forward and to start driving
				figure8Macro.addCommand(new Roll(speed, 0, 1000));
				//Start loop without slowing down
				figure8Macro.addCommand(new LoopStart(loopValue));
				///Tell Robot to perform 1st turn in the postive direction.
				figure8Macro.addCommand(new RotateOverTime(360, delayValue));
				//Add delay to allow the rotateovertime command to perform.
				figure8Macro.addCommand(new Delay(delayValue));
				//Rotate to perform the 2nd turn in the negitive direction
				figure8Macro.addCommand(new RotateOverTime(-360, delayValue));
				//Add delay to allow the rotateovertime command to perform.
				figure8Macro.addCommand(new Delay(delayValue));
				//End Loop
				figure8Macro.addCommand(new LoopEnd());
				//Come to Stop
				figure8Macro.addCommand(new Roll(0.0f,0,255));
				figure8Macro.setMode(MacroObject.MacroObjectMode.Normal);
				figure8Macro.setRobot(mRobot);
				figure8Macro.playMacro();
			}
		}
	}

	/**
	 * Called when vibrate button is clicked
	 * Vibrates the Spheros
	 * @param v
	 */
	public void vibrateMacroClicked(View v) {
		returnSpheroToStableState();

		if(mRobots.size() > 0){
			for( Robot mRobot : mRobots ) {
				//Create a new macro object to send to Sphero
				MacroObject vibrateMacro = new MacroObject();
				vibrateMacro.addCommand(new RGB(255, 0, 0, 0));
				// You must turn stabilization off to use the raw motors
				vibrateMacro.addCommand(new Stabilization(false,0));
				vibrateMacro.addCommand(new LoopStart(delayValue/50));
				// Run both motors forward for a milli second
				vibrateMacro.addCommand(new RawMotor(RawMotor.DriveMode.FORWARD, 90, RawMotor.DriveMode.FORWARD, 90, 0));
				vibrateMacro.addCommand(new Delay(1));
				// Run both motors backward for a milli second (to simulate a vibration)
				vibrateMacro.addCommand(new RawMotor(RawMotor.DriveMode.REVERSE, 90, RawMotor.DriveMode.REVERSE, 90, 0));
				vibrateMacro.addCommand(new Delay(1));
				vibrateMacro.addCommand(new LoopEnd());
				// Remember to turn stabilization back on to avoid difficulties driving
				vibrateMacro.addCommand(new Stabilization(true,0));
				vibrateMacro.addCommand(new RGB(0, 255, 0, 0));
				vibrateMacro.setMode(MacroObject.MacroObjectMode.Normal);
				vibrateMacro.setRobot(mRobot);
				vibrateMacro.playMacro();
			}
		}
	}

	/**
	 * Called when spin button is clicked
	 * Spins Spheros with tail light on
	 * @param v
	 */
	public void spinMacroClicked(View v) {
		returnSpheroToStableState();
		if(mRobots.size() > 0){
			for( Robot mRobot : mRobots ) {
				//Create a new macro object to send to Sphero
				MacroObject spinMacro = new MacroObject();
				// Turn on the tail light
				spinMacro.addCommand(new BackLED(255, 0));
				spinMacro.addCommand(new LoopStart(loopValue));
				// Tell Sphero to rotate over time 360 degrees at a certain speed
				spinMacro.addCommand(new RotateOverTime(360, 2000/speedValue));
				// Rotate over time requires a delay as well
				spinMacro.addCommand(new Delay(2000/speedValue));
				spinMacro.addCommand(new LoopEnd());
				// Turn off the back led
				spinMacro.addCommand(new BackLED(0, 0));
				spinMacro.setMode(MacroObject.MacroObjectMode.Normal);
				spinMacro.setRobot(mRobot);
				spinMacro.playMacro();
			}
		}
	}

	/**
	 * Called when flip macro is clicked. 
	 * Flips the ball around.
	 * @param v
	 */
	public void flipMacroClicked(View v) {
		returnSpheroToStableState();

		if(mRobots.size() > 0){
			for( Robot mRobot : mRobots ) {
				//Create a new macro object to send to Sphero
				MacroObject flipMacro = new MacroObject();
				flipMacro.addCommand(new RGB(0, 0, 255, 0));
				// You must turn stabilization off to use the raw motors
				flipMacro.addCommand(new Stabilization(false,0));
				// Run both motors forward at full power
				flipMacro.addCommand(new RawMotor(RawMotor.DriveMode.FORWARD, 255, RawMotor.DriveMode.FORWARD, 255, 0));
				// Delay for a certain time period
				flipMacro.addCommand(new Delay(delayValue));
				// Remember to turn stabilization back on
				flipMacro.addCommand(new Stabilization(true,0));
				flipMacro.addCommand(new RGB(0, 255, 0, 0));
				flipMacro.setMode(MacroObject.MacroObjectMode.Normal);
				flipMacro.setRobot(mRobot);
				flipMacro.playMacro();
			}
		}
	}

	/**
	 * Called when stop button is clicked
	 * @param v
	 */
	public void stopMacroClicked(View v) {
		returnSpheroToStableState();
	}

	/**
	 * Puts Spheros back into their default state
	 */
	private void returnSpheroToStableState() {
		if(mRobots.size() > 0){
			for( Robot mRobot : mRobots ) {
				AbortMacroCommand.sendCommand(mRobot); // abort command
				StabilizationCommand.sendCommand(mRobot, true); // turn on stabilization
				RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255); // make Sphero White
				FrontLEDOutputCommand.sendCommand(mRobot, 0.0f);  // Turn off tail light
				RollCommand.sendStop(mRobot);  // Stop rolling
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		mRobots.clear();
		// Shutdown Sphero connection view
		mSpheroConnectionView.shutdown();
		//Disconnect Robots
		RobotProvider.getDefaultProvider().disconnectControlledRobots();
	}

}
