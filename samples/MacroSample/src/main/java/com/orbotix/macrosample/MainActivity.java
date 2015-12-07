package com.orbotix.macrosample;

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
import android.widget.SeekBar;
import android.widget.TextView;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.common.RobotChangedStateListener;
import com.orbotix.macro.AbortMacroCommand;
import com.orbotix.macro.MacroObject;
import com.orbotix.macro.cmd.BackLED;
import com.orbotix.macro.cmd.Delay;
import com.orbotix.macro.cmd.Fade;
import com.orbotix.macro.cmd.LoopEnd;
import com.orbotix.macro.cmd.LoopStart;
import com.orbotix.macro.cmd.RGB;
import com.orbotix.macro.cmd.RawMotor;
import com.orbotix.macro.cmd.Roll;
import com.orbotix.macro.cmd.RotateOverTime;
import com.orbotix.macro.cmd.Stabilization;

import java.util.ArrayList;
import java.util.List;

/**
 * Macro sample
 *
 * Create macros in code to send to the robot for execution.
 * Macros can be customized using the seekbars to see the effects on various commands.
 */
public class MainActivity extends Activity implements RobotChangedStateListener,  SeekBar.OnSeekBarChangeListener, AdapterView.OnItemClickListener {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;

    private ConvenienceRobot mRobot;

    private ListView mListView;

    private SeekBar mSpeedSeekBar;
    private SeekBar mDelaySeekBar;
    private SeekBar mLoopsSeekBar;

    private TextView mSpeedText;
    private TextView mDelayText;
    private TextView mLoopText;

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

    private void initViews() {
        mListView = (ListView) findViewById( R.id.list );
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, getResources().getStringArray( R.array.macros ) );
        mListView.setAdapter( adapter );
        mListView.setOnItemClickListener( this );

        mSpeedSeekBar = (SeekBar) findViewById( R.id.seekbar_speed );
        mDelaySeekBar = (SeekBar) findViewById( R.id.seekbar_delay );
        mLoopsSeekBar = (SeekBar) findViewById( R.id.seekbar_loops );

        mDelaySeekBar.setOnSeekBarChangeListener( this );
        mSpeedSeekBar.setOnSeekBarChangeListener( this );
        mLoopsSeekBar.setOnSeekBarChangeListener( this );

        mSpeedText = (TextView) findViewById( R.id.text_speed );
        mDelayText = (TextView) findViewById( R.id.text_delay );
        mLoopText = (TextView) findViewById( R.id.text_loops );

    }

    //Set the robot to a default 'clean' state between running macros
    private void setRobotToDefaultState() {
        if( mRobot == null )
            return;

        mRobot.sendCommand( new AbortMacroCommand() );
        mRobot.setLed( 1.0f, 1.0f, 1.0f );
        mRobot.enableStabilization( true );
        mRobot.setBackLedBrightness( 0.0f );
        mRobot.stop();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch( seekBar.getId() ) {
            case R.id.seekbar_speed: {
                mSpeedText.setText( String.valueOf( progress ) + "%" );
                break;
            }
            case R.id.seekbar_delay: {
                mDelayText.setText( String.valueOf( progress ) );
                break;
            }
            case R.id.seekbar_loops: {
                mLoopText.setText( String.valueOf( progress ) );
                break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //no-op
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //no-op
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

        //Start the selected macro
        String item = adapter.getItemAtPosition( position ).toString();
        if( getString( R.string.macro_color ).equalsIgnoreCase( item ) ) {
            runColorMacro();
        } else if( getString( R.string.macro_square ).equalsIgnoreCase( item ) ) {
            runSquareMacro();
        } else if( getString( R.string.macro_shape ).equalsIgnoreCase( item ) ) {
            runShapeMacro();
        } else if( getString( R.string.macro_figure_8 ).equalsIgnoreCase( item ) ) {
            runFigureEightMacro();
        } else if( getString( R.string.macro_vibrate ).equalsIgnoreCase( item ) ) {
            runVibrateMacro();
        } else if (getString( R.string.macro_spin ).equalsIgnoreCase( item ) ) {
            runSpinMacro();
        } else if( getString( R.string.macro_abort ).equalsIgnoreCase( item ) ) {
            setRobotToDefaultState();
        }
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        switch (type) {
            case Online: {
                //Save the robot as a ConvenienceRobot for additional utility methods
                mRobot = new ConvenienceRobot(robot);
                setRobotToDefaultState();

                break;
            }
        }
    }

    //Fade the robot LED between three colors in a loop
    private void runColorMacro() {
        if( mRobot == null )
            return;

        setRobotToDefaultState();

        MacroObject macro = new MacroObject();

        //Loop as many times as the loop seekbar value
        macro.addCommand( new LoopStart( mLoopsSeekBar.getProgress() ) );

        //Fade to cyan. Duration of the fade is set to the value of the delay seekbar
        macro.addCommand( new Fade( 0, 255, 255, mDelaySeekBar.getProgress() ) );
        //Set a delay so that the next command isn't processed until the previous one is done
        macro.addCommand( new Delay( mDelaySeekBar.getProgress() ) );

        //Fade to magenta. Duration of the fade is set to the value of the delay seekbar
        macro.addCommand( new Fade( 255, 0, 255, mDelaySeekBar.getProgress() ) );
        //Set a delay so that the next command isn't processed until the previous one is done
        macro.addCommand( new Delay( mDelaySeekBar.getProgress() ) );

        //Fade to yellow. Duration of the fade is set to the value of the delay seekbar
        macro.addCommand( new Fade( 255, 255, 0, mDelaySeekBar.getProgress() ) );
        //Set a delay so that the next command isn't processed until the previous one is done
        macro.addCommand( new Delay( mDelaySeekBar.getProgress() ) );

        //End the current loop and go back to LoopStart if more iterations expected
        macro.addCommand( new LoopEnd() );

        //Send the macro to the robot and play
        macro.setMode( MacroObject.MacroObjectMode.Normal );
        mRobot.playMacro( macro );
    }

    //Move the robot in a pattern based on the speed and loop seekbar values
    private void runShapeMacro() {
        //If the looping value is set to null or the robot isn't connected, return
        if( mRobot == null || mLoopsSeekBar.getProgress() == 0 )
            return;

        setRobotToDefaultState();

        MacroObject macro = new MacroObject();
        float speed = mSpeedSeekBar.getProgress() * 0.01f;

        //Set the robot LED color to blue
        macro.addCommand( new RGB( 0, 0, 255, 255 ) );

        //Loop through driving in various directions
        for( int i = 0; i < mLoopsSeekBar.getProgress(); i++ ) {
            macro.addCommand( new Roll( speed, i * ( 360 / mLoopsSeekBar.getProgress() ), 0 ) );
            macro.addCommand( new Delay( mDelaySeekBar.getProgress() ) );
            macro.addCommand( new Roll( 0.0f, i * ( 260 / mLoopsSeekBar.getProgress() ), 255 ) );
        }

        //Stop the robot
        macro.addCommand( new Roll( 0.0f, 0, 255 ) );

        //Send the macro to the robot and play
        macro.setMode( MacroObject.MacroObjectMode.Normal );
        macro.setRobot(mRobot.getRobot());
        macro.playMacro();

    }

    //Move the robot in the shape of a square with each edge being a new color
    private void runSquareMacro() {
        if( mRobot == null )
            return;

        setRobotToDefaultState();
        float speed = mSpeedSeekBar.getProgress() * 0.01f;

        MacroObject macro = new MacroObject();

        //Set the robot LED to green
        macro.addCommand( new RGB( 0, 255, 0, 255 ) );
        //Move the robot forward
        macro.addCommand( new Roll( speed, 0, 0 ) );
        //Wait until the robot should stop moving
        macro.addCommand( new Delay( mDelaySeekBar.getProgress() ) );
        //Stop
        macro.addCommand( new Roll( 0.0f, 0, 255 ) );

        //Set the robot LED to blue
        macro.addCommand( new RGB( 0, 0, 255, 255 ) );
        //Move the robot to the right
        macro.addCommand( new Roll( speed, 90, 0 ) );
        //Wait until the robot should stop moving
        macro.addCommand( new Delay( mDelaySeekBar.getProgress() ) );
        //Stop
        macro.addCommand( new Roll( 0.0f, 90, 255 ) );

        //Set the robot LED to yellow
        macro.addCommand( new RGB( 255, 255, 0, 255 ) );
        //Move the robot backwards
        macro.addCommand( new Roll( speed, 180, 0 ) );
        //Wait until the robot should stop moving
        macro.addCommand( new Delay( mDelaySeekBar.getProgress() ) );
        //Stop
        macro.addCommand( new Roll( 0.0f, 180, 255 ) );

        //Set the robot LED to red
        macro.addCommand( new RGB( 255, 0, 0, 255 ) );
        //Move the robot to the left
        macro.addCommand( new Roll( 255, 270, 0 ) );
        //Wait until the robot should stop moving
        macro.addCommand( new Delay( mDelaySeekBar.getProgress() ) );
        //Stop
        macro.addCommand( new Roll( 0.0f, 270, 255 ) );

        //Send the macro to the robot and play
        macro.setMode( MacroObject.MacroObjectMode.Normal );
        macro.setRobot( mRobot.getRobot() );
        macro.playMacro();

    }

    //Drive the robot in a figure 8 formation
    private void runFigureEightMacro() {
        if( mRobot == null )
            return;

        setRobotToDefaultState();

        float speed = mSpeedSeekBar.getProgress() * 0.01f;

        MacroObject macro = new MacroObject();

        macro.addCommand( new Roll( speed, 0, 1000 ) );
        macro.addCommand( new LoopStart( mLoopsSeekBar.getProgress() ) );
        //Pivot
        macro.addCommand( new RotateOverTime( 360, mDelaySeekBar.getProgress() ) );
        macro.addCommand( new Delay( mDelaySeekBar.getProgress() ) );
        //Pivot
        macro.addCommand( new RotateOverTime( -360, mDelaySeekBar.getProgress() ) );
        macro.addCommand( new Delay( mDelaySeekBar.getProgress() ) );
        macro.addCommand( new LoopEnd() );
        //Stop
        macro.addCommand( new Roll( 0.0f, 0, 255 ) );

        //Send the macro to the robot and play
        macro.setMode( MacroObject.MacroObjectMode.Normal );
        macro.setRobot( mRobot.getRobot() );
        macro.playMacro();
    }

    //Flip the robot forward and backwards while changing the LED color
    private void runVibrateMacro() {
        if( mRobot == null )
            return;

        setRobotToDefaultState();

        MacroObject macro = new MacroObject();

        //Stabilization must be turned off before you can issue motor commands
        macro.addCommand( new Stabilization( false, 0 ) );

        macro.addCommand( new LoopStart( mLoopsSeekBar.getProgress() ) );
        //Change the LED to red
        macro.addCommand( new RGB( 255, 0, 0, 0 ) );
        //Run the robot's motors backwards
        macro.addCommand( new RawMotor( RawMotor.DriveMode.REVERSE, 255, RawMotor.DriveMode.REVERSE, 255, 100 ) );
        macro.addCommand( new Delay( 100 ) );
        //Change the LED to green
        macro.addCommand( new RGB( 0, 255, 0, 0 ) );
        //Run the robot's motors forward
        macro.addCommand( new RawMotor(RawMotor.DriveMode.FORWARD, 255, RawMotor.DriveMode.FORWARD, 255, 100 ) );
        macro.addCommand( new Delay( 100 ) );
        macro.addCommand( new LoopEnd() );

        //Turn stabilization back on
        macro.addCommand( new Stabilization( true, 0 ) );

        //Send the macro to the robot and play
        macro.setMode( MacroObject.MacroObjectMode.Normal );
        macro.setRobot( mRobot.getRobot() );
        macro.playMacro();
    }

    //Spin the robot in circles
    private void runSpinMacro() {
        setRobotToDefaultState();
        float speed = mSpeedSeekBar.getProgress() * 0.01f;

        MacroObject macro = new MacroObject();

        //Set the back LED to full brightness
        macro.addCommand( new BackLED( 255, 0 ) );

        //Loop through rotating the robot
        macro.addCommand( new LoopStart( mLoopsSeekBar.getProgress() ) );
        macro.addCommand( new RotateOverTime( 360, (int) ( 500 * speed ) ) );
        macro.addCommand( new Delay( (int) ( 500 * speed ) ) );
        macro.addCommand( new LoopEnd() );

        //Dim the back LED
        macro.addCommand( new BackLED( 0, 0 ) );

        //Send the macro to the robot and play
        macro.setMode(MacroObject.MacroObjectMode.Normal);
        macro.setRobot(mRobot.getRobot());
        macro.playMacro();
    }
}
