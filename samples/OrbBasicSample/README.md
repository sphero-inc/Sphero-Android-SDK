![logo](http://update.orbotix.com/developer/sphero-small.png)

# OrbBasic Sample

This sample demonstrates how to load and execute OrbBasic programs on Sphero.  OrbBasic, like the macro executive, is another powerful resource that Sphero provides for autonomous behavior and integration with smart phone applications. It allows your to run a set of instructions on the ball which react to Sphero's sensors, without having to stream the data to the smartphone first.

## OrbBasic Programs

There are a few example programs in this sample.  Before you look at erasing the storage, loading or executing, or the rest of the sample code.  Take a look at the OrbBasic programs that reside in the `/res/raw/` directory.

The sample programmatically loads the .orbbas files in this directory into the ListView.

## Initializing an OrbBasic Program Object

In the sample, when you click an item in the ListView, you will initialize the *OrbBasicProgram* object.  This is the first step in putting an OrbBasic program on the ball.  At this point, you are only providing the object with the byte array of data, or the text file content.  Here is the code:

	// Retrieve byte array from file
	Resources res = getResources();
	
	InputStream in_s = res.openRawResource(mOrbBasicProgramResource);
	byte[] program = new byte[in_s.available()];
	in_s.read(program);
	
	// Create the OrbBasic Program object
	mOrbBasicProgram = new OrbBasicProgram(program);
	mOrbBasicProgram.setRobot(mRobot);
	
	// Set the listener for the OrbBasic Program Events
	mOrbBasicProgram.setOrbBasicProgramEventListener(new OrbBasicProgram.OrbBasicProgramEventListener() {
	    @Override
	    public void onEraseCompleted(boolean success) {
	        String successStr = (success) ? "Success":"Failure";
	        addMessageToStatus("Done Erasing: " + successStr);
	    }
	
	    @Override
	    public void onLoadProgramComplete(boolean success) {
	        String successStr = (success) ? "Success":"Failure";
	        addMessageToStatus("Done Loading: " + successStr);
	    }
	});
                    
The event listener notifies you when an erase storage command or a load program command is finished.  

## Erasing Storage

The OrbBasic program exists on temporary storage in memory.  In most instances, you will need to make room for your program, by erasing the current storage.  You do this with the  `eraseStorage()` function.  

You can listen to the event listener to see if the storage was successfully erased.

## Loading the Program

Before you can execute the OrbBasic program, you must first load the program on to the robot. You can do this with the `loadProgram()` function.  

After you have received a successful load program event from the event listener, you may execute the program.

## Executing the Program

You can run the program with the `executeProgram()` function.  Depending on the content of your program, you may need set up an AsyncDataListener to receive messages from the program.

The AsyncDataListener set up is the same as the StreamingExample, Locator Sample, and a few others in our SDK.

In the *onRobotConnected* event of the *SpheroConnectionView* you should set up AsyncDataListening when you connect to a robot.

			@Override
			public void onRobotConnected(Robot arg0) {
				mRobot = arg0;
				mSpheroConnectionView.setVisibility(View.GONE);

				// Set the AsyncDataListener that will process print and error messages
				DeviceMessenger.getInstance().addAsyncDataListener(mRobot, mDataListener);
			}
			
The AsyncDataListener should be responsible for handling print and error messages.  You can print ASCII or Binary error messages.  For the point of the sample, we only deal with ASCII messages.  The code is as follows:

    private DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {

            if(data instanceof OrbBasicPrintMessageAsyncData){
                OrbBasicPrintMessageAsyncData printMessage = (OrbBasicPrintMessageAsyncData)data;
                addMessageToStatus(printMessage.getMessage());
            }
            else if(data instanceof OrbBasicErrorASCIIAsyncData ){
                OrbBasicErrorASCIIAsyncData errorMessageASCII = (OrbBasicErrorASCIIAsyncData)data;
                addMessageToStatus("Error:" + errorMessageASCII.getErrorASCII());
            }
            else if(data instanceof OrbBasicErrorBinaryAsyncData ) {
                //OrbBasicErrorBinaryAsyncData errorMessageBinary = (OrbBasicErrorBinaryAsyncData)data;
            }
        }
    };

## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

