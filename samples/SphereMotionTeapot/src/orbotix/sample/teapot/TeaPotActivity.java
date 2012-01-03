package orbotix.sample.teapot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.*;

public class TeaPotActivity extends Activity {
    
    public static final String TAG = "Orbotix";
    public static final boolean DEBUG = true;
    
    private static final int REQUEST_STARTUP = 101;
    
    private Robot mRobot;
    
    private final DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {
            log("Data recieved");
        }
    };
    
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
    
    public static void log(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // stop the streaming data when we leave
        SetDataStreamingCommand.sendCommand(mRobot, 0, 0,
                SetDataStreamingCommand.DATA_STREAMING_MASK_OFF, 0);

        // unregister the async data listener to prevent a memory leak.
        DeviceMessenger.getInstance().removeAsyncDataListener(mRobot, mDataListener);

        // disconnect from the ball
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        switch (requestCode) {
            case REQUEST_STARTUP:
                if (resultCode == RESULT_OK) {
                    mRobot = RobotProvider.getDefaultProvider().findRobot(data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID));
                    DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);
                        log("Sending SetDataStreamingCommand");
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
