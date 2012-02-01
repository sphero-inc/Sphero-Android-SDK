package orbotix.twophones;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import orbotix.achievement.AchievementManager;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.*;
import orbotix.robot.multiplayer.LocalMultiplayerClient;
import orbotix.robot.multiplayer.RemotePlayer;
import orbotix.robot.widgets.ControllerActivity;
import orbotix.robot.widgets.calibration.CalibrationView;
import orbotix.robot.widgets.joystick.JoystickView;
import orbotix.twophones.util.LogUtil;
import org.json.JSONObject;

import java.util.List;

public class ConnectionActivity extends ControllerActivity
{

    /**
     * ID for launching the StartupActivity
     */
    private final int STARTUP_ACTIVITY = 0;

    /**
     * Sphero
     */
    private Robot mRobot = null;
    
    private final static String sGameName = "TwoPhonesOneBall Game";
    
    private boolean mGameRunning = false;

    private JoystickView mJoystick;
    private CalibrationView mCalibration;

    /**
     * LocalMultiplayerClient communicates with the MultiplayerManager service through intents.
     */
    private LocalMultiplayerClient mMultiplayerClient;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        AchievementManager.setupApplication("twop8989ffcfb7b8591a5c7ec656ad266b6f", "94RAt5xxt2wxiYgf5LDl", this);

        //Instantiate the LocalMultiplayerClient
        mMultiplayerClient = new LocalMultiplayerClient(this);

        //Show the startup activity when the service comes online
        mMultiplayerClient.setOnOnlineListener(new LocalMultiplayerClient.OnOnlineListener() {
            @Override
            public void onOnline(Context context) {
                showStartupActivity();
            }
        });
        
        //If an error comes from the client, ask the player to check wifi connection
        mMultiplayerClient.setOnConnectionErrorListener(new LocalMultiplayerClient.OnConnectionErrorListener() {
            @Override
            public void onConnectionError(Context context, Exception e) {
                Toast.makeText(context, "There was a connection error. Please check your wifi settings.", Toast.LENGTH_LONG).show();
            }
        });

        //When a device command is received from the client, post that to current robot
        mMultiplayerClient.setOnDeviceCommandReceivedListener(new LocalMultiplayerClient.OnDeviceCommandReceivedListener() {
            @Override
            public void onDeviceCommandReceived(Context context, JSONObject command_json) {

                if(mRobot != null){
                    DeviceCommand d = DeviceCommandFactory.createFromJson(command_json);
                    DeviceMessenger.getInstance().postCommand(mRobot, d);
                }
            }
        });

        mMultiplayerClient.setOnPlayersChangedListener(new LocalMultiplayerClient.OnPlayersChangedListener() {
            @Override
            public void onPlayersChanged(Context context, List<RemotePlayer> players) {

                if(mMultiplayerClient.getIsHost()){
                    if(!mGameRunning){
                        if(players.size() > 1){

                            mMultiplayerClient.startGame();

                            mGameRunning = true;
                        }
                    }else if(players.size() == 1){

                        mMultiplayerClient.returnToLobby();

                        mGameRunning = false;
                    }
                }

            }
        });

        //Set unique session id
        mMultiplayerClient.setSessionId("twop8989");

        //Start multiplayer service
        mMultiplayerClient.open();

        //Get the JoystickView
        mJoystick = (JoystickView)findViewById(R.id.joystick);
        mJoystick.setEnabled(false);
        addController(mJoystick);
        
        //Get CalibrationView
        mCalibration = (CalibrationView)findViewById(R.id.calibration);
        mCalibration.setEnabled(false);
        addController(mCalibration);

    }

    @Override
    protected void onStop() {
        super.onStop();

        //Stop LocalMultiplayerClient
        mMultiplayerClient.close();

        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == STARTUP_ACTIVITY){

            if(resultCode == RESULT_OK){

                //Connect to Sphero
                final String robot_id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);
                
                if(robot_id != null && !robot_id.equals("")){
                    mRobot = RobotProvider.getDefaultProvider().findRobot(robot_id);
                }
            }


            if(mRobot == null){
                //Didn't connect. Find a game to join.
                LogUtil.d("No robot specified. Looking for game to join.");
                mMultiplayerClient.requestAvailableGames();

            }else{
                //Connected. Host a game.
                LogUtil.d("Robot connected. Hosting game.");
                mMultiplayerClient.hostNewGame(sGameName);

                //Give the robot to the controllers
                mJoystick.setRobot(mRobot);
                mCalibration.setRobot(mRobot);

                //Set the controls to drive the ball.
                drive();
            }
        }
    }

    public void showStartupActivity(){

        Intent i = new Intent(this, StartupActivity.class);
        startActivityForResult(i, STARTUP_ACTIVITY);
    }

    public void drive(){

        mJoystick.setVisibility(View.VISIBLE);
        mJoystick.setEnabled(true);

        mCalibration.setVisibility(View.VISIBLE);
        mCalibration.setEnabled(true);
    }
    
    
}
