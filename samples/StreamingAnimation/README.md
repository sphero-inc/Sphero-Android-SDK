![logo](http://update.orbotix.com/developer/sphero-small.png)

# Streaming Animation SampleStreamingAnimation demonstrates how to use Sphero as a controller to move and rotate a Sphero ball image around the screen.  To control the image, we use asynchronous data streaming of the IMU values of **Roll, Pitch, and Yaw**.  Roll and Pitch control x and y translation and Yaw controls rotating the image.## UI Elements
The samples has a `main.xml` resource file that contains the SpheroConnectionView, IMU TextViews, Calibrate Button, and Sphero Image. If you want the SpheroConnectionView explained in more detail, look at the Android SDK ReadMe.

    <RelativeLayout
        android:id="@+id/relative_layout"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent" >

      <Button
          android:id="@+id/button_calibrate"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:layout_alignParentTop="true"
          android:layout_alignParentRight="true"
          android:text="@string/calibrate"
          android:onClick="calibrateClicked"/>  
        
      <TextView
          android:id="@+id/roll"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:layout_alignParentTop="true"
          android:layout_alignParentLeft="true"
          android:text="@string/roll"
          android:textColor="#FFF"/>  

      <TextView
          android:id="@+id/pitch"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:layout_below="@id/roll"
          android:text="@string/pitch"
          android:textColor="#FFF"/>  
          
      <TextView
          android:id="@+id/yaw"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:layout_below="@id/pitch"
          android:text="@string/yaw"
          android:textColor="#FFF"/>  
      
      <ImageView
          android:id="@+id/image_sphero"
          android:layout_width="fill_parent"
          android:layout_height="fill_parent"
          android:src="@drawable/sphero_ball"
          android:scaleType="matrix"/>

    </RelativeLayout>

It is important to note the ImageView, because it is a little bit more advanced.  The `matrix` scale type is telling Android that we will control the scale and rotation of the image ourselves using matrix transformations.  

### Making the Sample Fullscreen

In the `AndroidManifest.xml` see the <Activity> tag for our StreamingAnimationAcitivty:

	<activity android:name=".CoinCollectorActivity"
                  android:label="@string/app_name"
                  android:theme="@android:style/Theme.Black.NoTitleBar.Fullscreen">
                  
We simply added the theme attribute to make the game full screen (remove the titlebar).## Code
    
### Getting the Android Device Screen Size

Most Android devices have different screen sizes.  To get the correct width and height of the screen (in pixels) we use the `DisplayMetrics` class.  We will use this width and height to bound the Sphero image's movement.  This code in the `onCreate()` method.

        // Get Screen width and height
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenHeight = displaymetrics.heightPixels;
        mScreenWidth = displaymetrics.widthPixels;

### Moving Sphero LogoThe algorithm that moves the logo around, using the Sphero ball as a controller, is simple.  However, you need to know about the orientation units of **roll, pitch, and yaw** before the algorithm will make sense.  Here is an image that will help you understand these values which are in degrees.![android.jpg](https://github.com/orbotix/Sphero-Android-SDK/raw/master/assets/IMU.png)
The algorithm treats the roll value as the x-axis velocity and the pitch value as y-axis velocity and updates the image x and y position based on these values.  The algorithm then uses the value of yaw to control the rotation of the image.  The function to update the sphero motion is below:
    /**
     * Update the Sphero Logo Location
     * @param roll in degrees from data streaming
     * @param pitch in degrees from data streaming
     * @param yaw in degrees from data streaming
     */
    private void updateSpheroPosition(double roll, double pitch, double yaw) {
    	
    	// Find the length of the velocity vector and the angle
	    double length = Math.sqrt(pitch * pitch + roll * roll);
	    double moveAngle = -Math.atan2(-pitch, roll);
	    
	    // Adjust this value to change the sensitivity of Sphero moving
	    final float SENSITIVITY = 0.8f;
	    
	    // Compute the velocity of the Sphero image
	    double adjustedX = length * Math.cos(moveAngle) * SENSITIVITY;
	    double adjustedY = length * Math.sin(moveAngle) * SENSITIVITY;
	    
	    // Add new distance to the Sphero image
	    mImageSpheroLoc.x += adjustedX;
	    mImageSpheroLoc.y += adjustedY;
	    
	    // Check boundaries
	    if( (mImageSpheroLoc.x + mImageSpheroBounds.x) > mScreenWidth ) {
	    	mImageSpheroLoc.x = mScreenWidth - mImageSpheroBounds.x;
	    }
	    if( (mImageSpheroLoc.y + mImageSpheroBounds.y) > mScreenHeight ) {
	    	mImageSpheroLoc.y = mScreenHeight - mImageSpheroBounds.y;
	    }
	    if( mImageSpheroLoc.x < 0 ) {
	    	mImageSpheroLoc.x = 0;
	    }
	    if( mImageSpheroLoc.y < 0 ) {
	    	mImageSpheroLoc.y = 0;
	    }
	    
	    // Create Sphero translation matrix
	    Matrix matrix = new Matrix();
	    matrix.reset();
	    matrix.postTranslate(mImageSpheroLoc.x, mImageSpheroLoc.y);
	    
	    // Rotate around Sphero center
	    Point spheroCenter = new Point(mImageSpheroLoc.x+(mImageSpheroBounds.x/2),
	    							   mImageSpheroLoc.y+(mImageSpheroBounds.y/2));
	    matrix.postRotate((int)-yaw, spheroCenter.x, spheroCenter.y);

	    // Apply translation matrix
	    mImageSphero.setScaleType(ScaleType.MATRIX);
	    mImageSphero.setImageMatrix(matrix);
    }
    
### Calling the updateSpheroPosition Function

The `StreamingExample` set up the data for us, so all we need to do is get it to the function we created.  We do this by replacing the code in `onDataReceived(DeviceAsyncData data)` to 
 
    //get the frames in the response
    List<DeviceSensorsData> data_list = ((DeviceSensorsAsyncData)data).getAsyncData();
    if(data_list != null){

        //Iterate over each frame
        for(DeviceSensorsData datum : data_list){

            //Show attitude data
            AttitudeData attitude = datum.getAttitudeData();
            
            // Get the values of roll and yaw
            int roll = attitude.getAttitudeSensor().roll;
            int pitch = attitude.getAttitudeSensor().pitch;
            int yaw = attitude.getAttitudeSensor().yaw;
            
            // Display data values in the text view
    		mTextRoll.setText(getString(R.string.roll) + roll);
    		mTextPitch.setText(getString(R.string.pitch) + pitch);
    		mTextYaw.setText(getString(R.string.yaw) + yaw);
            
            // Calculate the new image position
            updateSpheroPosition(roll, pitch, yaw);
        }
    }## Questions

For questions, please visit our developer's forum at [http://forum.gosphero.com/](http://forum.gosphero.com/) or email me at michael@orbotix.com

	  