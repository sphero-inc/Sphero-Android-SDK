package orbotix.robot.app;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotControl;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.utilities.RobotArrayAdapter;

/**
 * Activity for initiating a connection to a Sphero. This activity is used at start up to start device discovery and
 * connects to a Sphero if only one is found, otherwise it gives the user the choice to select from a
 * list of Spheros.
 *
 * The AndroidManifest of the client code using this activity needs to have an entry similar to the following:
 * <pre>
 * {@code
 * <activity android:name="orbotix.robot.app.StartupActivity"
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
public class StartupActivity extends Activity {
	/** Extra data key for a robot unique id that is used to select automatically from multiple Spheros */
	public static final String EXTRA_ROBOT_ID = "orbotix.robot.startup.intent.extra.ROBOT_ID";
    public static final String EXTRA_FLURRY_ENABLED = "orbotix.robot.FLURRY_ID";
    public static final String EXTRA_RETURN_ON_NONE_PAIRED = "orbotix.robot.NONE_PAIRED_SKIP";
    public static final String ACTION_FLURRY_LOG_BUY_SPHERO = "orbotix.sphero.BUY";

    private static final int ENABLE_ROBOT_ADAPTER_REQUEST = 0;
    private static final int BLUETOOTH_SETTINGS_REQUEST = 1;
    public static final int RESULT_NONE_PAIRED = 789;

    private static final String RETRY_BUTTON_TEXT = "Retry";
    private static final String CONNECT_BUTTON_TEXT = "Connect";
    private static final String SETTINGS_BUTTON_TEXT = "Go To Settings";
    private static final String STARTUP_PREFERENCES = "orbotix.sphero.STARTUP_PREFS";
    private static final String PREFERENCE_NEVER_PAIRED = "orbotix.sphero.NEVER_PAIRED";

	private final RobotProvider robotProvider = RobotProvider.getDefaultProvider();
    private RobotArrayAdapter robotArrayAdapter;
    private SharedPreferences preferences;

	private TextView statusLabel;
	private Button mainButton;
    private Button buyButton;
	private ListView robotList;
    private ProgressBar progressBar;
    private boolean flurryEnabled;
    private boolean returnOnNonePaired;

	// customizable layout elements
	private ViewGroup backgroundLayout;
	private ImageView titleIconImageView;
	private TextView dialogTitleTextView;
    private int mRetryBackgroundResourceId;
    private int mConnectBackgroundResourceId;
    private int mSettingsBackgroundResourceId;

	private String robotId = null;
	private boolean connectionStarted = false;

    private OnClickListener connectListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mainButton.setVisibility(View.INVISIBLE);
            if (robotProvider.getRobots().size() == 0) {
                statusLabel.setText(R.string.startup_scanning);
                robotProvider.initiateConnection(robotId);
            } else if (robotProvider.getRobotControls().size() > 0) {
                robotList.setOnItemClickListener(null);
                robotProvider.connectControlledRobots();
            }
        }
    };

    private OnClickListener retryListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mainButton.setVisibility(View.INVISIBLE);
            robotProvider.connectControlledRobots();
        }
    };

    private OnClickListener settingsListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent settings_intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(settings_intent);
            finish();
        }
    };

    private final BroadcastReceiver providerBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(RobotProvider.ACTION_ROBOT_CONNECTING)) {
                progressBar.setVisibility(View.VISIBLE);
                buyButton.setVisibility(View.INVISIBLE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREFERENCE_NEVER_PAIRED, false);
                editor.commit();
				Robot robot = robotProvider.findRobot(intent.getStringExtra(RobotProvider.EXTRA_ROBOT_ID));
				StringBuilder builder = new StringBuilder();
				builder.append(StartupActivity.this.getString(R.string.startup_connecting));
				builder.append(" ");
				builder.append(robot.getName());
				statusLabel.setText(builder.toString());
				robotArrayAdapter.notifyDataSetChanged();
			} else if (action.equals(RobotProvider.ACTION_ROBOT_CONNECT_SUCCESS)) {
                progressBar.setVisibility(View.INVISIBLE);
				Robot robot = robotProvider.findRobot(intent.getStringExtra(RobotProvider.EXTRA_ROBOT_ID));
				Intent data = new Intent();
				data.putExtra(EXTRA_ROBOT_ID, robot.getUniqueId());
				StartupActivity.this.setResult(RESULT_OK, data);
                robotProvider.setBroadcastContext(null);
				finish();
			} else if (action.equals(RobotProvider.ACTION_ROBOT_MULTIPLE_FOUND)) {
                progressBar.setVisibility(View.INVISIBLE);
                buyButton.setVisibility(View.INVISIBLE);
				statusLabel.setText(R.string.startup_select_robot);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREFERENCE_NEVER_PAIRED, false);
                editor.commit();
                setMainButtonText(CONNECT_BUTTON_TEXT);
                buyButton.setVisibility(View.INVISIBLE);
                mainButton.setOnClickListener(connectListener);
		    	robotList.setOnItemClickListener(deviceClickListener);
                if (robotId != null) {
                    robotProvider.control(robotProvider.findRobot(robotId));
                    robotArrayAdapter.notifyDataSetChanged();
                    robotProvider.connectControlledRobots();
                }
			} else if (action.equals(RobotProvider.ACTION_ROBOT_CONNECT_FAILED)) {
                progressBar.setVisibility(View.INVISIBLE);
				statusLabel.setText(R.string.startup_connection_failed);
                setMainButtonText(RETRY_BUTTON_TEXT);
                buyButton.setVisibility(View.INVISIBLE);
                mainButton.setOnClickListener(retryListener);
                mainButton.setVisibility(View.VISIBLE);
		    	robotList.setOnItemClickListener(deviceClickListener);
			} else if (action.equals(RobotProvider.ACTION_ROBOT_NONE_FOUND)) {
                if (returnOnNonePaired) {
                    returnToAppWithNonePaired();
                    return;
                }
                progressBar.setVisibility(View.INVISIBLE);
				statusLabel.setText(R.string.startup_no_robots);
                setMainButtonText(RETRY_BUTTON_TEXT);
                mainButton.setOnClickListener(connectListener);
                if (preferences.getBoolean(PREFERENCE_NEVER_PAIRED, true)) {
                    buyButton.setVisibility(View.VISIBLE);
                }
				mainButton.setVisibility(View.VISIBLE);
            } else if (action.equals(RobotProvider.ACTION_ROBOT_NONE_PAIRED)) {
                if (returnOnNonePaired) {
                    returnToAppWithNonePaired();
                    return;
                }
                progressBar.setVisibility(View.INVISIBLE);
				statusLabel.setText(R.string.startup_no_paired_robots);
                setMainButtonText(SETTINGS_BUTTON_TEXT);
                if (preferences.getBoolean(PREFERENCE_NEVER_PAIRED, true)) {
                    buyButton.setVisibility(View.VISIBLE);
                }
                mainButton.setOnClickListener(settingsListener);
				mainButton.setVisibility(View.VISIBLE);
			}
		}
	};

    private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Robot.ACTION_FOUND.equals(action)) {
				String robot_address = intent.getStringExtra(Robot.EXTRA_ROBOT_ID);
				Robot robot = robotProvider.findRobot(robot_address);
				if (robot != null) {
					robotArrayAdapter.add(robot);
				}
			}
		}
    };

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener deviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	Robot robot = robotArrayAdapter.getItem(position); // account for header view
//        	if (robot.isUnderControl()) {
//        		return;
//        	}
        	mainButton.setVisibility(View.VISIBLE);
        	// release previous controlled robot
        	if (robotProvider.hasRobotControl()) {
        		RobotControl control = robotProvider.getRobotControls().get(0);
        		robotProvider.disconnectControlledRobots();
        		robotProvider.removeControl(control);
        	}

        	// Pass the Robot object to robot provider to start controlling the robot
            robotProvider.control(robot);
            robotArrayAdapter.notifyDataSetChanged();
        }
    };

    public void buyButtonPressed(View v) {
        if (flurryEnabled) {
            Intent buyClicked = new Intent(ACTION_FLURRY_LOG_BUY_SPHERO);
            sendBroadcast(buyClicked);
        }
        String url = "http://store.gosphero.com";
        Intent buySpheroIntent = new Intent(Intent.ACTION_VIEW);
        buySpheroIntent.setData(Uri.parse(url));
        startActivity(buySpheroIntent);
    }


    private void startConnection() {
    	if (connectionStarted) return;
    	connectionStarted = true;

		IntentFilter connecting_filter = new IntentFilter(RobotProvider.ACTION_ROBOT_CONNECTING);
		registerReceiver(providerBroadcastReceiver, connecting_filter);
		IntentFilter connect_success_filter = new IntentFilter(RobotProvider.ACTION_ROBOT_CONNECT_SUCCESS);
		registerReceiver(providerBroadcastReceiver, connect_success_filter);
		IntentFilter connect_failed_filter = new IntentFilter(RobotProvider.ACTION_ROBOT_CONNECT_FAILED);
		registerReceiver(providerBroadcastReceiver, connect_failed_filter);
		IntentFilter none_found_filter = new IntentFilter(RobotProvider.ACTION_ROBOT_NONE_FOUND);
		registerReceiver(providerBroadcastReceiver, none_found_filter);
		IntentFilter connect_multiple_filter = new IntentFilter(RobotProvider.ACTION_ROBOT_MULTIPLE_FOUND);
		registerReceiver(providerBroadcastReceiver, connect_multiple_filter);
		IntentFilter none_paired_filter = new IntentFilter(RobotProvider.ACTION_ROBOT_NONE_PAIRED);
		registerReceiver(providerBroadcastReceiver, none_paired_filter);

		IntentFilter discovery_filter = new IntentFilter(Robot.ACTION_FOUND);
		registerReceiver(discoveryReceiver, discovery_filter);

		// use this to enable discovery mode
		//robotProvider.initiateConnection(robotId);

		// use this to show and connect to paired devices only
		robotProvider.findRobots();
    }

    /**
	 * Sets the text of the mainButton to the string new text provided
	 *
	 * @param newText the new text to be used in the button
	 */
	private void setMainButtonText(String newText) {
		mainButton.setText(newText);
	}

    private void returnToAppWithNonePaired() {
        setResult(RESULT_NONE_PAIRED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch (requestCode) {
    	case ENABLE_ROBOT_ADAPTER_REQUEST:
    		if (resultCode == RESULT_OK) {
    			startConnection();
    		} else {
    			finish();
    		}
			break;
        case BLUETOOTH_SETTINGS_REQUEST:
            // do nothing for now
            break;
    	}
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        flurryEnabled = getIntent().getBooleanExtra(EXTRA_FLURRY_ENABLED, false);
        returnOnNonePaired = getIntent().getBooleanExtra(EXTRA_RETURN_ON_NONE_PAIRED, false);

		setContentView(R.layout.startup);
        preferences = getSharedPreferences(STARTUP_PREFERENCES, Context.MODE_PRIVATE);

		// get references to customizable layout elements
        progressBar = (ProgressBar)findViewById(R.id.StartupProgress);
		statusLabel = (TextView)findViewById(R.id.StatusLabel);
        mainButton = (Button)findViewById(R.id.RetryConnectButton);
        buyButton = (Button)findViewById(R.id.BuyButton);

        // Initialize array adapter.
        robotArrayAdapter = new RobotArrayAdapter(this);

        // setup list view
    	robotList = (ListView) findViewById(R.id.RobotList);
    	robotList.setAdapter(robotArrayAdapter);

		robotId = getIntent().getStringExtra(EXTRA_ROBOT_ID);
		robotProvider.setBroadcastContext(this);
  	}

	@Override
	protected void onStart() {
		super.onStart();

		if (robotProvider.isAdapterEnabled()) {
			startConnection();
		} else {
			Intent intent = robotProvider.getAdapterIntent();
			startActivityForResult(intent, ENABLE_ROBOT_ADAPTER_REQUEST);
		}
	}

    @Override
    protected void onStop() {
        super.onStop();
        if (connectionStarted) {
            try {
                unregisterReceiver(providerBroadcastReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            try {
                unregisterReceiver(discoveryReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else {
            robotProvider.setBroadcastContext(null);
        }
    }
}
