package com.orbotix.orbbasicloader;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.orbbasic.OrbBasicControl;
import com.orbotix.orbbasic.OrbBasicEventListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * Orb Basic Loader sample
 *
 * Loads defined Orb Basic programs from files and runs them on the connected robot
 *
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener, OrbBasicEventListener, RobotChangedStateListener {

    private ConvenienceRobot mRobot;
    private OrbBasicControl mOrbBasicControl;

    private ListView mListView;
    private TextView mDisplayText;

    private Button mAbortBtn;
    private Button mExecuteBtn;
    private Button mEraseBtn;

    private boolean shouldStop = false;

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
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.list);
        mDisplayText = (TextView) findViewById( R.id.text_display );
        mAbortBtn = (Button) findViewById( R.id.btn_abort );
        mExecuteBtn = (Button) findViewById( R.id.btn_execute );
        mEraseBtn = (Button) findViewById( R.id.btn_erase );

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.orb_basic_programs));
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(this);
        mAbortBtn.setOnClickListener(this);
        mExecuteBtn.setOnClickListener(this);
        mEraseBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //If the DiscoveryAgent is not already looking for robots, start discovery.
        if( !DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery(getApplicationContext());
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

        if( mOrbBasicControl != null ) {
            mOrbBasicControl = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(null);
    }

    //Take the name of an Orb Basic program file from the Assets folder and load it onto the robot
    private void setupProgram( String programFileName ) {
        if( mOrbBasicControl == null || TextUtils.isEmpty( programFileName ) )
            return;

        try {
            InputStream in = getAssets().open(programFileName);
            byte[] program = new byte[in.available()];
            in.read(program);

            //Set program stores the program bytes in the OrbBasicControl
            mOrbBasicControl.setProgram( program );

            //Load program copies the program bytes to the robot for execution
            mOrbBasicControl.loadProgram();
        } catch( IOException e ) {
            Log.e( "Sphero", "IO Exception reading program: " + e.getMessage() );
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        if( mOrbBasicControl == null )
            return;

        if( getString(R.string.ob_color_drive ).equalsIgnoreCase(adapter.getItemAtPosition( position ).toString() ) ) {
            setupProgram( "color_drive.orbbas" );
            shouldStop = true;
        } else if( getString( R.string.ob_data ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
            setupProgram( "data_example.orbbas" );
        } else if( getString( R.string.ob_program_error ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
            setupProgram( "program_error.orbbas" );
        } else if( getString( R.string.ob_simple_assignment ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
            setupProgram( "simple_assignment.orbbas" );
        } else if( getString( R.string.ob_syntax_error ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
            setupProgram( "syntax_error.orbbas" );
        } else if( getString( R.string.ob_toss_double_shake ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
            setupProgram( "toss_double-shake.orbbas");
        }
    }

    @Override
    public void onEraseCompleted(boolean success) {
        if( success ) {
            mDisplayText.setText( "All Orb Basic programs erased from robot" );
            shouldStop = false;
        } else {
            mDisplayText.setText( "Orb Basic programs failed to be erased from robot" );
        }
    }

    //Program sent to the robot
    @Override
    public void onLoadProgramComplete(boolean success) {
        if( success ) {
            mDisplayText.setText( "Orb Basic program loaded to robot" );
        } else {
            mDisplayText.setText( "Orb Basic program failed to load to robot" );
        }
    }

    @Override
    public void onPrintMessage(String message) {
        mDisplayText.setText( "Print: " + message );
    }

    @Override
    public void onErrorMessage(String message) {
        //Note, "User abort" isn't really an error, it just notifies you if the user has
        //aborted the program manually
        mDisplayText.setText( "Error Message: " + message );
        if( shouldStop )
            mRobot.stop();
    }

    @Override
    public void onErrorByteArray(byte[] bytes) {

    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online: {
                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot( robot );

                //Create an OrbBasicControl for loading programs onto the robot
                mOrbBasicControl = new OrbBasicControl( robot );
                mOrbBasicControl.addEventListener( this );
                mDisplayText.setText( "Robot connected!" );
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if( mOrbBasicControl == null )
            return;

        switch( v.getId() ) {
            case R.id.btn_abort: {
                //Stop any running programs on the robot
                mOrbBasicControl.abortProgram();
                break;
            }
            case R.id.btn_erase: {
                //Clear the robot's flash memory containing the Orb Basic programs
                mOrbBasicControl.eraseStorage();
                break;
            }
            case R.id.btn_execute: {
                //Start running any Orb Basic programs stored on the robot.
                mDisplayText.setText( "Executing program" );
                mOrbBasicControl.executeProgram();
                break;
            }
        }
    }
}
