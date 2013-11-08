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
	
	/**
	 * Connection or Setting Button
	 */
	private Button mConnectOrSettingsButton;
	
	/**
	 * Notify the Activity of a click
	 */
	private OnConnectButtonClickListener mOnConnectButtonClickListener;
	
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
		mConnectOrSettingsButton = (Button)this.findViewById(R.id.button_settings_or_connect);
		mConnectOrSettingsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Notify Activity the Connect Button was clicked
				if( mConnectOrSettingsButton.getText().equals("Connect") ) {
					if( mOnConnectButtonClickListener != null ) {
						mOnConnectButtonClickListener.onConnectClick();
					}
				}
				else {
					if( mOnConnectButtonClickListener != null ) {
						mOnConnectButtonClickListener.onSettingsClick();
					}
				}
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
	
	/**
	 * Switch to Settings button
	 */
	public void switchToSettingsButton() {
		mConnectOrSettingsButton.setText("Settings");
	}
	
	/**
	 * Switch to Settings button
	 */
	public void switchToConnectButton() {
		mConnectOrSettingsButton.setText("Connect");
	}
	
	/**
	 * Set the click listener
	 * @param listener
	 */
	public void setOnConnectButtonClickListener(OnConnectButtonClickListener listener) {
		mOnConnectButtonClickListener = listener;
	}
	
    /**
     * A listener to be run when the connect button is clicked, so the activity can connect
     */
    public interface OnConnectButtonClickListener {
        public void onConnectClick();
        public void onSettingsClick();
    }
}
