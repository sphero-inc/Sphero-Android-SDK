
![logo](http://update.orbotix.com/developer/sphero-small.png)

# Macro Loader Sample

Adding MacroLab macros to app
importing the files out of MacroLab:

Follow the tutorial below to get the file attached to an E-mail to your own address.

http://forum.gosphero.com/showthread...5-Share-Macros

## Placing Files in Xcode:

1. Drag file into assets.

2. Import MacroCommands and AbortMacro from RobotLibrary:

3. Calling the files in Main Activity:

## Code to Load a Macro and Play
        
    //Checks if Robot is Null
   	if(mRobot != null){
    	FileManager files= new FileManager();
        MacroObject macro= null;
       	try {
        	// Opens the Macro Binary dance1
			macro = files.getMacro(v.getContext(), "dance1.sphero"); //Small Dance
	 		//Sets the macro size
	 		macro.setMode(MacroObjectMode.Normal);
	       	//Set Robot
	 		macro.setRobot(mRobot);
	 		//Send Macro to Sphero
	 		mRobot.executeMacro(macro);                 
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    

## Returning Sphero to a Stable State

When you want to stop a previous played command, its easy to send in an Abort and reset the Sphero's state you want it to continue in.  The code is as follows:

	AbortMacroCommand.sendCommand(mRobot); // abort command
	mRobot.enableStabilization(true); // turn on stabilization
	mRobot.setColor(255, 255, 255); // make Sphero White
	mRobot.setBackLEDBrightness(0.0f);  // Turn off tail light
	mRobot.stop();  // Stop rolling

## Chunky Macros vs Normal Macros

There are two forms of macro sizes; normal and chunky. 
Most macros can be loaded as normal macros but for long events or dances, (like the example below) the user would need to load it as a chunky command.
Chunky Macro are any commands exceeding 254 bytes
	
	//opens the Macro Binary dance1
	macro = files.getMacro(v.getContext(), "bigdance.sphero"); //Shape
	//Sets the macro size
	macro.setMode(MacroObjectMode.Chunky);
	                     

The Sample includes 4 macros that include a strobe, clover, small dance and large dance.

## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)
