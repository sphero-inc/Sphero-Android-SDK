#HelloWorld

This sample code demonstrates how to connect to a Sphero and blink it's RGB LED blue. This sample does not use the `SpheroConnectionView`, because we wanted it to be simple and pair to the first Sphero device that it finds paired to the phone.  This sample is a good starting point if you want to make your own custom Connection View. Also, the code sample is the basis for the application created in the Quick Start Guide. 

The connection is made in your Activity by simply adding the following onResume() and wiring the `connected()` method. 

		protected void onResume() {
        super.onResume();

        RobotProvider.getDefaultProvider().addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnected(Robot robot) {
            	// Save the robot
                mRobot = (Sphero) robot;
                // Start the connected method
                HelloWorldActivity.this.connected();
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                Log.d(TAG, "Connection Failed: " + sphero);
                Toast.makeText(HelloWorldActivity.this, "Sphero Connection Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnected(Robot robot) {
                Log.d(TAG, "Disconnected: " + robot);
                Toast.makeText(HelloWorldActivity.this, "Sphero Disconnected", Toast.LENGTH_SHORT).show();
                HelloWorldActivity.this.stopBlink();
                mRobot = null;
            }
        });

        RobotProvider.getDefaultProvider().addDiscoveryListener(new DiscoveryListener() {
            @Override
            public void onBluetoothDisabled() {
                Log.d(TAG, "Bluetooth Disabled");
                Toast.makeText(HelloWorldActivity.this, "Bluetooth Disabled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void discoveryComplete(List<Sphero> spheros) {
                Log.d(TAG, "Found " + spheros.size() + " robots");
            }

            @Override
            public void onFound(List<Sphero> sphero) {
                Log.d(TAG, "Found: " + sphero);
                RobotProvider.getDefaultProvider().connect(sphero.iterator().next());
            }
        });

        boolean success = RobotProvider.getDefaultProvider().startDiscovery(this);
        if(!success){
            Toast.makeText(HelloWorldActivity.this, "Unable To start Discovery!", Toast.LENGTH_LONG).show();
        }
    }
    
    
Next, if you didn't save the robot in the `onConnected()` callback, you need to get a reference to a Robot object from RobotProvider using the following line of code.

                mRobot = RobotProvider.getDefaultProvider().findRobot(robot_id);

RobotProvider is a singleton that is used to manage connected Spheros, and is capable of allowing multiple connections. So, the Robot object is how you reference which Sphero to communicate with. 

The final call in `onActivityResult()` is to ``blink()``, which is method that toggles the RGB LED's blue component every second. The LED is controlled by using the `setColor()` method. This method encapsulates the RGBLEDOutputCommand for convenience (less typing is better right?), and posts this call to DeviceMessenger, which sends the command down to the ball. The blink method commands the device to turn off the LED with the following code line.

                mRobot.setColor(0,0,0);
                
                
Where `mRobot` is the reference to the Robot object that the API uses determine which device to send a command to, and the last arguments set the brightness of red, green, and blue components of the LED. In this case, all color components are turned off. The method sets the blue LED component to full brightness with the following code line.

                mRobot.setColor(0, 0, 255);

It finally uses a `Handler` object to call it self after a 1 second delayed with the lit boolean parameter toggled, thus toggling the blue LED component on and off.

When the application closes the connection is closed by the following line in the `onStop()` method.

        mRobot.disconnect();
        
                
## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)
 



