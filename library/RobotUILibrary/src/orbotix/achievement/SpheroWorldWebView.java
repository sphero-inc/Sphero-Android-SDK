package orbotix.achievement;

import android.app.Activity;
import android.graphics.LightingColorFilter;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import orbotix.robot.app.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for displaying a view allowing users to login to SpheroWorld and authorize the application.  After a user
 * has logged in this activity will display the list of the apps achievements and the user's progress toward earning
 * them.
 */
public class SpheroWorldWebView extends Activity {
    //Private methods
    private WebView webview;

    private class RobotAchievementWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("Orbotix", "Loading URL " + url);
            if(url.equalsIgnoreCase("https://app.gosphero.com/back//")) {
                finish();
                return true;
            }

            String parts[] = url.split("/access_token/");
            if(parts.length > 1) {
                AchievementManager.setToken(parts[1]);
                if(AchievementManager.getAppID().equals("sphe172c542260dd83c709eba5a449efe59a")) {
                    loadBallStats();
                } else {
                    loadAchievements();
                }
                return true;
            }

            view.loadUrl(url);
            return true;
        }
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.sphero_world_web_view);
        webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new RobotAchievementWebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);

        if(AchievementManager.getOAuth() != null && !AchievementManager.getOAuth().equals("0") && !AchievementManager.getOAuth().equals("")) {
            if(AchievementManager.getAppID().equals("sphe172c542260dd83c709eba5a449efe59a")) {
                //If this is the Sphero App we go to the ball stats page
                loadBallStats();
            } else {
                //If this app isn't Sphero we go to the Achievements page
                loadAchievements();
            }
        } else {
            String URL = "https://app.gosphero.com/m/authorize?client_id=" + AchievementManager.getAppID() + "&client_secret="+ AchievementManager.getAppSecret() +"&redirect_uri=http://www.orbotix.com&mac_address=" + AchievementManager.getCurrentRobotAddress();
            webview.loadUrl(URL);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if(AchievementManager.getAppID().equals("sphe172c542260dd83c709eba5a449efe59a")) {
            //If this is the sphero app be sure to check for an updated sphero name.
            AchievementManager.doUpdateSpheroName();
        }
    }

    private void loadAchievements() {
        String URL = "https://app.gosphero.com/m/a/" + AchievementManager.getAppID() + "/user_achievements";
         Map<String, String> header = new HashMap<String, String>();
         String value = "Bearer " + AchievementManager.getOAuth();
         header.put("Authorization", value);
         webview.loadUrl(URL, header);
    }

    private void loadBallStats() {
        String URL = "https://app.gosphero.com/m/s/" + AchievementManager.getCurrentRobotAddress();
        Map<String, String> header = new HashMap<String, String>();
        String value = "Bearer " + AchievementManager.getOAuth();
        header.put("Authorization", value);
        webview.loadUrl(URL, header);
    }
}