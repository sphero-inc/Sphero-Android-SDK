package com.orbotix.sample.buttondrive;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RollCommand;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

/** Activity for controlling the Sphero with five control buttons. */
public class ButtonDriveActivity extends Activity {

    private Sphero mRobot;

    /** The Sphero Connection View */
    private SpheroConnectionView mSpheroConnectionView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
        mSpheroConnectionView.addConnectionListener(new ConnectionListener() {

            @Override
            public void onConnected(Robot robot) {
                //SpheroConnectionView is made invisible on connect by default
                mRobot = (Sphero) robot;
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
     * When the user clicks "STOP", stop the Robot.
     *
     * @param v The View that had been clicked
     */
    public void onStopClick(View v) {
        if (mRobot != null) {
            // Stop robot
            mRobot.stop();
        }
    }

    /**
     * When the user clicks a control button, roll the Robot in that direction
     *
     * @param v The View that had been clicked
     */
    public void onControlClick(View v) {
        // Find the heading, based on which button was clicked
        final float heading;
        switch (v.getId()) {

            case R.id.ninety_button:
                heading = 90f;
                break;

            case R.id.one_eighty_button:
                heading = 180f;
                break;

            case R.id.two_seventy_button:
                heading = 270f;
                break;

            default:
                heading = 0f;
                break;
        }

        // Set speed. 60% of full speed
        final float speed = 0.6f;

        // Roll robot
        mRobot.drive(heading, speed);
    }
}
