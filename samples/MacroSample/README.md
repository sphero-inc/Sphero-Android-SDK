![logo](http://update.orbotix.com/developer/sphero-small.png)

# Macro Sample

This sample demonstrates how to connect to multiple balls with the `SpheroConnectionView` and also how to programmatically create macros and run them on multiple Spheros.

## Connecting to Multiple Spheros

Instead of having a member variable that holds a single Robot object, you now have a member variable that is an ArrayList that holds multiple Robot objects.

	/** The Sphero Robots */
	private ArrayList<Robot> mRobots = new ArrayList<Robot>();
	
In previous samples, we have hidden the `SpheroConnectionView` after the first Robot connected.  However, in this sample, we will continue to add Robots to the ArrayList until the user clicks the done button.  The program code for this is below:

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
		
		// This event listener will notify you when these events occur, it is up to you what you 		want to do during them
		mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() 		{
			@Override
			public void onRobotConnectionFailed(Robot arg0) {}
			@Override
			public void onNonePaired() {}

			@Override
			public void onRobotConnected(Robot arg0) {
				// Add the robot
				mRobots.add(arg0);
			}
		});	
	
## Creating Macros

There are 5 main steps to running a macro on a Sphero.
	
1. To create a macro 

		MacroObject macro = new MacroObject();      

2. Add commands to the object

		macro.addCommand(…);
		
3. Set the Macro transmission mode.  Almost always use Normal, unless your Macro is longer than 254 bytes, in which case, use Chunky

		macro.setMode(MacroObject.MacroObjectMode.Normal);

4. Set the Robot to transmit the macro to

		macro.setRobot(mRobot);

5. Lastly, play the macro

		mRobot.executeMacro(macro);
		
## Running Macros on Multiple Spheros

To run the macros on multiple Spheros, simply wrap the macro command creation and play inside a for each statement that runs the macro on each Sphero.

		if(mRobots.size() > 0){
			for( Robot mRobot : mRobots ) {
				// Macro Code
			}
		}

## Return Sphero to Default State

When you want to end a macro, or play another macro, use the abort command and then set the Spheros back to their default state.

	/**
	 * Puts Spheros back into their default state
	 */
	private void returnSpheroToStableState() {
		if(mRobots.size() > 0){
			for( Robot mRobot : mRobots ) {
				AbortMacroCommand.sendCommand(mRobot); // abort command
				mRobot.enableStabilization(true); // turn on stabilization
				mRobot.setColor(255, 255, 255); // make Sphero White
				mRobot.setBackLEDBrightness(0.0f);  // Turn off tail light
				mRobot.stop();  // Stop rolling
			}
		}
	}


## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

