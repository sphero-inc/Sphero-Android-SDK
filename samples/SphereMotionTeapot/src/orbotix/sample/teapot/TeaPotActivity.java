package orbotix.sample.teapot;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import orbotix.robot.base.*;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;

public class TeaPotActivity extends Activity {

	/**
	 * Sphero Connection View
	 */
    private SpheroConnectionView mSpheroConnectionView;

    /** 
     * Robot
     */
    private Robot mRobot;
    private Handler mHandler = new Handler();
    
    /**
     * Data Streaming Packet Counts
     */
    private final static int TOTAL_PACKET_COUNT = 200;
    private final static int PACKET_COUNT_THRESHOLD = 50;
    private int mPacketCounter;
    
    /** 
     * Teapot Surface Code
     */
    private MyGLSurfaceView mGLSurfaceView;
    
    private final DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {
            if (data instanceof DeviceSensorsAsyncData) {
                DeviceSensorsData ballData = ((DeviceSensorsAsyncData)data).getAsyncData().get(0);

                // If we are getting close to packet limit, request more
                mPacketCounter++;
                if( mPacketCounter > (TOTAL_PACKET_COUNT - PACKET_COUNT_THRESHOLD) ) {
                    requestDataStreaming();
                }
                
                float[] sensorData = new float[3];
                sensorData[0] = (float)ballData.getAttitudeData().getAttitudeSensor().pitch;
                sensorData[1] = (float)ballData.getAttitudeData().getAttitudeSensor().roll;
                sensorData[2] = (float)ballData.getAttitudeData().getAttitudeSensor().yaw;
                mGLSurfaceView.onSensorChanged(sensorData);
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mGLSurfaceView = new MyGLSurfaceView(this);
        mGLSurfaceView.setRenderer(new TeapotRenderer());
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Add GL Surface, but hide it until Sphero is connected
        FrameLayout layout = (FrameLayout)findViewById(R.id.teapot_layout);
        layout.addView(mGLSurfaceView);
        mGLSurfaceView.setVisibility(View.GONE);
        
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
				mGLSurfaceView.setVisibility(View.VISIBLE);
				
				// Calling Stream Data Command right after the robot connects, will not work
				// You need to wait a second for the robot to initialize
				mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // turn rear light on
                        BackLEDOutputCommand.sendCommand(mRobot, 1.0f);
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
				Toast.makeText(TeaPotActivity.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
    }

    @Override
    protected void onStop() {
        super.onStop();

        // turn stabilization back on
        StabilizationCommand.sendCommand(mRobot, true);

        // turn rear light off
        BackLEDOutputCommand.sendCommand(mRobot, 0.0f);

        // stop the streaming data when we leave
        SetDataStreamingCommand.sendCommand(mRobot, 0, 0,
                SetDataStreamingCommand.DATA_STREAMING_MASK_OFF, 0);

        // unregister the async data listener to prevent a memory leak.
        DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mDataListener);

        // Disconnect properly
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }
    
    private void requestDataStreaming() {

        if(mRobot != null){

            // Set up a bitmask containing the sensor information we want to stream
            final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_ANGLES_FILTERED_ALL;

            // Specify a divisor. The frequency of responses that will be sent is 400hz divided by this divisor.
            final int divisor = 20;

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
