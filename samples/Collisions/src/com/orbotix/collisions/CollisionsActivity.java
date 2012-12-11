package com.orbotix.collisions;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.ConfigureCollisionDetectionCommand;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.DeviceMessenger.AsyncDataListener;
import orbotix.robot.base.DeviceAsyncData;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.sensor.Acceleration;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;

public class CollisionsActivity extends Activity {

	private TextView mAccelXValueLabel;
	private TextView mAccelYValueLabel;
	private TextView mAccelZValueLabel;
	private CheckBox mXAxisCheckBox;
	private CheckBox mYAxisCheckBox;
	private TextView mPowerXValueLabel;
	private TextView mPowerYValueLabel;
	private TextView mSpeedValueLabel;
	private TextView mTimestampLabel;

	private Robot mRobot;
	
    /**
     * The Sphero Connection View
     */
    private SpheroConnectionView mSpheroConnectionView;
    
    private Handler mHandler = new Handler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViewById(R.id.back_layout).requestFocus();

		// initialize value labels
		mAccelXValueLabel = (TextView) findViewById(R.id.accel_x_value);
		mAccelXValueLabel.setText("0.0");

		mAccelYValueLabel = (TextView) findViewById(R.id.accel_y_value);
		mAccelYValueLabel.setText("0.0");

		mAccelZValueLabel = (TextView) findViewById(R.id.accel_z_value);
		mAccelZValueLabel.setText("0.0");

		mXAxisCheckBox = (CheckBox) findViewById(R.id.axis_x_checkbox);
		mXAxisCheckBox.setChecked(false);

		mYAxisCheckBox = (CheckBox) findViewById(R.id.axis_y_checkbox);
		mYAxisCheckBox.setChecked(false);

		mPowerXValueLabel = (TextView) findViewById(R.id.power_x_value);
		mPowerXValueLabel.setText("0.0");

		mPowerYValueLabel = (TextView) findViewById(R.id.power_y_value);
		mPowerYValueLabel.setText("0.0");

		mSpeedValueLabel = (TextView) findViewById(R.id.speed_value);
		mSpeedValueLabel.setText("0.0");

		mTimestampLabel = (TextView) findViewById(R.id.time_stamp_value);
		mTimestampLabel.setText(SystemClock.uptimeMillis() + " ms");
		
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
			
				// Calling Configure Collision Detection Command right after the robot connects, will not work
				// You need to wait a second for the robot to initialize
				mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
        				// Start streaming collision detection data
        				//// First register a listener to process the data
        				DeviceMessenger.getInstance().addAsyncDataListener(mRobot,
        						mCollisionListener);

        				ConfigureCollisionDetectionCommand.sendCommand(mRobot, ConfigureCollisionDetectionCommand.DEFAULT_DETECTION_METHOD,
        						45, 45, 100, 100, 100);
                    }
                }, 1000);
			}
			
			@Override
			public void onBluetoothNotEnabled() {
				// See UISample Sample on how to show BT settings screen, for now just notify user
				Toast.makeText(CollisionsActivity.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Assume that collision detection is configured and disable it.
		ConfigureCollisionDetectionCommand.sendCommand(mRobot, ConfigureCollisionDetectionCommand.DISABLE_DETECTION_METHOD, 0, 0, 0, 0, 0);
		
		// Remove async data listener
		DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mCollisionListener);
		
        //Disconnect Robots on stop
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
	}
	
	private final AsyncDataListener mCollisionListener = new AsyncDataListener() {

		public void onDataReceived(DeviceAsyncData asyncData) {
			if (asyncData instanceof CollisionDetectedAsyncData) {
				final CollisionDetectedAsyncData collisionData = (CollisionDetectedAsyncData) asyncData;

				// Update the UI with the collision data
				Acceleration acceleration = collisionData.getImpactAcceleration();
				mAccelXValueLabel = (TextView) findViewById(R.id.accel_x_value);
				mAccelXValueLabel.setText("" + acceleration.x);

				mAccelYValueLabel = (TextView) findViewById(R.id.accel_y_value);
				mAccelYValueLabel.setText("" + acceleration.y);

				mAccelZValueLabel = (TextView) findViewById(R.id.accel_z_value);
				mAccelZValueLabel.setText("" + acceleration.z);

				mXAxisCheckBox = (CheckBox) findViewById(R.id.axis_x_checkbox);
				mXAxisCheckBox.setChecked(collisionData.hasImpactXAxis());

				mYAxisCheckBox = (CheckBox) findViewById(R.id.axis_y_checkbox);
				mYAxisCheckBox.setChecked(collisionData.hasImpactYAxis());

				CollisionPower power = collisionData.getImpactPower();
				mPowerXValueLabel = (TextView) findViewById(R.id.power_x_value);
				mPowerXValueLabel.setText("" + power.x);

				mPowerYValueLabel = (TextView) findViewById(R.id.power_y_value);
				mPowerYValueLabel.setText("" + power.y);

				mSpeedValueLabel = (TextView) findViewById(R.id.speed_value);
				mSpeedValueLabel.setText("" + collisionData.getImpactSpeed());

				mTimestampLabel = (TextView) findViewById(R.id.time_stamp_value);
				mTimestampLabel.setText(collisionData.getImpactTimeStamp() + " ms");
			}
		}
	};
}