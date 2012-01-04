package orbotix.sample.teapot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.*;
import orbotix.robot.sensor.DeviceSensorData;

import java.util.ArrayList;

public class TeaPotActivity extends Activity {
    
    public static final String TAG = "Orbotix";
    public static final boolean DEBUG = true;
    
    private static final int REQUEST_STARTUP = 101;
    
    private Robot mRobot;
    
    private final DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {
            if (data instanceof DeviceSensorsAsyncData) {
                //log("Data recieved");
                ArrayList<DeviceSensorData> dataList = ((DeviceSensorsAsyncData)data).getAsyncData();
                for (DeviceSensorData sensorData : dataList) {
                    log("Acelerometer x: " + sensorData.getmAccelerometerData().getFilteredAcceleration().x);
                    log("Acelerometer y: " + sensorData.getmAccelerometerData().getFilteredAcceleration().y);
                    log("Acelerometer z: " + sensorData.getmAccelerometerData().getFilteredAcceleration().z);
                }
            }
        }
    };

    public static String toBinaryString(byte n) {
        StringBuilder sb = new StringBuilder("00000000");
        for (int bit = 0; bit < 8; bit++) {
            if (((n >> bit) & 1) > 0) {
                sb.setCharAt(7 - bit, '1');
            }
        }
        return sb.toString();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent startupIntent = new Intent(this, StartupActivity.class);
        startActivityForResult(startupIntent, REQUEST_STARTUP);
    }

    /**
     * Convenience logging method. This will always log with the default Orbotix tag if the DEBUG flag is set to true;
     * @param message the message to be displayed in the log
     */
    public static void log(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

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
                    // in this case the accelerometer for all three axes.
                    SetDataStreamingCommand.sendCommand(mRobot, 5, 5,
                            SetDataStreamingCommand.DATA_STREAMING_MASK_ACCELEROMETER_X_FILTERED |
                            SetDataStreamingCommand.DATA_STREAMING_MASK_ACCELEROMETER_Y_FILTERED |
                            SetDataStreamingCommand.DATA_STREAMING_MASK_ACCELEROMETER_Z_FILTERED, 0);
                } else {
                    mRobot = null;
                }
                break;
        }
    }
}
