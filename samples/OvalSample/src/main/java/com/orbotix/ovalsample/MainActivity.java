package com.orbotix.ovalsample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

/**
 * Oval sample
 *
 * Loads a predefined Oval program from the Assets folder that blinks the robot's LED
 *
 */
public class MainActivity extends Activity implements RobotChangedStateListener, OvalControl.OvalControlListener, View.OnClickListener {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;

    private ConvenienceRobot mRobot;
    private OvalControl mOvalControl;

    private EditText mUpdateEditText;
    private Button mUpdateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        /*
            Associate a listener for robot state changes with the DualStackDiscoveryAgent.
            DualStackDiscoveryAgent checks for both Bluetooth Classic and Bluetooth LE.
            DiscoveryAgentClassic checks only for Bluetooth Classic robots.
            DiscoveryAgentLE checks only for Bluetooth LE robots.
        */
        DualStackDiscoveryAgent.getInstance().addRobotStateListener( this );

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
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(null);
    }

    private void initViews() {
        mUpdateButton = (Button) findViewById( R.id.update_button );
        mUpdateButton.setOnClickListener( this );

        mUpdateEditText = (EditText) findViewById( R.id.animation_speed_edit_text );

    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online: {
                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot(robot);

                //Create an OvalControl for sending oval programs to the connected robot
                mOvalControl = new OvalControl( robot, MainActivity.this );

                //Reset the OVM so you're working with a clean slate
                //Send the programs when the OVM resets
                mOvalControl.resetOvm(true);
            }
        }
    }

    @Override
    public void onProgramFailedToSend(OvalControl control, String message) {

    }

    @Override
    public void onProgramSentSuccessfully(OvalControl control) {
    }

    @Override
    public void onOvmReset(OvalControl control) {
        //Load the blinking program from a file and send it to the robot
        String program = null;
        try {
            program = FileManager.getOvalProgram( getAssets().open( "sample.oval" ) );
        } catch( Exception e ) {}

        if( program != null ) {
            mOvalControl.sendOval( program );
        }
    }

    @Override
    public void onOvalNotificationReceived(OvalControl control, OvalDeviceBroadcast notification) {
        Log.v("OvalNotification", "Received oval notification: " + Arrays.toString(notification.getInts()));
    }

    @Override
    public void onOvmRuntimeErrorReceived(OvalControl control, OvalErrorBroadcast notification) {
        Log.v("OvalError", "Received oval error: " + notification.getErrorCode());
    }

    @Override
    public void onOvalQueueEmptied(OvalControl control) {
    }

    @Override
    public void onClick(View v) {
        if( mOvalControl == null )
            return;

        // Take the number presented in the EditText and use that to change the rate of
        // blinking for the robot's LED
        String num;
        try {
            num = mUpdateEditText.getText().toString();
            if( Float.valueOf( num ) <= 0.0f ) {
                num = "0.1";
                mUpdateEditText.setText( num );
            }
        } catch( NumberFormatException e ) {
            num = "0.25";
            mUpdateEditText.setText( num );
        }

        //Send a new line to the OVM in order to change the value of the speed variable
        mOvalControl.sendOval( "speed=" + num + ";...");
    }
}
