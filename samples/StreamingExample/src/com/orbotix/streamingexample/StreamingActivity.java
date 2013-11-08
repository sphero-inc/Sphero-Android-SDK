package com.orbotix.streamingexample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import orbotix.robot.base.Robot;
import orbotix.robot.sensor.AccelerometerData;
import orbotix.robot.sensor.AttitudeSensor;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.SensorFlag;
import orbotix.sphero.SensorListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

public class StreamingActivity extends Activity {
    /** Sphero Connection Activity */
    private SpheroConnectionView mSpheroConnectionView;
    private Handler mHandler = new Handler();

    /** Robot to from which we are streaming */
    private Sphero mSphero = null;

    //The views that will show the streaming data
    private ImuView mImuView;
    private CoordinateView mAccelerometerFilteredView;
    private ConnectionListener mConnectionListener;

    private final SensorListener mSensorListener = new SensorListener() {
        @Override
        public void sensorUpdated(DeviceSensorsData datum) {
            //Show attitude data
            AttitudeSensor attitude = datum.getAttitudeData();
            if (attitude != null) {
                mImuView.setPitch(String.format("%+3d", attitude.pitch));
                mImuView.setRoll(String.format("%+3d", attitude.roll));
                mImuView.setYaw(String.format("%+3d", attitude.yaw));
            }

            //Show accelerometer data
            AccelerometerData accel = datum.getAccelerometerData();
            if (attitude != null) {
                mAccelerometerFilteredView.setX(String.format("%+.4f", accel.getFilteredAcceleration().x));
                mAccelerometerFilteredView.setY(String.format("%+.4f", accel.getFilteredAcceleration().y));
                mAccelerometerFilteredView.setZ(String.format("%+.4f", accel.getFilteredAcceleration().z));
            }
        }
    };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Get important views
        mImuView = (ImuView) findViewById(R.id.imu_values);
        mAccelerometerFilteredView = (CoordinateView) findViewById(R.id.accelerometer_filtered_coordinates);

        // Find Sphero Connection View from layout file
        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);

        // This event listener will notify you when these events occur, it is up to you what you want to do during them

        mConnectionListener = new ConnectionListener() {
            @Override
            public void onConnected(Robot sphero) {
                Log.d("StreamingActivity", "Connected...");
                // Hide the connection view. Comment this code if you want to connect to multiple robots in other apps.
                // Currently streaming doesn't support multiple robots.
                mSpheroConnectionView.setVisibility(View.INVISIBLE);

                mSphero = (Sphero) sphero;
                mSphero.setBackLEDBrightness(1.0f);
                mSphero.setColor(50, 130, 60);
                mSphero.enableStabilization(false);  // disable
                mSphero.getSensorControl().setRate(10  /*Hz*/);
                mSphero.getSensorControl().addSensorListener(mSensorListener, SensorFlag.ACCELEROMETER_NORMALIZED, SensorFlag.ATTITUDE);
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                // let the SpheroConnectionView handle or hide it and do something here...
            }

            @Override
            public void onDisconnected(Robot sphero) {
                mSpheroConnectionView.startDiscovery();
            }
        };
        mSpheroConnectionView.addConnectionListener(mConnectionListener);
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
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        if (mSphero != null) {
            // make sure to remove the streaming listener!
            mSphero.getSensorControl().removeSensorListener(mSensorListener);
            mSphero.disconnect(); // Disconnect Robot properly
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}
