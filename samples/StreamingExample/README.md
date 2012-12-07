![logo](http://update.orbotix.com/developer/sphero-small.png)

# Sensor StreamingSphero supports asynchronous data streaming of certain control system and sensor parameters.  This is great for using Sphero as a controller, or for retreiving data about its environment.  In this document we show on Android how to setup and receive asynchronous data from Sphero.

## Available Sensor Parameters
As of Firmware 1.20, Sphero can stream the following values: 1. **Accelerometer** (X, Y, Z) 2. **GyroScope** (X, Y, Z)
 3. **IMU** (Roll, Pitch, and Yaw)
 4. **Back EMF** (Left Motor, Right Motor)
 5. **Quaternions** - *New to Firmware 1.20*
 6. **Location Data** (X, Y, Vx, Vy) - *New to Firmware 1.20*## Accelerometer

An accelerometer measures the force of gravity in 3-dimensions (x,y,z).  A few uses are for determing shake gestures and collisions. 

![android.jpg](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/accelerometer.png)

On Sphero, you have access to the raw and filtered accelerometer data.  You should always stream the filtered data, unless you have use for the raw data.  The filtered accelerometer data is in units of g.  So, 1 G = a value of 9.81 m/s^2. 
## Gyroscope

A gyroscope is a device for measuring or maintaining orientation, based on the principles of angular momentum. It returns the rate of angular velocity.

![android.jpg](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/gyroscope.png)## IMU
The IMU uses the accelerometer and gyroscope on Sphero to determine values for Roll, Pitch, and Yaw.  These values (in degrees) can be used to determine the orientation of Sphero.
![android.jpg](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/IMU.png)## Back EMF

Back electromotive force (abbreviated Back EMF) is the voltage, or electromotive force, that pushes against the current which induces it.  Before we created the Locator, this could be used to determine how fast Sphero was traveling. It can still be used to determing what is going on with the motors.

## Quaternions

		Note: You need firmware 1.20 or above on Sphero, or these values will always be 0

Quaternions are a number system that extends the complex numbers.  They are used to represent orientation in 3D-space.  Typically, these are four numbers, all ranging from 0-1.  For data transmission reasons, we give you 4 numbers from 0-10000.  Hence, the units on these return numbers are (1/10000th) of a Q.  

## Locator Data

		Note: You need firmware 1.20 or above on Sphero, or these values will always be 0
		
The locator returns values for the x,y position of Sphero on the floor, and the current velocity vector of Sphero.  Please see the locator sample documentation for more information.## Requesting Data Streaming
In the Set Data Streaming command, we recommend a value of 20 <= divisor <= 50 and packetFrames=1 for most purposes.  Since the maximum sensor sampling rate is ~420 Hz, if we take divisor=20 and packetFrames=1 we get approx. 420/20 = ~21 packets/second each containing one set of requested data values.  For iOS devices divisor=20 works well.  For many Android devices divisor = 10 or less is possible (42+ samples/second).
    private void requestDataStreaming() {

        if(mRobot != null){
        	
            final long mask = SetDataStreamingCommand.DATA_STREAMING_MASK_ACCELEROMETER_FILTERED_ALL |
            				  SetDataStreamingCommand.DATA_STREAMING_MASK_IMU_ANGLES_FILTERED_ALL;
            
            final int divisor = 50;

            final int packet_frames = 1;

            mPacketCounter = 0;
            
            final int response_count = TOTAL_PACKET_COUNT;  // 200

            SetDataStreamingCommand.sendCommand(mRobot, divisor, packet_frames, mask, response_count);
        }
    }
    For real time applications setting packetFrames > 1 is usually pointless since you are only interested in the most recent data.  However it is possible to obtain all the samples by setting, for instance, divisor=1 and packetFrames=21 (~20 packets/second each containing 21 sets of data.It is important to note that we are only requesting 200 sets of data from this Set Data Streaming Command.  Therefore, you will have to request after you get close to 200.  This is done to solve the problem of requesting infinite data putting the Sphero robot in a bad state if your app crashes and did not disconnect properlly.   
## Receiving Async Data Packets

You will receive an `onDataReceived` callback at the frequency in which you requested data streaming.  The callback will contain `DeviceAsyncData` with a certain number of frames (also determined when requesting data).  The data will contain all the variables you requested as well.

In this example, you have access to the Attitude (IMU) data and the filtered accelerometer data. 
 
    private DeviceMessenger.AsyncDataListener mDataListener = new DeviceMessenger.AsyncDataListener() {
        @Override
        public void onDataReceived(DeviceAsyncData data) {

            if(data instanceof DeviceSensorsAsyncData){

                // If we are getting close to packet limit, request more
                mPacketCounter++;
                if( mPacketCounter > (TOTAL_PACKET_COUNT - PACKET_COUNT_THRESHOLD) ) {
                    requestDataStreaming();
                }

                //get the frames in the response
                List<DeviceSensorsData> data_list = ((DeviceSensorsAsyncData)data).getAsyncData();
                if(data_list != null){

                    //Iterate over each frame
                    for(DeviceSensorsData datum : data_list){

                        //Show attitude data
                        AttitudeData attitude = datum.getAttitudeData();
                        if(attitude != null){
                            mImuView.setPitch("" + attitude.getAttitudeSensor().pitch);
                            mImuView.setRoll("" + attitude.getAttitudeSensor().roll);
                            mImuView.setYaw("" + attitude.getAttitudeSensor().yaw);
                        }

                        //Show accelerometer data
                        AccelerometerData accel = datum.getAccelerometerData();
                        if(attitude != null){
                            mAccelerometerFilteredView.setX(""+accel.getFilteredAcceleration().x);
                            mAccelerometerFilteredView.setY("" + accel.getFilteredAcceleration().y);
                            mAccelerometerFilteredView.setZ("" + accel.getFilteredAcceleration().z);
                        }
                    }
                }
            }
        }
    };## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

	  