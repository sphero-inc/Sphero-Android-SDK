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
import orbotix.robot.base.*;
import orbotix.robot.sensor.AttitudeSensor;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.SensorFlag;
import orbotix.sphero.SensorListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

public class StreamingAnimationActivity extends Activity {
    /** Sphero Connection Activity */
    private SpheroConnectionView mSpheroConnectionView;
    private Handler mHandler = new Handler();

    /** Robot to from which we are streaming */
    private Sphero mRobot = null;

    /** Sphero Image that collects coins */
    private ImageView mImageSphero;

    /** Display the streaming data of the IMU */
    private TextView mTextRoll;
    private TextView mTextPitch;
    private TextView mTextYaw;

    private Point mImageSpheroLoc;                 // The X and Y position of Sphero image (pixels)
    private Point mImageSpheroBounds;             // The width and height of Sphero image (pixels)
    private int mScreenWidth;                     // Phone Screen Width (pixels)
    private int mScreenHeight;                     // Phone Screen Height (pixels)

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Keep Screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get Sphero image into a variable
        mImageSphero = (ImageView) findViewById(R.id.image_sphero);

        // Text Views
        mTextRoll = (TextView) findViewById(R.id.roll);
        mTextPitch = (TextView) findViewById(R.id.pitch);
        mTextYaw = (TextView) findViewById(R.id.yaw);

        // Get Screen width and height
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenHeight = displaymetrics.heightPixels;
        mScreenWidth = displaymetrics.widthPixels;

        // Find Sphero Connection View from layout file
        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
        mSpheroConnectionView.addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(Robot sphero) {
                mRobot = (Sphero) sphero;
                mSpheroConnectionView.setVisibility(View.INVISIBLE);
                mRobot.setBackLEDBrightness(1.0f);

                // turn stabilization off
                mRobot.enableStabilization(false);
                // register the async data listener

                // Start streaming data
                mRobot.getSensorControl().addSensorListener(new SensorListener() {
                    @Override
                    public void sensorUpdated(DeviceSensorsData sensorDataArray) {
                        AttitudeSensor attitude = sensorDataArray.getAttitudeData();

                           // Get the values of roll and yaw
                           int roll = attitude.roll;
                           int pitch = attitude.pitch;
                           int yaw = attitude.yaw;

                           // Display data values in the text view
                           mTextRoll.setText(getString(R.string.roll) + roll);
                           mTextPitch.setText(getString(R.string.pitch) + pitch);
                           mTextYaw.setText(getString(R.string.yaw) + yaw);

                           // Calculate the new image position
                           updateSpheroPosition(roll, pitch, yaw);
                    }
                }, SensorFlag.ATTITUDE);

                // Show Sphero image and set width and height and starting position
                mImageSpheroBounds = new Point(mImageSphero.getDrawable().getBounds().width(),
                        mImageSphero.getDrawable().getBounds().height());
                mImageSpheroLoc = new Point(mImageSpheroBounds.x / 2, mImageSpheroBounds.y / 2);
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                // let the SpheroConnectionView handle or hide it and do something here...
            }

            @Override
            public void onDisconnected(Robot sphero) {
                mSpheroConnectionView.startDiscovery();
            }
        });
    }

    /** Called when the user comes back to this app */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list of Spheros
        mSpheroConnectionView.startDiscovery();
    }

    /** Called when the user presses the back or home button */
    @Override
    protected void onPause() {
        super.onPause();
        // Disconnect Robot properly
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    /**
     * Called when the Calibrate Button is clicked
     *
     * @param v
     */
    public void calibrateClicked(View v) {
        // Make the current orientation of Sphero 0,0,0 (roll,pitch,yaw)
        // This is how you can get the user to point the blue tail light towards them
        // And have Sphero right side up
        SetHeadingCommand.sendCommand(mRobot, 0);
    }

    /**
     * Update the Sphero Logo Location
     *
     * @param roll  in degrees from data streaming
     * @param pitch in degrees from data streaming
     * @param yaw   in degrees from data streaming
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
        if ((mImageSpheroLoc.x + mImageSpheroBounds.x) > mScreenWidth) {
            mImageSpheroLoc.x = mScreenWidth - mImageSpheroBounds.x;
        }
        if ((mImageSpheroLoc.y + mImageSpheroBounds.y) > mScreenHeight) {
            mImageSpheroLoc.y = mScreenHeight - mImageSpheroBounds.y;
        }
        if (mImageSpheroLoc.x < 0) {
            mImageSpheroLoc.x = 0;
        }
        if (mImageSpheroLoc.y < 0) {
            mImageSpheroLoc.y = 0;
        }

        // Create Sphero translation matrix
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate(mImageSpheroLoc.x, mImageSpheroLoc.y);

        // Rotate around Sphero center
        Point spheroCenter = new Point(mImageSpheroLoc.x + (mImageSpheroBounds.x / 2),
                mImageSpheroLoc.y + (mImageSpheroBounds.y / 2));
        matrix.postRotate((int) -yaw, spheroCenter.x, spheroCenter.y);

        // Apply translation matrix
        mImageSphero.setScaleType(ScaleType.MATRIX);
        mImageSphero.setImageMatrix(matrix);
    }
}
