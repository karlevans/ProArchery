/*
 * ProArchery (c) 2011
 *
 * Author: Karl Evans
 * ProArchery Activity 
 *
 * My first android game which was created to simulate real world archery.
 * This code is quite old and was developed around 2011 by myself.
 * It is no longer on google play but when it was it had over 250000 downloads.
 * It was a free game, which displayed adverts at the bottom of the screen.
 * I never intended to make the source code public but would like to provide it
 * as a sample of my work in order to find new employment and if it is of use to
 * other develper then thats all good too :-)
 */
package com.proarchery;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import com.proarchery.GameView.GameThread;
import com.proarchery.GameActivity;
import com.proarchery.GameSelectionActivity;

// ProArchery Activity
public class ProArchery extends Activity {
	private PowerManager.WakeLock wl;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        final Button newGameBtn 		= (Button) findViewById(R.id.new_game); 
        final Button helpGameBtn 		= (Button) findViewById(R.id.help_game);
        final Button exitGameBtn 		= (Button) findViewById(R.id.exit_game);  
        final TextView txtFirst 		= (TextView) findViewById(R.id.first_value);
        final TextView txtSecond 		= (TextView) findViewById(R.id.second_value);
        final TextView txtThird 		= (TextView) findViewById(R.id.third_value);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
       
        File sdDir = new File(Environment.getExternalStorageDirectory().getPath());
        File proarcheryDir = new File(sdDir.getAbsolutePath() + "/com.proarchery");
       
        proarcheryDir.mkdir(); 
		FileReader in;
		
		File filetst = new File(proarcheryDir + "/data.txt");
		
        // Get the players data if it exists. If not initialise it
		if(!filetst.exists()) {
			 FileWriter out;		       		      
		        	try {
		        		out = new FileWriter(new File(proarcheryDir + "/data.txt"));
		        		out.write("0,0,0");
		        		out.close();
		        	} catch (IOException e) {
		        		// TODO Auto-generated catch block
		        		e.printStackTrace();		        		
		        	}
		}
		
		
		if(filetst.exists()) {
			try {
				in = new FileReader(new File(proarcheryDir + "/data.txt"));
				BufferedReader br = new BufferedReader(in);
				String s;
				s = br.readLine();
				String[] ss = s.split(",");
				txtFirst.setText("x " + ss[0]);
				txtSecond.setText("x " + ss[1]);
				txtThird.setText("x " + ss[2]);
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();				
			}
		} 
        
        // New Game Selected
        newGameBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks          
            	Intent myIntent = new Intent(ProArchery.this, GameSelectionActivity.class);
        		ProArchery.this.startActivity(myIntent);       
            }
        });     
        
     // Help Game Selected
        helpGameBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks
                final Dialog dialog = new Dialog(ProArchery.this);
                dialog.setContentView(R.layout.help_screen);
                dialog.setTitle("Pro Archery Help");
                dialog.setCancelable(true);
 
                TextView txtInfo = (TextView) dialog.findViewById(R.id.archery_help);
                Button btnClose = (Button) dialog.findViewById(R.id.close_help);
                
                txtInfo.setText(R.string.helptext);
                txtInfo.setMovementMethod(new ScrollingMovementMethod());

                btnClose.setOnClickListener(new OnClickListener() {
                @Override
                    public void onClick(View v) {
                       dialog.dismiss();
                    }
                });
                dialog.show();              
               
            }
        });        
                        
        
     // Exit Game Selected
        exitGameBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks
            	wl.release();
                System.exit(1);
            }
        });    
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	wl.release();
            System.exit(1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}