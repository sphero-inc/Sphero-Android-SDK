#HelloWorld

This sample code demonstrates how to connect to a Sphero and blink it's RGB LED blue. Also, the code sample is the bases for the application created in the Quick Start Guide. 

The connection is made by the StartupActivity which is include in the RobotUILibrary. The StartupActivity is launch in the HelloWorldActivity's ``onStart()`` method, and the `onActivityResult()` is used to capture a Robot object that is used to reference the connected Sphero. The robot's unique identifier is returned in the StartupActivity's results Intent data, and is retrieved with the following code line.

	            final String robot_id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);

Next, you need to get a reference to a Robot object from RobotProvider using the following line of code.

                mRobot = RobotProvider.getDefaultProvider().findRobot(robot_id);

RobotProvider is a singleton that is used to manage connected Spheros, and is capable of allowing multiple connections. So, the Robot object is how you reference which Sphero to communicate with. 

The final call in `onActivityResult()` is to ``blink()``, which is method that toggles the RGB LED's blue component on or off every second. The LED is controlled by using the RGBLEDOutputCommand class to message the device. This is a subclass of DeviceCommand which is used to encapsulate commands, and posted to DeviceMessenger. DeviceCommand subclasses have `sendCommand()` class methods that post the command to DeviceMessenger for convenience. The blink method commands the device to turn off the LED with the following code line.

                RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 0);

Where `mRobot` is the reference to the Robot object that the API uses determine which device to send a command to, and the last arguments set the brightness of red, green, and blue components of the LED. In this case, all color components are turned off. The method sets the blue LED component to full brightness with the following code line.

                RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 255);

It finally uses a `Handler` object to call it self after a 1 second delayed with the lit boolean parameter toggled, thus toggling the blue LED component on and off.

When the application closes the connection is closed by the following line in the `onStop()` method.

        RobotProvider.getDefaultProvider().removeAllControls();
 



