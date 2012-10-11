![logo](http://update.orbotix.com/developer/sphero-small.png)

# Sensor StreamingSphero supports asynchronous data streaming of certain control system and sensor parameters.  This is great for using Sphero as a controller, or for retreiving data about its environment.  In this document we show on Android how to setup and receive asynchronous data from Sphero.

## Available Sensor Parameters
As of Firmware 1.20, Sphero can stream the following values: 1. **Accelerometer** (X, Y, Z) 2. **GyroScope** (X, Y, Z)
 3. **IMU** (Roll, Pitch, and Yaw)
 4. **Back EMF** (Left Motor, Right Motor)
 5. **Quaternions** - *New to Firmware 1.20*
 6. **Location Data** (X, Y, Vx, Vy) - *New to Firmware 1.20*## Accelerometer

## Gyroscope
## IMU
## Requesting Data Streaming
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
## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/)

	  