![logo](http://update.orbotix.com/developer/sphero-small.png)

# LocatorSphero Locator is a firmware feature that provides real-time position and velocity information about the robot.  After setting up locator data streaming, you will receive async data packets containing Sphero's X,Y position, and X,Y velocity in cm and cm/s respectively.  
In this document, we show how to configure the locator and use it's information on Android.	Note: This command only works on Sphero's with Firmware 1.20 or greater'
	
## Preparing the Data Streaming Listener
To start, you must make a listener that can handle the async packets received from the ball. To do this, you make a *LocatorListener* to handle the information. This internal method `onLocatorChanged(LocatorData locatorData)` will be called every time the ball's locator is updated.	private LocatorListener mLocatorListener = new LocatorListener() {
        @Override
        public void onLocatorChanged(LocatorData locatorData) {
        	// Do stuff with the locator data	
        }
    }
    
## Attaching the Data Streaming Listener

Assuming you already have a Sphero object that is connected, attaching the listener is done with this line.

	mRobot.getSensorControl().addLocatorListener(mLocatorListener);
	
## Configuring the Data Streaming Listener

Now that the listener is connected, you must set the data streaming rate (at the least). The maximum sensor sampling rate is ~420 Hz, but we recommend using a value in the 20-40Hz range for Android devices. At 20 Hz, virtually every device will not see a slowdown from the packet detection. However, 40 Hz is only viable when targeting only high-end devices. To set the streaming value, use the `setRate(int hz)` member method of the *SensorControl* class of the Sphero.

	mRobot.getSensorControl().setRate(20 /*Hz*/);
	
Now you're set to receive locator data from the Sphero!
## Interpreting Locator DataThe locator treats the ground as a 2D plane and provides Sphero’s position in X,Y coordinates.  By default, roll heading 0, points down the positive Y-axis with positive X-axis to the right.  So, if you shake Sphero awake and send a roll command with heading 0, you will see the Sphero’s Y coordinate counting up.  If you then send a roll command with heading 90, Sphero’s X coordinate will count up.
There are two API commands that affect the locator coordinate system.  Most obviously, the Configure Locator command allows you to set the position of Sphero and rotate the locator coordinate system with respect to roll headings.  For instance, if you would prefer that heading 0 corresponds to the positive X-axis (instead of Y) you could achieve this with the Configure Locator command by setting the yaw tare to 90.  This is achieved by calling this command in code:	ConfigureLocatorCommand.sendCommand(mRobot, flag, newX, newY, newYaw);
The parameters are as follows:
1. **flag**: Determines whether calibrate commands automatically correct the yaw tare value. When 0, the positive Y axis coincides with heading 0 (assuming you do not change the yaw tare manually using this API command). When 1, a roll heading 0 will always increment Y, no matter if you calibrate or not.
2. **newX**: Set the current X coordinates of Sphero on the ground plane in centimeters.
3. **newY**: Set the current Y coordinates of Sphero on the ground plane in centimeters.
4. **newYaw**: Controls how the X,Y-plane is aligned with Sphero’s heading coordinate system. When this parameter is set to zero, it means that having yaw = 0 corresponds to facing down the Y- axis in the positive direction. The value will be interpreted in the range 0-359 inclusive.The Set Heading (Calibrate) command also affects locator coordinates in a different way.  Assume no Configure Locator commands are sent.  Then heading 0 corresponds to the positive Y-axis.  The Set Heading command changes the meaning of “heading 0”.  There are two options:1. The locator Y-axis continues to correspond to heading 0 but points in a different real-world direction. (yaw tare is unchanged, see the discussion of yaw tare in Appendix A)2. The locator Y-axis continues to point in the same real-world direction but corresponds to a different heading angle. (yaw tare is modified).
Depending on your particular needs, one of these behaviors may be more useful/intuitive.  We think of the first option as “no correction” and the second as “auto-correction”.  You can turn this feature on and off with the configure Locator command.  If the first flag bit is set to true auto-correction is turned on.  This is the default configuration.
## Making the Most of the Locator
Depending on your needs there are a few ways to set up and use the Locator.1. Do you need to know position (go to 2) or just distance traveled (go to 3)?2. Do you want to use roll commands to move Sphero to specific locator positions (go to 4) or does it suffice to simply know where Sphero is (go to 5)?3. Do you want to know an arc length/odometer distance (go to 6) or do you want to the distance “as the crow flies” (go to 5)?4. See The Full Setup.5. See The Default Setup.6. See Distance Traveled.## The Default SetupSign up for locator position streaming.  Leave the locator in its default configuration with flag 0 set to true.  The application may use the standard joystick and calibration UI elements freely.## Distance TraveledSign up for locator velocity streaming.  Leave the locator in its default configuration with flag 0 set to true.  The application may use the standard joystick and calibration UI elements freely.  If you want to keep track of distance in the odometer sense, when a data streaming callback comes in accumulate a total using the following formula:
	Distance Traveled += √(Vx^2 + Vy^2 ) * dt
Where `Vx` and `Vy` are the x and y velocity components and `dt` is the time between data samples.  Rather than actually measuring dt (and fighting with Bluetooth latency to get a good timestamp) it is better to synthesize it using the formula:
	dt = N/420Where N is the divisor used in the Set Locator Streaming command.## The Full Setup
The easiest way to use locator coordinates and roll command headings together in your app is to turn auto-correction off (set the first flag bit to false) and avoid the Set Heading command altogether.  You may freely use the Configure Locator command to set the location of Sphero as long as you always set yaw tare to zero.  Again, do not use the Set Heading command!
Your app may freely use the standard joystick UI element.  Unfortunately, the calibration UI element uses the Set Heading command and so should be avoided.  To achieve the same effect one can use Roll commands with a speed of zero to rotate the ball.  Of course, this does not change the meaning of yaw angles the same way Set Heading does.  In order to simulate this effect, you must maintain a private yaw offset, which you set when reorienting the ball.  Your joystick (or other control mechanism) must be modified to add this yaw offset to every outgoing roll command.In this way, the user gets all the functionality of the set heading command without altering the locator coordinate system or the IMU headings.  If you want to drive from one location toward another the following formula gives the heading you should use:
	∅(heading) = (π/2) - atan2( Ygoto – Ysphero,Xgoto - Xphero )
This angle is in radians.  In order to use it in a roll command you must convert it to integer degrees in the range [0,359].
## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

	  