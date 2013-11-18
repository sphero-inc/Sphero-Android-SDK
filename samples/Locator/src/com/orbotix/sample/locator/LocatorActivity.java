package com.orbotix.sample.locator;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import orbotix.robot.base.ConfigureLocatorCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RollCommand;
import orbotix.robot.sensor.LocatorData;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.LocatorListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

public class LocatorActivity extends Activity {

    private static final String TAG = "OBX-LocatorSample";
    /** Robot to from which we are streaming */
    private Sphero mRobot = null;

    /** The Sphero Connection View */
    private SpheroConnectionView mSpheroConnectionView;

//    private Handler mHandler = new Handler();

    private LocatorListener mLocatorListener = new LocatorListener() {
        @Override
        public void onLocatorChanged(LocatorData locatorData) {
            Log.d(TAG, locatorData.toString());
            if (locatorData != null) {
                ((TextView) findViewById(R.id.txt_locator_x)).setText(locatorData.getPositionX() + " cm");
                ((TextView) findViewById(R.id.txt_locator_y)).setText(locatorData.getPositionY() + " cm");
                ((TextView) findViewById(R.id.txt_locator_vx)).setText(locatorData.getVelocityX() + " cm/s");
                ((TextView) findViewById(R.id.txt_locator_vy)).setText(locatorData.getVelocityY() + " cm/s");
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.back_layout).requestFocus();

        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
//        mSpheroConnectionView.setSingleSpheroMode(true);
        // Set the connection event listener 
        mSpheroConnectionView.addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(Robot sphero) {
                mRobot = (Sphero) sphero;
                // Skip this next step if you want the user to be able to connect multiple Spheros
                mSpheroConnectionView.setVisibility(View.INVISIBLE);
                mRobot.getSensorControl().addLocatorListener(mLocatorListener);
                mRobot.getSensorControl().setRate(5);
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
        //Set the AsyncDataListener that will process each response.
        if (mRobot != null) {
            mRobot.getSensorControl().removeLocatorListener(mLocatorListener);
            mRobot.disconnect();           // Disconnect Robot properly
        }
    }

    /**
     * When the user clicks the configure button, it calls this function
     *
     * @param v
     */
    public void configurePressed(View v) {

        if (mRobot == null) return;

        int newX = 0;   // The locator's current X position value will be set to this value
        int newY = 0;   // The locator's current Y position value will be set to this value
        int newYaw = 0; // The yaw value you set this to, will represent facing down the +y_axis

        // Try parsing the integer values from the edit text boxes, if not, use zeros
        try {
            newX = Integer.parseInt(((EditText) findViewById(R.id.edit_new_x)).getText().toString());
        } catch (NumberFormatException e) {
        }

        try {
            newY = Integer.parseInt(((EditText) findViewById(R.id.edit_new_y)).getText().toString());
        } catch (NumberFormatException e) {
        }

        try {
            newYaw = Integer.parseInt(((EditText) findViewById(R.id.edit_new_yaw)).getText().toString());
        } catch (NumberFormatException e) {
        }

        // Flag will be true if the check box is clicked, false if it is not
        // When the flag is off (default behavior) the x, y locator grid is rotated with the calibration
        // When the flag is on the x, y locator grid is fixed and Sphero simply calibrates within it
        int flag = ((CheckBox) findViewById(R.id.checkbox_flag)).isChecked() ?
                ConfigureLocatorCommand.ROTATE_WITH_CALIBRATE_FLAG_ON :
                ConfigureLocatorCommand.ROTATE_WITH_CALIBRATE_FLAG_OFF;

        ConfigureLocatorCommand.sendCommand(mRobot, flag, newX, newY, newYaw);
    }

    public void upPressed(View v) {
        mRobot.drive(0, .6f);
    }

    public void rightPressed(View v) {
        mRobot.drive(90, .6f);
    }

    public void downPressed(View v) {
        mRobot.drive(180, .6f);
    }

    public void leftPressed(View v) {
        mRobot.drive(270, .6f);
    }

    public void stopPressed(View v) {
        mRobot.stop();
    }
}
