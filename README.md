![android.jpg](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/image001.jpg)

# Android Developer Quick Start Guide

## Overview
 
This Guide walks you through the basics of creating mobile apps for Android that leverage the Orbotix Sphero SDK. The examples in this guide were built using Java and although we strive to support **ALL** the various Android devices available, but there are a few that are known to cause problems. Visit our developer forum for more information. The goal of this developer guide along with sample code is to give the developer a taste of the wide variety of things Sphero can do, respond to, and keep track of.

*In general this guide will walk you through:*

 - [Installing the SDK](https://github.com/orbotix/Sphero-Android-SDK#installing-the-sphero-android-sdk)
 - [Changing Sphero's Color](https://github.com/orbotix/Sphero-Android-SDK#add-code-to-blink-the-rgb-led)
 - [Using the Roll Command to move Sphero](https://github.com/orbotix/Sphero-Android-SDK#sendings-roll-commands) 

### Before we begin - Installing Eclipse

    Notice: The Sphero Android SDK works with Android 2.2+ and Java Compiler Level 6.0(1.6)+

Before you begin to develop applications that interface with Sphero on Android, you will need to install the Android developer tools. We often use Eclipse but there are many other well written Android IDEs out there and these same basic steps are most often still applicable. 

 - Install the [Android SDK](http://developer.android.com/sdk/index.html) and the [Eclipse Plugin](http://developer.android.com/sdk/eclipse-adt.html)

## Installing the Sphero Android SDK

 - Download the latest version of the [Sphero Android SDK](https://github.com/orbotix/Sphero-Android-SDK/zipball/master)
     * *You can always keep up to date by watching our [GitHub Repository](https://github.com/orbotix/Sphero-Android-SDK)*
     
## Available Samples  

The Sphero Android SDK has over 15 different sample projects.  Starting with these is the best way to get started programming Sphero!

1.  [**HelloWorld**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/HelloWorld) - How to connect to a Sphero and blink it's RGB LED blue.
2.  [**ButtonDrive**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/ButtonDrive) - Drive a Sphero by sending roll commands at 0°, 90°, 180°, and 270°.
3.  [**Collisions**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/Collisions) - Set up collision detection, a firmware feature that sends an asyncronous message when an impact is detected.
4.  [**AcheivementSample**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/AchievementSample) - Add acheivements to the SpheroWorld back end, and track them from an application.
5.  [**Locator**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/Locator) - How to use the Sphero Locator, a firmware feature that provides real-time position and velocity information about Sphero.
6.  [**MacroSample**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/MacroSample) - How to connect to multiple balls, programmatically create macros, and run them on multiple Spheros.
7.  [**MacroLoader**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/MacroLoader) - Load macros created in MacroLab and run them on the ball.
8.  [**MultiplayerLobby**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/MultiplayerLobby) - Connect two phones over a wifi network for the use of multiplayer games.  This is soon to be deprecated in favor of better services like http://playphone.com/.  
9.  [**OptionFlags**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/OptionFlags) - Control option flags, which are settings that persist through Sphero's power cycles.
10. [**OrbBasicSample**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/OrbBasicSample) - This sample demonstrates how to load and execute OrbBasic programs on Sphero.
11. [**SelfLevel**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/SelfLevel) - Connect to Sphero and perform the self level command.
12. [**StreamingExample**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/StreamingExample) - How to use Sphero's ability to asynchronously stream data from certain control systems and sensors. This allows you to use Sphero as a controller to games. 
13. [**StreamingAnimation**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/StreamingAnimation) - Move and rotate a 2D object on the screen using Sphero's data streaming.
14. [**SpheroMotionTeapot**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/SphereMotionTeapot) - Move and rotate a 3D object on the screen using Sphero's data streaming.
15. [**UISample**](https://github.com/orbotix/Sphero-Android-SDK/tree/master/samples/UISample) - This is a great resource for application development.  It contains a pre-made drive joystick, a calibration widget, a color changing widget, and a sleep widget.  
     
## Importing a Sphero Sample Project

To import a sample into Eclipse, right-click in the project explorer, or click the File menu and select "**Import…**" 

   ![QSG-libs.png](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/image004.png)
   
Select the 'Existing Project into Workspace' option under the 'General' tab.  Then browse to the folder that holds the HelloWorld Sample.  It will be in the directory where you downloaded the Sphero SDK.

   ![QSG-libs.png](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/image005.png)

At this point there should be no "red X's" or "red !'s" next to the smaple project you just imported. If there aren't any, you are now ready to run it on an Android device. Keep in mind that **Sphero projects cannot run inside of the emulator and will fail compilation**. If you have problems try these fixes.

1. Right click the project, and go to Properties.  Under the **Android** tab on the left, the check box next to Android 2.2 (or above) should be checked.  If you don't see any Android options, you need to download the Eclipse ADT plugin.![QSG-libs.png](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/image006.png)

2. Right click the project, and go to Properties.  Under the **Java Compiler** tab on the left, the compiler level should be 1.6 or above. 

3. If the problem still persists, try a "Project -> Clean" in the file menu.  60% of the time, this works all the time.

## Create a New Android Project in Eclipse With Sphero or Integrating Sphero Into an Existing Project  

If you are creating a new project it is important to take special notice to the Android API Level and the Java compliance level. The Sphero SDK currently supports: 
    
 - Android API level 8 (Android 2.2) or greater. 
 - Java language compliance level 6.0(1.6) or greater.
    
### Integrating the Sphero Libraries Into Your Project

You can start a new Sphero project using the libraries in the library folder or start a project using one of the sample projects from the samples folder. This quick start guide describes how to start a new project. 
 
 To start, create a new Android project in your Eclipse workspace. Then, place the libs folder from the SDK's library folder into your Android project's folder.  
   
   ![QSG-libs.png](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/image002.png)
   

### Setting the dependency to RobotLibrary.jar  
   
Eclipse should automatically add RobotLibrary.jar to the Android Dependencies folder.  But, if it does not, set the dependency in the project's properties in the Properties->Java Build Path-> Libraries dialog.  This will allow your project access to all the public method names in RobotLibrary.jar. 
 
 ![QSG-jar-depend.png](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/image003.png) 

## Using the Sphero Android SDK
     
### Add code to connect to a sphero  
    
 The RobotLibrary includes a view called `SpheroConnectionView` which will 
 handle connecting to a Sphero. When the view sends an `onRobotConnected` event you are ready to 
 send commands.  
    
  - To use the `SpheroConnectionView` add the following code to your Activity's xml layout file
 
        <LinearLayout
            xmlns:android="http://schemas.android.com/apk/res/android"
            android:layout_width="fill_parent"
            android:layout_height="fill_parent"
            android:background="#ff888888" >

            <orbotix.view.connection.SpheroConnectionView
                android:id="@+id/sphero_connection_view"
                android:layout_width="fill_parent"
                android:layout_height="fill_parent"
                android:background="#FFF" />
                
        </LinearLayout>
       
  - This will show the `SpheroConnectionView` when the Activity starts.  It is important to put the view last in a frame layout, so when you hide the rest of your layout will be visible.  Also, you must create the listener for the `SpheroConnectionView`.  This will fire events to let you know the user's interaction with the `SpheroConnectionView` and then you can do what you please.  This code snippet shows how to create the listener and how to hide the connection view when a Sphero is connected.  
  
	     // Find Sphero Connection View from layout file
        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);

        // This event listener will notify you when these events occur, it is up to you what you want to do during them
        ConnectionListener mConnectionListener = new ConnectionListener() {
            @Override
            // The method to run when a Sphero is connected
            public void onConnected(Robot sphero) {
            	// Hides the Sphero Connection View
                mSpheroConnectionView.setVisibility(View.INVISIBLE);
				// Cache the Sphero so we can send commands to it later
                mSphero = (Sphero) sphero;
                // You can add commands to set up the ball here, these are some examples
                
                // Set the back LED brightness to full
                mSphero.setBackLEDBrightness(1.0f);
                // Set the main LED color to blue at full brightness
                mSphero.setColor(0, 0, 255);
                
                // End examples
            }
			
			// The method to run when a connection fails
            @Override
            public void onConnectionFailed(Robot sphero) {
                // let the SpheroConnectionView handle or hide it and do something here...
            }

			// Ran when a Sphero connection drops, such as when the battery runs out or Sphero sleeps
            @Override
            public void onDisconnected(Robot sphero) {
            	// Starts looking for robots
                mSpheroConnectionView.startDiscovery();
            }
        };
        // Add the listener to the Sphero Connection View
        mSpheroConnectionView.addConnectionListener(mConnectionListener);
	
  - These events are useful feedback from the user.  For example, you could use the `onConnectionFailed(Robot sphero)` method to prompt the user to check that the Sphero is eligible for connection then retrying the connection.
  
  - You must also prepare the bluetooth adapter on each start of the app, so that the app is aware of Sphero's nearby so that it can display them and connect to them. It is best practice to do this inside of the `onResume()` method.
  
	    @Override
	    protected void onResume() {
	    	// Required by android, this line must come first
	        super.onResume();
	        // This line starts the discovery process which finds Sphero's which can be connected to
	        mSpheroConnectionView.startDiscovery();
	    }
	  
  - You must ensure that the robot is cleaned up properly by ensuring discovery is cancelled, and disconnecting the robot. This is best done in the `onPause()` method in your activity. **Do not forget to stop discovery as this consumes a lot of resources on the device!**
	    
	    @Override
	    protected void onPause() {
	        super.onPause();
	        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
	        if (mSphero != null) {
	            mSphero.disconnect(); // Disconnect Robot properly
	        }
	    } 

### Add code to blink the RGB LED.

Now it is time to add code that sends a command to Sphero. In this case we will blink the 
RGB LED blue. As opposed to previous versions of the SDK, commands are now sent via the Sphero object that you cached in the previous step. Commands are now sent using the dot method notation, as all objects in Java. Here is the code for the `blink()` method sends the SetRGBLEDCommand to blink LED.

		private void blink(final boolean lit){
		    
		    if(mSphero != null){
		        
		        //If not lit, send command to show blue light, or else, send command to show no light
		        if(lit){
		        	mSphero.setColor(0, 0, 0);                               // 1
		        }else{
		        	mSphero.setColor(0, 0, 255);                             // 2
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
	
1. This line will send a command to turn off the LED. `mSphero` is the Robot object that will receive the
	command, and last three parameters turn of the red, green, and blue components of the LED. A 0 value 
	for the color component will set the LED components brightness off.  
2. This line will send a command to turn on the blue LED at full brightness. 255 is full brightness, and is
	only set for the blue component of the LED.  
3.  This line creates a Handler that is used to post a delayed call to the `blink()` method after 1 second
	with the lit parameter inverted, so the next call toggles the LED on or off.

### Modify the AndroidManifest.xml file.

Before running the application, you will need to add permissions to use bluetooth, 

		    <uses-permission android:name="android.permission.BLUETOOTH" />
		    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

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
	    		mSphero.drive(0.0f, 1.0f);                                           // 1
	    		
	    		// Send a delayed message on a handler
	    		final Handler handler = new Handler();                               // 2
	    		handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// Send a stop to Sphero
						mSphero.stop()                                               // 3
					}
				}, 2000);
	    		
	    	}
	    }

1. This line sends the heading of 0° and the maximum speed of 1.0 to Sphero.
2. This line creates the handle that is used to send the delayed stop command.
3. This line tells the ball to stop

	Next add a call to `drive()` in the `onActivityResult()` below the call to `blink()`.

**Run the application on an Android Device, if all went well Sphero should have moved forward just a little.**



## Where is Sphero going?

If you have successfully completed the quick start guide then Sphero should have moved after running the modified code.  What is interesting to note here is that Sphero just went in a *random* direction.  The direction was not random at all, Sphero believe it or not has a *front* and a *back*.  It is necessary for the application to determine what direction forward is for the user from the point of view of the ball.  We call this step `Calibration` and it is **required** to properly drive Sphero in a predictable direction.  To learn more about calibration and using the `BackLED` to set Sphero's orientation please check out the `UISampler` Sample project.

## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

