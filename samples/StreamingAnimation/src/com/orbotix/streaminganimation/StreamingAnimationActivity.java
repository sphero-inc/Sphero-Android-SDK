package com.orbotix.streaminganimation;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;
import orbotix.robot.base.*;
import orbotix.robot.sensor.AttitudeData;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;
import java.util.List;

public class StreamingAnimationActivity extends Activity
{
    /**
     * Sphero Connection Activity
     */
    private SpheroConnectionView mSpheroConnectionView;
    private Handler mHandler = new Handler();
    
    /**
     * Robot to from which we are streaming
     */
    private Robot mRobot = null;
    
    /**
     * Data Streaming Packet Counts
     */
    private final static int TOTAL_PACKET_COUNT = 200;
    private final static int PACKET_COUNT_THRESHOLD = 50;
    private int mPacketCounter;

    /**
     * Sphero Image that collects coins
     */
    private ImageView mImageSphero;
    
    /**
     * Display the streaming data of the IMU
     */
    private TextView mTextRoll;
    private TextView mTextPitch;
    private TextView mTextYaw;
    
    private Point mImageSpheroLoc; 				 // The X and Y position of Sphero image (pixels)
    private Point mImageSpheroBounds;			 // The width and height of Sphero image (pixels)
    private int mScreenWidth;					 // Phone Screen Width (pixels)
    private int mScreenHeight;					 // Phone Screen Height (pixels) 
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Keep Screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Get Sphero image into a variable
        mImageSphero = (ImageView)findViewById(R.id.image_sphero);
        
        // Text Views
        mTextRoll = (TextView)findViewById(R.id.roll);
        mTextPitch = (TextView)findViewById(R.id.pitch);
        mTextYaw = (TextView)findViewById(R.id.yaw);

        // Get Screen width and height
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenHeight = displaymetrics.heightPixels;
        mScreenWidth = displaymetrics.widthPixels;
        
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
                        
                        // Show Sphero image and set width and height and starting position
                        mImageSpheroBounds = new Point(mImageSphero.getDrawable().getBounds().width(),
                        							   mImageSphero.getDrawable().getBounds().height());
                        mImageSpheroLoc = new Point(mImageSpheroBounds.x/2, mImageSpheroBounds.y/2);
                    }
                }, 1000);
			}
			
			@Override
			public void onBluetoothNotEnabled() {
				// See ButtonDrive Sample on how to show BT settings screen, for now just notify user
				Toast.makeText(StreamingAnimationActivity.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
		mSpheroConnectionView.showSpheros();
    }

    @Override
    protected void onStop() {
        super.onStop();

		// Shutdown Sphero connection view
        if(mRobot != null){

            StabilizationCommand.sendCommand(mRobot, true);
            FrontLEDOutputCommand.sendCommand(mRobot, 0f);

            RobotProvider.getDefaultProvider().removeAllControls();
        }
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
			            
			            // Get the values of roll and yaw
			            int roll = attitude.getAttitudeSensor().roll;
			            int pitch = attitude.getAttitudeSensor().pitch;
			            int yaw = attitude.getAttitudeSensor().yaw;
			            
					    // Display data values in the text view
					    mTextRoll.setText(getString(R.string.roll) + roll);
					    mTextPitch.setText(getString(R.string.pitch) + pitch);
					    mTextYaw.setText(getString(R.string.yaw) + yaw);
			            
					    // Calculate the new image position
					    updateSpheroPosition(roll, pitch, yaw);
			        }
			    }
            }
        }
    };
    
    /**
     * Called when the Calibrate Button is clicked
     * @param v
     */
    public void calibrateClicked(View v) {
    	// Make the current orientation of Sphero 0,0,0 (roll,pitch,yaw)
    	// This is how you can get the user to point the blue tail light towards them
    	// And have Sphero right side up
    	CalibrateCommand.sendCommand(mRobot, 0);
    }
    
    /**
     * Update the Sphero Logo Location
     * @param roll in degrees from data streaming
     * @param pitch in degrees from data streaming
     * @param yaw in degrees from data streaming
     */
    private void updateSpheroPosition(double roll, double pitch, double yaw) {
    	
    	// Find the length of the velocity vector and the angle
	    double length = Math.sqrt(pitch * pitch + roll * roll);
	    double moveAngle = -Math.atan2(-pitch, roll);
	    
	    // Adjust this value to change the sensitivity of Sphero moving
	    final float SENSITIVITY = 0.8f;
	    
	    // Compute the velocity of the Sphero image
	    double adjustedX = length * Math.cos(moveAngle) * SENSITIVITY;
	    double adjustedY = length * Math.sin(moveAngle) * SENSITIVITY;
	    
	    // Add new distance to the Sphero image
	    mImageSpheroLoc.x += adjustedX;
	    mImageSpheroLoc.y += adjustedY;
	    
	    // Check boundaries
	    if( (mImageSpheroLoc.x + mImageSpheroBounds.x) > mScreenWidth ) {
	    	mImageSpheroLoc.x = mScreenWidth - mImageSpheroBounds.x;
	    }
	    if( (mImageSpheroLoc.y + mImageSpheroBounds.y) > mScreenHeight ) {
	    	mImageSpheroLoc.y = mScreenHeight - mImageSpheroBounds.y;
	    }
	    if( mImageSpheroLoc.x < 0 ) {
	    	mImageSpheroLoc.x = 0;
	    }
	    if( mImageSpheroLoc.y < 0 ) {
	    	mImageSpheroLoc.y = 0;
	    }
	    
	    // Create Sphero translation matrix
	    Matrix matrix = new Matrix();
	    matrix.reset();
	    matrix.postTranslate(mImageSpheroLoc.x, mImageSpheroLoc.y);
	    
	    // Rotate around Sphero center
	    Point spheroCenter = new Point(mImageSpheroLoc.x+(mImageSpheroBounds.x/2),
	    							   mImageSpheroLoc.y+(mImageSpheroBounds.y/2));
	    matrix.postRotate((int)-yaw, spheroCenter.x, spheroCenter.y);

	    // Apply translation matrix
	    mImageSphero.setScaleType(ScaleType.MATRIX);
	    mImageSphero.setImageMatrix(matrix);
    }
}
