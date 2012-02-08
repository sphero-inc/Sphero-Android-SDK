package orbotix.twophones;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;

/**
 * Created by Orbotix Inc.
 * Date: 2/6/12
 *
 * @author Adam Williams
 */
public class ColorButtonView extends RelativeLayout {

    /**
     * The Robot to control, containing either a DirectControlStrategy, or a MultiplayerControlStrategy
     */
    private Robot mRobot = null;
    
    private Button mRedButton = null;
    private Button mGreenButton = null;
    private Button mBlueButton = null;

    private boolean mEnabled = true;
    
    public ColorButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.color_button_view, this);

        //Get buttons and set click listeners that send color values to Robot
        mRedButton   = (Button)findViewById(R.id.red_button);
        mRedButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onRedClicked();
            }
        });

        mGreenButton = (Button)findViewById(R.id.green_button);
        mGreenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onGreenClicked();
            }
        });

        mBlueButton  = (Button)findViewById(R.id.blue_button);
        mBlueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBlueClicked();
            }
        });
        
    }

    public void setRobot(Robot robot){
        mRobot = robot;
    }

    /**
     * Makes the current robot go red
     */
    public void onRedClicked(){
        
        sendColor(255, 0, 0);
    }

    /**
     * Makes current robot go green
     */
    public void onGreenClicked(){
       sendColor(0, 255, 0);
    }

    /**
     * Makes current robot go blue
     */
    public void onBlueClicked(){
        sendColor(0,0,255);
    }

    public void setEnabled(boolean val){
        super.setEnabled(val);
        mEnabled = val;
    }

    /**
     * Sends the provided color values to the Robot
     * @param r
     * @param g
     * @param b
     */
    private void sendColor(int r, int g, int b){
        
        if(mRobot != null && mEnabled){
            RGBLEDOutputCommand c = new RGBLEDOutputCommand(r, g, b);
            mRobot.doCommand(c, 0);
        }
    }

}
