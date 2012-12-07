package orbotix.robot.widgets;

import orbotix.uisample.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

	/**
	 * Override default constructor to set the background of the image button
	 * @param context
	 * @param attrs
	 */
	public CalibrateImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
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
		updateButtonImages();
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
		
		// Set as background for this image button
		this.setImageDrawable(layerDrawable);
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
}
