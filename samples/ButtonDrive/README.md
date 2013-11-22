![logo](http://update.orbotix.com/developer/sphero-small.png)

# ButtonDrive

You can see from the diagram how Sphero's roll commands correspond to its heading in space.

![android.jpg](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/heading.png)

![Screen Shot](https://github.com/orbotix/Sphero-Android-SDK/raw/master/samples/ButtonDrive/README.png)

This code sample demonstrates driving a Sphero by sending roll commands at 0°, 90°, 180°, and 270°. The roll command 
takes a heading from 0° to 360°, and a relative speed from 0.0 to 1.0. Assuming you have a Sphero object called myRobot that is initialized, to command the ball to go 90° at half speed would be:

	mRobot.drive(90.0, 0.5);

The ball is stopped with the `mRobot.stop()` call.

## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)
