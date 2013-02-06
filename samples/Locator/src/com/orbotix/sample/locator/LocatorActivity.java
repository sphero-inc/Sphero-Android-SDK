package com.orbotix.sample.locator;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import orbotix.robot.base.*;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.robot.sensor.LocatorData;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;
import java.util.List;

public class LocatorActivity extends Activity
{   
    /**
     * Robot to from which we are streaming
     */
    private Robot mRobot = null;
    
    /**
     * The Sphero Connection View
     */
    private SpheroConnectionView mSpheroConnectionView;
    
    private Handler mHandler = new Handler();

    /**
     * AsyncDataListener that will be assigned to the DeviceMessager, listen for streaming data, and then do the
     *
     */
    private DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {

            if(data instanceof DeviceSensorsAsyncData){
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
        findViewById(R.id.back_layout).requestFocus();

        mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
        // Set the connection event listener 
        mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
        	// If the user clicked a Sphero and it failed to connect, this event will be fired
			@Override
			public void onRobotConnectionFailed(Robot robot) {}
			// If there are no Spheros paired to this device, this event will be fired
			@Override
			public void onNonePaired() {}
			// The user clicked a Sphero and it successfully paired.
			@Override
			public void onRobotConnected(Robot robot) {
				mRobot = robot;
				// Skip this next step if you want the user to be able to connect multiple Spheros
				mSpheroConnectionView.setVisibility(View.GONE);
				
				// This delay post is to give the connection time to be created
				mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Start streaming Locator values
                        requestDataStreaming();
                        
                        //Set the AsyncDataListener that will process each response.
                        DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener); 
                    }
                }, 1000);
			}
			
			@Override
			public void onBluetoothNotEnabled() {
				// See UISample Sample on how to show BT settings screen, for now just notify user
				Toast.makeText(LocatorActivity.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
    }

    /**
     * Called when the user comes back to this app
     */
    @Override
    protected void onResume() {
    	super.onResume();
        // Refresh list of Spheros
        mSpheroConnectionView.showSpheros();
    }
    
    /**
     * Called when the user presses the back or home button
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	//Set the AsyncDataListener that will process each response.
        DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mDataListener); 
    	// Disconnect Robot properly
    	RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    private void requestDataStreaming(){

        if(mRobot == null) return;

        // Set up a bitmask containing the sensor information we want to stream, in this case locator
        // with which only works with Firmware 1.20 or greater.
        final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_LOCATOR_ALL;

        //Specify a divisor. The frequency of responses that will be sent is 400hz divided by this divisor.
        final int divisor = 1;

        //Specify the number of frames that will be in each response. You can use a higher number to "save up" responses
        //and send them at once with a lower frequency, but more packets per response.
        final int packet_frames = 20;

        // Count is the number of async data packets Sphero will send you before
        // it stops.  Use a count of 0 for infinite data streaming.
        final int response_count = 0;

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
