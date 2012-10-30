package orbotix.uisample;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.widgets.ControllerActivity;
import orbotix.robot.widgets.joystick.JoystickView;
import orbotix.robot.app.ColorPickerActivity;
import orbotix.view.calibration.CalibrationButtonView;
import orbotix.view.calibration.CalibrationButtonView.CalibrationCircleLocation;
import orbotix.view.calibration.CalibrationView;

public class UiSampleActivity extends ControllerActivity
{
    /**
     * ID to start the StartupActivity for result to connect the Robot
     */
    private final static int STARTUP_ACTIVITY      = 0;

    /**
     * ID to start the ColorPickerActivity for result to select a color
     */
    private final static int COLOR_PICKER_ACTIVITY = 1;

    /**
     * The Robot to control
     */
    private Robot mRobot;
    
    private CalibrationButtonView mCalibrationButtonViewAbove;
    private CalibrationButtonView mCalibrationButtonViewBelow;
    private CalibrationButtonView mCalibrationButtonViewRight;
    private CalibrationButtonView mCalibrationButtonViewLeft;
    
    private CalibrationView mCalibrationTwoFingerView;
    
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

        // Add the two finger calibration method
        mCalibrationTwoFingerView = (CalibrationView)findViewById(R.id.calibration_two_finger);
        
        // Initialize calibrate button view where the calibration circle shows above button
        // This is the default behavior
        mCalibrationButtonViewAbove = (CalibrationButtonView)findViewById(R.id.calibration_above);
        mCalibrationButtonViewAbove.setCalibrationButton((View)findViewById(R.id.calibration_button_above));
        // You can also change the size of the calibration views
        mCalibrationButtonViewAbove.setRadius(300);
        
        // Initialize calibrate button view where the calibration circle shows below button
        mCalibrationButtonViewBelow = (CalibrationButtonView)findViewById(R.id.calibration_below);
        mCalibrationButtonViewBelow.setCalibrationButton((View)findViewById(R.id.calibration_button_below));
        mCalibrationButtonViewBelow.setCalibrationCircleLocation(CalibrationCircleLocation.BELOW);
        
        // Initialize calibrate button view where the calibration circle shows to the right of the button
        mCalibrationButtonViewRight = (CalibrationButtonView)findViewById(R.id.calibration_right);
        mCalibrationButtonViewRight.setCalibrationButton((View)findViewById(R.id.calibration_button_right));
        mCalibrationButtonViewRight.setCalibrationCircleLocation(CalibrationCircleLocation.RIGHT);
        
        // Initialize calibrate button view where the calibration circle shows to the left of the button
        mCalibrationButtonViewLeft = (CalibrationButtonView)findViewById(R.id.calibration_left);
        mCalibrationButtonViewLeft.setCalibrationButton((View)findViewById(R.id.calibration_button_left));
        mCalibrationButtonViewLeft.setCalibrationCircleLocation(CalibrationCircleLocation.LEFT);
    }

    /**
     * On start, start the StartupActivity to connect to the Robot
     */
    @Override
    protected void onStart() {
        super.onStart();
        
        //Start StartupActivity to connect to Robot
        Intent i = new Intent(this, StartupActivity.class);
        startActivityForResult(i, STARTUP_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(resultCode == RESULT_OK){
            if(requestCode == STARTUP_ACTIVITY){

                //Get the connected Robot
                final String robot_id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);
                mRobot = RobotProvider.getDefaultProvider().findRobot(robot_id);

                //Set connected Robot to the Controllers
                setRobot(mRobot);
                
                // Make sure you let the calibration views know the robot it should control
                mCalibrationButtonViewAbove.setRobot(mRobot);
                mCalibrationButtonViewBelow.setRobot(mRobot);
                mCalibrationButtonViewRight.setRobot(mRobot);
                mCalibrationButtonViewLeft.setRobot(mRobot);
                mCalibrationTwoFingerView.setRobot(mRobot);
                
            }else if(requestCode == COLOR_PICKER_ACTIVITY){
                
                if(mRobot != null){
                    //Get the colors
                    mRed   = data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_RED, 0xff);
                    mGreen = data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, 0xff);
                    mBlue  = data.getIntExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, 0xff);

                    //Set the color
                    RGBLEDOutputCommand.sendCommand(mRobot, mRed, mGreen, mBlue);
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Disconnect Robots on stop
        if(mRobot != null){
            RobotProvider.getDefaultProvider().disconnectControlledRobots();
        }
    }

    /**
     * When the user clicks the "Color" button, show the ColorPickerActivity
     * @param v The Button clicked
     */
    public void onColorClick(View v){

        Intent i = new Intent(this, ColorPickerActivity.class);

        //Tell the ColorPickerActivity which color to have the cursor on.
        i.putExtra(ColorPickerActivity.EXTRA_COLOR_RED, mRed);
        i.putExtra(ColorPickerActivity.EXTRA_COLOR_GREEN, mGreen);
        i.putExtra(ColorPickerActivity.EXTRA_COLOR_BLUE, mBlue);

        startActivityForResult(i, COLOR_PICKER_ACTIVITY);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
    	mCalibrationButtonViewBelow.interpretMotionEvent(event);
    	mCalibrationButtonViewAbove.interpretMotionEvent(event);
    	mCalibrationButtonViewRight.interpretMotionEvent(event);
    	mCalibrationButtonViewLeft.interpretMotionEvent(event);
    	mCalibrationTwoFingerView.interpretMotionEvent(event);
    	return super.dispatchTouchEvent(event);
    }
}
