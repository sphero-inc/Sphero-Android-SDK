package orbotix.sample.teapot;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import orbotix.robot.base.*;
import orbotix.robot.sensor.DeviceSensorsData;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.SensorFlag;
import orbotix.sphero.SensorListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

public class TeaPotActivity extends Activity {

    /** Sphero Connection View */
    private SpheroConnectionView mSpheroConnectionView;

    /** Robot */
    private Sphero mRobot;

    /** Teapot Surface Code */
    private MyGLSurfaceView mGLSurfaceView;

    private final SensorListener mDataListener = new SensorListener() {
        @Override
        public void sensorUpdated(DeviceSensorsData ballData) {
            float[] sensorData = new float[3];
            sensorData[0] = (float) ballData.getAttitudeData().pitch;
            sensorData[1] = (float) ballData.getAttitudeData().roll;
            sensorData[2] = (float) ballData.getAttitudeData().yaw;
            mGLSurfaceView.onSensorChanged(sensorData);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLSurfaceView = new MyGLSurfaceView(this);
        mGLSurfaceView.setRenderer(new TeapotRenderer());
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Add GL Surface, but hide it until Sphero is connected
        FrameLayout layout = (FrameLayout) findViewById(R.id.teapot_layout);
        layout.addView(mGLSurfaceView);
        mGLSurfaceView.setVisibility(View.GONE);

        // Find Sphero Connection View from layout file - autostarts discovery (startDiscovery())
        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
        // This event listener will notify you when these events occur, it is up to you what you want to do during them
        mSpheroConnectionView.addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(Robot sphero) {
                // Set the robot
                mRobot = (Sphero) sphero;
                // Hide the connection view. Comment this code if you want to connect to multiple robots
                mSpheroConnectionView.setVisibility(View.INVISIBLE);
                mGLSurfaceView.setVisibility(View.VISIBLE);

                // Calling Stream Data Command right after the robot connects, will not work
                // You need to wait a second for the robot to initialize
                // turn rear light on
                mRobot.setBackLEDBrightness(1.0f);
                // turn stabilization off
                mRobot.enableStabilization(false);
                // register the async data listener
                mRobot.getSensorControl().setRate(60);
                mRobot.getSensorControl().addSensorListener(mDataListener, SensorFlag.ATTITUDE);
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                // let the SpheroConnectionView handle or hide it and do something here...
            }

            @Override
            public void onDisconnected(Robot sphero) {
                mSpheroConnectionView.startDiscovery();
                mGLSurfaceView.setVisibility(View.INVISIBLE);
            }
        });
    }

    /** Called when the user comes back to this app */
    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
        // Refresh list of Spheros
        mSpheroConnectionView.startDiscovery();
    }

    /** Called when the user presses the back or home button */
    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
        // unregister the async data listener to prevent a memory leak.
        if(mRobot !=null){
            mRobot.disconnect();
        }
    }
}
