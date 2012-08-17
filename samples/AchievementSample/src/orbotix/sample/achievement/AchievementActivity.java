package orbotix.sample.achievement;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import orbotix.achievement.AchievementManager;
import orbotix.achievement.SpheroWorldWebView;

public class AchievementActivity extends Activity
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);      
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Initialize the AchievementManager with the app's identification info
        AchievementManager.setupApplication("achi1f9d344ef233e3dc7b76a54d3471832d", "5zermu5Qp8cKEqpcybQP", this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        
        //Clean up AchievementManager. It is important to do this in this onStop method of your Activity.
        AchievementManager.onStop();
    }

    /**
     * When the user clicks on the "RED" button, have the AchievementManager send these events to 
     * SpheroWorld to record the user's progress toward these achievements.
     * @param v
     */
    public void onRedClick(View v){
        AchievementManager.recordEvent("red");
    }

    /**
     * When the user clicks on the "GREEN" button, have the AchievementManager send the event to SpheroWorld to
     * record the user's progress toward achievements.
     * @param v
     */
    public void onGreenClick(View v){
        AchievementManager.recordEvent("green");
    }

    /**
     * When the user clicks on the "BLUE" button, have the AchievementManager send the event to SpheroWorld to
     * record the user's progress toward achievements.
     * @param v
     */
    public void onBlueClick(View v){
        AchievementManager.recordEvent("blue");
    }

    /**
     * When the user clicks on the "SpheroWorld" button, open the SpheroWorld login site in the SpheroWorldWebView
     * Activity
     * @param v
     */
    public void onSpheroWorldClick(View v){
        
        Intent i = new Intent(this, SpheroWorldWebView.class);
        startActivity(i);
    }
}
