package orbotix.robot.widgets;

import orbotix.uisample.R;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class NoSpheroConnectedView extends RelativeLayout {

	/**
	 * Custom URL to for developer referral program 
	 */
	private String mCustomURL = "http://store.gosphero.com";
	
	public NoSpheroConnectedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupView(context);
	}
	
	public NoSpheroConnectedView(Context context) {
		super(context);
		setupView(context);
	}
	
	/**
	 * Call to set up the UI of the View
	 * @param context passed from the constructor
	 */
	private void setupView(final Context context) {
		// Inflate the layout from XML 
		LayoutInflater.from(context).inflate(R.layout.no_sphero_connected, this);
		
		// Set up button to open browser url
		Button getASpheroButton = (Button)this.findViewById(R.id.button_get_a_sphero);
		getASpheroButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Open the URL
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mCustomURL));
				context.startActivity(browserIntent);
			}
		});
		
		// Set up button to open browser url
		Button settingsButotn = (Button)this.findViewById(R.id.button_settings);
		settingsButotn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Open the Bluetooth Settings Intent
				Intent settingsIntent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
				context.startActivity(settingsIntent);
			}
		});
	}
	
	/**
	 * Set the custom URL for developer referral program
	 * @param url
	 */
	public void setCustomURL(String url) {
		mCustomURL = url;
	}
	
	/**
	 * Getter for the customer URL
	 * @return the current custom URL
	 */
	public String getCustomURL() {
		return mCustomURL;
	}
}
