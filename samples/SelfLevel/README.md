![logo](http://update.orbotix.com/developer/sphero-small.png)



# Self Level

This sample code demonstrates how to connect to a Sphero and perform the self level command.  The self level routine attempts to achieve a horizontal orientation with Sphero.  This is what you see Sphero doing when you place it in a charge with Firmware 1.20 or greater.  It is also useful for controller apps when you want to level the Sphero in a user's hand.

	Note: This command will only work with Sphero Firmware 1.20 or greater

## Default Self Level Command

A basic self level command can be performed by the developer by calling 

	SelfLevelCommand.sendCommand(mRobot);
	
This will make you Sphero attempt to level itself.  You can also provide the RKSelfLevelCommand with settings to customize the self level command to perform perfect for your application.  The settings are as follows:

1. **Angle Limit**: The amount of degrees the roll and pitch angles must be within to be considered level.
2. **Accuracy Time**: The length of time in centiseconds the roll and pitch angles must be within the Angle Limit before it is considered level. 
3. **Timeout**: The length of time in seconds you will allow Sphero to try and self level itself to within the angle limit and accuracy time.

When you do *SelfLevelCommand.sendCommand(mRobot)* the values for these settings it uses are, **Angle Limit** = 2 **Accuracy Time** = 30 (300 milliseconds), **Timeout** = 15.

Additional settings include the options flag.  This flag is a bitwise mask the size of a byte.  The settings include:

1. **Start Bit**: 0 aborts the routine if in progress. 1 starts the routine.
2. **Final Angle**: 0 just stops when it satisfies roll and pitch angles. 1 rotates to heading equal to the heading prior to the self level.
3. **Sleep Bit**: 0 stays awake after leveling. 1 goes to sleep after leveling.
4. **Control System Bit**: 0 turns control system off after leveling. 1 turns control system on after leveling.

When you do *SelfLevelCommand.sendCommand(mRobot)* the values for these settings it uses are, the **Stop Bit** = 1, **Final Angle Bit** = 1, **Sleep Bit** = 0, and **Control System Bit** = 0.

## Customized Self Level Command

To take advantage of the settings described above, you can implement the custom command with the following code:

	SelfLevelCommand.sendCommand(mRobot, options, angleLimit, timeout, accuracy);
	
The options flag is created with the following code:

	RKSelfLevelCommandOptions options = RKSelfLevelCommandOptionStart;
	
To add options to the flag you simply logical or the other options to the options variable.  For example, this code below will set all the bits to 1:

	options = SelfLevelCommand.OPTION_START | SelfLevelCommand.OPTION_KEEP_HEADING |			  SelfLevelCommand.OPTION_SLEEP_AFTER | SelfLevelCommand.OPTION_CONTROL_SYSTEM_ON;
	
## Self Level Async Response Message

When the self level command completes, either successfully or unsuccessfully, it will send an async message on the it status upon completion.  To register to receive async messages, put this code in the handleRobotOnline callback:

	DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);
   
Withing the handleAsyncData callback we print out the async message status code in readable text to a label with the following code:

    private DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {

            if(data instanceof SelfLevelCompleteAsyncData){

                // Cast the self level complete object
                SelfLevelCompleteAsyncData resultCode = (SelfLevelCompleteAsyncData)data;

                // Print result to text view at the top of the screen
                TextView resultTextView = (TextView)SelfLevelActivity.this.findViewById(R.id.txt_self_level);

                // Print result code in human readable format
                switch( resultCode.getResultCode() ) {

                    case SelfLevelCompleteAsyncData.RESULT_CODE_UKNOWN:
                        resultTextView.setText("Unknown Error");
                        break;

                    case SelfLevelCompleteAsyncData.RESULT_CODE_TIMEOUT:
                        resultTextView.setText("Timeout");
                        break;

                    case SelfLevelCompleteAsyncData.RESULT_CODE_SENSORS_ERROR:
                        resultTextView.setText("Sensors Error");
                        break;

                    case SelfLevelCompleteAsyncData.RESULT_CODE_SELF_LEVEL_DISABLED:
                        resultTextView.setText("Self Level Disabled");
                        break;

                    case SelfLevelCompleteAsyncData.RESULT_CODE_ABORTED:
                        resultTextView.setText("Aborted");
                        break;
                        
                    case SelfLevelCompleteAsyncData.RESULT_CODE_CHARGER_NOT_FOUND:
                    	resultTextView.setText("Charger Not Found");
                        break;
                        
                    case SelfLevelCompleteAsyncData.RESULT_CODE_SUCCESS:
                        resultTextView.setText("Success");
                        break;

                }
            }
        }
    };

## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

	 




