package com.orbotix.sample.helloworld;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import orbotix.robot.base.*;

import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.sphero.*;

/** Connects to an available Sphero robot, and then flashes its LED. */
public class HelloWorldActivity extends Activity {
    public static final String TAG = "OBX-HelloWorld";

    /** The Sphero Robot */
    private Sphero mRobot;

    private boolean connecting = false;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    /** Called when the user comes back to this app */
    @Override
    protected void onResume() {
        super.onResume();

        RobotProvider.getDefaultProvider().addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(Robot robot) {
                mRobot = (Sphero) robot;
                HelloWorldActivity.this.connected();
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                Log.d(TAG, "Connection Failed: " + sphero);
                Toast.makeText(HelloWorldActivity.this, "Sphero Connection Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnected(Robot robot) {
                Log.d(TAG, "Disconnected: " + robot);
                Toast.makeText(HelloWorldActivity.this, "Sphero Disconnected", Toast.LENGTH_SHORT).show();
                HelloWorldActivity.this.stopBlink();
                mRobot = null;
            }
        });

        RobotProvider.getDefaultProvider().addDiscoveryListener(new DiscoveryListener() {
            @Override
            public void onBluetoothDisabled() {
                Log.d(TAG, "Bluetooth Disabled");
                Toast.makeText(HelloWorldActivity.this, "Bluetooth Disabled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void discoveryComplete(List<Sphero> spheros) {
                Log.d(TAG, "Found " + spheros.size() + " robots");
            }

            @Override
            public void onFound(List<Sphero> sphero) {
                Log.d(TAG, "Found: " + sphero);
                RobotProvider.getDefaultProvider().connect(sphero.iterator().next());
            }
        });

        boolean success = RobotProvider.getDefaultProvider().startDiscovery(this);
        if(!success){
            Toast.makeText(HelloWorldActivity.this, "Unable To start Discovery!", Toast.LENGTH_LONG).show();
        }
    }

    private void connected() {
        Log.d(TAG, "Connected On Thread: " + Thread.currentThread().getName());
        Log.d(TAG, "Connected: " + mRobot);
        Toast.makeText(HelloWorldActivity.this, mRobot.getName() + " Connected", Toast.LENGTH_LONG).show();

        final SensorControl control = mRobot.getSensorControl();
        control.addSensorListener(new SensorListener() {
            @Override
            public void sensorUpdated(DeviceSensorsData sensorDataArray) {
                Log.d(TAG, sensorDataArray.toString());
            }
        } ,SensorFlag.ACCELEROMETER_NORMALIZED, SensorFlag.GYRO_NORMALIZED);

        control.setRate(1);
        mRobot.enableStabilization(false);
        mRobot.drive(90, 0);
        mRobot.setBackLEDBrightness(.5f);

        mRobot.getCollisionControl().startDetection(255,255,255,255,255);
        mRobot.getCollisionControl().addCollisionListener(new CollisionListener() {
            public void collisionDetected(CollisionDetectedAsyncData collisionData) {
                Log.d(TAG, collisionData.toString());
            }
        });

        HelloWorldActivity.this.blink(false); // Blink the robot's LED

        boolean preventSleepInCharger = mRobot.getConfiguration().isPersistentFlagEnabled(PersistentOptionFlags.PreventSleepInCharger);
        Log.d(TAG, "Prevent Sleep in charger = " + preventSleepInCharger);
        Log.d(TAG, "VectorDrive = " + mRobot.getConfiguration().isPersistentFlagEnabled(PersistentOptionFlags.EnableVectorDrive));

        mRobot.getConfiguration().setPersistentFlag(PersistentOptionFlags.PreventSleepInCharger, false);
        mRobot.getConfiguration().setPersistentFlag(PersistentOptionFlags.EnableVectorDrive, true);

        Log.d(TAG, "VectorDrive = " + mRobot.getConfiguration().isPersistentFlagEnabled(PersistentOptionFlags.EnableVectorDrive));
        Log.v(TAG, mRobot.getConfiguration().toString());

    }

    /** Called when the user presses the back or home button */
    @Override
    protected void onPause() {
        super.onPause();
        this.stopBlink();
        if (mRobot != null) {
            mRobot.disconnect();
        }
    }

    boolean blinking = true;

    private void stopBlink() {
        blinking = false;
    }

    /**
     * Causes the robot to blink once every second.
     *
     * @param lit
     */
    private void blink(final boolean lit) {
        if (mRobot == null) {
            blinking = false;
            return;
        }

        //If not lit, send command to show blue light, or else, send command to show no light
        if (lit) {
            mRobot.setColor(0, 0, 0);

        } else {
            mRobot.setColor(0, 255, 0);
        }

        if (blinking) {
            //Send delayed message on a handler to run blink again
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    blink(!lit);
                }
            }, 2000);
        }
    }
}
