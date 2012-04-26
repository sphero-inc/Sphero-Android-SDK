package orbotix.robot.app;

import android.view.View;
import orbotix.robot.base.RobotControl;
import orbotix.robot.base.RobotProvider;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import orbotix.robot.widgets.calibration.CalibrationDialView;

/**
 * Activity for displaying a calibration wheel which is used to align the heading on the mobile device
 * with the Orbotix device.
 *
 * The AndroidManifest of the client code using this activity needs to have an entry similar to the folowing:
 * <pre>
 * {@code
 * <activity android:name="orbotix.robot.app.CalibrationActivity"
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
 * <h1>Layout Customization</h1>
 * <p>
 * Some of the layout elements can be customized by passing in resource identifiers for {@link Drawable} resources
 * before starting the new intent. Currently, the customizable elements of the {@link CalibrationActivity} are:
 * <p>
 * <ul><li>Background Image - {@link EXTRA_BACKGROUND_RESOURCE_ID}
 * <li>Title Icon - {@link EXTRA_TITLE_ICON_RESOURCE_ID}
 * <li>Title Text - {@link EXTRA_TITLE_TEXT_RESOURCE_ID}
 * </ul>
 * To use a custom title icon you can simply add the resource identifier to the extras bundle in the {@link Intent}
 * similar to the following:
 * <pre>
 * {@code
 * // create an intent for the CalibrationActivity
 * Intent calibration_intent = new Intent(this, CalibrationActivity.class);
 *
 * // specify the REQUIRED robot id
 * calibration_intent.putExtra(CalibrationActivity.ROBOT_ID_EXTRA, myRobotId);
 *
 * // add your custom resource identifier to the intent's extras bundle
 * calibration_intent.putExtra(CalibrationActivity.EXTRA_TITLE_ICON_RESOURCE_ID, R.drawable.my_title_icon);
 *
 * // start the activity
 * startActivity(calibration_intent);
 * }
 * </pre>
 */
public class CalibrationActivity extends Activity implements CalibrationDialView.CalibrationEventListener {
	/** Extra data key used to pass the Robot's id in the activity's start intent. REQUIRED */
	public static final String ROBOT_ID_EXTRA = "orbotix.robot.cal.intent.extra.ID";
	/** Extra data key for a resource id to be used for the background image of this activity. */
	public static final String EXTRA_BACKGROUND_RESOURCE_ID = "orbotix.robot.cal.intent.extra.BACKGROUND";
	/** Extra data key for a resource id to be used for the title icon of this activity. */
	public static final String EXTRA_TITLE_ICON_RESOURCE_ID = "orbotix.robot.cal.intent.extra.TITLE_ICON";
	/** Extra data key for a resource id to be used for the title text of this activity. */
	public static final String EXTRA_TITLE_TEXT_RESOURCE_ID = "orbotix.robot.cal.intent.extra.TITLE_TEXT";

	private CalibrationDialView calibrationView;
	private RobotControl robotControl;

	// customizable layout elements
	private ViewGroup backgroundLayout;
	private ImageView dialogIconImageView;
	private TextView dialogTitleTextView;

	/**
	 * Sets the background image of the {@link CalibrationActivity} to the {@link Drawable} found
	 * at the specified resource id.
	 *
	 * @see android.view.View#setBackgroundResource(int) latency warning for bigger drawables
	 * @param resourceId the resource identifier of the {@link Drawable}
	 */
	public void setBackgroundImageResource(int resourceId) {
		backgroundLayout.setBackgroundResource(resourceId);
	}

	/**
	 * Sets the title icon image of the {@link CalibrationActivity} to the {@link Drawable} found
	 * at the specified resource id.
	 *
	 * @see ImageView#setImageResource(int) latency warning for bigger drawables
	 * @param resourceId the resource identifier of the {@link Drawable}
	 */
	public void setTitleIconImageResource(int resourceId) {
		dialogIconImageView.setImageResource(resourceId);
	}

	/**
	 * Sets the title text of the {@link CalibrationActivity} to the string resource corresponding
	 * to the given resource id.
	 *
	 * @param resourceId the resource identifier of the String to be used for the title's text
	 */
	public void setTitleTextResource(int resourceId) {
		dialogTitleTextView.setText(resourceId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.calibrate);

		// get references to customizable layout elements
		backgroundLayout = (ViewGroup)findViewById(R.id.BackgroundLayout);
		dialogIconImageView = (ImageView)findViewById(R.id.TitleIcon);
		dialogTitleTextView = (TextView)findViewById(R.id.TitleText);

		// load customized layout elements if specified
		int titleIconResourceId = getIntent().getIntExtra(EXTRA_TITLE_ICON_RESOURCE_ID, 0);
		if (titleIconResourceId != 0) {
			setTitleIconImageResource(titleIconResourceId);
		}

		int backgroundResourceId = getIntent().getIntExtra(EXTRA_BACKGROUND_RESOURCE_ID, 0);
		if (backgroundResourceId != 0) {
			setBackgroundImageResource(backgroundResourceId);
		}

		int titleTextResourceId = getIntent().getIntExtra(EXTRA_TITLE_TEXT_RESOURCE_ID, 0);
		if (titleTextResourceId != 0) {
			setTitleTextResource(titleTextResourceId);
		} else {
			setTitleTextResource(R.string.calibration_title);
		}

		calibrationView = (CalibrationDialView)findViewById(R.id.CalibrationView);
		calibrationView.setCalibrationEventListener(this);

		Intent intent = getIntent();
		String robot_id = intent.getStringExtra(ROBOT_ID_EXTRA);
		if (robot_id != null) {
			RobotProvider provider = RobotProvider.getDefaultProvider();
			robotControl = provider.getRobotControl(provider.findRobot(robot_id));
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (robotControl != null) {
			robotControl.startCalibration();
		}
	}

	/**
	 * CalibrationEventListener method implementation.
	 * @see orbotix.robot.widgets.calibration.CalibrationDialView
	 */
	@Override
	public void onCalibrationStart() {
	}

	/**
	 * CalibrationEventListener method implementation. Issues the stop calibration command.
	 * @see orbotix.robot.widgets.calibration.CalibrationDialView
	 */
	@Override
	public void onCalibrationStop(boolean calibrate) {
		if (robotControl != null) {
			robotControl.stopCalibration(calibrate);
		}
		finish();
	}

    public void onDoneButtonClicked(View v) {
        if (robotControl != null) {
			robotControl.stopCalibration(true);
		}
		finish();
    }

    @Override
    public void onBackPressed() {
        // this will cancel the calibration
        if (robotControl != null) {
			robotControl.stopCalibration(false);
		}
		finish();
    }

	/**
	 * CalibrationEventListener method implementation. Rotates a Sphero.
	 * @see orbotix.robot.widgets.calibration.CalibrationDialView
	 */
	@Override
	public void onHeadingRotation(float heading) {
		if (robotControl != null) {
			robotControl.rotate(heading);
		}
	}

}
