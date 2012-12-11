
## New in this version (1.4):

#### New API additions.
* Added onBluetoothNotEnabled() method to the **SpheroConnectionView** listener
* Streaming **Quaternions** now return in values of -1.0 to 1.0 instead of -10,000 to 10,0000
* Deprecated **CalibrateCommand**. Now use **SetHeadingCommand** which is the same code, different name.
* Deprecated **FrontLEDOutputCommand**.  Now use **BackLEDOutputCommand** which is the same code, different name.

#### New in Sample code
* **StreamingAnimation** 
	* Demonstrates how to use Sphero as a controller to move and rotate an image on screen.
* **UISample**
	* Added SlideToSleepView
	* Added customizable calibration button widget
	* Added **NoSpheroConnectView** with customizable URL for Sphero purchase referrals
	* Added Sphero Controller Tutorial Image
	* Added better support for portrait and landscape

## Fixes and such:

#### In the API
* Fixed no Spheros paired bug in SpheroConnectionView
* Deprecated shutdown method in SpheroConnectionView
* Fixed bug in SpheroVerse when renaming a Sphero

#### In the samples
 	



