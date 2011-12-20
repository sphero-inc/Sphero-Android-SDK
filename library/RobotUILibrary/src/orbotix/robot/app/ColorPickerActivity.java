package orbotix.robot.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import orbotix.robot.widgets.HSBColorPickerView;

/**
 * Activity for displaying a color picker dialog which is used to for changing the color LED for Sphero. The
 * Activity broadcasts an intent every time the color is changed and passes the final color back in result's
 * data intent.
 *
 * The AndroidManifest of the client code using this activity needs to have an entry similar to the following:
 * <pre>
 * {@code
 * <activity android:name="orbotix.robot.app.ColorPickerActivity"
 *			android:configChanges="orientation"
 *			android:screenOrientation="portrait"
 *			android:theme="@android:style/Theme.Translucent">
 *		<intent-filter>
 *			<action android:name="android.intent.action.VIEW" />
 *			<category android:name="android.intent.category.DEFAULT" />
 *		</intent-filter>
 * </activity>
 * }
 * </pre>
 */
public class ColorPickerActivity extends Activity {
	/** Action key for broadcast intents that are sent every time the color changes.*/
	public static final String ACTION_COLOR_CHANGE = "orbotix.robot.intent.action.COLOR_CHANGE";
	/** Extra data key for the red color which is sent with the color change broadcast intent and
	 * in the result data intent.
	 */
	public static final String EXTRA_COLOR_RED = "orbotix.robot.intent.extra.COLOR_RED";
	/** Extra data key for the green color which is sent with the color change broadcast intent and
	 * in the result data intent.
	 */
	public static final String EXTRA_COLOR_GREEN = "orbotix.robot.intent.extra.COLOR_GREEN";
	/** Extra data key for the blue color which is sent with the color change broadcast intent and
	 * in the result data intent.
	 */
	public static final String EXTRA_COLOR_BLUE = "orbotix.robot.intent.extra.COLOR_BLUE";

    /**
     * Extra data key for result. When the user clicks "roll", this key will point to a "true" value.
     */
    public static final String EXTRA_ROLL       = "orbotix.robot.intent.extra.ROLL";

	private static final int MAX_VALUE = 255;
	private static final float ROUNDED_CORNER_RADIUS = 15.0f;
	private static final float GLOSS_INTENSITY = 0.2f;
	private static final long DOUBLE_CLICK_DELAY = 200; // millis

	private SeekBar redSeekBar = null;
	private SeekBar greenSeekBar = null;
	private SeekBar blueSeekBar = null;

	// Colors
	private int mPreviousColor;
	private int red = 0;
	private int green = 0;
	private int blue = 0;

	// View Animator
	private ViewAnimator mViewAnimator;

	// RGB text views in hsb picker
	private TextView mRedText;
	private TextView mGreenText;
	private TextView mBlueText;

	// Preview boxes
	private ShapeDrawable mPreviousBoxBackground;
	private ShapeDrawable mNewBoxBackground;

    //private Button mRGBPreviousColorBox;
	//private Button mRGBNewColorBox;

	// RGB Conversion View
	private Handler handler = new Handler();
	private boolean rgbClickedOnce = false;

	// listener for the hsbColorPicker
	private OnColorChangedListener listener = new OnColorChangedListener() {

		@Override
		public void OnColorChanged(int newColor) {
			red = Color.red(newColor);
			green = Color.green(newColor);
			blue = Color.blue(newColor);
			mRedText.setText(Integer.toString(red));
			mGreenText.setText(Integer.toString(green));
			mBlueText.setText(Integer.toString(blue));
            redSeekBar.setProgress(red);
            greenSeekBar.setProgress(green);
            blueSeekBar.setProgress(blue);
			mNewBoxBackground.getPaint().setShader(getPreviewBackground(newColor, mNewBoxBackground.getBounds()));
			mPreviousBoxBackground.getPaint().setShader(getPreviewBackground(mPreviousColor, mPreviousBoxBackground.getBounds()));
			mNewBoxBackground.invalidateSelf();
			mPreviousBoxBackground.invalidateSelf();
			broadcastColorChange();
		}

	};

	/**
	 * Returns a "glossy" background using the given color and the given bounds. Currently,
	 * the gloss is applied vertically from top to bottom.
	 *
	 * @param color the color to be "glossed"
	 * @param rect the bounds of the area to be glossed
	 * @return a {@link LinearGradient} to be used for the background of a color preview
	 */
	private LinearGradient getPreviewBackground(int color, Rect rect) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[1] -= GLOSS_INTENSITY;
		hsv[2] += GLOSS_INTENSITY;
		int glossColor = Color.HSVToColor(hsv);
		return new LinearGradient(rect.left,
								  rect.top,
								  rect.left,
								  rect.bottom,
								  new int[] {color, glossColor, color},
								  new float[] {0.0f, 0.5f, 0.50001f},
								  Shader.TileMode.CLAMP);
	}

	/**
	 * Used by the activity to set it's RGB values.
	 * @param r Red color component.
	 * @param g Green color component.
	 * @param b Blue color component.
	 */
	public void setColor(int r, int g, int b) {
		red = r;
		green = g;
		blue = b;
	}

	/**
	 * Accessor to the red color component.
	 * @return Value from 0 to 255.
	 */
	public int getRed() {
		return red;
	}

	/**
	 * Accessor to the green color component.
	 * @return Value from 0 to 255.
	 */
	public int getGreen() {
		return green;
	}

	/**
	 * Accessor to the blue color component.
	 * @return Value from 0 to 255.
	 */
	public int getBlue() {
		return blue;
	}

    /**
     * When the "roll" button is clicked, close, and also send a "roll" value in the result intent
     * @param v
     */
    public void onRollClick(View v){

        Intent result_intent = new Intent();
		result_intent.putExtra(EXTRA_COLOR_RED, red);
		result_intent.putExtra(EXTRA_COLOR_GREEN, green);
		result_intent.putExtra(EXTRA_COLOR_BLUE, blue);
        result_intent.putExtra(EXTRA_ROLL, true);
		setResult(RESULT_OK, result_intent);
		finish();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.hsb_color_picker);

		Intent intent = getIntent();
		red = intent.getIntExtra(EXTRA_COLOR_RED, 0);
		green = intent.getIntExtra(EXTRA_COLOR_GREEN, 0);
		blue = intent.getIntExtra(EXTRA_COLOR_BLUE, 0);

		mPreviousColor = Color.rgb(red, green, blue);

		setupRedSeekBar();
		setupGreenSeekBar();
		setupBlueSeekBar();

		mViewAnimator = (ViewAnimator)findViewById(R.id.ColorPickerViewAnimator);
		setupPickerItems();
		LinearLayout rgbView = (LinearLayout)findViewById(R.id.RGBConversionLayout);

		// setup double click on the rgb conversion view to switch to the rgb picker
		rgbView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (rgbClickedOnce) {
					mViewAnimator.showNext();
					rgbClickedOnce = false;
				} else {
					rgbClickedOnce = true;
					handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							rgbClickedOnce = false;
						}

					}, DOUBLE_CLICK_DELAY);
				}

			}
		});

        //Show the "roll" button, if specified
        if(intent.getBooleanExtra(EXTRA_ROLL, false)){

            findViewById(R.id.roll_button_layout).setVisibility(View.VISIBLE);
        }
	}

	@Override
	protected void onStart() {
		super.onStart();
		listener.OnColorChanged(Color.rgb(red, green, blue));
	}

	private void setupPickerItems() {
		final HSBColorPickerView hsbPicker = (HSBColorPickerView)findViewById(R.id.ColorPickerBase);
		hsbPicker.setNewColor(Color.rgb(red, green, blue));
		hsbPicker.setOnColorChangedListener(listener);

		// preview boxes
		Button previousColorBox = (Button)findViewById(R.id.previous_color);
		Button newColorBox = (Button)findViewById(R.id.new_color);

		// shapes for the backgrounds of the previews
		float[] cornerRadii = new float[] {ROUNDED_CORNER_RADIUS, ROUNDED_CORNER_RADIUS,
										   ROUNDED_CORNER_RADIUS, ROUNDED_CORNER_RADIUS,
										   ROUNDED_CORNER_RADIUS, ROUNDED_CORNER_RADIUS,
										   ROUNDED_CORNER_RADIUS, ROUNDED_CORNER_RADIUS};
		// previous color preview
		RoundRectShape shape1 = new RoundRectShape(cornerRadii, null, null);
		mPreviousBoxBackground = new ShapeDrawable(shape1);
		previousColorBox.setBackgroundDrawable(mPreviousBoxBackground);
		OnClickListener previousListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int tempColor = mPreviousColor;
				mPreviousColor = Color.rgb(red, green, blue);
				hsbPicker.setNewColor(tempColor);
				listener.OnColorChanged(tempColor);
			}

		};

        previousColorBox.setOnClickListener(previousListener);

		// new color preview
		RoundRectShape shape2 = new RoundRectShape(cornerRadii, null, null);
		mNewBoxBackground = new ShapeDrawable(shape2);
		newColorBox.setBackgroundDrawable(mNewBoxBackground);

		// rgb text views
		mRedText = (TextView)findViewById(R.id.RValueText);
		mGreenText = (TextView)findViewById(R.id.GValueText);
		mBlueText = (TextView)findViewById(R.id.BValueText);
	}

	/**
	 * Captures the back button key to set the result intent and finish the activity.
	 */
	@Override
	public void onBackPressed() {
		finishPickingColor();
	}

    private void finishPickingColor() {
        Intent result_intent = new Intent();
		result_intent.putExtra(EXTRA_COLOR_RED, red);
		result_intent.putExtra(EXTRA_COLOR_GREEN, green);
		result_intent.putExtra(EXTRA_COLOR_BLUE, blue);
		setResult(RESULT_OK, result_intent);
		finish();
    }

	private void setupRedSeekBar() {
		redSeekBar = (SeekBar)findViewById(R.id.RedSeekBar);
		redSeekBar.setMax(MAX_VALUE);
		redSeekBar.setProgress(red);
		redSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				red = progress;
                listener.OnColorChanged(Color.rgb(red, green, blue));
				broadcastColorChange();
			}
		});
	}

	private void setupGreenSeekBar() {
		greenSeekBar = (SeekBar)findViewById(R.id.GreenSeekBar);
		greenSeekBar.setMax(MAX_VALUE);
		greenSeekBar.setProgress(green);
		greenSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				green = progress;
                listener.OnColorChanged(Color.rgb(red, green, blue));
				broadcastColorChange();
			}
		});
	}

	private void setupBlueSeekBar() {
		blueSeekBar = (SeekBar)findViewById(R.id.BlueSeekBar);
		blueSeekBar.setMax(MAX_VALUE);
		blueSeekBar.setProgress(blue);
		blueSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser)
			{
				blue = progress;
                listener.OnColorChanged(Color.rgb(red, green, blue));
				broadcastColorChange();
			}
		});
	}

	private void broadcastColorChange() {
		Intent intent = new Intent(ACTION_COLOR_CHANGE);
		intent.putExtra(EXTRA_COLOR_RED, red);
		intent.putExtra(EXTRA_COLOR_GREEN, green);
		intent.putExtra(EXTRA_COLOR_BLUE, blue);
		sendBroadcast(intent);
	}

	public interface OnColorChangedListener {
		/**
		 * Called when the color changes withing a color picker view.
		 *
		 * @param newColor the new color selected in the color picker view.
		 */
		public void OnColorChanged(int newColor);
	}


}
