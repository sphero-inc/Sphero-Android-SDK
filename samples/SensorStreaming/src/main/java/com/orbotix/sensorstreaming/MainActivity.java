package com.orbotix.sensorstreaming;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.async.DeviceSensorAsyncMessage;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;
import com.orbotix.common.sensor.AccelerometerData;
import com.orbotix.common.sensor.AttitudeSensor;
import com.orbotix.common.sensor.BackEMFSensor;
import com.orbotix.common.sensor.DeviceSensorsData;
import com.orbotix.common.sensor.GyroData;
import com.orbotix.common.sensor.QuaternionSensor;
import com.orbotix.common.sensor.SensorFlag;
import com.orbotix.subsystem.SensorControl;

import java.util.ArrayList;
import java.util.List;

/**
 * Sensor Streaming sample
 *
 * Displays output from various sensors on the connected robot
 *
 */
public class MainActivity extends Activity implements RobotChangedStateListener, ResponseListener {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;

    private ConvenienceRobot mRobot;

    private TextView mAccelX;
    private TextView mAccelY;
    private TextView mAccelZ;
    private TextView mYawValue;
    private TextView mRollValue;
    private TextView mPitchValue;
    private TextView mQ0Value;
    private TextView mQ1Value;
    private TextView mQ2Value;
    private TextView mQ3Value;
    private TextView mGyroX;
    private TextView mGyroY;
    private TextView mGyroZ;
    private TextView mLeftMotor;
    private TextView mRightMotor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        /*
            Associate a listener for robot state changes with the DualStackDiscoveryAgent.
            DualStackDiscoveryAgent checks for both Bluetooth Classic and Bluetooth LE.
            DiscoveryAgentClassic checks only for Bluetooth Classic robots.
            DualStackDiscoveryAgent checks only for Bluetooth LE robots.
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

    private void initViews() {
        mAccelX = (TextView) findViewById( R.id.accel_x );
        mAccelY = (TextView) findViewById( R.id.accel_y );
        mAccelZ = (TextView) findViewById( R.id.accel_z );

        mRollValue = (TextView) findViewById( R.id.value_roll );
        mPitchValue = (TextView) findViewById( R.id.value_pitch );
        mYawValue = (TextView) findViewById( R.id.value_yaw );

        mQ0Value = (TextView) findViewById( R.id.value_q0 );
        mQ1Value = (TextView) findViewById( R.id.value_q1 );
        mQ2Value = (TextView) findViewById( R.id.value_q2 );
        mQ3Value = (TextView) findViewById( R.id.value_q3 );

        mGyroX = (TextView) findViewById( R.id.gyroscope_x );
        mGyroY = (TextView) findViewById( R.id.gyroscope_y );
        mGyroZ = (TextView) findViewById( R.id.gyroscope_z );

        mLeftMotor = (TextView) findViewById( R.id.left_motor );
        mRightMotor = (TextView) findViewById( R.id.right_motor );
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
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online: {

                //Use bitwise OR operations to create a flag to notify the robot what sensors we're interested in
                long sensorFlag = SensorFlag.QUATERNION.longValue()
                        | SensorFlag.ACCELEROMETER_NORMALIZED.longValue()
                        | SensorFlag.GYRO_NORMALIZED.longValue()
                        | SensorFlag.MOTOR_BACKEMF_NORMALIZED.longValue()
                        | SensorFlag.ATTITUDE.longValue();

                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot( robot );

                //Remove stabilization so the robot can be turned in all directions without correcting itself
                mRobot.enableStabilization(false);

                //Enable sensors based on the flag defined above, and stream their data ten times a second to the mobile device
                mRobot.enableSensors( sensorFlag, SensorControl.StreamingRate.STREAMING_RATE10 );

                //Listen to data responses from the robot
                mRobot.addResponseListener(this);

                break;
            }
        }
    }

    @Override
    public void handleResponse(DeviceResponse response, Robot robot) {

    }

    @Override
    public void handleStringResponse(String stringResponse, Robot robot) {

    }

    @Override
    public void handleAsyncMessage(AsyncMessage asyncMessage, Robot robot) {
        if( asyncMessage == null )
            return;

        //Check the asyncMessage type to see if it is a DeviceSensor message
        if( asyncMessage instanceof DeviceSensorAsyncMessage ) {
            DeviceSensorAsyncMessage message = (DeviceSensorAsyncMessage) asyncMessage;

            if( message.getAsyncData() == null
                    || message.getAsyncData().isEmpty()
                    || message.getAsyncData().get( 0 ) == null )
                return;

            //Retrieve DeviceSensorsData from the async message
            DeviceSensorsData data = message.getAsyncData().get( 0 );

            //Extract the accelerometer data from the sensor data
            displayAccelerometer(data.getAccelerometerData());

            //Extract attitude data (yaw, roll, pitch) from the sensor data
            displayAttitude(data.getAttitudeData());

            //Extract quaternion data from the sensor data
            displayQuaterions( data.getQuaternion() );

            //Display back EMF data from left and right motors
            displayBackEMF( data.getBackEMFData().getEMFFiltered() );

            //Extract gyroscope data from the sensor data
            displayGyroscope( data.getGyroData() );
        }
    }

    private void displayBackEMF( BackEMFSensor sensor ) {
        if( sensor == null )
            return;

        mLeftMotor.setText( String.valueOf( sensor.leftMotorValue ) );
        mRightMotor.setText( String.valueOf( sensor.rightMotorValue ) );
    }

    private void displayGyroscope( GyroData data ) {
        mGyroX.setText( String.valueOf( data.getRotationRateFiltered().x ) );
        mGyroY.setText( String.valueOf( data.getRotationRateFiltered().y ) );
        mGyroZ.setText( String.valueOf( data.getRotationRateFiltered().z ) );
    }

    private void displayAccelerometer( AccelerometerData accelerometer ) {
        if( accelerometer == null || accelerometer.getFilteredAcceleration() == null ) {
            return;
        }

        //Display the readings from the X, Y and Z components of the accelerometer
        mAccelX.setText( String.format( "%.4f", accelerometer.getFilteredAcceleration().x ) );
        mAccelY.setText( String.format( "%.4f", accelerometer.getFilteredAcceleration().y ) );
        mAccelZ.setText( String.format( "%.4f", accelerometer.getFilteredAcceleration().z ) );
    }

    private void displayAttitude( AttitudeSensor attitude ) {
        if( attitude == null )
            return;

        //Display the pitch, roll and yaw from the attitude sensor
        mRollValue.setText( String.format( "%3d", attitude.roll ) + "°" );
        mPitchValue.setText( String.format( "%3d", attitude.pitch ) + "°" );
        mYawValue.setText( String.format( "%3d", attitude.yaw) + "°" );
    }

    private void displayQuaterions( QuaternionSensor quaternion ) {
        if( quaternion == null )
            return;

        //Display the four quaterions data
        mQ0Value.setText( String.format( "%.5f", quaternion.getQ0() ) );
        mQ1Value.setText( String.format( "%.5f", quaternion.getQ1()) );
        mQ2Value.setText( String.format( "%.5f", quaternion.getQ2()) );
        mQ3Value.setText( String.format( "%.5f", quaternion.getQ3()) );

    }
}
