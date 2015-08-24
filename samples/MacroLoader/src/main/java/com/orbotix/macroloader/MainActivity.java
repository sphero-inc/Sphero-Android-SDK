package com.orbotix.macroloader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.ResponseListener;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.common.internal.AsyncMessage;
import com.orbotix.common.internal.DeviceResponse;
import com.orbotix.macro.AbortMacroCommand;
import com.orbotix.macro.MacroObject;
import com.orbotix.macro.cmd.MacroCommandCreationException;

import java.io.IOException;

/**
 * Macro Loader sample
 *
 * Loads defined macros from files and runs them on the connected robot
 *
 */
public class MainActivity extends Activity implements RobotChangedStateListener, AdapterView.OnItemClickListener, ResponseListener {

    private ConvenienceRobot mRobot;

    private ListView mListView;
    private MacroObject mMacro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
            Associate a listener for robot state changes with the DualStackDiscoveryAgent.
            DualStackDiscoveryAgent checks for both Bluetooth Classic and Bluetooth LE.
            DiscoveryAgentClassic checks only for Bluetooth Classic robots.
            DiscoveryAgentLE checks only for Bluetooth LE robots.
        */
        DualStackDiscoveryAgent.getInstance().addRobotStateListener( this );

        initViews();
    }

    private void initViews() {
        mListView = (ListView) findViewById( R.id.list );
        mListView.setOnItemClickListener(this);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray( R.array.macros ) );
        mListView.setAdapter(adapter);
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
                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot(robot);
                break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        //If the robot is null, return since it's probably not connected
        if( mRobot == null )
            return;

        try {
            //Create a MacroObject from a macro file stored in the assets folder
            if( getString( R.string.macro_strobe ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
                mMacro = FileManager.getMacro( getAssets().open("strobelight.sphero" ) );
            } else if( getString( R.string.macro_rainbow ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
                mMacro = FileManager.getMacro( getAssets().open( "colorm.sphero" ) );
            } else if( getString( R.string.macro_clover ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
                mMacro = FileManager.getMacro( getAssets().open( "symboll.sphero" ) );
            } else if( getString( R.string.macro_small_dance ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
                mMacro = FileManager.getMacro( getAssets().open( "dance1.sphero" ) );
            } else if( getString( R.string.macro_medium_square ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
                mMacro = FileManager.getMacro( getAssets().open( "squaremedium.sphero" ) );
            } else if( getString( R.string.macro_large_dance ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
                mMacro = FileManager.getMacro( getAssets().open( "bigdance.sphero" ) );
            } else if( getString( R.string.macro_stop ).equalsIgnoreCase( adapter.getItemAtPosition( position ).toString() ) ) {
                //Stop any macros that are currently running, and turn robot stabalization back on
                if( mMacro != null ) {
                    mMacro.stopMacro();
                    mRobot.enableStabilization( true );
                    mMacro = null;
                }
            } else {
                mMacro = null;
            }
        } catch( IOException e ) {
            Log.e( "Sphero", "IOException: " + e.getMessage() );
        } catch( MacroCommandCreationException e ) {
            Log.e( "Sphero", "MacroCommandCreationException: " + e.getMessage() );
        }

        if( mMacro != null ) {
            //Abort any previously running macros
            mRobot.sendCommand(new AbortMacroCommand());

            /*
                Send the macro in one chunk with a max size of 250 bytes.
                Larger macros can be sent with a mode of Chunky. Limited to 1024 bytes total.
             */
            mMacro.setMode(MacroObject.MacroObjectMode.Normal);

            //Associate a robot with the macro object
            mMacro.setRobot(mRobot.getRobot());

            //Start the macro
            mMacro.playMacro();
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

    }
}
