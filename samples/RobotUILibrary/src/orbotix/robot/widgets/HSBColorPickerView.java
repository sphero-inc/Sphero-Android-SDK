package orbotix.robot.widgets;

import orbotix.robot.app.ColorPickerActivity.OnColorChangedListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import orbotix.robot.app.R;

/**
 * A Custom View that displays a Hue Saturation Brightness/Value color picker complete with a color wheel
 * for simultaneously selecting hue [0...360) and saturation [0...1] and a brightness bar for selecting 
 * brightness [0...1].  To indicate the current selection crosshairs are drawn on the color wheel and a
 * height indicator is drawn on the brightness bar. 
 * <p>
 * To obtain updates on the selected color, create an {@link OnColorChangedListener} and either pass the 
 * listener into the constructor or use 
 * {@link HSBColorPickerView#setOnColorChangedListener(OnColorChangedListener)}.
 * <p>
 * Make sure to set the initial color using {@link HSBColorPickerView#setNewColor(int)}. 
 */
public class HSBColorPickerView extends View {
	
	//private static final String TAG = "Orbotix";
	private static final int DEFAULT_COLOR = Color.GREEN;
	private static final int SPACE_BETWEEN_WHEEL_AND_BAR = 10;
	private static final float ARC_WIDTH = 50.0f;
	private static final float CROSSHAIRS_SIZE = 30.0f;
	private static final float BRIGHTNESS_INDICATOR_SIZE = 15.0f;
	
	// Color Wheel
	private Bitmap mWheel;
	private Point mWheelPosition;
	private Point mWheelCenter;
	private double mRadius;
	
	// Background
	private Bitmap mBackground;
	private Point mBackgroundPosition;
	
	// Brightness Bar
	private Path mArcPath;
	private RectF mInnerArcRect;
	private RectF mOuterArcRect;
	
	// Touch control
	private boolean isTrackingHSWheel;
	private boolean isTrackingBBar;
	
	// Color
	private float[] mPreviousColor;
	private float[] mNewColor;
	
	// listener
	private OnColorChangedListener mListener;
	
	public HSBColorPickerView(Context context, AttributeSet attrs, int defStyle, int startingColor, OnColorChangedListener listener) {
		this(context, attrs, defStyle, startingColor);
		mListener = listener;
	}
	
	public HSBColorPickerView(Context context, AttributeSet attrs, int defStyle, int startingColor) {
		super(context, attrs, defStyle);
		mPreviousColor = new float[3];
		mNewColor = new float[3];
		Color.colorToHSV(startingColor, mPreviousColor);
		System.arraycopy(mPreviousColor, 0, mNewColor, 0, mPreviousColor.length);
		mBackground = BitmapFactory.decodeResource(getResources(), R.drawable.color_picker_wheel_background);
		mWheel = BitmapFactory.decodeResource(getResources(), R.drawable.color_wheel);
	}

	public HSBColorPickerView(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs, defStyle, DEFAULT_COLOR);
	}
	
	public HSBColorPickerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0, DEFAULT_COLOR);
	}
	
	public HSBColorPickerView(Context context) {
		this(context, null, 0, DEFAULT_COLOR);
	}
	
	/**
	 * Changes the currently displayed color to the new color. This implementation invalidates the view.
	 * This can also be used to set the initial color when this view is shown. The 
	 * {@link orbotix.robot.app.ColorPickerActivity.OnColorChangedListener} will be notified of the change.
	 * 
	 * @param newColor the desired color for this view to display in the color wheel and brightness bar.
	 */
	public void setNewColor(int newColor) {
		Color.colorToHSV(newColor, mNewColor);
		updateListener();
		invalidate();
	}
	
	/**
	 * Sets the {@link orbotix.robot.app.ColorPickerActivity.OnColorChangedListener} to be updated when the color in this
	 * view is changed either by user input or by setting a color using {@link HSBColorPickerView#setNewColor(int)}.
	 */
	public void setOnColorChangedListener(OnColorChangedListener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// draw the background and color wheel
		canvas.drawBitmap(mBackground, mBackgroundPosition.x, mBackgroundPosition.y, null);
		canvas.drawBitmap(mWheel, mWheelPosition.x, mWheelPosition.y, null);
		
		// setup paint for the gradient filling
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(2.0f);
		// create a shader that will show the range of brightness settings
		float[] gradientStartColor = new float[3];
		float[] gradientEndColor = new float[3];
		System.arraycopy(mNewColor, 0, gradientStartColor, 0, mNewColor.length);
		System.arraycopy(mNewColor, 0, gradientEndColor, 0, mNewColor.length);
		gradientStartColor[2] = 1.0f;
		gradientEndColor[2] = 0.0f;
		Shader gradientShader = new LinearGradient((float)(mWheelPosition.x + mRadius), 
												   mWheelPosition.y, 
												   mOuterArcRect.right, 
												   mWheelPosition.y + mWheel.getHeight(), 
												   Color.HSVToColor(gradientStartColor), 
												   Color.HSVToColor(gradientEndColor), 
												   Shader.TileMode.MIRROR);
		paint.setShader(gradientShader);
		canvas.drawPath(mArcPath, paint);
		
		drawHSCrosshairs(canvas);
		drawBrightnessIndicator(canvas);
	}
	
	private void drawHSCrosshairs(Canvas canvas) {
		float crosshairsRadius = CROSSHAIRS_SIZE / 2.0f;
		float[] HSCrosshairsCenter = findCrosshairsCenter();
		//Log.d(TAG, String.format("Reference point: (%f, %f)", HSCrosshairsCenter[0], HSCrosshairsCenter[1]));
		// find the actual coordinates of the center of the crosshairs wrt the whole screen
		float absoluteCenterX = (float)mWheelCenter.x + HSCrosshairsCenter[0];
		// note: the (-) is intentional due to the inversion of the y coordinate system (wrt cartesian)
		float absoluteCenterY = (float)mWheelCenter.y - HSCrosshairsCenter[1];
		
		// make convenience variables for size
		float topBottomWidth = 0.35f * CROSSHAIRS_SIZE;
		float topBottomHeight = 0.3f * CROSSHAIRS_SIZE;
		float leftRightWidth = 0.3f * CROSSHAIRS_SIZE;
		float leftRightHeight = 0.35f * CROSSHAIRS_SIZE;
		
		// make convenience variables for bounds
		float left = absoluteCenterX - crosshairsRadius;
		float right = absoluteCenterX + crosshairsRadius;
		float top = absoluteCenterY - crosshairsRadius;
		float bottom = absoluteCenterY + crosshairsRadius;
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(1.0f);
		
		// draw the top triangle
		Path topTrianglePath = new Path();
		topTrianglePath.moveTo(absoluteCenterX - topBottomWidth, top);
		topTrianglePath.lineTo(absoluteCenterX + topBottomWidth, top);
		topTrianglePath.lineTo(absoluteCenterX, top + topBottomHeight);
		topTrianglePath.close();
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(topTrianglePath, paint);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(topTrianglePath, paint);
		
		// draw the bottom triangle
		Path bottomTrianglePath = new Path();
		bottomTrianglePath.moveTo(absoluteCenterX + topBottomWidth, bottom);
		bottomTrianglePath.lineTo(absoluteCenterX - topBottomWidth, bottom);
		bottomTrianglePath.lineTo(absoluteCenterX, bottom - topBottomHeight);
		bottomTrianglePath.close();
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(bottomTrianglePath, paint);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(bottomTrianglePath, paint);
		
		// draw the left triangle
		Path leftTrianglePath = new Path();
		leftTrianglePath.moveTo(left, absoluteCenterY + leftRightHeight);
		leftTrianglePath.lineTo(left, absoluteCenterY - leftRightHeight);
		leftTrianglePath.lineTo(left + leftRightWidth, absoluteCenterY);
		leftTrianglePath.close();
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(leftTrianglePath, paint);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(leftTrianglePath, paint);

		// draw the right triangle
		Path rightTrianglePath = new Path();
		rightTrianglePath.moveTo(right, absoluteCenterY - leftRightHeight);
		rightTrianglePath.lineTo(right, absoluteCenterY + leftRightHeight);
		rightTrianglePath.lineTo(right - leftRightWidth, absoluteCenterY);
		rightTrianglePath.close();
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(rightTrianglePath, paint);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(rightTrianglePath, paint);
	}
	
	private void drawBrightnessIndicator(Canvas canvas) {
        // get a representation of the height of the indicator within the bar
        float brightnessHeight = mNewColor[2] * mWheel.getHeight();
        //Log.d(TAG, String.format("Brightness height: %f", brightnessHeight));
        // convert the height to an absolute y position based on the bar's position
        float absoluteY = mWheelPosition.y + mWheel.getHeight() - brightnessHeight;
        // get the y value "above" the x axis for the x coordinate calculation
        // note: because of symmetry, the sign doesn't matter so we'll use the positive
        float heightAboveXAxis = Math.abs(brightnessHeight - (float)mRadius);
        //Log.d(TAG, String.format("Height above X: %f", heightAboveXAxis));
        float leftEdgeRadius = (float)(mRadius + SPACE_BETWEEN_WHEEL_AND_BAR);
        float rightEdgeRadius = leftEdgeRadius + ARC_WIDTH;
        // get the x coordinate relative to the center of the wheel
        float leftXInCircle = (float) Math.sqrt(leftEdgeRadius * leftEdgeRadius - heightAboveXAxis * heightAboveXAxis);
        float rightXInCircle = (float) Math.sqrt(rightEdgeRadius * rightEdgeRadius - heightAboveXAxis * heightAboveXAxis);
        // get the absolute x and y coordinates of the left edge of the bar at the bar height
        float leftX = mWheelCenter.x + leftXInCircle;
        float rightX = mWheelCenter.x + rightXInCircle;
        
        float indicatorHeight = BRIGHTNESS_INDICATOR_SIZE / 2.0f;
        float indicatorWidth = BRIGHTNESS_INDICATOR_SIZE;
        
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(1.0f);
		
		Path leftTrianglePath = new Path();
		leftTrianglePath.moveTo(leftX, absoluteY - indicatorHeight);
		leftTrianglePath.lineTo(leftX + indicatorWidth, absoluteY);
		leftTrianglePath.lineTo(leftX, absoluteY + indicatorHeight);
		leftTrianglePath.close();
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(leftTrianglePath, paint);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(leftTrianglePath, paint);
		
		Path rightTrianglePath = new Path();
		rightTrianglePath.moveTo(rightX, absoluteY - indicatorHeight);
		rightTrianglePath.lineTo(rightX - indicatorWidth, absoluteY);
		rightTrianglePath.lineTo(rightX, absoluteY + indicatorHeight);
		rightTrianglePath.close();
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(rightTrianglePath, paint);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(rightTrianglePath, paint);
	}
	
	/**
	 * Uses the currently selected color to determine the point on the hue/saturation wheel that corresponds
	 * to that hue/saturation combination. 
	 * 
	 * @return a float[2] where float[0] is the x coordinate and float[1] is the y coordinate.
	 */
	private float[] findCrosshairsCenter() {
        // find hypotenuse for calculations
		float distanceFromCenter = mNewColor[1] * (float)mRadius;
		// angle should be in degrees so we need to change to radians
		float angle = mNewColor[0] * ((float)Math.PI / 180.0f);
		float xFromCenter = distanceFromCenter * (float)Math.cos(angle);
		float yFromCenter = distanceFromCenter * (float)Math.sin(angle);
		return new float[] {xFromCenter, yFromCenter};
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int height = bottom - top;
		int width = right - left;
		//Log.d(TAG, String.format("Height: %d Width: %d", height, width));
		
		float sizeMultiplier = 1.0f;
		int forcedPadding = 10;
		
		// determine the position of the background image first
		// then all other items will depend on this one
		// for now, we will just use simple midpoint adjustment
		int pos_x = 0;
		if (mBackground.getWidth() < width) {
			pos_x = (width - mBackground.getWidth()) / 2;
		} else if (mBackground.getWidth() > width) {
			sizeMultiplier = (float)(width) / (float)mBackground.getWidth();
			mBackground = Bitmap.createScaledBitmap(mBackground, (int)(mBackground.getWidth() * sizeMultiplier), (int)(mBackground.getHeight() * sizeMultiplier), true);
			pos_x = (width - mBackground.getWidth()) / 2;
		}
		int pos_y = 0;
		if (mBackground.getHeight() < height) {
			pos_y = (height - mBackground.getHeight()) / 2;
		} else if (mBackground.getHeight() > height) {
			sizeMultiplier = (float)(height - forcedPadding) / (float)mBackground.getHeight();
			mBackground = Bitmap.createScaledBitmap(mBackground, (int)(mBackground.getWidth() * sizeMultiplier), (int)(mBackground.getHeight() * sizeMultiplier), true);
			pos_y = (height - mBackground.getHeight()) / 2;
		}
		mBackgroundPosition = new Point(pos_x, pos_y);
		
		// determine the position of the color wheel
		pos_y = 0;
		if (sizeMultiplier < 1.0f) {
			mWheel = Bitmap.createScaledBitmap(mWheel, (int)(mWheel.getWidth() * sizeMultiplier) - forcedPadding, (int)(mWheel.getHeight() * sizeMultiplier) - forcedPadding, true);
		}
		if (mWheel.getHeight() < height) {
			pos_y = (height - mWheel.getHeight()) / 2;
		}
		// put the wheel in the center, taking into account the brightness bar
		pos_x = (int) (mBackgroundPosition.x + (mBackground.getWidth() - mWheel.getWidth() - SPACE_BETWEEN_WHEEL_AND_BAR - ARC_WIDTH) / 2);
		mRadius = mWheel.getWidth()/2.0;
		mWheelPosition = new Point(pos_x, pos_y);
		mWheelCenter = new Point(mWheelPosition.x + (int)mRadius, mWheelPosition.y + (int)mRadius);
		
		// make the Path for the arc
		// find the angle for the inner arc
		double angle = Math.acos(mRadius / (mRadius + (double)SPACE_BETWEEN_WHEEL_AND_BAR));
		// change angle to degrees
		angle *= (180.0/Math.PI);
		// get the complementary angle (which is the one we want)
		angle = 90 - angle;
		//Log.d(TAG, String.format("Angle: %f", angle));
		// make the rects that hold the ovals for the arcs
		// the first (inner) will have a radius equal to the sum of the color wheel radius
		// and the space between the color wheel and the brightness bar
		mInnerArcRect = new RectF(mWheelPosition.x - SPACE_BETWEEN_WHEEL_AND_BAR, 
								  mWheelPosition.y - SPACE_BETWEEN_WHEEL_AND_BAR, 
								  mWheelPosition.x + mWheel.getWidth() + SPACE_BETWEEN_WHEEL_AND_BAR, 
								  mWheelPosition.y + mWheel.getHeight() + SPACE_BETWEEN_WHEEL_AND_BAR);
		// the second (outer) will have a radius equal to the sum of the inner radius
		// and the width of the brightness bar
		mOuterArcRect = new RectF(mInnerArcRect.left - ARC_WIDTH, 
								  mInnerArcRect.top - ARC_WIDTH, 
								  mInnerArcRect.right + ARC_WIDTH, 
								  mInnerArcRect.bottom + ARC_WIDTH);
		// create a path to hold the bar drawing
		mArcPath = new Path();
		// add the first arc
		mArcPath.arcTo(mInnerArcRect, (float)(angle * -1.0), (float)(2.0 * angle));
		// find the angle for the outer arc 
		angle = Math.acos(mRadius / (mRadius + (double)SPACE_BETWEEN_WHEEL_AND_BAR + (double)ARC_WIDTH));
		// change angle to degrees
		angle *= (180.0/Math.PI);
		// get the complementary angle (which is the one we want)
		angle = 90 - angle;
		//Log.d(TAG, String.format("Angle: %f", angle));
		// add the second arc (a line is added in between the end points of the arcs)
		mArcPath.arcTo(mOuterArcRect, (float)angle, (float)(-2.0*angle));
		mArcPath.close();
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - mWheelCenter.x;
        float y = event.getY() - mWheelCenter.y;
        double distanceFromCenter = Math.sqrt(x*x + y*y); 
        boolean inWheel = false;
        boolean inBar = false;
        if (distanceFromCenter <= mRadius) {
        	inWheel = true;
        } else if (distanceFromCenter <= (mRadius + SPACE_BETWEEN_WHEEL_AND_BAR + ARC_WIDTH) &&
        		event.getX() > mWheelCenter.x){
        	inBar = true;
        }
        
        switch (event.getAction()) {
        	// always change to the new color immediately using the appropriate control
            case MotionEvent.ACTION_DOWN:
                isTrackingHSWheel = inWheel;
                isTrackingBBar = inBar;
                if (inWheel) {
                    setCurrentHueSaturation(x, y, distanceFromCenter);
                } else if (inBar) {
                	setCurrentBrightness(event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isTrackingHSWheel) {
                    setCurrentHueSaturation(x, y, distanceFromCenter);
                } else if (isTrackingBBar) {
                	setCurrentBrightness(event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
            	isTrackingHSWheel = false;
                isTrackingBBar = false;
                break;
        }
        return true;
    }
	
	private void setCurrentBrightness(float yPosition) {
		// get the height (in pixels) from the bottom of the bar (wheel too) to the touch
		float touchHeight = (float)mWheelPosition.y + (float)mWheel.getHeight() - yPosition;
		// normalize the height of the touch
		float brightness = touchHeight / (float)mWheel.getHeight();
        
        // replace the brightness with the new value if it is in [0, 1]
        if (0.0f <= brightness && brightness <= 1.0f) {
            // set the new brightness
            mNewColor[2] = brightness;
            updateListener();
            //Log.d(TAG, String.format("Set brightness to: %f", brightness));
            invalidate();
        }
	}
	
	private void setCurrentHueSaturation(float x, float y, double distance) {
		// get the angle of the point in the color wheel
		// note: the (-) is intentional due to the inversion of the y coordinate system (wrt cartesian)
		float angle = -(float)java.lang.Math.atan2(y, x);
		//Log.d(TAG, String.format("Angle Rad: %f", angle));
        if (angle < 0.0f) {
        	angle += (float)(2.0 * Math.PI);
        }
        // change angle to degrees
		float hue = (float)(angle * (180.0/Math.PI));
		//Log.d(TAG, String.format("Angle Deg: %f", hue));
		// normalize the distance from center
        float saturation = (float)(distance / mRadius);
        // replace the hue and saturation with the new values
        mNewColor[0] = hue;
        // only replace the saturation if the value is within range, otherwise the crosshairs will be
        // drawn outside of the wheel
        if (saturation <= 1.0f) {
        	mNewColor[1] = saturation;
        }
        updateListener();
        invalidate();
	}
	
	private void updateListener() {
		if (mListener != null) {
	        mListener.OnColorChanged(Color.HSVToColor(mNewColor));
		}
	}

}
