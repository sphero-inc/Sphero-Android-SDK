package com.orbotix.buttondrive;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Button Drive sample
 *
 * Connect either a Bluetooth Classic or Bluetooth LE robot to an Android Device, then
 * drive the robot by pressing buttons on the screen.
 * Headings are all based off of the back LED being considered the back of the robot
 *
 * 0 moves forward
 * 90 moves right
 * 180 moves backward
 * 270 moves left
 */
public class MainActivity extends Activity implements View.OnClickListener, RobotChangedStateListener {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;
    private static final float ROBOT_VELOCITY = 0.6f;

    private ConvenienceRobot mRobot;

    private Button mBtn0;
    private Button mBtn90;
    private Button mBtn180;
    private Button mBtn270;
    private Button mBtnStop;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        /*
            Associate a listener for robot state changes with the DualStackDiscoveryAgent.
            DualStackDiscoveryAgent checks for both Bluetooth Classic and Bluetooth LE.
            DiscoveryAgentClassic checks only for Bluetooth Classic robots.
            DiscoveryAgentLE checks only for Bluetooth LE robots.
       */
        DualStackDiscoveryAgent.getInstance().addRobotStateListener( this );

        initViews();

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int hasLocationPermission = checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION );
            if( hasLocationPermission != PackageManager.PERMISSION_GRANTED ) {
                Log.e( "Sphero", "Location permission has not already been granted" );
                List<String> permissions = new ArrayList<String>();
                permissions.add( Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()] ), REQUEST_CODE_LOCATION_PERMISSION );
            } else {
                Log.d( "Sphero", "Location permission already granted" );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                for( int i = 0; i < permissions.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        startDiscovery();
                        Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void initViews() {
        mBtn0 = (Button) findViewById( R.id.btn_0 );
        mBtn90 = (Button) findViewById( R.id.btn_90 );
        mBtn180 = (Button) findViewById( R.id.btn_180 );
        mBtn270 = (Button) findViewById( R.id.btn_270 );
        mBtnStop = (Button) findViewById( R.id.btn_stop );

        mBtn0.setOnClickListener( this );
        mBtn90.setOnClickListener( this );
        mBtn180.setOnClickListener( this );
        mBtn270.setOnClickListener( this );
        mBtnStop.setOnClickListener( this );
    }

    @Override
    protected void onStart() {
        super.onStart();

        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            startDiscovery();
        }
    }

    private void startDiscovery() {
        //If the DiscoveryAgent is not already looking for robots, start discovery.
        if( !DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery( this );
            } catch (DiscoveryException e) {
                Log.e("Sphero", "DiscoveryException: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onStop() {
        //If the DiscoveryAgent is in discovery mode, stop it.
        if( DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }

        //If a robot is connected to the device, disconnect it
        if( mRobot != null ) {
            mRobot.disconnect();
            mRobot = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DualStackDiscoveryAgent.getInstance().addRobotStateListener( null );
    }

    @Override
    public void onClick(View v) {
        //If the robot is null, then it is probably not connected and nothing needs to be done
        if( mRobot == null ) {
            return;
        }

        /*
            When a heading button is pressed, set the robot to drive in that heading.
            All directions are based on the back LED being considered the back of the robot.
            0 moves in the opposite direction of the back LED.
         */
        switch( v.getId() ) {
            case R.id.btn_0: {
                //Forward
                mRobot.drive( 0.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_90: {
                //To the right
                mRobot.drive( 90.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_180: {
                //Backward
                mRobot.drive( 180.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_270: {
                //To the left
                mRobot.drive( 270.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_stop: {
                //Stop the robot
                mRobot.stop();
                break;
            }
        }
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online: {
                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot(robot);
                break;
            }
        }
    }
}
