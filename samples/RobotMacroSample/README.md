![logo](http://update.orbotix.com/developer/sphero-small.png)

# MacrosJava

This sample code demonstrates how to write macros in Android Java.


To create a macro,  

        RKMacroObject *macro = [RKMacroObject new];        

This call is implemented to create an object obtaining macro commands. 

To add new commands to the object:

        [macro addCommand: ...];

And to finally play the macro:

		[macro playMacro]; 

Simple Square, no repeat.

	//Set up Shape Square
    	Button macrobutton2 = (Button) findViewById(R.id.button1);  
    	macrobutton2.setOnClickListener(new View.OnClickListener() { 
    		
    	    public void onClick(View v) {  
    	       //Send Abort Macro
    	    	//Mention Bad states and changing to default
    	    	AbortMacroCommand.sendCommand(mRobot);
    	        StabilizationCommand.sendCommand(mRobot, true);
    	        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
    	        //Stop Command
    	    	
    	    	if(mRobot != null){
                    MacroObject macro= null;
         
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
    	});

Roll Commands: Give a speed to travel, a direction in an 360 degree path, and a delay for the period of time of the action.


</br>

Colors Fade during action (Circle)
</br>

Slew(Fade) is a parallel command
</br>

When Slew action is performed, either have it run 
parallel to a roll command or a delay.
</br>

If the user was to include a blink color it would then end the slew abruptly.

        //ColorFade with Loop
        Button macrobutton1 = (Button) findViewById(R.id.button1);  
    	macrobutton1.setOnClickListener(new View.OnClickListener() { 
    		
    	    public void onClick(View v) {  
                //Abort Macro
    	        AbortMacroCommand.sendCommand(mRobot);
    	        StabilizationCommand.sendCommand(mRobot, true);
    	        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
    	        
    	    	if(mRobot != null){
                    MacroObject macro= null;
        
                    	//Create a new macro object to send to Sphero
                        MacroObject fadeMacro = new MacroObject();
                        fadeMacro.addCommand(new LoopStart(loopValue));
                        fadeMacro.addCommand(new RGB(0, 255, 255, delayValue));
                        fadeMacro.addCommand(new RGB(255, 0, 255, delayValue));
                        fadeMacro.addCommand(new RGB(255, 255, 0, delayValue));
                        fadeMacro.addCommand(new LoopEnd());
                        fadeMacro.setMode(MacroObject.MacroObjectMode.Normal);
                        fadeMacro.setRobot(mRobot);
                        fadeMacro.playMacro();

                }
    	    }  
    	});

</br>

Macro Shape
</br>

Changes the Shape depending on the number of loops you 
include
</br>

Example: 4 loops makes a square
360 degrees / (4)= 90 degree turns     

	//Set up Shape 
     	Button macrobutton3 = (Button) findViewById(R.id.button1);  
     	macrobutton2.setOnClickListener(new View.OnClickListener() { 
     		
     	    public void onClick(View v) {  
     	       
     	    	AbortMacroCommand.sendCommand(mRobot);
     	        StabilizationCommand.sendCommand(mRobot, true);
     	        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
     	    	
     	    	if(mRobot != null){
                     MacroObject macro= null;
         
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
     	});

</br>
Figure 8 repeat
</br>
RotateOverTime is a parallel command.
</br>
When a rotation action is performed, either have it run parallel to a color command or a delay.
</br>
If the user was to include a drive command it would then end the rotation abruptly.


	//Set up Shape Figure8
     	Button macrobutton4 = (Button) findViewById(R.id.button1);  
     	macrobutton2.setOnClickListener(new View.OnClickListener() { 
     		
     	    public void onClick(View v) {  
     	       
     	    	AbortMacroCommand.sendCommand(mRobot);
     	        StabilizationCommand.sendCommand(mRobot, true);
     	        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
     	    	
     	    	if(mRobot != null){
                     MacroObject macro= null;
         
                     	//Create a new macro object to send to Sphero
                        MacroObject figureMacro = new MacroObject();
                        //Tell Robot to look forward and to start driving
                        figureMacro.addCommand(new Roll(speedValue, 0, 1000));
                        //Start loop without slowing down
                        figureMacro.addCommand(new LoopStart(loopValue));
                        ///Tell Robot to perform 1st turn in the positive direction.
                        figureMacro.addCommand(new RotateOverTime(360, delayValue));
                        //Add delay to allow the rotateovertime command to perform.
                        figureMacro.addCommand(new Delay(delayValue));
                        //Rotate to perform the 2nd turn in the negative direction
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
     	});
     	
     
Stop All Macros:
</br>
When you want to end a macro, or play another macro, use the abort command and then set sphere in the wanted state.

    	Button stopbutton = (Button) findViewById(R.id.button1);  
    	stopbutton.setOnClickListener(new View.OnClickListener() { 
    		
    	    public void onClick(View v) {  
    	        AbortMacroCommand.sendCommand(mRobot);
    	        StabilizationCommand.sendCommand(mRobot, true);
    	        RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);
    	    }  
    	});
