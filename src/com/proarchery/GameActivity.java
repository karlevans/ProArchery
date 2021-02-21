/*
 * ProArchery (c) 2011
 *
 * Author: Karl Evans
 * Main activity class for the game 
 *
 */
package com.proarchery;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.proarchery.GameView.GameThread;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity {
	// Handle to thread to control game.
    private GameThread mGameThread;    

    // A handle to the View in which the game is running.
    private GameView mGameView;
    private Button shoot_btn;
    private PowerManager.WakeLock wl;

	// Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // tell system to use the layout defined in our XML file
        setContentView(R.layout.game);
       
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        PowerManager pm = (PowerManager) GameActivity.this.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        
        // get handles to the GameView from XML, and its GameThread
        mGameView = (GameView) findViewById(R.id.archery);        
        
        mGameThread = mGameView.getThread();
        
        mGameThread.setScreenSize(dm.widthPixels, dm.heightPixels);
        Bundle b = getIntent().getExtras();
          
        mGameThread.setRound(b.getString("ROUND"));         
         
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        mGameThread.strPhoneID = telephonyManager.getDeviceId();
       
        // mGameThread.doStart();
        mGameThread.doStart();
        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            mGameThread.setState(GameThread.STATE_READY);            
        } else {
            // we are being restored: resume a previous game
            mGameThread.restoreState(savedInstanceState);            
        }                
      
        mGameView.setTextViewScore((TextView) findViewById(R.id.actual_score));
        mGameView.setTextViewEnds((TextView) findViewById(R.id.game_ends));
        mGameView.setTextViewClass((TextView) findViewById(R.id.archers_class));
        mGameView.setTextViewWindSpeed((TextView) findViewById(R.id.windspeed_value));
        mGameView.setTextViewDistance((TextView) findViewById(R.id.distance_value));
        
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/ProLamina.ttf");
        mGameThread.tf = tf;
        
        // Diaplay adverts from AdMob
   		AdView adview = (AdView)findViewById(R.id.adView);
   		
        AdRequest re = new AdRequest();
        // re.setTesting(true);
        // re.addTestDevice(re.TEST_EMULATOR);
        // re.setGender(AdRequest.Gender.MALE);
        // re.setGender(AdRequest.Gender.FEMALE); 
        adview.loadAd(re);
        // END OF ADS
    }
    
    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mGameView.getThread().interrupt(); // pause game when Activity pauses 
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     * 
     * @param outState a Bundle into which this Activity should save its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState);
        mGameThread.saveState(outState);      
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.exit(1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
