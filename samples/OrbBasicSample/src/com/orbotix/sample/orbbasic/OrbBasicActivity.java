package com.orbotix.sample.orbbasic;

import android.app.ListActivity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import orbotix.robot.base.*;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.DiscoveryListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class OrbBasicActivity extends ListActivity {
    private static final String TAG = "OBX-OrbBasic";

    /** Sphero Connection View */
    private SpheroConnectionView mSpheroConnectionView;

    /** Robot to from which we are running OrbBasic programs on */
    private Sphero mRobot = null;
    private int mOrbBasicProgramResource;

    /** UI */
    private TextView mTxtStatus;
    private OrbBasicProgramListAdapter mListAdapter;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTxtStatus = (TextView) findViewById(R.id.txt_status);
        // Auto scrolls to new data
        mTxtStatus.setMovementMethod(new ScrollingMovementMethod());
        loadRawResourcesIntoList();

        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
        mSpheroConnectionView.setSingleSpheroMode(true); // todo - allow connection of more than one.

        RobotProvider.getDefaultProvider().addConnectionListener(new ConnectionListener() {

            @Override
            public void onConnected(Robot robot) {
                mRobot = (Sphero) robot;
                Log.d(TAG, "Version:" + mRobot.getVersion());
                mRobot.getOrbBasicControl().addEventListener(new OrbBasicControl.EventListener() {
                    @Override
                    public void onEraseCompleted(boolean success) {
                        String successStr = (success) ? "Success" : "Failure";
                        addMessageToStatus("Done Erasing: " + successStr);
                    }

                    @Override
                    public void onLoadProgramComplete(boolean success) {
                        String successStr = (success) ? "Success" : "Failure";
                        addMessageToStatus("Done Loading: " + successStr);
                    }

                    @Override
                    public void onPrintMessage(String message) {
                        addMessageToStatus(message);
                    }

                    @Override
                    public void onErrorMessage(String message) {
                        Log.d("ERROR", message);
                        addMessageToStatus("ERROR: " + message);
                    }

                    @Override
                    public void onErrorByteArray(byte[] bytes) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                });
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                // let the SpheroConnectionView handle or hide it and do something here...
            }

            @Override
            public void onDisconnected(Robot sphero) {
                mSpheroConnectionView.startDiscovery();
            }

        });
    }

    /** Called when the user comes back to this app */
    @Override
    protected void onResume() {
        super.onResume();
        mSpheroConnectionView.startDiscovery();
    }

    /** Called when the user presses the back or home button */
    @Override
    protected void onPause() {
        super.onPause();

        // Disconnect Robot properly
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
        RobotProvider.getDefaultProvider().removeDiscoveryListeners();
    }

    /** Loads the contents of the raw res folder and puts it into the ListView adapter */
    private void loadRawResourcesIntoList() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        ArrayList<String> listStr = new ArrayList<String>();
        Field[] fields = R.raw.class.getFields();
        for (Field f : fields)
            try {
                listStr.add(f.getName());
                list.add(f.getInt(null));
            } catch (IllegalArgumentException e) {
                Log.e("Orbotix.Sample", "IllegalArgument " + e.getLocalizedMessage());
            } catch (IllegalAccessException e) {
                Log.e("Orbotix.Sample", "IllegalAccess " + e.getLocalizedMessage());
            }
        mListAdapter = new OrbBasicProgramListAdapter();
        mListAdapter.setOrbBasicProgramNames(listStr);
        mListAdapter.setProgramResources(list);
        getListView().setAdapter(mListAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mOrbBasicProgramResource = mListAdapter.getOrbBasicPrograms().get(i).intValue();
                addMessageToStatus(mListAdapter.getOrbBasicProgramNames().get(i) + " now selected");

                try {
                    // Retrieve byte array from file
                    Resources res = getResources();

                    InputStream in_s = res.openRawResource(mOrbBasicProgramResource);
                    byte[] program = new byte[in_s.available()];
                    in_s.read(program);

                    // Create the OrbBasic Program object
                    mRobot.getOrbBasicControl().setProgram(program);

                } catch (Exception e) {
                    addMessageToStatus("Error Decoding Resource");
                }
            }
        });
    }


    /** Append Button Pressed */
    public void loadPressed(View v) {
        addMessageToStatus("Loading OrbBasic Program...");
        mRobot.getOrbBasicControl().loadProgram();
    }

    /** Abort Button Pressed */
    public void abortPressed(View v) {
        addMessageToStatus("Aborting OrbBasic Program");
        mRobot.getOrbBasicControl().abortProgram();
    }

    /** Execute Button Pressed */
    public void executePressed(View v) {
        addMessageToStatus("Executing OrbBasic Program");
        mRobot.getOrbBasicControl().executeProgram();
    }

    /** Erase Button Pressed */
    public void erasePressed(View v) {
        addMessageToStatus("Erasing OrbBasic Program...");
        mRobot.getOrbBasicControl().eraseStorage();
    }

    /**
     * Function to append a string to a TextView as a new line
     *
     * @param msg to append
     */
    private void addMessageToStatus(String msg) {
        // append the new string
        mTxtStatus.append(msg + "\n");
        // find the amount we need to scroll.  This works by
        // asking the TextView's internal layout for the position
        // of the final line and then subtracting the TextView's height
        final int scrollAmount = mTxtStatus.getLayout().getLineTop(mTxtStatus.getLineCount())
                - mTxtStatus.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            mTxtStatus.scrollTo(0, scrollAmount);
        else
            mTxtStatus.scrollTo(0, 0);
    }

    /** A BaseAdapter that keeps track of the OrbBasic Programs in the raw folder */
    private class OrbBasicProgramListAdapter extends BaseAdapter {

        private List<Integer> mOrbBasicPrograms = new ArrayList<Integer>();
        private List<String> mOrbBasicProgramNames = new ArrayList<String>();

        public void setProgramResources(List<Integer> programs) {
            mOrbBasicPrograms = programs;
            notifyDataSetChanged();
        }

        public void setOrbBasicProgramNames(List<String> programNames) {
            mOrbBasicProgramNames = programNames;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mOrbBasicPrograms.size();
        }

        @Override
        public String getItem(int i) {
            return mOrbBasicProgramNames.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public List<Integer> getOrbBasicPrograms() {
            return mOrbBasicPrograms;
        }

        public List<String> getOrbBasicProgramNames() {
            return mOrbBasicProgramNames;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null) {
                view = new OrbBasicProgramListItemView(viewGroup.getContext());
            }

            String program = getItem(i);
            OrbBasicProgramListItemView list_item = (OrbBasicProgramListItemView) view;

            // Display OrbBasic Player Name
            list_item.setText(program);

            return view;
        }
    }
}
