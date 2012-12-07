package orbotix.robot.widgets;

import orbotix.uisample.R;
import orbotix.view.calibration.CalibrationButtonView.CalibrationCircleLocation;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class CalibrateImageButton extends ImageButton {

	/**
	 * The Color of the foreground and background of the button images
	 */
	private int mColorBackground = Color.rgb(30, 144, 255);
	private int mColorForeground = Color.WHITE;
	private CalibrationCircleLocation mOrientation;
	
	/**
	 * Define the custom orientations
	 */
	public static final int ABOVE = 1;
	public static final int LEFT  = 2;
	public static final int RIGHT = 3;
	public static final int BELOW = 4;
	
	/**
	 * Override default constructor to set the background of the image button
	 * @param context
	 * @param attrs
	 */
	public CalibrateImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mOrientation = CalibrationCircleLocation.ABOVE;
		// Grab custom attributes
		if(attrs != null){
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalibrateImageButton);
			
			// Check if background color has been set by developer
			if(a.hasValue(R.styleable.CalibrateImageButton_background_color)){
				mColorBackground = a.getColor(R.styleable.CalibrateImageButton_background_color, mColorBackground);
			}
			// Check if foreground color has been set by developer
			if(a.hasValue(R.styleable.CalibrateImageButton_foreground_color)){
				mColorForeground = a.getColor(R.styleable.CalibrateImageButton_foreground_color, mColorForeground);
			}
			// Check orientation
			if(a.hasValue(R.styleable.CalibrateImageButton_widget_orientation)) {
				// Set the orientation of the widget
				int orientation = a.getInt(R.styleable.CalibrateImageButton_widget_orientation, ABOVE);
				if( orientation == ABOVE ) mOrientation = CalibrationCircleLocation.ABOVE;
				else if( orientation == LEFT ) mOrientation = CalibrationCircleLocation.LEFT;
				else if( orientation == RIGHT ) mOrientation = CalibrationCircleLocation.RIGHT;
				else mOrientation = CalibrationCircleLocation.BELOW;
			}
		
			a.recycle();
		}
		
		// Set the background colors
		updateButtonImages();
	}

	/**
	 * Override default constructor to set the background of the image button
	 * @param context
	 */
	public CalibrateImageButton(Context context) {
		super(context);
		//updateButtonImages();
	}

	/**
	 * Called when we need to change the default image colors
	 * And combine them into one image for the button
	 */
	private void updateButtonImages() {
		// Create drawables of desired colors  
		Drawable backgroundDrawable = getResources().getDrawable(R.drawable.calibrate_background);
		backgroundDrawable.setColorFilter(mColorBackground, PorterDuff.Mode.MULTIPLY);
		Drawable foregroundDrawable = getResources().getDrawable(R.drawable.calibrate_foreground);
		foregroundDrawable.setColorFilter(mColorForeground, PorterDuff.Mode.MULTIPLY);

		// Combine drawables into one for the image button
		Drawable[] layers = new Drawable[2];
		layers[0] = backgroundDrawable;
		layers[1] = foregroundDrawable;
		LayerDrawable layerDrawable = new LayerDrawable(layers);
		
		Matrix matrix = new Matrix();
		float angle = 0.0f;
		if( mOrientation == CalibrationCircleLocation.LEFT ) angle = -90.0f;
		else if( mOrientation == CalibrationCircleLocation.RIGHT ) angle = 90.0f;
		else if( mOrientation == CalibrationCircleLocation.BELOW ) angle = 180.0f;
		matrix.postRotate(angle);
		Bitmap bitmap = drawableToBitmap(layerDrawable);
		Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		
		// Set as background for this image button
		this.setImageBitmap(rotatedBitmap);
	}

	/**
	 * Set background color of the button
	 * Use Color.COLOR or Color.rgb() to create the color you want
	 */
	public void setBackgroundColor(int color) {
		mColorBackground = color;
		updateButtonImages();
	}

	/**
	 * Set foreground color of the button
	 * Use Color.COLOR or Color.rgb() to create the color you want
	 */	
	public void setForegroundColor(int color) {
		mColorForeground = color;
		updateButtonImages();
	}
	
	/**
	 * Set the draw orientation
	 * @param location an enum of the CalibrationCircleLocation constant
	 */
	public void setOrientation(CalibrationCircleLocation orientation) {
		mOrientation = orientation;
		updateButtonImages();
	}
	
	public static Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
	}
}
