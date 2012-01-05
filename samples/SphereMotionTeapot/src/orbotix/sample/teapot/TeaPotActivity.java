package orbotix.sample.teapot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.TextView;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.*;
import orbotix.robot.sensor.DeviceSensorData;

import java.util.ArrayList;

public class TeaPotActivity extends Activity {
    
    public static final String TAG = "Teapot";
    public static final boolean DEBUG = true;

    private MyGLSurfaceView mGLSurfaceView;
    private int sensorMode;
    
    private static final int REQUEST_STARTUP = 101;
    
    private Robot mRobot;
    private PowerManager.WakeLock screenWakeLock;
    
    private TextView pitchText, rollText, yawText, pitchRawText, rollRawText, yawRawText;
    
    private final DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {
            if (data instanceof DeviceSensorsAsyncData) {
                //log("Data recieved");
                ArrayList<DeviceSensorData> dataList = ((DeviceSensorsAsyncData)data).getAsyncData();
                byte[] rawData = ((DeviceSensorsAsyncData)data).getRawData();
                int frame = 0, dataPoint = 0;
                /*for (DeviceSensorData sensorData : dataList) {
                    pitchText.setText(Double.toString(sensorData.getmAttitudeData().getAttitudeSensor().pitch));
                    pitchRawText.setText(toBinaryString(rawData[(frame * 3)]) + " " + toBinaryString(rawData[(frame * 3) + 1]));
                    rollText.setText(Double.toString(sensorData.getmAttitudeData().getAttitudeSensor().roll));
                    rollRawText.setText(toBinaryString(rawData[(frame * 3) + 2]) + " " + toBinaryString(rawData[(frame * 3) + 3]));
                    yawText.setText(Double.toString(sensorData.getmAttitudeData().getAttitudeSensor().yaw));
                    yawRawText.setText(toBinaryString(rawData[(frame * 3) + 4]) + " " + toBinaryString(rawData[(frame * 3) + 5]));
                    frame++;
                }*/

                float[] sensorData = new float[3];
                DeviceSensorData ballData = dataList.get(0);
                sensorData[0] = (float)ballData.getmAttitudeData().getAttitudeSensor().pitch;
                sensorData[1] = (float)ballData.getmAttitudeData().getAttitudeSensor().roll;
                sensorData[2] = (float)ballData.getmAttitudeData().getAttitudeSensor().yaw;
                mGLSurfaceView.onSensorChanged(sensorData);
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
        /*setContentView(R.layout.main);
        pitchText = (TextView)findViewById(R.id.pitch);
        pitchRawText = (TextView)findViewById(R.id.pitch_raw);
        rollText = (TextView)findViewById(R.id.roll);
        rollRawText = (TextView)findViewById(R.id.roll_raw);
        yawText = (TextView)findViewById(R.id.yaw);
        yawRawText = (TextView)findViewById(R.id.yaw_raw);*/
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

    /**
     * Convenience logging method. This will always log with the default TAG if the DEBUG flag is set to true;
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

    public static double getPositiveAngleDegrees(double angleIn) {
        if (angleIn < 0.0) {
            return getPositiveAngleDegrees(angleIn + 360.0);
        } else if (angleIn > 360.0) {
            return getPositiveAngleDegrees(angleIn - 360.0);
        } else {
            return angleIn;
        }
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
                    SetDataStreamingCommand.sendCommand(mRobot, 1, 1,
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
