package com.orbotix.sample.optionflags;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import orbotix.robot.base.*;
import orbotix.robot.internal.DeviceResponse;
import orbotix.sphero.ConfigurationControl;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.PersistentOptionFlags;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

public class OptionFlagActivity extends Activity {
    /** Sphero Connection View */
    private SpheroConnectionView mSpheroConnectionView;

    /** Robot to from which we are streaming */
    private Sphero mSphero = null;

    private CheckBox cbPreventSleepInCharger;
    private CheckBox cbEnableVectorDrive;
    private CheckBox cbDisableSelfLevelInCharger;
    private CheckBox cbEnableTailPersistent;
    private CheckBox cbEnableMotionTimeout;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
        mSpheroConnectionView.addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(Robot sphero) {
                mSphero = (Sphero) sphero;
                updateUI();
                DeviceMessenger.getInstance().addResponseListener(mSphero, mResponseListener);
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                // let the SpheroConnectionView handle or hide it and do something here...
            }

            @Override
            public void onDisconnected(Robot sphero) {
                mSpheroConnectionView.startDiscovery();
            }
        });

        cbPreventSleepInCharger = (CheckBox) findViewById(R.id.checkbox_bit0);
        cbEnableVectorDrive = ((CheckBox) findViewById(R.id.checkbox_bit1));
        cbDisableSelfLevelInCharger = (CheckBox) findViewById(R.id.checkbox_bit2);
        cbEnableTailPersistent = ((CheckBox) findViewById(R.id.checkbox_bit3));
        cbEnableMotionTimeout = ((CheckBox) findViewById(R.id.checkbox_bit4));
    }

    /** Called when the user comes back to this app */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list of Spheros
        mSpheroConnectionView.startDiscovery();
    }

    /** Called when the user presses the back or home button */
    @Override
    protected void onPause() {
        super.onPause();
        // Disconnect Robot properly
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    /**
     * When the set option flags button is pressed
     *
     * @param v
     */
    public void setOptionFlagsPressed(View v) {
        if (mSphero == null) return;

        ConfigurationControl cc = mSphero.getConfiguration();

        // Logical OR the bit for prevent Sphero from sleep when placed in charger
        // You would use this to maximize battery life for displaying notifications, visualizations, etc.

        cc.setPersistentFlag(PersistentOptionFlags.PreventSleepInCharger, (cbPreventSleepInCharger.isChecked()));
        cc.setPersistentFlag(PersistentOptionFlags.EnableVectorDrive, cbEnableVectorDrive.isChecked());
        cc.setPersistentFlag(PersistentOptionFlags.DisableSelfLevelInCharger, (cbPreventSleepInCharger.isChecked()));
        cc.setPersistentFlag(PersistentOptionFlags.EnablePersistentTailLight, cbEnableTailPersistent.isChecked());

        // This is useful so if your app crashes or you go out of bluetooth range, then the ball will stop
        // rolling after the roll timeout
        cc.setPersistentFlag(PersistentOptionFlags.EnableMotionTimeout, cbEnableMotionTimeout.isChecked());
    }

    /**
     * When the restore defaults button is pressed
     *
     * @param v
     */
    public void restoreDefaultOptionFlagsPressed(View v) {
        SetOptionFlagsCommand.sendDefaultOptionFlagsCommand(mSphero);
    }

    /**
     * When the refresh option flags button is pressed
     *
     * @param v
     */
    public void refreshOptionFlagsPressed(View v) {
//        mSphero.getConfiguration().update();
        GetOptionFlagsCommand.sendCommand(mSphero);
    }

    public void updateUI() {
        ConfigurationControl cc = mSphero.getConfiguration();
        Log.d("OBX-demo", cc.toString());
        Boolean preventSleepInChargerSet = cc.isPersistentFlagEnabled(PersistentOptionFlags.PreventSleepInCharger);
        Boolean enableVectorDriveSet = cc.isPersistentFlagEnabled(PersistentOptionFlags.EnableVectorDrive);
        Boolean diasbleSelfLevelInChargerSet = cc.isPersistentFlagEnabled(PersistentOptionFlags.DisableSelfLevelInCharger);
        Boolean tailLightAlwaysOn = cc.isPersistentFlagEnabled(PersistentOptionFlags.EnablePersistentTailLight);
        Boolean enableMotionTimeout = cc.isPersistentFlagEnabled(PersistentOptionFlags.EnableMotionTimeout);

        // update checkboxes
        cbEnableMotionTimeout.setChecked(enableMotionTimeout);
        cbEnableTailPersistent.setChecked(tailLightAlwaysOn);
        cbPreventSleepInCharger.setChecked(preventSleepInChargerSet);
        cbDisableSelfLevelInCharger.setChecked(diasbleSelfLevelInChargerSet);
        cbEnableVectorDrive.setChecked(enableVectorDriveSet);
    }

    /**
     * DeviceResponseListener that will be assigned to the DeviceMessager.
     * Listens for a response of type get option flags, and prints the results
     */
    private DeviceMessenger.DeviceResponseListener mResponseListener = new DeviceMessenger.DeviceResponseListener() {
        @Override
        public void onResponse(DeviceResponse response) {
            if (response instanceof GetOptionFlagsResponse) {
                updateUI();
            }
        }
    };
}
