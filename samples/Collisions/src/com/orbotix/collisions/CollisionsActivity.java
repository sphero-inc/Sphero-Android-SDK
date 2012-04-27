package com.orbotix.collisions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.CheckBox;
import android.widget.TextView;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.CollisionDetectedAsyncData;
import orbotix.robot.base.CollisionDetectedAsyncData.CollisionPower;
import orbotix.robot.base.ConfigureCollisionDetectionCommand;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.DeviceMessenger.AsyncDataListener;
import orbotix.robot.base.DeviceAsyncData;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.sensor.Acceleration;

public class CollisionsActivity extends Activity {

	private static final int STARTUP_ACTIVITY_RESULTS = 0;

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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
	}

	@Override
	protected void onStart() {
		super.onStart();

		Intent intent = new Intent(this, StartupActivity.class);
		startActivityForResult(intent, STARTUP_ACTIVITY_RESULTS);
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Assume that collision detection is configured and disable it.
		ConfigureCollisionDetectionCommand.sendCommand(mRobot, ConfigureCollisionDetectionCommand.DISABLE_DETECTION_METHOD, 0, 0, 0, 0, 0);
		
		// Remove async data listener
		DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mCollisionListener);
		
		// Disconnect from the robot.
		RobotProvider.getDefaultProvider().removeAllControls();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == STARTUP_ACTIVITY_RESULTS
				&& resultCode == Activity.RESULT_OK) {
			// Get a reference to the connected Sphero
			final String robot_id = data
					.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);

			if (robot_id != null && !robot_id.equals("")) {
				mRobot = RobotProvider.getDefaultProvider().findRobot(robot_id);
			}

			// Start streaming collision detection data
			//// First register a listener to process the data
			DeviceMessenger.getInstance().addAsyncDataListener(mRobot,
					mCollisionListener);

			//// Now send a command to enable streaming collisions
			//// 
			ConfigureCollisionDetectionCommand.sendCommand(mRobot, ConfigureCollisionDetectionCommand.DEFAULT_DETECTION_METHOD,
					45, 45, 100, 100, 100);
		}

	}
}