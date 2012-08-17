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
	
To get the current state of the option flags, you must register a response callback when *StartupActivity* returns.  Then, you call the command to request the option flags.  This is done with the following code:

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            if(requestCode == sStartupActivity){

                //Get the Robot from the StartupActivity
                String id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);
                mRobot = RobotProvider.getDefaultProvider().findRobot(id);

                // Set the response listener that will process get option flags responses
                DeviceMessenger.getInstance().addResponseListener(mRobot, mResponseListener);

                // Get the current option flags
                GetOptionFlagsCommand.sendCommand(mRobot);
            }
        }
    }
	
Now you will be notified by the handleResponse method when you receive a response from Sphero.  Retreive the option flags from the following callback:

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
                        
           }
     }
	
Here we are getting the state of each option flag in a boolean variable which will be either false (off) or true (on).

## Set Option Flags

You can change the value of the option flags by sending a bitwise mask to Sphero with the *SetOptionFlagsCommand.sendCommand(mRobot, optionFlags);*.  This is done in code by:

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
    
    // Send command to set option flags
    SetOptionFlagsCommand.sendCommand(mRobot, optionFlags);
    
Here we set the value of each flag to the value of a UI CheckBox.  We then send the command to ball to set the option flags.
## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

	  