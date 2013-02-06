
## New in this version (1.5):

#### New API additions in RobotKit.
* Added support for DeviceMessage encoding/decoding.  
* Added support for infinite data streaming implemented at the SDK level.
* Added abort program in the OrbBasicProgram class
* Refactored CalibrationView and CalibrationButtonView
* Improved Bluetooth Connectivity on certain Android Devices

## Fixes and such:

#### In the API

* Fixed DeviceConnection bug that was occasionally causing improper disconnection issues

#### In the samples
* SensorStreaming - now uses a packet count of 0 for infinite data streaming.
* Locator - now uses a packet count of 0 for infinite data streaming.
* StreamingAnimation - now uses a packet count of 0 for infinite data streaming.
* UISample - Refactored the CalibrationImageButtonView	
 	



