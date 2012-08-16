
![logo](http://update.orbotix.com/developer/sphero-small.png)

# Temporary Macro Sample

Adding MacroLab macros to app
importing the files out of MacroLab:

Follow the tutorial below to get the file attached to an E-mail to your own address.

http://forum.gosphero.com/showthread...5-Share-Macros

Placing Files in Xcode:

Drag file into assets.

Import MacroCommands and AbortMacro from RobotLibrary:
</br>

Calling the files in Main Activity:

Code:
  
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
    

Stopping all actions:

</br>
When you want to stop a previous played command, its easy to send in an Abort and then reset the Sphero's state you want it to continue in.
Code:

                //Aborts Previous Macro Command
    	        AbortMacroCommand.sendCommand(mRobot);
    	        StabilizationCommand.sendCommand(mRobot, true);

Chunky Macros vs Normal Macros:
</br>
There are two forms of macro sizes; normal and chunky. 
</br>
Most macros can be loaded as normal macros but for long events or dances, (like the example below) the user would need to load it as a chunky command.
Chunky Macro are any commands exceeding 254 bytes
	
	 //opens the Macro Binary dance1
						 macro = files.getMacro(v.getContext(), "bigdance.sphero"); //Shape
						 //Sets the macro size
						 macro.setMode(MacroObjectMode.Chunky);
	                     



</br>
The Sample includes 4 macros that include a strobe, clover, small dance and large dance.
