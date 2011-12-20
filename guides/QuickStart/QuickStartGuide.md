#Quick Start Guide

1. Download the SDK.

	<!-- File in actual information on where to download the SDK libraries archive. -->

2. Create an android project in Eclipse.  
    
    The libraries are made to support andriod API level 8 (Android 2.2) or greater. 
    Plus, you need set the Java language compliance level to 6.0(1.6).
    
3. Place the libs folder from the SDK into your android project's folder.  
   
     ![title](file://localhost/Users/brian/Documents/Codework/MOBILE-ANDROID-SDK/guides/QuickStart/QSG-libs.png)

4. Set the dependency to RobotLibrary.jar.  
   
    Set the dependency in the project's properties in the Properties->Java Build 
    Path-> Libraries dialog.

    ![title](file://localhost/Users/brian/Documents/Codework/MOBILE-ANDROID-SDK/guides/QuickStart/QSG-jar-depend.png)
    
5. Add the RobotUILibrary source library into the workspace.  

	 Import the library into workspace using Import->General->Existing projects    
	 into Workspace. You can have the importer copy the source into your 
     workspace folder or you can place it into the workspace folder before 
     importing. Once it is imported, you need to set the project dependent on 
     RobotLibrary.jar, which is  added using the Properties->Java Build ->Libraries 
     dialog. You will need to add RobotUILibrary as library project to your 
     application project from the projects Properties dialog.
     
     ![title](file://localhost/Users/brian/Documents/Codework/MOBILE-ANDROID-SDK/guides/QuickStart/QSG-library-project.png)
     
     RobotUILibrary is distributed as source, so you can 
     customize the look of the resources to fit in with your 
     applications.
     
6. Add code to connect to a sphero.  
    
    The RobotUILibrary includes an Activity called  StartupActivity which will 
    handle connectioning to a Sphero. When the activity exits, your are ready to 
    send commands.  
    
   To run the StartupActivity, add the following code to the main activity's onStart() method,  

        Intent i = new Intent(this, StartupActivity.class);  
        startActivityForResult(i, STARTUP_ACTIVITY);  
       
   This will launch the StartupActivity using the intent, and sets an activity results 
   identifier. The ``STARTUP_ACTIVITY`` constant is returned in the ``onActivityResult()`` method 
   after the StartupActivity finishes as shown in the code below.  
   
       private final static int STARTUP_ACTIVITY = 0;

       protected void onActivityResult(int requestCode, int resultCode, Intent data){
           super.onActivityResult(requestCode, resultCode, data);
           if(requestCode == STARTUP_ACTIVITY && resultCode == RESULT_OK){
               //Get the connected Robot
               final String robot_id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);  // 1
               if(robot_id != null && !robot_id.equals("")){
                   mRobot = RobotProvider.getDefaultProvider().findRobot(robot_id);          // 2
               }
               //Start blinking
               blink(false);                                                                 // 3
           }
        }
    
   (1)	This line gets an identifier for the robot that was connected returned from the activity.  
   (2) 	This line gets a Robot object for the robot identifier which is used to identifier the connected
      Sphero in other API calls. (The API supports connection to multiple Spheros.)  
   (3) 	This line calls a method that will be used to blink Sphero's LED.  

7. Add code to blink the RGB LED.

	Now it is time to add code that sends a command to Sphero. In this case we will blink the 
	RGB LED blue. Commands are encapsulated by subclasses of DeviceCommand and message to Sphero using 
	DeviceMesssenger singleton. For convenience, DeviceCommand subclasses have class `sendCommand()`
	methods that will post messages to the DeviceMessenger singleton. Here is the code for the 
	`blink()` method sends the SetRGBLEDCommand to blink LED.

	   private void blink(final boolean lit){
	       
	       if(mRobot != null){
	           
	           //If not lit, send command to show blue light, or else, send command to show no light
	           if(lit){
	               RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 0);        // 1
	           }else{
	               RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 255);      // 2
	           }
	           
	           //Send delayed message on a handler to run blink again
	           final Handler handler = new Handler();                       // 3
	           handler.postDelayed(new Runnable() {
	               public void run() {
	                   blink(!lit);
	               }
	           }, 1000);
	       }
	   }
	
	(1) This line will send a command to turn off the LED. `mRobot` is the Robot object that will receive the
		command, and last three parameters turn of the red, green, and blue components of the LED. A 0 value 
		for the color component will set the LED components brightness off.  
	(2) This line will send a command to turn on the blue LED at full brightness. 255 is full brightness, and is
		only set for the blue component of the LED.  
	(3) This line creates a Handler that is used to post a delayed call to the `blink()` method after 1 second
		with the lit parameter inverted, so the next call toggles the LED on or off.

8. Add to the AndroidManifest.xml file.

	Before running the application, you need to add an activity entry for the StartupActivity to the 
	AndroidManifest.xml file. For convenience, you can copy this from the RobotUILibrary project's AndroidManifest.xml 
	file. Add the following,

		<activity android:name="orbotix.robot.app.StartupActivity"
			android:theme="@android:style/Theme.Translucent"
			android:launchMode="singleTop"/>

	You will need to add permissions to use bluetooth too, so add the following line,

		    <uses-permission android:name="android.permission.BLUETOOTH" />

	Also, add `android:launchMode="singleTask"` to the main activity.


8. Run on a device.

9. Changing the RGB LED command to a roll command.

	So, cool you got the LED to blink, but it's a ball that can drive with your Android device. So, how would you do that?
	Well, to make it go you send Sphero a RollCommand. RollCommand takes two parameter to make it go. One parameter is a 
	heading in degrees from 0° to 360° and the other is a speed from 0.0 to 1.0. For example, a heading of 90° at a speed 
	a speed of 0.5 will tell Sphero to turn clockwise 90° at half speed (1.0 is full speed). Once this command is issued 
	Sphero will go forever at this heading and speed, so you will need to stop it some how. RollCommand has a `sendStop()`
	method for this.

	Now, it's time to modify the code. Let's send Sphero forward at full speed for 2 seconds. So, add the following method 
	to the main activity.

	    private void drive() {
	    	if(mRobot != null) {
	    		// Send a roll command to Sphero so it goes forward at full speed.
	    		RollCommand.sendCommand(mRobot, 0.0f, 1.0f);                         // 1
	    		
	    		// Send a delayed message on a handler
	    		final Handler handler = new Handler();                               // 2
	    		handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// Send a stop to Sphero
						RollCommand.sendStop(mRobot);                               // 3
					}
				}, 2000);
	    		
	    	}
	    }

	(1) This line sends the heading of 0° and the maximum speed of 1.0 to Sphero.
	(2) This line creates the handle that is used to send the delayed stop command.
	(3) This line uses the RollCommand to send the stop.

	Next add a call to `drive()` in the `onActivityResult()` below the call to `blink()`.

10. Run again.

11. Did you notice... Calibration and the necessity to set the 0° heading.

	Wait a minute did that really go forward. Isn't forward in the direction that I'm facing? Sphero really doesn't know 
	what forward is to you or your Android device. So, it just picks a arbitrary direction for it's 0° heading. So, you 
	have to tell it what should be 0°. This process of aiming Sphero is called calibration, and is the subject of the 
	<!--Insert guide name and a link that covers calibration-->