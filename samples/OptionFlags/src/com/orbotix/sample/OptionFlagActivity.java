package com.orbotix.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import orbotix.robot.base.*;
import orbotix.view.connection.SpheroConnectionView;
import orbotix.view.connection.SpheroConnectionView.OnRobotConnectionEventListener;

public class OptionFlagActivity extends Activity
{
    /**
     * Sphero Connection View
     */
    private SpheroConnectionView mSpheroConnectionView;

    /**
     * Robot to from which we are streaming
     */
    private Robot mRobot = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		mSpheroConnectionView = (SpheroConnectionView)findViewById(R.id.sphero_connection_view);
		mSpheroConnectionView.setOnRobotConnectionEventListener(new OnRobotConnectionEventListener() {
			@Override
			public void onRobotConnectionFailed(Robot arg0) {}
			@Override
			public void onNonePaired() {}
			
			@Override
			public void onRobotConnected(Robot arg0) {
				mRobot = arg0;
				mSpheroConnectionView.setVisibility(View.GONE);
				
				 // Set the response listener that will process get option flags responses
                DeviceMessenger.getInstance().addResponseListener(mRobot, mResponseListener);

                // Get the current option flags
                GetOptionFlagsCommand.sendCommand(mRobot);
			}
			
			@Override
			public void onBluetoothNotEnabled() {
				// See UISample Sample on how to show BT settings screen, for now just notify user
				Toast.makeText(OptionFlagActivity.this, "Bluetooth Not Enabled", Toast.LENGTH_LONG).show();
			}
		});
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Disconnect Robots on stop
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    /**
     * When the set option flags button is pressed
     * @param v
     */
    public void setOptionFlagsPressed(View v) {
        if( mRobot == null ) return;

        long optionFlags = 0;

        // Logical OR the bit for prevent Sphero from sleep when placed in charger
        // You would use this to maximize battery life for displaying notifications, visualizations, etc.
        if( ((CheckBox)findViewById(R.id.checkbox_bit0)).isChecked() )
            optionFlags |= GetOptionFlagsResponse.OPTION_FLAGS_PREVENT_SLEEP_IN_CHARGER;

        // Logical OR the bit for enabling vector drive
        if( ((CheckBox)findViewById(R.id.checkbox_bit1)).isChecked() )
            optionFlags |= GetOptionFlagsResponse.OPTION_FLAGS_ENABLE_VECTORE_DRIVE;

        // Logical OR the bit for disabling self levels when you place Sphero in the charger
        if( ((CheckBox)findViewById(R.id.checkbox_bit2)).isChecked() )
            optionFlags |= GetOptionFlagsResponse.OPTION_FLAGS_DISABLE_SELF_LEVEL_IN_CHARGER;

        // Logical OR the bit for keeping the tail light of Sphero always on
        // This is helpful when demonstrating new users how to use Sphero.  They will always know which way it faces.
        if( ((CheckBox)findViewById(R.id.checkbox_bit3)).isChecked() )
            optionFlags |= GetOptionFlagsResponse.OPTION_FLAGS_TAIL_LIGHT_ALWAYS_ON;

        // Logical OR the bit for enabling motion timeout
        // This is useful so if your app crashes or you go out of bluetooth range, then the ball will stop
        // rolling after the roll timeout
        if( ((CheckBox)findViewById(R.id.checkbox_bit4)).isChecked() )
            optionFlags |= GetOptionFlagsResponse.OPTION_FLAGS_ENABLE_MOTION_TIMOUT;

        // Send set options command
        SetOptionFlagsCommand.sendCommand(mRobot, optionFlags);
    }

    /**
     * When the restore defaults button is pressed
     * @param v
     */
    public void restoreDefaultOptionFlagsPressed(View v) {
        SetOptionFlagsCommand.sendDefaultOptionFlagsCommand(mRobot);
    }

    /**
     * When the refresh option flags button is pressed
     * @param v
     */
    public void refreshOptionFlagsPressed(View v) {
       GetOptionFlagsCommand.sendCommand(mRobot);
    }

    /**
     * DeviceResponseListener that will be assigned to the DeviceMessager.
     * Listens for a response of type get option flags, and prints the results
     */
    private DeviceMessenger.DeviceResponseListener mResponseListener = new DeviceMessenger.DeviceResponseListener() {
        @Override
        public void onResponse(DeviceResponse response) {

            if(response instanceof GetOptionFlagsResponse){

                // Cast the get option flags response object
                GetOptionFlagsResponse getOptionFlagsResponse = (GetOptionFlagsResponse)response;

                // Grab the information of whether the option flags are set or not
                boolean preventSleepInChargerSet = getOptionFlagsResponse.isOptionFlagSet(
                        GetOptionFlagsResponse.OPTION_FLAGS_PREVENT_SLEEP_IN_CHARGER);
                boolean enableVectorDriveSet = getOptionFlagsResponse.isOptionFlagSet(
                        GetOptionFlagsResponse.OPTION_FLAGS_ENABLE_VECTORE_DRIVE);
                boolean diasbleSelfLevelInChargerSet = getOptionFlagsResponse.isOptionFlagSet(
                        GetOptionFlagsResponse.OPTION_FLAGS_DISABLE_SELF_LEVEL_IN_CHARGER);
                boolean tailLightAlwaysOn = getOptionFlagsResponse.isOptionFlagSet(
                        GetOptionFlagsResponse.OPTION_FLAGS_TAIL_LIGHT_ALWAYS_ON);
                boolean enableMotionTimeout = getOptionFlagsResponse.isOptionFlagSet(
                        GetOptionFlagsResponse.OPTION_FLAGS_ENABLE_MOTION_TIMOUT);

                // Grab the descriptions of the flags from the string.xml file and append their values
                ((TextView)findViewById(R.id.txt_bit0)).setText(
                        OptionFlagActivity.this.getString(R.string.bit0_description) + " " +
                        new Boolean(preventSleepInChargerSet).toString());
                ((TextView)findViewById(R.id.txt_bit1)).setText(
                        OptionFlagActivity.this.getString(R.string.bit1_description) + " " +
                        (new Boolean(enableVectorDriveSet)).toString());
                ((TextView)findViewById(R.id.txt_bit2)).setText(
                        OptionFlagActivity.this.getString(R.string.bit2_description) + " " +
                        (new Boolean(diasbleSelfLevelInChargerSet)).toString());
                ((TextView)findViewById(R.id.txt_bit3)).setText(
                        OptionFlagActivity.this.getString(R.string.bit3_description) + " " +
                        (new Boolean(tailLightAlwaysOn)).toString());
                ((TextView)findViewById(R.id.txt_bit4)).setText(
                        OptionFlagActivity.this.getString(R.string.bit4_description) + " " +
                        (new Boolean(enableMotionTimeout)).toString());
            }
        }
    };
}
