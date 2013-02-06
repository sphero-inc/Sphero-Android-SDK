package orbotix.uisample;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import orbotix.robot.app.ColorPickerActivity;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RobotProvider.OnRobotDisconnectedListener;
import orbotix.robot.base.SleepCommand;
import orbotix.robot.widgets.CalibrationImageButtonView;
import orbotix.robot.widgets.NoSpheroConnectedView;
import orbotix.robot.widgets.NoSpheroConnectedView.OnConnectButtonClickListener;
import orbotix.robot.widgets.SlideToSleepView;
import orbotix.robot.widgets.joystick.JoystickView;
import orbotix.view.calibration.CalibrationView;
import orbotix.view.calibration.widgets.ControllerActivity;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;

public class UiSampleActivity extends ControllerActivity
{
    /**
     * ID to start the StartupActivity for result to connect the Robot
     */
    private final static int  STARTUP_ACTIVITY            = 0;
	private static final int  BLUETOOTH_ENABLE_REQUEST = 11;
	private static final int  BLUETOOTH_SETTINGS_REQUEST = 12;

    /**
     * ID to start the ColorPickerActivity for result to select a color
     */
    private final static int COLOR_PICKER_ACTIVITY = 1;
    private boolean mColorPickerShowing = false;

    /**
     * The Robot to control
     */
    private Robot mRobot;
    
    /**
     * One-Touch Calibration Button
     */
    private CalibrationImageButtonView mCalibrationImageButtonView;
    
    /**
     * Calibration View widget
     */
    private CalibrationView mCalibrationView;
    
    /**
     * Slide to sleep view
     */
    private SlideToSleepView mSlideToSleepView;
    
    /**
     * No Sphero Connected Pop-Up View
     */
    private NoSpheroConnectedView mNoSpheroConnectedView;
    
    /**
     * Sphero Connection View
     */
    private SpheroConnectionView mSpheroConnectionView;
    
    //Colors
    private int mRed   = 0xff;
    private int mGreen = 0xff;
    private int mBlue  = 0xff;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Add the JoystickView as a Controller
        addController((JoystickView)findViewById(R.id.joystick));

        // Add the calibration view
        mCalibrationView = (CalibrationView)findViewById(R.id.calibration_view);
        
        // Set up sleep view
        mSlideToSleepView = (SlideToSleepView)findViewById(R.id.slide_to_sleep_view);
        mSlideToSleepView.hide();
        // Send ball to sleep after completed widget movement
        mSlideToSleepView.setOnSleepListener(new SlideToSleepView.OnSleepListener() {
			@Override
			public void onSleep() {
				SleepCommand.sendCommand(mRobot, 0, 0);
			}
		});
        
        // Initialize calibrate button view where the calibration circle shows above button
        // This is the default behavior
        mCalibrationImageButtonView = (CalibrationImageButtonView)findViewById(R.id.calibration_image_button);
        mCalibrationImageButtonView.setCalibrationView(mCalibrationView);
        // You can also change the size and location of the calibration views (or you can set it in XML)
        mCalibrationImageButtonView.setRadius(100);
        mCalibrationImageButtonView.setOrientation(CalibrationView.CalibrationCircleLocation.ABOVE);
        
        
        // Grab the No Sphero Connected View
        mNoSpheroConnectedView = (NoSpheroConnectedView)findViewById(R.id.no_sphero_connected_view);
        mNoSpheroConnectedView.setOnConnectButtonClickListener(new OnConnectButtonClickListener() {

			@Override
			public void onConnectClick() {
				mSpheroConnectionView.setVisibility(View.VISIBLE);
				mSpheroConnectionView.showSpheros();
			}

			@Override
			public void onSettingsClick() {
				// Open the Bluetooth Settings Intent
				Intent settingsIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
				UiSampleActivity.this.startActivityForResult(settingsIntent, BLUETOOTH_SETTINGS_REQUEST);
			}
		});
        
        // Setup on robot disconnect to show no sphero connected view
        // Register to be notified when Sphero disconnects (out of range, battery dead, sleep, etc.)
        RobotProvider.getDefaultProvider().setOnRobotDisconnectedListener(new OnRobotDisconnectedListener() {
			@Override
			public void onRobotDisconnected(Robot robot) {
				mNoSpheroConnectedView.setVisibility(View.VISIBLE);
			}
		});
        
        
        // Set up the Sphero Connection View
        mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
        mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
			
			@Override
			public void onRobotConnectionFailed(Robot arg0) {}
			
			@Override
			public void onRobotConnected(Robot arg0) {
				// Set Robot
				mRobot = arg0;
                //Set connected Robot to the Controllers
                setRobot(mRobot);
                
                // Make sure you let the calibration view knows the robot it should control
                mCalibrationView.setRobot(mRobot);
                
                // Make connect sphero pop-up invisible if it was previously up
                mNoSpheroConnectedView.setVisibility(View.GONE);
                mNoSpheroConnectedView.switchToConnectButton();
                // Hide Connection View since we only want to connect to one robot
                mSpheroConnectionView.setVisibility(View.GONE);
			}
			
			@Override
			public void onNonePaired() {
				mSpheroConnectionView.setVisibility(View.GONE);
				mNoSpheroConnectedView.switchToSettingsButton();
				mNoSpheroConnectedView.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onBluetoothNotEnabled() {
				// Bluetooth isn't enabled, so we show activity to enable bluetooth in settings
	            Intent i = RobotProvider.getDefaultProvider().getAdapterIntent();
	            startActivityForResult(i, BLUETOOTH_ENABLE_REQUEST);
			}
		});
    }
    
    /**
     * Called when the user comes back to this app
     */
    @Override
    protected void onResume() {
    	super.onResume();
    	if( mColorPickerShowing ) {
    		mColorPickerShowing = false;
    		return;
    	}
        // Refresh list of Spheros
        mSpheroConnectionView.showSpheros();
    }
    
    /**
     * Called when the user presses the back or home button
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	if( mColorPickerShowing ) return;
    	// Disconnect Robot properly
    	RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == COLOR_PICKER_ACTIVITY){
                if(mRobot != null){
                    //Get the colors
                    mRed   = data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_RED, 0xff);
                    mGreen = data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, 0xff);
                    mBlue  = data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, 0xff);

                    //Set the color
                    RGBLEDOutputCommand.sendCommand(mRobot, mRed, mGreen, mBlue);
                }
            }
            else if( requestCode == BLUETOOTH_ENABLE_REQUEST ) {
            	// User enabled bluetooth, so refresh Sphero list
            	mSpheroConnectionView.setVisibility(View.VISIBLE);
            	mSpheroConnectionView.showSpheros();
            }
        }
        else {
        	if(requestCode == STARTUP_ACTIVITY){   
        		// Failed to return any robot, so we bring up the no robot connected view
        		mNoSpheroConnectedView.setVisibility(View.VISIBLE);
        	}
        	else if( requestCode == BLUETOOTH_ENABLE_REQUEST ) {
        		
        		// User clicked "NO" on bluetooth enable settings screen
                Toast.makeText(UiSampleActivity.this, 
                		"Enable Bluetooth to Connect to Sphero", Toast.LENGTH_LONG).show();
        	}
        	else if( requestCode == BLUETOOTH_SETTINGS_REQUEST ) {
        		// User enabled bluetooth, so refresh Sphero list
            	mSpheroConnectionView.setVisibility(View.VISIBLE);
            	mSpheroConnectionView.showSpheros();
        	}
        }
    }

    /**
     * When the user clicks the "Color" button, show the ColorPickerActivity
     * @param v The Button clicked
     */
    public void onColorClick(View v){

    	mColorPickerShowing = true;
        Intent i = new Intent(this, ColorPickerActivity.class);

        //Tell the ColorPickerActivity which color to have the cursor on.
        i.putExtra(ColorPickerActivity.EXTRA_COLOR_RED, mRed);
        i.putExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, mGreen);
        i.putExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, mBlue);

        startActivityForResult(i, COLOR_PICKER_ACTIVITY);
    }
    
    /**
     * When the user clicks the "Sleep" button, show the SlideToSleepView shows
     * @param v The Button clicked
     */
    public void onSleepClick(View v){
    	mSlideToSleepView.show();
    }
    
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
    	mCalibrationView.interpretMotionEvent(event);
    	mSlideToSleepView.interpretMotionEvent(event);
    	return super.dispatchTouchEvent(event);
    }
}
