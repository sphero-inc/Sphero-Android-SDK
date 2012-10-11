![android.jpg](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/image001.jpg)

# Android Developer Quick Start Guide

## Overview
 
This Guide walks you through the basics of creating mobile apps for Android that leverage the Orbotix Sphero SDK. The examples in this guide were built using Java and although we strive to support **ALL** the various Android devices available there are a few that are known to cause problems. Visit our developer forum for more information. The goal of this developer guide along with sample code is to give the developer a taste of the wide variety of things Sphero can do, respond to, and keep track of.

*In general this guide will walk you through:*

 - Installing the SDK
 - Changing Sphero's Color
 - Using the Roll Command to move Sphero 

### Before we begin - Installing Eclipse

    Notice: The Sphero Android SDK works with Android 2.2+

Before you begin to develop applications that interface with Sphero on Android, you will need to install the Android developer tools. We often use Eclipse but there are many other well written Android IDEs out there and these same basic steps are most often still applicable. 

 - Install the [Android SDK](http://developer.android.com/sdk/index.html) and the [Eclipse Plugin](http://developer.android.com/sdk/eclipse-adt.html)

## Installing the Sphero Android SDK

 - Download the latest version of the [Sphero Android SDK](https://github.com/orbotix/Sphero-Android-SDK/zipball/master)
     * *You can always keep up to date by watching our [GitHub Repository](https://github.com/orbotix/Sphero-Android-SDK)*

### Create a new Android project in Eclipse.  

When creating a new project it is important to take special notice to the Android API Level and the Java compliance level. The Sphero SDK currently supports: 
    
 - Andriod API level 8 (Android 2.2) or greater. 
 - Java language compliance level 6.0(1.6) .
    
### Integrating the Sphero Libraries 

 You can start a new Sphero project using the libraries in the library folder or start a project using one of the sample projects from the samples folder. This quick start guide describes how to start a new project. 
 
 To start, create a new Android project in your Eclipse workspace. Then, place the libs folder from the SDK's library folder into your Android project's folder.  
   
   ![QSG-libs.png](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/image002.png)

### Setting the dependency to RobotLibrary.jar.  
   
  Set the dependency in the project's properties in the Properties->Java Build Path-> Libraries dialog.  This will allow your project access to all the public method names in RobotLibrary.jar. 
 
 ![QSG-jar-depend.png](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/image003.png)
    
### Adding the RobotUILibrary source library into the workspace.  

 Import the source library into your workspace by using Import->General->"Existing projects    
into Workspace". You can have the importer copy the source into your workspace folder or you can place it into the workspace folder before importing. Once it is imported, you need to set the project dependent on RobotLibrary.jar, which is  added using the Properties->Java Build ->Libraries dialog. You will need to add RobotUILibrary as library project to your application project from the projects Properties dialog.
    
  ![QSG-library-project.png](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/image004.png)
    
  RobotUILibrary is distributed as source, so you can customize the look of the resources to fit in with your applications.  

## Using the Sphero Android SDK
     
### Add code to connect to a sphero.  
    
 The RobotUILibrary includes an Activity called  StartupActivity which will 
 handle connectioning to a Sphero. When the activity exits, your are ready to 
 send commands.  
    
  - To run the StartupActivity, add the following code to the main activity's onStart() method,  

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
    
   1.	This line gets an identifier for the robot that was connected returned from the activity.  
   2. 	This line gets a Robot object for the robot identifier which is used to identifier the connected
      Sphero in other API calls. (The API supports connection to multiple Spheros.)  
   3. 	This line calls a method that will be used to blink Sphero's LED.  

### Add code to blink the RGB LED.

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
	
1. This line will send a command to turn off the LED. `mRobot` is the Robot object that will receive the
	command, and last three parameters turn of the red, green, and blue components of the LED. A 0 value 
	for the color component will set the LED components brightness off.  
2. This line will send a command to turn on the blue LED at full brightness. 255 is full brightness, and is
	only set for the blue component of the LED.  
3.  This line creates a Handler that is used to post a delayed call to the `blink()` method after 1 second
	with the lit parameter inverted, so the next call toggles the LED on or off.

### Modify the AndroidManifest.xml file.

Before running the application, you need to add an activity entry for the StartupActivity to the 
AndroidManifest.xml file. For convenience, you can copy this from the RobotUILibrary project's AndroidManifest.xml 
file. Add the following,

		<activity android:name="orbotix.robot.app.StartupActivity"
			android:theme="@android:style/Theme.Translucent"
			android:launchMode="singleTop"/>

- You will also need to add permissions to use bluetooth, 

		    <uses-permission android:name="android.permission.BLUETOOTH" />
		    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

- Add `android:launchMode="singleTask"` to the main activity.


### Run on a device.

 - Run the Application on a supported Android Device.  **Turn Bluetooth ON**.

 - At this point in time you will want to Pair your Android Device to Sphero from within the settings.

### Sendings Roll commands

 - Using Roll Commands to **Move** Sphero.

 - Using Roll Commands to **Stop** Sphero.

	So, you got the LED to blink… that's Awesome! But let's also take advantage of the amazing technology inside Sphero and drive the ball around a little bit. 
	In order to move Sphero you will need to send a RollCommand. The RollCommand takes two parameters.

   1.  Heading in degrees from 0° to 360° 
   2.  Speed from 0.0 to 1.0. 

For example, a heading of 90° at a speed of 0.5 will tell Sphero to turn clockwise 90° at half speed (1.0 is full speed). Once this command is issued Sphero will continue at this heading and speed until it hits something or runs out of range, so you will need to stop the ball using the RollCommand and `sendStop()`.

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

1. This line sends the heading of 0° and the maximum speed of 1.0 to Sphero.
2. This line creates the handle that is used to send the delayed stop command.
3. This line uses the RollCommand to send the stop.

	Next add a call to `drive()` in the `onActivityResult()` below the call to `blink()`.

**Run the application on an Android Device, if all went well Sphero should have moved forward just a little.**



## Where is Sphero going?

If you have successfully completed the quick start guide then Sphero should have moved after running the modified code.  What is interesting to note here is that Sphero just went in a *random* direction.  The direction was not random at all, Sphero believe it or not has a *front* and a *back*.  It is necessary for the application to determine what direction forward is for the user from the point of view of the ball.  We call this step `Calibration` and it is **required** to properly drive Sphero in a predictable direction.  To learn more about calibration and using the `BackLED` to set Sphero's orientation please check out the `UISampler` Sample project.
