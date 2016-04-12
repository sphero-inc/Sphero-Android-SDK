package com.orbotix.locator;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.async.DeviceSensorAsyncMessage;
import com.orbotix.command.ConfigureLocatorCommand;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;
import com.orbotix.common.sensor.SensorFlag;
import com.orbotix.subsystem.SensorControl;

import java.util.ArrayList;
import java.util.List;

/**
 * Locator sample
 *
 * Keeps track of the robot's position by recording direction and speed.
 * This sample demonstrates how to use the Locator and Velocity sensors.
 *
 * For more explanation on driving, see the Button Drive sample
 *
 */
public class MainActivity extends Activity implements View.OnClickListener, RobotChangedStateListener, ResponseListener {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;
    private static final float ROBOT_VELOCITY = 0.6f;

    private ConvenienceRobot mRobot;

    private Button mBtn0;
    private Button mBtn90;
    private Button mBtn180;
    private Button mBtn270;
    private Button mBtnStop;
    private Button mBtnConfiguration;
    private EditText mEditTextNewX;
    private EditText mEditTextNewY;
    private EditText mEditTextNewYaw;
    private CheckBox mCheckboxFlag;

    private TextView mTextViewLocatorX;
    private TextView mTextViewLocatorY;
    private TextView mTextViewLocatorVX;
    private TextView mTextViewLocatorVY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    private void initViews() {
        mBtn0 = (Button) findViewById( R.id.btn_0 );
        mBtn90 = (Button) findViewById( R.id.btn_90 );
        mBtn180 = (Button) findViewById( R.id.btn_180 );
        mBtn270 = (Button) findViewById( R.id.btn_270 );
        mBtnStop = (Button) findViewById( R.id.btn_stop );
        mBtnConfiguration = (Button) findViewById( R.id.btn_configure );
        mEditTextNewX = (EditText) findViewById( R.id.edit_new_x );
        mEditTextNewY = (EditText) findViewById( R.id.edit_new_y );
        mEditTextNewYaw = (EditText) findViewById( R.id.edit_new_yaw );
        mCheckboxFlag = (CheckBox) findViewById( R.id.checkbox_flag );
        mTextViewLocatorX = (TextView) findViewById( R.id.txt_locator_x );
        mTextViewLocatorY = (TextView) findViewById( R.id.txt_locator_y );
        mTextViewLocatorVY = (TextView) findViewById( R.id.txt_locator_vy );
        mTextViewLocatorVX = (TextView) findViewById( R.id.txt_locator_vx );

        mBtn0.setOnClickListener( this );
        mBtn90.setOnClickListener( this );
        mBtn180.setOnClickListener( this );
        mBtn270.setOnClickListener( this );
        mBtnStop.setOnClickListener( this );
        mBtnConfiguration.setOnClickListener( this );
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

    @Override
    public void onClick(View v) {
        if( mRobot == null ) {
            return;
        }

        switch( v.getId() ) {
            case R.id.btn_0: {
                mRobot.drive( 0.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_90: {
                mRobot.drive( 90.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_180: {
                mRobot.drive( 180.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_270: {
                mRobot.drive( 270.0f, ROBOT_VELOCITY );
                break;
            }
            case R.id.btn_stop: {
                mRobot.stop();
                break;
            }case R.id.btn_configure: {
                configureLocator();
                break;
            }
        }
    }

    //Set the displayed location to user determined values
    private void configureLocator() {
        if( mRobot == null )
            return;

        int newX = 0;
        int newY = 0;
        int newYaw = 0;

        try {
            newX = Integer.parseInt( mEditTextNewX.getText().toString() );
        } catch( NumberFormatException e ) {}

        try {
            newY = Integer.parseInt( mEditTextNewY.getText().toString() );
        } catch( NumberFormatException e ) {}

        try {
            newYaw = Integer.parseInt( mEditTextNewYaw.getText().toString() );
        } catch( NumberFormatException e ) {}

        int flag = mCheckboxFlag.isChecked() ?
                ConfigureLocatorCommand.ROTATE_WITH_CALIBRATE_FLAG_ON
                : ConfigureLocatorCommand.ROTATE_WITH_CALIBRATE_FLAG_OFF;

        mRobot.sendCommand( new ConfigureLocatorCommand( flag, newX, newY, newYaw ) );
    }

    @Override
    public void handleResponse(DeviceResponse response, Robot robot) {
    }

    @Override
    public void handleStringResponse(String stringResponse, Robot robot) {
    }

    @Override
    public void handleAsyncMessage(AsyncMessage asyncMessage, Robot robot) {
        if( asyncMessage instanceof DeviceSensorAsyncMessage ) {
            float positionX = ( (DeviceSensorAsyncMessage) asyncMessage ).getAsyncData().get( 0 ).getLocatorData().getPositionX();
            float positionY = ( (DeviceSensorAsyncMessage) asyncMessage ).getAsyncData().get( 0 ).getLocatorData().getPositionY();
            float velocityX = ( (DeviceSensorAsyncMessage) asyncMessage ).getAsyncData().get( 0 ).getLocatorData().getVelocity().x;
            float velocityY = ( (DeviceSensorAsyncMessage) asyncMessage ).getAsyncData().get( 0 ).getLocatorData().getVelocity().y;

            mTextViewLocatorX.setText(positionX + "cm");
            mTextViewLocatorY.setText( positionY + "cm" );
            mTextViewLocatorVX.setText( velocityX + "cm/s" );
            mTextViewLocatorVY.setText( velocityY + "cm/s" );
        }
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online: {

                //Sensor flags can be bitwise ORed together to enable multiple sensors
                long sensorFlag = SensorFlag.VELOCITY.longValue() | SensorFlag.LOCATOR.longValue();

                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot(robot);

                //Enable sensors based on earlier defined flags, and set the streaming rate.
                //This example streams data from the connected robot 10 times a second.
                mRobot.enableSensors( sensorFlag, SensorControl.StreamingRate.STREAMING_RATE10 );
                mRobot.addResponseListener( this );

                break;
            }
        }
    }
}
