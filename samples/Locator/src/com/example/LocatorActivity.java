package com.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.*;
import orbotix.robot.sensor.AccelerometerData;
import orbotix.robot.sensor.AttitudeData;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.robot.sensor.LocatorData;
import orbotix.robot.widgets.calibration.CalibrationView;

import java.util.List;

public class LocatorActivity extends Activity
{
    /**
     * ID for starting the StartupActivity
     */
    private final static int sStartupActivity = 0;
    
    /**
     * Data Streaming Packet Counts
     */
    private final static int TOTAL_PACKET_COUNT = 200;
    private final static int PACKET_COUNT_THRESHOLD = 50;
    private int mPacketCounter;

    /**
     * Robot to from which we are streaming
     */
    private Robot mRobot = null;

    /**
     * AsyncDataListener that will be assigned to the DeviceMessager, listen for streaming data, and then do the
     *
     */
    private DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {

            if(data instanceof DeviceSensorsAsyncData){
            	
            	// If we are getting close to packet limit, request more
            	mPacketCounter++;
            	if( mPacketCounter > (TOTAL_PACKET_COUNT - PACKET_COUNT_THRESHOLD) ) {
            		requestDataStreaming();
            	}

                //get the frames in the response
                List<DeviceSensorsData> data_list = ((DeviceSensorsAsyncData)data).getAsyncData();
                if(data_list != null){

                    // Iterate over each frame, however we set data streaming as only one frame
                    for(DeviceSensorsData datum : data_list){

                        LocatorData locatorData = datum.getLocatorData();
                        if( locatorData != null ) {
                            ((TextView)findViewById(R.id.txt_locator_x)).setText(locatorData.getPositionX() + " cm");
                            ((TextView)findViewById(R.id.txt_locator_y)).setText(locatorData.getPositionY() + " cm");
                            ((TextView)findViewById(R.id.txt_locator_vx)).setText(locatorData.getVelocityX() + " cm/s");
                            ((TextView)findViewById(R.id.txt_locator_vy)).setText(locatorData.getVelocityY() + " cm/s");
                        }
                    }
                }
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Show the StartupActivity to connect to Sphero
        startActivityForResult(new Intent(this, StartupActivity.class), sStartupActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            if(requestCode == sStartupActivity){

                //Get the Robot from the StartupActivity
                String id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);
                mRobot = RobotProvider.getDefaultProvider().findRobot(id);

                // Start streaming Locator values
                requestDataStreaming();
                
                //Set the AsyncDataListener that will process each response.
                DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);

                // Let Calibration View know which robot we are connected to
                CalibrationView calibrationView = (CalibrationView)findViewById(R.id.calibration_widget);
                calibrationView.setRobot(mRobot);
            }
        }
    }

    /**
     * Calibrate Sphero when a two finger event occurs
     */
    public boolean dispatchTouchEvent(MotionEvent event) {
        CalibrationView calibrationView = (CalibrationView)findViewById(R.id.calibration_widget);
        // Notify Calibration widget of a touch event
        calibrationView.interpretMotionEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mRobot != null){
            // Make sure the ball doesn't roll across the world
            RollCommand.sendStop(mRobot);
            // Disconnect properly
            RobotProvider.getDefaultProvider().disconnectControlledRobots();
        }
    }

    private void requestDataStreaming(){

        if(mRobot == null) return;

        // Set up a bitmask containing the sensor information we want to stream, in this case locator
        // with which only works with Firmware 1.20 or greater.
        final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_LOCATOR_ALL;

        //Specify a divisor. The frequency of responses that will be sent is 400hz divided by this divisor.
        final int divisor = 50;

        //Specify the number of frames that will be in each response. You can use a higher number to "save up" responses
        //and send them at once with a lower frequency, but more packets per response.
        final int packet_frames = 1;

        // Reset finite packet counter
        mPacketCounter = 0;
        
        // Count is the number of async data packets Sphero will send you before
        // it stops.  You want to register for a finite count and then send the command
        // again once you approach the limit.  Otherwise data streaming may be left
        // on when your app crashes, putting Sphero in a bad state 
        final int response_count = TOTAL_PACKET_COUNT;


        // Send this command to Sphero to start streaming.  
        // If your Sphero is on Firmware less than 1.20, Locator values will display as 0's
        SetDataStreamingCommand.sendCommand(mRobot, divisor, packet_frames, mask, response_count);
    }

    /**
     * When the user clicks the configure button, it calls this function
     * @param v
     */
    public void configurePressed(View v) {

        if( mRobot == null ) return;

        int newX = 0;   // The locator's current X position value will be set to this value
        int newY = 0;   // The locator's current Y position value will be set to this value
        int newYaw = 0; // The yaw value you set this to, will represent facing down the +y_axis

        // Try parsing the integer values from the edit text boxes, if not, use zeros
        try {
            newX = Integer.parseInt(((EditText)findViewById(R.id.edit_new_x)).getText().toString());
        } catch (NumberFormatException e) {}

        try {
            newY = Integer.parseInt(((EditText)findViewById(R.id.edit_new_y)).getText().toString());
        } catch (NumberFormatException e) {}

        try {
            newYaw = Integer.parseInt(((EditText)findViewById(R.id.edit_new_yaw)).getText().toString());
        } catch (NumberFormatException e) {}

        // Flag will be true if the check box is clicked, false if it is not
        // When the flag is off (default behavior) the x, y locator grid is rotated with the calibration
        // When the flag is on the x, y locator grid is fixed and Sphero simply calibrates within it
        int flag = ((CheckBox)findViewById(R.id.checkbox_flag)).isChecked() ?
                        ConfigureLocatorCommand.ROTATE_WITH_CALIBRATE_FLAG_ON :
                        ConfigureLocatorCommand.ROTATE_WITH_CALIBRATE_FLAG_OFF;

        ConfigureLocatorCommand.sendCommand(mRobot, flag, newX, newY, newYaw);
    }

    public void upPressed(View v) {
        RollCommand.sendCommand(mRobot, 0, 0.6f);
    }

    public void rightPressed(View v) {
        RollCommand.sendCommand(mRobot, 90, 0.6f);
    }

    public void downPressed(View v) {
        RollCommand.sendCommand(mRobot, 180, 0.6f);
    }

    public void leftPressed(View v) {
        RollCommand.sendCommand(mRobot, 270, 0.6f);
    }

    public void stopPressed(View v) {
        RollCommand.sendStop(mRobot);
    }
}
