package com.orbotix.macroloader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Macro Loader sample
 *
 * Loads defined macros from files and runs them on the connected robot
 *
 */
public class MainActivity extends Activity implements RobotChangedStateListener, AdapterView.OnItemClickListener, ResponseListener {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;

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
        mListView = (ListView) findViewById( R.id.list );
        mListView.setOnItemClickListener(this);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray( R.array.macros ) );
        mListView.setAdapter(adapter);
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
