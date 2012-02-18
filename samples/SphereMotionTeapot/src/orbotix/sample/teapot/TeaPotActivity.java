package orbotix.sample.teapot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.TextView;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.*;
import orbotix.robot.sensor.DeviceSensorsData;

public class TeaPotActivity extends Activity {

    private static final int REQUEST_STARTUP = 101;

    private MyGLSurfaceView mGLSurfaceView;
    private Robot mRobot;
    private PowerManager.WakeLock screenWakeLock;
    
    private TextView pitchText, rollText, yawText, pitchRawText, rollRawText, yawRawText;
    
    private final DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {
            if (data instanceof DeviceSensorsAsyncData) {
                DeviceSensorsData ballData = ((DeviceSensorsAsyncData)data).getAsyncData().get(0);

                float[] sensorData = new float[3];
                sensorData[0] = (float)ballData.getAttitudeData().getAttitudeSensor().pitch;
                sensorData[1] = (float)ballData.getAttitudeData().getAttitudeSensor().roll;
                sensorData[2] = (float)ballData.getAttitudeData().getAttitudeSensor().yaw;
                mGLSurfaceView.onSensorChanged(sensorData);
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new MyGLSurfaceView(this);
        mGLSurfaceView.setRenderer(new TeapotRenderer());
        setContentView(mGLSurfaceView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PowerManager power_manager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        screenWakeLock = power_manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Teapot");
        screenWakeLock.acquire();
        Intent startupIntent = new Intent(this, StartupActivity.class);
        startActivityForResult(startupIntent, REQUEST_STARTUP);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (screenWakeLock != null) {
            screenWakeLock.release();
            screenWakeLock = null;
        }

        // turn stabilization back on
        StabilizationCommand.sendCommand(mRobot, true);

        // turn rear light off
        FrontLEDOutputCommand.sendCommand(mRobot, 0.0f);

        // stop the streaming data when we leave
        SetDataStreamingCommand.sendCommand(mRobot, 0, 0,
                SetDataStreamingCommand.DATA_STREAMING_MASK_OFF, 0);

        // unregister the async data listener to prevent a memory leak.
        DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mDataListener);

        // pause here for a tenth of a second to allow the previous commands to go through before we shutdown
        // the connection to the ball
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // disconnect from the ball
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        switch (requestCode) {
            case REQUEST_STARTUP:
                if (resultCode == RESULT_OK) {
                    // get the robot
                    mRobot = RobotProvider.getDefaultProvider().findRobot(data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID));

                    // register the async data listener
                    DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);

                    // turn rear light on
                    FrontLEDOutputCommand.sendCommand(mRobot, 1.0f);

                    // turn stabilization off
                    StabilizationCommand.sendCommand(mRobot, false);

                    // turn data streaming on for the specific types we want
                    //
                    // Parameters:
                    // (1) mRobot   - the robot from which we want the data
                    // (2) 2        - this is the divisor applied to the maximum data rate of 400Hz coming back from the
                    //                ball we want the data to come back at 200Hz so we use 2 (400Hz/2)
                    // (3) 1        - this is how many sensor 'snapshots' are delivered every time we get a data packet
                    //                from the ball. In this case, we only want 1, but if you don't need to process data
                    //                in real time, you could slow down the data rate and increase the number of
                    //                snapshots returned. The snapshots are always returned in an
                    //                ArrayList<DeviceSensorData> in the order they were created.
                    // (4) mask     - these are the different sensor values we would like to have returned. All of the
                    //                available sensors are listed in SetDataStreamingCommand
                    // (4) 0        - this is the total number of packets we want returned. If you just wanted a small
                    //                window of sensor data from the ball, you could set this to a specific number of
                    //                packets to cover that time period based on the divisor and snapshot count set
                    //                in the previous parameters. You can also set this to 0 for infinite packets. This
                    //                will stream information back to the phone until it is stopped (by sending 0 in the
                    //                divisor parameter) or the ball shuts down.
                    //
                    SetDataStreamingCommand.sendCommand(mRobot, 2, 1,
                            SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_PITCH_ANGLE_FILTERED |
                            SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_ROLL_ANGLE_FILTERED |
                            SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_YAW_ANGLE_FILTERED, 0);
                } else {
                    mRobot = null;
                }
                break;
        }
    }
}
