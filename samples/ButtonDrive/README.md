# ButtonDrive

![Screen Shot](https://github.com/orbotix/Sphero-Android-SDK/raw/master/samples/ButtonDrive/README.png)

This code sample demonstrates driving a Sphero by sending roll commands at 0°, 90°, 180°, and 270°. The roll command 
takes a heading from 0° to 360°, and a relative speed from 0.0 to 1.0. So, to command the ball to go 90° at half speed would be:

    RollCommand.sendCommand(mRobot, 90.0, 0.5);

The ball is stopped with the `RKRollCommand.sendStop()`.

