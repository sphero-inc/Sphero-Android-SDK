![logo](http://update.orbotix.com/developer/sphero-small.png)

# Sensor StreamingSphero supports asynchronous data streaming of certain control system and sensor parameters.  This is great for using Sphero as a controller, or for retrieving data about its environment.  In this document we show on Android how to setup and receive asynchronous data from Sphero.

## Available Sensor Parameters
As of Firmware 1.20, Sphero can stream the following values: 1. **Accelerometer** (X, Y, Z) 2. **GyroScope** (X, Y, Z)
 3. **IMU** (Roll, Pitch, and Yaw)
 4. **Back EMF** (Left Motor, Right Motor)
 5. **Quaternions** - *New to Firmware 1.20*
 6. **Location Data** (X, Y, Vx, Vy) - *New to Firmware 1.20*## Accelerometer

An accelerometer measures the force of gravity in 3-dimensions (x,y,z).  A few uses are for determining shake gestures and collisions. 

![android.jpg](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/accelerometer.png)

On Sphero, you have access to the raw and filtered accelerometer data.  You should always stream the filtered data, unless you have use for the raw data.  The filtered accelerometer data is in units of g.  So, 1 G = a value of 9.81 m/s^2. 
## Gyroscope

A gyroscope is a device for measuring or maintaining orientation, based on the principles of angular momentum. It returns the rate of angular velocity.

![android.jpg](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/gyroscope.png)## IMU
The IMU uses the accelerometer and gyroscope on Sphero to determine values for Roll, Pitch, and Yaw.  These values (in degrees) can be used to determine the orientation of Sphero.
![android.jpg](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/IMU.png)## Back EMF

Back electromotive force (abbreviated Back EMF) is the voltage, or electromotive force, that pushes against the current which induces it.  Before we created the Locator, this could be used to determine how fast Sphero was traveling. It can still be used to determining what is going on with the motors.

## Quaternions

		Note: You need firmware 1.20 or above on Sphero, or these values will always be 0

Quaternions are a number system that extends the complex numbers.  They are used to represent orientation in 3D-space.  Typically, these are four numbers, all ranging from 0-1.  For data transmission reasons, we give you 4 numbers from 0-10000.  Hence, the units on these return numbers are (1/10000th) of a Q.  

## Locator Data

		Note: You need firmware 1.20 or above on Sphero, or these values will always be 0
		
The locator returns values for the x,y position of Sphero on the floor, and the current velocity vector of Sphero.  Please see the locator sample documentation for more information.## Requesting Data Streaming
For the sensor data streaming command, the maximum sensor sampling rate is ~420 Hz. We recommend using a value in the 20-40Hz range for Android devices. At 20 Hz, virtually every device will not see a slowdown from the packet detection. However, 40 Hz is only viable when targeting only high-end devices.    private void requestDataStreaming() {

        if(mRobot != null) {
			mRobot.getSensorControl().setRate(10 /*Hz*/);
			mRobot.getSensorControl().addSensorListener
        }
    }
    ## Receiving Async Data Packets

You will receive an `sensorUpdated` callback at the frequency in which you requested data streaming.  The callback will contain `DeviceSensorsData` with a certain number of frames (also determined when requesting data).  The data will contain all the variables you requested as well.

In this example, you have access to the Attitude (IMU) data and the filtered accelerometer data. 
 
    private final SensorListener mSensorListener = new SensorListener() {
        @Override
        public void sensorUpdated(DeviceSensorsData datum) {
            //Show attitude data
            AttitudeSensor attitude = datum.getAttitudeData();
            if (attitude != null) {
                mImuView.setPitch(String.format("%+3d", attitude.pitch));
                mImuView.setRoll(String.format("%+3d", attitude.roll));
                mImuView.setYaw(String.format("%+3d", attitude.yaw));
            }

            //Show accelerometer data
            AccelerometerData accel = datum.getAccelerometerData();
            if (attitude != null) {
                mAccelerometerFilteredView.setX(String.format("%+.4f", accel.getFilteredAcceleration().x));
                mAccelerometerFilteredView.setY(String.format("%+.4f", accel.getFilteredAcceleration().y));
                mAccelerometerFilteredView.setZ(String.format("%+.4f", accel.getFilteredAcceleration().z));
            }
        }
    };## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

	  