package com.orbotix.sample.multiplayerlobby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import orbotix.multiplayer.LocalMultiplayerClient;
import orbotix.multiplayer.MultiplayerGame;
import orbotix.multiplayer.RemotePlayer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Orbotix Inc.
 * Date: 4/18/12
 *
 * @author Adam Williams
 */
public class LobbyActivity extends Activity {

    private LocalMultiplayerClient mMultiplayerClient;

    /**
     * Key for getting whether this is a hosted game or a joined game from the Intent
     */
    public static final String EXTRA_ACTION = "EXTRA_ACTION";

    /**
     * Key for getting the game to join from the Intent
     */
    public static final String EXTRA_GAME = "EXTRA_GAME";

    /**
     * Key for getting the user name from the Intent
     */
    public static final String EXTRA_USER_NAME = "EXTRA_USER_NAME";
    
    /**
     * Value designating that this is a hosted game
     */
    public static final int EXTRA_ACTION_HOST_GAME = 0;

    /**
     * Value designating that this is a game to be joined
     */
    public static final int EXTRA_ACTION_JOIN_GAME = 1;
    
    

    //Views
    private TextView mGameStateText;
    private TextView mPlayersText;
    private Button mStartGameButton;
    private Button mEndGameButton;
    private Button mPauseGameButton;
    private Button mResumeGameButton;
    private TextView mChatroomText;
    private EditText mChatField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.lobby_activity);

        //Get important views
        mGameStateText    = (TextView)findViewById(R.id.game_state_text);
        mPlayersText      = (TextView)findViewById(R.id.players_text);
        mStartGameButton  = (Button)findViewById(R.id.start_game_button);
        mEndGameButton    = (Button)findViewById(R.id.end_game_button);
        mPauseGameButton  = (Button)findViewById(R.id.pause_game_button);
        mResumeGameButton = (Button)findViewById(R.id.resume_game_button);
        mChatroomText     = (TextView)findViewById(R.id.chatroom_text);
        mChatField        = (EditText)findViewById(R.id.chat_field);
        
        //Get user name from intent
        Intent i = getIntent();
        final String name = i.getStringExtra(EXTRA_USER_NAME);

        //Instantiate MultiplayerClient
        mMultiplayerClient = new LocalMultiplayerClient(this);

        //Set the user's name
        if(name != null){
            mMultiplayerClient.setLocalPlayer(new RemotePlayer(name, 0, false, false, true));
        }

        //Set session ID
        mMultiplayerClient.setSessionId("MPSample");

        //Open client
        mMultiplayerClient.open();

        //When players come in by request, update the players list
        mMultiplayerClient.setOnGetPlayersListener(new LocalMultiplayerClient.OnGetPlayersListener() {
            @Override
            public void onGetPlayers(Context context, List<RemotePlayer> players) {
                updatePlayerList(players);
            }
        });

        //When players are changed, update the player's list
        mMultiplayerClient.setOnPlayersChangedListener(new LocalMultiplayerClient.OnPlayersChangedListener() {
            @Override
            public void onPlayersChanged(Context context, List<RemotePlayer> players) {
                updatePlayerList(players);
            }
        });

        //When game data is received (a chat message), show the message in the chatroom area
        mMultiplayerClient.setOnGameDataReceivedListener(new LocalMultiplayerClient.OnGameDataReceivedListener() {
            @Override
            public void onGameDataReceived(Context context, JSONObject game_data, RemotePlayer sender) {

                if(game_data.has("CHAT")){

                    try {
                        addChatMessage(sender.getName(), game_data.getString("CHAT"));
                    } catch (JSONException e) {
                        Log.e(AvailableGamesActivity.TAG, "Failed to get chat message from game data.", e);
                    }
                }
            }
        });
        
        //When the game starts, reflect this in the TextView
        mMultiplayerClient.setOnGameStartListener(new LocalMultiplayerClient.OnGameStartListener() {
            @Override
            public void onGameStart(Context context) {
                
                mGameStateText.setText("Started");
                mPauseGameButton.setEnabled(true);
                mResumeGameButton.setEnabled(false);
                mStartGameButton.setEnabled(false);
                
                if(mMultiplayerClient.getIsHost()){
                    mEndGameButton.setEnabled(true);
                }
            }
        });
        
        //When the game ends, reflect this
        mMultiplayerClient.setOnGameEndListener(new LocalMultiplayerClient.OnGameEndListener() {
            @Override
            public void onGameEnd(Context context) {
                
                mGameStateText.setText("Ended");
                mResumeGameButton.setEnabled(false);
                mPauseGameButton.setEnabled(false);
                mStartGameButton.setEnabled(false);
                mEndGameButton.setEnabled(false);
            }
        });
        
        //When the game is paused, reflect this
        mMultiplayerClient.setOnGamePausedListener(new LocalMultiplayerClient.OnGamePausedListener() {
            @Override
            public void onGamePaused(Context context) {
                
                mGameStateText.setText("Paused");
                mResumeGameButton.setEnabled(true);
                mPauseGameButton.setEnabled(false);
            }
        });

        //Join or host the game, depending on which action was sent
        if(i.hasExtra(EXTRA_ACTION) && i.getIntExtra(EXTRA_ACTION, 0) == EXTRA_ACTION_JOIN_GAME){

            //Join game
            mMultiplayerClient.joinGame((MultiplayerGame)i.getParcelableExtra(EXTRA_GAME));


        }else {
            //Host game
            mMultiplayerClient.hostNewGame(mMultiplayerClient.getLocalPlayer().getName());

            mStartGameButton.setEnabled(true);
        }

        //Start a poll of player information to keep the latest latency information
        mMultiplayerClient.startPollingPlayers(1000);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mMultiplayerClient.leaveGame();
        mMultiplayerClient.stop();
    }


    private void addChatMessage(String username, String message){
        
        StringBuilder str = new StringBuilder();
        str.append(mChatroomText.getText());
        str.append(username+": "+message+"\n");
        mChatroomText.setText(str.toString());
    }
    
    private void sendChatMessage(String message){
        
        JSONObject json = new JSONObject();
        
        try {
            json.put("CHAT", message);
        } catch (JSONException e) {
            throw new RuntimeException("Failed to send game data.", e);
        }

        mMultiplayerClient.sendGameDataToAll(json);
    }

    /**
     * When the user clicks the "Send" button, send the message in teh chat field, if any
     * @param v
     */
    public void onSendClick(View v){
        
        final String message = mChatField.getText().toString();
        
        if(message != null && !message.equals("")){

            sendChatMessage(message);
            addChatMessage(mMultiplayerClient.getLocalPlayer().getName(), message);
            mChatField.setText("");

        }
    }

    /**
     * When the user clicks the "Start Game" button, start the game
     * @param v
     */
    public void onStartGameClick(View v){
        
        if(mMultiplayerClient.getIsHost()){
            
            mMultiplayerClient.startGame();
        }
    }

    /**
     * When the user clicks the "End Game" button, end the game
     * @param v
     */
    public void onEndGameClick(View v){
        
        if(mMultiplayerClient.getIsHost()){
            mMultiplayerClient.endGame();
        }
    }

    /**
     * When the user clicks the "Pause Game" button, pause the game
     * @param v
     */
    public void onPauseGameClick(View v){
        
        mMultiplayerClient.pauseGame();
    }

    /**
     * When the user clicks the "Resume Game" button, resume the game
     * @param v
     */
    public void onResumeGameClick(View v){

        mMultiplayerClient.resumeGame();
    }

    /**
     * Updates the player list when new information is available
     * @param players
     */
    private void updatePlayerList(List<RemotePlayer> players){

        StringBuilder str = new StringBuilder();

        for(RemotePlayer player : players){

            if(player.getIsHost()){
                str.append("* ");
            }
            str.append(player.getName()+" - "+player.getLatency()+" ms\n");
        }

        mPlayersText.setText(str.toString());
    }

}
