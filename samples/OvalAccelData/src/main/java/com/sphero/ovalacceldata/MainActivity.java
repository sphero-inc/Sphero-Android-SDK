package com.sphero.ovalacceldata;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.ovalcompiler.OvalControl;
import com.orbotix.ovalcompiler.response.async.OvalDeviceBroadcast;
import com.orbotix.ovalcompiler.response.async.OvalErrorBroadcast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements RobotChangedStateListener, OvalControl.OvalControlListener {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42; r4

    private ConvenienceRobot mRobot;
    private OvalControl mOvalControl;

    private TextView mXTextView;
    private TextView mYTextView;
    private TextView mZTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        DualStackDiscoveryAgent.getInstance().addRobotStateListener(this);

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

    private void initViews() {
        mXTextView = (TextView) findViewById( R.id.text_x );
        mYTextView = (TextView) findViewById( R.id.text_y );
        mZTextView = (TextView) findViewById( R.id.text_z );
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
        if (DualStackDiscoveryAgent.getInstance().isDiscovering()) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }

        //If a robot is connected to the device, disconnect it
        if (mRobot != null) {
            mRobot.disconnect();
            mRobot = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(null);
    }


    @Override
    public void onProgramFailedToSend(OvalControl control, String message) {
        Log.e( "Sphero", "onProgramFailedToSend" );
    }

    @Override
    public void onProgramSentSuccessfully(OvalControl control) {
        Log.e( "Sphero", "onProgramSentSuccessfully" );
    }

    @Override
    public void onOvmReset(OvalControl control) {
        //Load the collision program onto the robot
        String program = null;
        try {
            program = FileManager.getOvalProgram(getAssets().open("sample.oval"));
        } catch (Exception e) {
        }

        if (program != null) {
            mOvalControl.sendOval(program);
        }
    }

    @Override
    public void onOvalNotificationReceived(OvalControl control, OvalDeviceBroadcast notification) {
        Log.e("OvalNotification", "Received oval notification: " + Arrays.toString(notification.getFloats()));
        if( notification.getFloats() != null && notification.getFloats().length == 3 ) {
            mXTextView.setText(String.valueOf(notification.getFloats()[0]));
            mYTextView.setText( String.valueOf( notification.getFloats()[1] ) );
            mZTextView.setText( String.valueOf( notification.getFloats()[2] ) );
        }
    }

    @Override
    public void onOvmRuntimeErrorReceived(OvalControl control, OvalErrorBroadcast notification) {
        Log.e("OvalError", "Received oval error: " + notification.getErrorCode());
    }

    @Override
    public void onOvalQueueEmptied(OvalControl control) {

    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online: {
                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot(robot);

                //Create an OvalControl for sending oval programs to the connected robot
                mOvalControl = new OvalControl(robot, MainActivity.this);

                //Reset the OVM so you're working with a clean slate
                //Send the programs when the OVM resets
                mOvalControl.resetOvm(true);
            }
        }
    }
}
