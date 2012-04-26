package orbotix.robot.utilities;

import orbotix.robot.app.R;
import orbotix.robot.base.Robot;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

/**
 * Subclass of ArrayAdapter for handling cells in the StartupActivity's robot ListView.
 * @see orbotix.robot.app.StartupActivity
 *
 */
public class RobotArrayAdapter extends ArrayAdapter<Robot> {
	private static final String LOG_TAG = "Robot Picker";
	private static final boolean DEBUG = false;
	
	/**
	 * Constructor
	 */
	public RobotArrayAdapter(Context context) {
		super(context, 0);
	}
	
	/**
	 * Sets the robot name and which robot is under control for the cells of the 
	 * StartupActivity's ListView.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		
		if (view == null) {
			// inflate a new row view
			view = LayoutInflater.from(getContext()).inflate(R.layout.robot_picker_cell, null);
		} 
		
		Robot robot = getItem(position);
		
		if (DEBUG) Log.d(LOG_TAG, "Robot " + robot + " is " + (robot.isKnown() ? "known" : "unknown") + 
				" and is " + (robot.isUnderControl() ? "under control." : "out of control"));
		
		CheckedTextView name_label = (CheckedTextView)view.findViewById(R.id.name_label);
		name_label.setText(robot.getName());
		
		// Add the status for the device
		if(robot.isUnderControl()) {
			name_label.setChecked(true);
		} else {
			name_label.setChecked(false);
		}
		
		return view;
	}
}
