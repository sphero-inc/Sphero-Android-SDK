package orbotix.twophones;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.DeviceCommand;
import orbotix.robot.base.DeviceCommandFactory;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.multiplayer.LocalMultiplayerClient;
import orbotix.multiplayer.MultiplayerControlStrategy;
import orbotix.multiplayer.MultiplayerGame;
import orbotix.multiplayer.RemotePlayer;
import orbotix.robot.widgets.ControllerActivity;
import orbotix.robot.widgets.calibration.CalibrationView;
import orbotix.robot.widgets.joystick.JoystickView;
import orbotix.twophones.util.LogUtil;
import org.json.JSONException;
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
    private Button mPassButton;
    private ColorButtonView mColorButtonView;
    
    private boolean mDriving = false;
    
    private volatile boolean mLookingForGame = false;

    /**
     * LocalMultiplayerClient communicates with the MultiplayerManager service through intents.
     */
    private LocalMultiplayerClient mMultiplayerClient;


    //MultiplayerClient listeners

    /**
     * Shows the StartupActivity when the MultiplayerManager Service comes online
     */
    private LocalMultiplayerClient.OnOnlineListener mOnOnlineListener = new LocalMultiplayerClient.OnOnlineListener() {
        @Override
        public void onOnline(Context context) {
            showStartupActivity();
        }
    };

    /**
     * When a device command is received, run it on the local robot
     */
    private LocalMultiplayerClient.OnDeviceCommandReceivedListener mOnDeviceCommandReceivedListener = new LocalMultiplayerClient.OnDeviceCommandReceivedListener() {
        @Override
        public void onDeviceCommandReceived(Context context, JSONObject command_json) {

            if(mRobot != null && mMultiplayerClient.getIsHost()){
                DeviceCommand d = DeviceCommandFactory.createFromJson(command_json);
                mRobot.doCommand(d, 0);
            }
        }
    };

    /**
     * When the Client gets games, join a game, if appropriate
     */
    private LocalMultiplayerClient.OnGetGamesListener mOnGetGamesListener = new LocalMultiplayerClient.OnGetGamesListener() {
        @Override
        public void onGetGames(Context context, List<MultiplayerGame> games) {
            
            if(!mMultiplayerClient.getIsHost() && mRobot == null && games.size() > 0){

                //Join the first game
                joinGame(games.get(0));
            }
        }
    };

    /**
     * When the game starts, setup the UI based on whether or not the user is the host
     */
    private LocalMultiplayerClient.OnGameStartListener mOnGameStartListener = new LocalMultiplayerClient.OnGameStartListener() {
        @Override
        public void onGameStart(Context context) {
            
            if(mMultiplayerClient.getIsHost()){

                hideHostingMessage();
                drive();
            }else {

                hideJoiningMessage();
                controlColors();
            }
        }
    };

    /**
     * When the Client gets a list of players, start the game, if appropriate
     */
    private LocalMultiplayerClient.OnPlayersChangedListener mOnPlayersChangedListener = new LocalMultiplayerClient.OnPlayersChangedListener() {
        @Override
        public void onPlayersChanged(Context context, List<RemotePlayer> players) {

            if(mMultiplayerClient.getIsHost()){
                if(!mGameRunning){
                    if(players.size() > 1){


                        //Player connected, so start current game
                        mMultiplayerClient.startGame();

                        mGameRunning = true;
                    }
                }else if(players.size() == 1){

                    //Player disconnected, so host new game.
                    
                    hideControls();
                    showHostingMessage();

                    mMultiplayerClient.leaveGame();
                    
                    mMultiplayerClient.hostNewGame(sGameName);

                    mGameRunning = false;
                }
            }else{


                if(players.size() < 2){

                    //Player disconnected, so join new game
                    lookForGames();
                    
                    mGameRunning = false;
                }else{

                    //Get the first player that is not yourself
                    for(RemotePlayer p : players){
                        if(!mMultiplayerClient.isLocalPlayer(p)){

                            //Set the controllers' robot to this players' robot
                            Robot robot = new Robot(new MultiplayerControlStrategy(mMultiplayerClient, p));
                            //Register with robot provider
                            RobotProvider.getDefaultProvider().control(robot);

                            robot.setConnected(true);
                            setRobot(robot);
                            break;
                        }
                    }
                }

            }
        }
    };

    /**
     * When game data is received, check for a "pass" request in the data, and then swap color changing for driving
     */
    private LocalMultiplayerClient.OnGameDataReceivedListener mOnGameDataReceivedListener = new LocalMultiplayerClient.OnGameDataReceivedListener() {
        @Override
        public void onGameDataReceived(Context context, JSONObject game_data, RemotePlayer player) {

            try {
                
                //If the game data contains the "pass" request, then switch modes
                if(game_data.has("PASS") && game_data.get("PASS").equals("ur turn")){
                    
                    if(mDriving){
                        controlColors();
                    }else{
                        drive();
                    }
                }
            } catch (JSONException e) {
                LogUtil.e("Failed to get a value from game data JSON.", e);
            }
        }
    };

    /**
     * Shows a friendly error when there is a connection problem
     */
    private LocalMultiplayerClient.OnConnectionErrorListener mOnConnectionErrorListener = new LocalMultiplayerClient.OnConnectionErrorListener() {
        @Override
        public void onConnectionError(Context context, Exception e) {
            Toast.makeText(context, "There was a connection error. Please check your wifi settings.", Toast.LENGTH_LONG).show();
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Instantiate the LocalMultiplayerClient
        mMultiplayerClient = new LocalMultiplayerClient(this);

        //Show the startup activity when the service comes online
        mMultiplayerClient.setOnOnlineListener(mOnOnlineListener);
        
        //If an error comes from the client, ask the player to check wifi connection
        mMultiplayerClient.setOnConnectionErrorListener(mOnConnectionErrorListener);

        //When a device command is received from the client, post that to current robot
        mMultiplayerClient.setOnDeviceCommandReceivedListener(mOnDeviceCommandReceivedListener);

        //When new games show up, decide whether to join one
        mMultiplayerClient.setOnGetGamesListener(mOnGetGamesListener);

        mMultiplayerClient.setOnPlayersChangedListener(mOnPlayersChangedListener);

        mMultiplayerClient.setOnGameStartListener(mOnGameStartListener);

        //When game data is received, check for a "pass" request in the data, and then swap color changing for driving
        mMultiplayerClient.setOnGameDataReceivedListener(mOnGameDataReceivedListener);



        //Set unique session id
        mMultiplayerClient.setSessionId("TwoPhone");

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
        
        mColorButtonView = (ColorButtonView)findViewById(R.id.color_button_view);

        mPassButton = (Button)findViewById(R.id.pass_button);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mMultiplayerClient.leaveGame();

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
                lookForGames();

                showJoiningMessage();

            }else{
                //Connected. Host a game.
                LogUtil.d("Robot connected. Hosting game.");
                showHostingMessage();

                mMultiplayerClient.setLocalPlayer(new RemotePlayer("TPOB Android Host Player", 0, true, true, true));
                mMultiplayerClient.hostNewGame(sGameName);

                //Give the robot to the controllers
                setRobot(mRobot);
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
        mJoystick.invalidate();

        mCalibration.setVisibility(View.VISIBLE);
        mCalibration.setEnabled(true);
        mCalibration.invalidate();

        mPassButton.setVisibility(View.VISIBLE);
        mPassButton.setEnabled(true);

        mColorButtonView.setVisibility(View.INVISIBLE);
        mColorButtonView.setEnabled(false);
        mColorButtonView.invalidate();
        
        mDriving = true;
    }

    public void controlColors(){

        mJoystick.setVisibility(View.INVISIBLE);
        mJoystick.setEnabled(false);
        mJoystick.invalidate();

        mCalibration.setVisibility(View.INVISIBLE);
        mCalibration.setEnabled(false);
        mCalibration.invalidate();

        mPassButton.setVisibility(View.INVISIBLE);
        mPassButton.setEnabled(false);

        mColorButtonView.setVisibility(View.VISIBLE);
        mColorButtonView.setEnabled(true);
        mColorButtonView.invalidate();
        
        mDriving = false;
    }
    
    public void hideControls(){
        mJoystick.setVisibility(View.INVISIBLE);
        mJoystick.setEnabled(false);
        
        mCalibration.setVisibility(View.INVISIBLE);
        mCalibration.setEnabled(false);
        
        mPassButton.setVisibility(View.INVISIBLE);
        mPassButton.setEnabled(false);
        
        mColorButtonView.setVisibility(View.INVISIBLE);
        mColorButtonView.setEnabled(false);
        
        mDriving = false;
    }
    
    public void showHostingMessage(){
        
        findViewById(R.id.hosting_game_text).setVisibility(View.VISIBLE);
    }
    
    public void hideHostingMessage(){
        findViewById(R.id.hosting_game_text).setVisibility(View.INVISIBLE);
    }
    
    public void showJoiningMessage(){
        findViewById(R.id.joining_game_text).setVisibility(View.VISIBLE);
    }
    
    public void hideJoiningMessage(){
        findViewById(R.id.joining_game_text).setVisibility(View.INVISIBLE);
    }

    /**
     * When the user clicks on the "PASS" button, pass control to the other player
     */
    public void onPassClick(View view){

        
        try {
            JSONObject game_data = new JSONObject();
            game_data.put("PASS", "ur turn");
            
            mMultiplayerClient.sendGameDataToAll(game_data);
            
            controlColors();
            
        } catch (JSONException e) {
            LogUtil.e("Failed to send game data. JSONException.", e);
        }

    }

    @Override
    public void setRobot(Robot robot){
        super.setRobot(robot);

        mRobot = robot;
        mColorButtonView.setRobot(robot);
    }
    
    private void lookForGames(){
        
        if(!mLookingForGame){

            mLookingForGame = true;

            hideControls();
            showJoiningMessage();

            mMultiplayerClient.requestAvailableGames();
            //new JoinThread().start();
        }
    }

    private void joinGame(MultiplayerGame game){

        mLookingForGame = false;

        mMultiplayerClient.setLocalPlayer(new RemotePlayer("TPOB Android Guest Player", 0, false, false, true));
        mMultiplayerClient.joinGame(game);
    }
}
