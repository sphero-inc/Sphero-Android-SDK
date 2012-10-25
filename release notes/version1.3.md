
## New in this version:

#### New API additions.
* **orbBasic** - Adds API support for loading, executing, aborting and erasing orbBasic programs onto Sphero. Sphero has a built in BASIC interperter, and this API provides the framework support. The class additions are:
	* **OrbBasicProgram** - A class to encapsulate an orbBasic program.
	* **OrbBasicAppendFragmentCommand** - Class for sending code fragments to append to the program storage area on Sphero. 
	* **OrbBasicAppendFragmentResponse** - Class for the response from a RKOrbBasicAppendFragmentCommand.
	* **OrbBasicExecuteCommand** - Class for sending an execute command to Sphero.
	* **OrbBasicExecuteResponse** - Class for the response from a RKOrbBasicExecuteCommand.
	* **OrbBasicAbortCommand** - Class to send an abort execution command to Sphero.
	* **OrbBasicAbortResponse** - Class for the response from a RKOrbBasicAbortCommand.
	* **OrbBasicEraseStorageCommand** - Class for sending a command to erase the program storage area.
	* **OrbBasicEraseStorageResponse** - Class for the response from a RKOrbBasicEraseStorageCommand.
	* **OrbBasicPrintMessage** - Class that will contain print message generated while a program is running.
	* **OrbBasicErrorASCII** - Class that contains a execution error message while running a program.
	* **OrbBasicErrorBinary** - Class that contains a binary error 

* Addition of orbotix.view.connection.SpheroConnectionView to RobotLibrary to handle connecting to one or multiple Spheros.
* Added support for turning on power state notifications in the orbotix.robot.base package. Added the classes:
	* **SetPowerNotificaitonCommand** - Class used to send a command to set the power state notifications on. The library will turn this off when the connection is closed.
	* **SetPowerNotificationResponse** - Class to encapsulate the response return from a SetPowerNotificationCommand.
	* **PowerNotificationAsyncData** - Class that encapsulate the power state notifications sent from Sphero.
* Added orbotix.robot.base.SetInactivityTimeoutCommand and orbotix.robot.base.SetInactivityTimeoutResponse that client code to set the sleep timeout on Sphero.
* 
#### New sample code
* **OrbBasicSample** - Sample code that demonstrates the use OrbBasicProgram to load, execute, abort, and erase orbBasic programs.
* **MacroSample** - has new macros: vibrate, flip, and spin. 
* **MacroSample** - now has a multiple ball connection view.
* **StreamingExample** - Added README.md file to project.

## Fixes and such:

#### In the API
* Added getRobot() method to DeviceAsyncData class so client code can determine the robot that sent the message. One use of this would be when streaming sensor data from multiple Spheros. 
* Added new listeners to RobotProvider to completely replace the need to listen for broadcast intents for connection state changes.
* Fixed bug that would not update listeners set in RobotProvider if a broadcast context was not set.


#### In the samples
* Dependency on RobotUILibrary has been removed.



