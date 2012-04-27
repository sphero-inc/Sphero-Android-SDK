package orbotix.twophones.util;

import android.util.Log;

/**
 * Created by Orbotix Inc.
 * Date: 1/20/12
 *
 * @author Adam Williams
 */
public class LogUtil {
    
    public final static String TAG = "TwoPhonesOneBall";

    /**
     * Send a debug message through Android's Log
     * @param message
     */
    public static void d(String message){
        Log.d(TAG, message);
    }

    /**
     * Send an error message through Android's Log
     * @param message The messgae to log
     */
    public static void e(String message){
        e(message, null);
    }

    /**
     * Send an error message through Android's Log
     * @param message The message to log
     * @param e An optional Throwable to include with the log
     */
    public static void e(String message, Throwable e){
        Log.e(TAG, message, e);
    }
}
