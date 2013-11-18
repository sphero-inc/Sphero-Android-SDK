![logo](http://update.orbotix.com/developer/sphero-small.png)

# Option Flags

	Note: These commands only work on Sphero's with Firmware 1.20 or greater 

Option flags are settings that stay with Sphero even after it goes through power cycles.  There are currently 5 different settings you can change.  These are:

1.  Prevent Sleep In Charger
2.  Enable Vector Drive
3.  Disable Self Level In Charger
4.  Tail Light Always On
5.  Enable Motion Timeout

By default, your Sphero only has vector drive enabled.  

	Warning: If you are going to change any of these settings, please make sure to set them back to the default upon your app closing
	
## Get Current Option Flags


To get the populate the current state of the persistent option flags, you now need only call `update()` on the *ConfigurationControl* of Sphero.

	mRobot.getConfiguration().update();
	
This will get the flags as soon as it can. To avoid a race condition, a listener for the response is needed. Setup a listener to listen for the *GetOptionFlagsResponse*.

	private DeviceMessenger.DeviceResponseListener mResponseListener = new DeviceMessenger.DeviceResponseListener() {
        @Override
        public void onResponse(DeviceResponse response) {
            if (response instanceof GetOptionFlagsResponse) {
                // Do something with the populated flags
            }
        }
    };

Once the flags are populated, get a flag's state by using the `isPersistentFlagEnabled()` method like so:

	// This gets the "prevent sleep in charger" flag, and returns true if it's enabled, saving it to the boolean. 
	Boolean preventSleepInChargerSet = mRobot.getConfiguration().isPersistentFlagEnabled(PersistentOptionFlags.PreventSleepInCharger);
	
Here we are getting the state of each option flag in a boolean variable which will be either false (off) or true (on).

## Set Option Flags

You can change the value of the option flags by using *ConfigurationControl*'s method `setPersistentFlag(PersistentOptionFlags flags, bool enabled)` like so:

	mRobot.getConfiguration().setPersistentFlag(PersistentOptionFlags.PreventSleepInCharger, (cbPreventSleepInCharger.isChecked()));
	mRobot.getConfiguration().setPersistentFlag(PersistentOptionFlags.EnableVectorDrive, cbEnableVectorDrive.isChecked());
   	mRobot.getConfiguration().setPersistentFlag(PersistentOptionFlags.DisableSelfLevelInCharger, (cbPreventSleepInCharger.isChecked()));
    mRobot.getConfiguration().setPersistentFlag(PersistentOptionFlags.EnablePersistentTailLight, cbEnableTailPersistent.isChecked());
    
    
Here we set the value of each flag to the value of a UI CheckBox.  We then send the command to ball to set the option flags.
## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

	  