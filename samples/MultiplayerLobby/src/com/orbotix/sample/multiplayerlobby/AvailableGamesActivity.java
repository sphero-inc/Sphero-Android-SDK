package com.orbotix.sample.multiplayerlobby;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.orbotix.sample.multiplayerlobby.view.GameListItemView;
import orbotix.multiplayer.LocalMultiplayerClient;
import orbotix.multiplayer.MultiplayerGame;
import orbotix.multiplayer.RemotePlayer;

import java.util.ArrayList;
import java.util.List;

public class AvailableGamesActivity extends Activity
{
    /**
     * Tag for logging
     */
    public static final String TAG = "MultiplayerLobby";

    /**
     * ID For starting the SetNameDialog
     */
    private static final int sSetNameDialog = 0;
    
    private ListView mGamesList;
    private LocalMultiplayerClient mMultiplayerClient;

    private GamesListAdapter mGamesAdapter = new GamesListAdapter();
    
    private String mPlayerName = "Android Player";

    private Handler mFindGamesHandler = new Handler();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.available_games_activity);
        
        //Get important Views
        mGamesList = (ListView)findViewById(R.id.games_list);
        mGamesList.setAdapter(mGamesAdapter);

        //Set Games List item click listener. When the user clicks an item, join that game
        mGamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                MultiplayerGame game = (MultiplayerGame)adapterView.getItemAtPosition(i);

                Intent intent = new Intent(view.getContext(), LobbyActivity.class);
                intent.putExtra(LobbyActivity.EXTRA_ACTION, LobbyActivity.EXTRA_ACTION_JOIN_GAME);
                intent.putExtra(LobbyActivity.EXTRA_USER_NAME, mPlayerName);
                intent.putExtra(LobbyActivity.EXTRA_GAME, game);
                startActivity(intent);
            }
        });

        //Instantiate MultiplayerClient
        mMultiplayerClient = new LocalMultiplayerClient(this);

        //Set game session ID
        mMultiplayerClient.setSessionId("MPSample");

        //Set local player, pretty much only setting the name to the default name
        mMultiplayerClient.setLocalPlayer(new RemotePlayer(mPlayerName, 0, false, false, true));

        //Opens the multiplayer manager service, starting communications
        mMultiplayerClient.open();

        //When new games come in, update the games list
        mMultiplayerClient.setOnGetGamesListener(new LocalMultiplayerClient.OnGetGamesListener() {
            @Override
            public void onGetGames(Context context, List<MultiplayerGame> games) {

                mGamesAdapter.setGames(games);
            }
        });

        //When the multiplayer service is online, start requesting games
        mMultiplayerClient.setOnOnlineListener(new LocalMultiplayerClient.OnOnlineListener() {
            @Override
            public void onOnline(Context context) {
                //Request games from the multiplayer service
                mMultiplayerClient.requestAvailableGames();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mMultiplayerClient.leaveGame();

        mMultiplayerClient.close();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        mMultiplayerClient.requestAvailableGames();
    }

    /**
     * When the "Set Name" button is clicked, show the SetNameDialog
     * @param v
     */
    public void onSetNameClick(View v){
        showDialog(sSetNameDialog);
    }

    /**
     * When the "Create Game" button is clicked, host a game
     * @param v
     */
    public void onCreateGameClick(View v){

        Intent i = new Intent(this, LobbyActivity.class);
        i.putExtra(LobbyActivity.EXTRA_ACTION, LobbyActivity.EXTRA_ACTION_HOST_GAME);
        i.putExtra(LobbyActivity.EXTRA_USER_NAME, mPlayerName);
        startActivity(i);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        
        Dialog d = null;
        
        if(id == sSetNameDialog){
            d = new SetNameDialog(this);
        }
        
        return d;
    }

    /**
     * A BaseAdapter that keeps track of the games received from the multiplayer service
     */
    private class GamesListAdapter extends BaseAdapter {

        private List<MultiplayerGame> mGames = new ArrayList<MultiplayerGame>();


        public void setGames(List<MultiplayerGame> games){
            mGames.clear();
            mGames.addAll(games);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mGames.size();
        }

        @Override
        public MultiplayerGame getItem(int i) {
            return mGames.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if(view == null){
                view = new GameListItemView(viewGroup.getContext());
            }

            MultiplayerGame game = getItem(i);
            GameListItemView list_item = (GameListItemView)view;

            list_item.setText(game.getName());

            return view;
        }
    }

    /**
     * A Dialog that asks for a username, and sets this username
     */
    public class SetNameDialog extends Dialog {

        public SetNameDialog(Context context) {
            super(context);
            
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            
            setContentView(R.layout.set_name_dialog);

            //When the user clicks "done", record the name
            findViewById(R.id.done_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPlayerName = getNameString();
                    mMultiplayerClient.setLocalPlayer(new RemotePlayer(mPlayerName, 0, false, false, true));
                    dismissDialog(sSetNameDialog);
                }
            });
        }
        
        private String getNameString(){
            return ((EditText)findViewById(R.id.name_field)).getText().toString();
        }
    }
}
