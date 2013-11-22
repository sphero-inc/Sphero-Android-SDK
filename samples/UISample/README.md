![logo](http://update.orbotix.com/developer/sphero-small.png)

# UI Sample

## Startup Activity

Mostly all of our Sphero apps use the StartupActivity to connect to a robot. However, this newest release of the SDK introduced the `SpheroConnectionView`.  The UI Sample shows you how to still use the StartupActivity.  
    
  To run the StartupActivity, add the following code to the main activity's `onStart()` method,  
        
    Intent i = new Intent(this, StartupActivity.class);  
    startActivityForResult(i, STARTUP_ACTIVITY);  
       
   This will launch the StartupActivity using the intent, and sets an activity results 
   identifier. The `STARTUP_ACTIVITY` constant is returned in the `onActivityResult()` method 
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

## Color Picker

The ColorPickerActivity presents a color wheel and a brightness bar along with a couple preview windows and an RGB readout. This is a very easy way for a user to change the color of their Sphero™. 

![Color Picker](https://github.com/orbotix/Sphero-Android-SDK/raw/master/samples/UISample/colorPicker.png)


The color changes initiated by the user are then broadcast to provide access to anything registered to receive them. 

To use, you must first create an Intent.

	Intent colorPickerIntent = new Intent(this, ColorPickerActivity.class);
	
To ensure the color picker starts on the correct color, you must pass the RGB values in the intent.

	colorPickerIntent.putExtra(ColorPickerActivity.EXTRA_COLOR_RED, Color.red(mCurrentColor));
	colorPickerIntent.putExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, Color.green(mCurrentColor));
	colorPickerIntent.putExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, Color.blue(mCurrentColor));

To receive the color changes via a `BroadcastIntent`, you must register a broadcast receiver using an `IntentFilter`. In the receiver, you can do what you need with the color.

	private Robot mRobot;

	private BroadcastReceiver mColorChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// update colors
			int red = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR_RED, 0);
			int green = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, 0);
			int blue = intent.getIntExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, 0);
			
			// save the current color
			mCurrentColor = Color.rgb(red, green, blue);
			
			// change the color on the ball
			mRobot.setColor(red, green, blue);
		}
	};

Before you show the color picker, create an `IntentFilter` using the `ACTION_COLOR_CHANGE` from the `ColorPickerActivity`.

	IntentFilter filter = new IntentFilter(ColorPickerActivity.ACTION_COLOR_CHANGE);
	registerReceiver(mColorChangeReceiver, filter);

Make sure the activity is in your `AndroidManifest.xml`. We have provided example activity elements in the RobotUILibrary's manifest for your convenience. You can simply copy and paste the ColorPickerActivity from there, into the manifest for your application.


	<activity android:name="orbotix.robot.app.ColorPickerActivity"
		  android:screenOrientation="landscape"
          android:theme="@android:style/Theme.Translucent"
          android:launchMode="singleTop"/>

Notice the Theme applied here. If you are using the onStop() method to shut down the connection to Sphero and you don't want it called when you just want to go to the color picker, you can show the color picker as a transparent activity. This will only call `onPause()` and `onResume()` on your main activity. Now you can launch the ColorPickerActivity. If you are registering for the color change broadcasts every time you show the color picker, you may want to start the activity for result and unregister the receiver when the activity returns.


	startActivityForResult(colorPickerIntent, REQUEST_CHANGE_COLOR);


## Calibration View

The calibration view can be used to give the user some visual feedback while "aiming" their Sphero. It is meant to be an "all in one" widget combining the rotation gesture detection with the visual representation of calibration while sending the commands to Sphero to actually rotate and calibrate when finished.

![Calibration View](https://github.com/orbotix/Sphero-Android-SDK/raw/master/samples/UISample/calibration_view.png)

To use this widget, simply add it to your layout, set it up when you create your activity, and set the robot you would like to control when it is available.

Typically, the `CalibrationView` should take up the whole screen so that, when visible, it focuses the user's attention on the fact that they are aiming their Sphero. Your layout xml file should contain something similar to the following (near the bottom of the layout to ensure it will be placed above everything else).


	<orbotix.robot.widgets.calibration.CalibrationView android:id="@+id/CalibrationView"
												   android:layout_width="fill_parent"
	                                               android:layout_height="fill_parent"/>

Then, in your Activity's `onCreate(Bundle)` method, configure the view itself.

	setContentView(R.layout.main);
	mCalibrationView = (CalibrationView)findViewById(R.id.CalibrationView);
	mCalibrationView.setColor(Color.WHITE);
	mCalibrationView.setCircleColor(Color.WHITE);
	mCalibrationView.enable();

You can set the colors of the CalibrationView's different pieces to anything you wish. If you need to do something when the calibration view starts and stops, you can give the calibration view runnables to execute at specific times.
	
    mCalibrationView.setOnStartRunnable(new Runnable () {
    	public void run() {
    		map.pauseDrawing();
    		map.clear();
    	}
    });

    mCalibrationView.setOnEndRunnable(new Runnable () {
    	public void run() {
    		map.resumeDrawing();
    	}
    });

Now, to actually get it to control a Sphero, use the `setRobot(Robot)` method of the CalibrationView when the robot is available. (most likely in you `onActivityResult(int, int, Intent)` method when you return from the StartupActivity).

### One-Touch Calibration Button

We first created the two finger touch calibration widget, thinking it would be the easiest way to calibrate Sphero.  After some testing in the field, we discovered it isn't a very intuitive approach.  However, we keep it around, because most Sphero users are used to it already.

We created a new calibration widget button where you press and hold a button and then the widget will pop-up and you can calibrate Sphero by moving one finger.  To add one of these in xml, you must add the view, and a button that brings up the view. 

1) The view

    <orbotix.view.calibration.CalibrationButtonView
        android:id="@+id/calibration_above"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent" />
        
2) The button

    <Button
        android:id="@+id/calibration_button_above"
        style="@style/ControlButton"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"
        android:text="O" />
        
Then, in code you can add the button to the CalibrationButtonView.

        // Initialize calibrate button view where the calibration circle shows above button
        // This is the default behavior
        mCalibrationButtonViewAbove = (CalibrationButtonView)findViewById(R.id.calibration_above);
        mCalibrationButtonViewAbove.setCalibrationButton((View)findViewById(R.id.calibration_button_above));
        // You can also change the size of the calibration views
        mCalibrationButtonViewAbove.setRadius(300);
        mCalibrationButtonViewAbove.setCalibrationCircleLocation(CalibrationCircleLocation.ABOVE);
        
Here you can control the size of the calibration widget circle, and you can also control where the the circle pops-up relative to the button.

## Joystick View

The joystick view is just one of the ways a user can control Sphero. One of the benefits of a joystick over a two lever RC style control is ability to give direction to Sphero relative to the user's orientation. So, if the user remains stationary and facing the same direction, pushing the joystick puck away always makes Sphero travel in the same direction.

The `JoystickView` is also meant to be an "all in one" view. Once setup with a `Robot` object to control, the `JoystickView` takes care of the rest. If you would like more information on how the joystick works or you would like to implement your own joystick using some more advanced techniques, check out our DriveControl explanation.

![SpheroCam Joystick](https://github.com/orbotix/Sphero-Android-SDK/raw/master/samples/UISample/joystick.jpg)

The joystick can be added to your Activity's layout with something similar to the following.

	<com.orbotix.spherocam.ui.joystick.JoystickView xmlns:app="http://schemas.android.com/apk/res/your.package.here"
		android:id="@+id/Joystick"
    	android:layout_width="200dp"
    	android:layout_height="200dp"
    	android:layout_alignParentLeft="true"
    	android:layout_alignParentBottom="true"
    	app:puck_radius="25dp"
    	app:alpha="0.7"/>

You can adjust the size of the entire joystick using the `layout_width` and `layout_height` attributes. To control the size of the joystick's puck, use the `puck_radius` attribute. `alpha` adjusts the opacity of the joystick control (the higher the value, the more opaque the control). Once you connect (typically in the `StartupActivity`) and obtain a Robot object (usually done in the `onActivityResult(int, int, Intent)` method when returning from the StartupActivity), you can use the JoystickView's `setRobot(Robot)` method to activate the joystick and to begin controlling Sphero.

## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)
