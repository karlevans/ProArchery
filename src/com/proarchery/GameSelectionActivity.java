/*
 * ProArchery (c) 2011
 *
 * Author: Karl Evans
 * Game Selection activity which allows the player to select a match style 
 *
 */
package com.proarchery;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameSelectionActivity extends Activity {
    public String mRoundSelection;
	  public TextView mRoundInfo;
	  String [] mDescription;
	  private PowerManager.WakeLock wl;
	  
    // Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.game_selection);
        mRoundSelection = "";
        // Description of the tyoe of Archery round
        mDescription = new String [] {"The Hereford round is a lot of fun, conscistiing of 36 arrows at 80, 60 and 50 yards. You can gain Third, Second and First class in this round.",        							  
        							  "The Bristol round conscists of 36 arrows at 80, 60 and 50 yards. You can gain First, Second and First class in this round.",        							  
        						      "Long National will give you 48 arrows at 80 yards, and 24 arrows at sixty yards. You can gain first, Second or Third class in this round.",
        						      "In the National round you will have to shoot 48 arrows at sixty yards and 24 arrows at fisty yards. Only Second and Third class can be gained in this round.",
        						      "Lots of fun to be had with the Albion round. You will shoot 36 arrows at 80, 60 and 50 yards. A First, Second and Third class can be gained in this round.",
        						      "Shooting the Windsor round, you will have to shoot 36 arrows at 80, 60 and 40 yards. You will only be able to gain a Third or Second class score in this round.",
        						      "The Western round, sorry no gun shooting here, you will be required to shoot 48 arrows at 60 and 50 yards. You can only gain a Second or Third class in this round.",
        						      "Short Western, similar to the western but at shorter distance. You will be required to shoot 48 arrows and 50 and 40 yards. Only a Third class can be gained in this round.",
        						      "The Long Western and no thats not a shotgun. You will be shooting 48 arrows at both 80 and 60 yards. First, Second and Third class can be gained in this round.",
        						      "In the American round, you will be rquired to shoot 30 arrows at 60, 50 and also 40 yards. Only Second and Third class can be gained in this round.",
        						      "The Fita 70 round requires 72 arrows to be shot over 12 ends at a distance of 70 metres.",
        						      "The Glade league is 72 arrows over 12 ends and is shot at 70 metres and is the 70m Internet Archery Leauge."};
        
        mRoundInfo = (TextView)this.findViewById(R.id.round_info);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
        final Button playRoundBtn 		= (Button) findViewById(R.id.play_round);

        // Display AdMob Adverts
  		  AdView adview = (AdView)findViewById(R.id.adView);
  		
        AdRequest re = new AdRequest();
        // re.setTesting(true);
        // re.addTestDevice(re.TEST_EMULATOR);
        // re.setGender(AdRequest.Gender.MALE);
        // re.setGender(AdRequest.Gender.FEMALE); 
        adview.loadAd(re);
           
        // END OF ADS
        // New Game Selected
        playRoundBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks              
            	  Bundle b = new Bundle();
            	  b.putString("ROUND", mRoundSelection);
            	  Intent myIntent = new Intent(GameSelectionActivity.this, GameActivity.class);
            	  myIntent.putExtras(b);            	
        		    GameSelectionActivity.this.startActivity(myIntent);
            }
        });    
    }
    
    // Listen for a game selection
    public class MyOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent,
            View view, int pos, long id) {
        	  mRoundSelection = parent.getItemAtPosition(pos).toString();
            Context x = parent.getContext();
            mRoundInfo.setText(mDescription[pos]);
        }

        public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        wl.release();     
    }

    @Override
    protected void onResume() {
        super.onResume();
        wl.acquire();
    }

}
