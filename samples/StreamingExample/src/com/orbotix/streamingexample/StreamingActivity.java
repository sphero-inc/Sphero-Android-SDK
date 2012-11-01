package com.orbotix.streamingexample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import orbotix.robot.base.*;
import orbotix.robot.sensor.AccelerometerData;
import orbotix.robot.sensor.AttitudeData;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;
import java.util.List;

public class StreamingActivity extends Activity
{
    /**
     * Sphero Connection Activity
     */
    private SpheroConnectionView mSpheroConnectionView;
    private Handler mHandler = new Handler();

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

    //The views that will show the streaming data
    private ImuView mImuView;
    private CoordinateView mAccelerometerFilteredView;

    /**
     * AsyncDataListener that will be assigned to the DeviceMessager, listen for streaming data, and then do the
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

                    //Iterate over each frame
                    for(DeviceSensorsData datum : data_list){

                        //Show attitude data
                        AttitudeData attitude = datum.getAttitudeData();
                        if(attitude != null){
                            mImuView.setPitch("" + attitude.getAttitudeSensor().pitch);
                            mImuView.setRoll("" + attitude.getAttitudeSensor().roll);
                            mImuView.setYaw("" + attitude.getAttitudeSensor().yaw);
                        }

                        //Show accelerometer data
                        AccelerometerData accel = datum.getAccelerometerData();
                        if(attitude != null){
                            mAccelerometerFilteredView.setX(""+accel.getFilteredAcceleration().x);
                            mAccelerometerFilteredView.setY("" + accel.getFilteredAcceleration().y);
                            mAccelerometerFilteredView.setZ("" + accel.getFilteredAcceleration().z);
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

        //Get important views
        mImuView = (ImuView)findViewById(R.id.imu_values);
        mAccelerometerFilteredView = (CoordinateView)findViewById(R.id.accelerometer_filtered_coordinates);

		// Find Sphero Connection View from layout file
		mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
		// This event listener will notify you when these events occur, it is up to you what you want to do during them
		mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
			@Override
			public void onRobotConnectionFailed(Robot arg0) {}
			@Override
			public void onNonePaired() {}
			
			@Override
			public void onRobotConnected(Robot arg0) {
				// Set the robot
				mRobot = arg0;
				// Hide the connection view. Comment this code if you want to connect to multiple robots
				mSpheroConnectionView.setVisibility(View.GONE);
				
				// Calling Stream Data Command right after the robot connects, will not work
				// You need to wait a second for the robot to initialize
				mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // turn rear light on
                        FrontLEDOutputCommand.sendCommand(mRobot, 1.0f);
                        // turn stabilization off
                        StabilizationCommand.sendCommand(mRobot, false);
                        // register the async data listener
                        DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);
                        // Start streaming data
                        requestDataStreaming();
                    }
                }, 1000);
			}
			
			@Override
			public void onBluetoothNotEnabled() {
				// See ButtonDrive Sample on how to show BT settings screen, for now just notify user
				Toast.makeText(StreamingActivity.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
    }

    @Override
    protected void onStop() {
        super.onStop();

		// Shutdown Sphero connection view
		mSpheroConnectionView.shutdown();
        if(mRobot != null){

            StabilizationCommand.sendCommand(mRobot, true);
            FrontLEDOutputCommand.sendCommand(mRobot, 0f);

            RobotProvider.getDefaultProvider().removeAllControls();
        }
    }

    private void requestDataStreaming() {

        if(mRobot != null){

            // Set up a bitmask containing the sensor information we want to stream
            final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_ACCELEROMETER_FILTERED_ALL |
                    SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_ANGLES_FILTERED_ALL;

            // Specify a divisor. The frequency of responses that will be sent is 400hz divided by this divisor.
            final int divisor = 50;

            // Specify the number of frames that will be in each response. You can use a higher number to "save up" responses
            // and send them at once with a lower frequency, but more packets per response.
            final int packet_frames = 1;

            // Reset finite packet counter
            mPacketCounter = 0;

            // Count is the number of async data packets Sphero will send you before
            // it stops.  You want to register for a finite count and then send the command
            // again once you approach the limit.  Otherwise data streaming may be left
            // on when your app crashes, putting Sphero in a bad state 
            final int response_count = TOTAL_PACKET_COUNT;

            //Send this command to Sphero to start streaming
            SetDataStreamingCommand.sendCommand(mRobot, divisor, packet_frames, mask, response_count);
        }
    }
}
