/*
 * ProArchery (c) 2011
 *
 * Author: Karl Evans
 * Main GameView - The heart of the game 
 *
 */

package com.proarchery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import com.proarchery.Arrow;
import com.proarchery.Wind;
import com.proarchery.Card;
import com.proarchery.BowString;

import android.os.Vibrator;
import com.google.ads.*;

class GameView extends SurfaceView implements SurfaceHolder.Callback {
    class GameThread extends Thread {
      
        /*
         * State-tracking constants
         */
        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_READY = 3;
        public static final int STATE_RUNNING = 4;
        public static final int STATE_WIN = 5;      

        // The drawable to use as the background of the animation canvas
        private Bitmap mBackgroundImage;

        // Message handler used by thread to interact with TextView
        private Handler mHandler;

        /** Used to figure out elapsed time between frames */
        public long mLastTime;

        // The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN
        private int mMode;

        // Indicate whether the surface has been created and is ready to draw
        private boolean mRun = false;

        /// Scratch rect object. */
        private RectF mScratchRect;

        // Handle to the surface manager object we interact with
        private SurfaceHolder mSurfaceHolder;

        private BitmapDrawable mBackground;
        private Canvas x;
        private Bitmap scratch;
        
        public float mSightX;
        public float mSightY;
        
        private int mTargetX;
        private int mTargetY;
        
        public int mScreenWidth;
        public int mScreenHeight;
        private int mMiddleX;
        private int mMiddleY;
        private int mScoreBaloonX;
        private int mScoreBaloonY;
        public int mRunningTotal;
        public int mArrowCount=0;
        public int mTotalDistanceArrowCount;
        public int mTotalArrowsForDistance;
        Arrow arrow = null;
        Target mTarget;
        
        // METRES CONSTANTS for metre archery games
        public static final int THIRTY_METRES = 60;
        public static final int FOURTY_METRES = 50;
        public static final int FIFTY_METRES = 40;        
        public static final int SEVENTY_METRES = 25;
            
        // YARD CONSTANTS for yard archery games
        public static final int FOURTY_YARDS = 50;
        public static final int FIFTY_YARDS = 40;
        public static final int SIXTY_YARDS = 30;
        public static final int EIGHTY_YARDS = 20;
        public Integer mCurrentDistance;
        private TextView score_text;
        private TextView ends_text;
        private int mCurrentEnd;
        private int mMaxEnds;
        public String mCurrentRound;
        public static final int ALBION_ROUND = 0;
        public static final int WINDSOR_ROUND = 1;
        public Stack mRoundsDistance;
        public Stack mRoundsArrows;
        public Stack mClassification;
        public String mCurrentArchersClass;
        public Integer mBowmanScore;
        public Integer mFirstClassScore;
        public Integer mSecondClassScore;
        public Integer mThirdClassScore;
        public boolean mImperial;
        private Drawable mRiserImage;
        private Wind wind;
        public float mAngle;
        private Bitmap mStandImage;
        
        private int mDozenScore;
        private int mDozenCount;
        public static final int SAY_TEN = 10;
        public static final int SAY_NINE = 9;
        public static final int SAY_EIGHT = 8;
        public static final int SAY_SEVEN = 7;
        public static final int SAY_SIX = 6;
        public static final int SAY_FIVE = 5;
        public static final int SAY_FOUR = 4;
        public static final int SAY_THREE = 3;
        public static final int SAY_TWO = 2;
        public static final int SAY_ONE = 1;
        public static final int SAY_MISS = 0;
        public static final int HIT_SOUND = 100;
        public static final int SHOOT_SOUND = 101;
        public static final int MSG_SOUND = 102;
        public static final int DRAW_SOUND = 103;        

        public static final int MESSAGE_DISPLAYED = 1;
        public static final int MESSAGE_NOT_DISPLAYED = 0;
        public static final int ROUND_START = 1;
        public static final int ROUND_END = 2;
        public static final int ROUND_STARTED = 3;
		protected static final int DISPLAY_DLG = 0;
		
        public int game_state;
        private Card card=null;
        private BowString bowstring = null;
        private SoundPool soundPool;
        public double mAccelX;
        public double mAccelY;
        private HashMap soundPoolMap;
        public Typeface tf;
        public Dialog dlgSubmitScores;
        public String strPhoneID;
        private int mRiserWidth;
        public int mRiserHeight;
        public String mDistanceString;
        public int mFade=255;
        public GameThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;
            
            mRunningTotal = 0;
            
            mCurrentEnd = 1;
            mDozenScore = 0;
            mDozenCount = 0;
            mArrowCount=0;
            
            mAccelX=0;
            mAccelY=0;
            
            mTotalDistanceArrowCount = 0;
            mAngle=0;
            mCurrentArchersClass = "NOVICE";
            
            Resources res = mContext.getResources();
            mBackgroundImage = BitmapFactory.decodeResource(res, R.drawable.backdrop);

            // Get the bow image - actually a scan of my real bow	            	            	
            mRiserImage = context.getResources().getDrawable(R.drawable.riser);

            // Stand for the archery boss (target)	
            mStandImage = BitmapFactory.decodeResource(res, R.drawable.stand);
            initSounds();	
            game_state = ROUND_START;
        }

        private void initSounds() {
            soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
            soundPoolMap = new HashMap();

            soundPoolMap.put(SAY_TEN, soundPool.load(getContext(), R.raw.ten, 1));
            soundPoolMap.put(SAY_NINE, soundPool.load(getContext(), R.raw.nine, 1));
            soundPoolMap.put(SAY_EIGHT, soundPool.load(getContext(), R.raw.eight, 1));
            soundPoolMap.put(SAY_SEVEN, soundPool.load(getContext(), R.raw.seven, 1));
            soundPoolMap.put(SAY_SIX, soundPool.load(getContext(), R.raw.six, 1));
            soundPoolMap.put(SAY_FIVE, soundPool.load(getContext(), R.raw.five, 1));
            soundPoolMap.put(SAY_FOUR, soundPool.load(getContext(), R.raw.four, 1));
            soundPoolMap.put(SAY_THREE, soundPool.load(getContext(), R.raw.three, 1));
            soundPoolMap.put(SAY_TWO, soundPool.load(getContext(), R.raw.two, 1));
            soundPoolMap.put(SAY_ONE, soundPool.load(getContext(), R.raw.one, 1));
            soundPoolMap.put(SAY_MISS, soundPool.load(getContext(), R.raw.miss, 1));
            
            soundPoolMap.put(HIT_SOUND, soundPool.load(getContext(), R.raw.hit, 1));
            soundPoolMap.put(SHOOT_SOUND, soundPool.load(getContext(), R.raw.shoot, 1));
            soundPoolMap.put(MSG_SOUND, soundPool.load(getContext(), R.raw.msg, 1));
            soundPoolMap.put(DRAW_SOUND, soundPool.load(getContext(), R.raw.draw, 1));
       }

        public void playSound(int sound, float rate) {
            AudioManager mgr = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
            float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);    
            float volume = streamVolumeCurrent / streamVolumeMax;

            // Play the sound with the correct volume
            soundPool.play((Integer) soundPoolMap.get(sound), volume, volume, 1, 0, rate);     
        }

        // Set the current round. This is the type of archery game
        public void setRound(String round) {
        	mCurrentRound = round;
            mRoundsDistance = new Stack();
            mRoundsArrows = new Stack();
            mClassification = new Stack();

            if(round.contentEquals("Hereford")) {
            	mMaxEnds = 18;
            	mRoundsDistance.push(EIGHTY_YARDS);
            	mRoundsDistance.push(SIXTY_YARDS);
            	mRoundsDistance.push(FIFTY_YARDS);
            	
            	mImperial = true; // How it is measured
            	
                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(36));
            	mRoundsArrows.push(new Integer(36));
            	mRoundsArrows.push(new Integer(36));
            	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(884)); // FIRST
            	mClassification.push(new Integer(723)); // SECOND
            	mClassification.push(new Integer(477)); // THIRD
            	
            }
            
            if(round.contentEquals("Fita 70")) {
            	mMaxEnds = 12;
            	
            	mRoundsDistance.push(SEVENTY_METRES);            	
            	
            	mImperial = false;
            	
                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(72));
            	            	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(437)); // FIRST
            	mClassification.push(new Integer(340)); // SECOND
            	mClassification.push(new Integer(197)); // THIRD
            	
            }
            
            if(round.contentEquals("Glade League")) {
            	mMaxEnds = 12;
            	
            	mRoundsDistance.push(SEVENTY_METRES);            	
            	
            	mImperial = false;
            	
                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(72));
            	            	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(437)); // FIRST
            	mClassification.push(new Integer(340)); // SECOND
            	mClassification.push(new Integer(197)); // THIRD
            	
            }
            
            if(round.contentEquals("Albion")) {
            	mMaxEnds = 18;
            	
                // Change of distance after each round
            	mRoundsDistance.push(EIGHTY_YARDS);
            	mRoundsDistance.push(SIXTY_YARDS);            	            	
            	mRoundsDistance.push(FIFTY_YARDS);            	            	
            	
            	mImperial = true;
            	
                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(36));
            	mRoundsArrows.push(new Integer(36));
            	mRoundsArrows.push(new Integer(36));  
            	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(700)); // FIRST
            	mClassification.push(new Integer(590)); // SECOND
            	mClassification.push(new Integer(412)); // THIRD
            }
            
            if(round.contentEquals("Windsor")) {
            	mMaxEnds = 18;
            	
                // Change of distance after each round
            	mRoundsDistance.push(EIGHTY_YARDS);
            	mRoundsDistance.push(SIXTY_YARDS);            	
            	mRoundsDistance.push(FOURTY_YARDS);            	
            	            	            	
            	mImperial = true;
            	
                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(36));
            	mRoundsArrows.push(new Integer(36));
            	mRoundsArrows.push(new Integer(36));  
            	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(0)); 	// FIRST
            	mClassification.push(new Integer(713)); // SECOND
            	mClassification.push(new Integer(563)); // THIRD
            }
            
            if(round.contentEquals("Bristol")) {
            	mMaxEnds = 24;

                // Change of distance after each round
            	mRoundsDistance.push(EIGHTY_YARDS);
            	mRoundsDistance.push(SIXTY_YARDS);            	
            	mRoundsDistance.push(FIFTY_YARDS);            	
            	
            	mImperial = true;

                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(72));
            	mRoundsArrows.push(new Integer(48));            	            	
            	mRoundsArrows.push(new Integer(24));
            	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(884)); // FIRST
            	mClassification.push(new Integer(723)); // SECOND
            	mClassification.push(new Integer(477)); // THIRD
            }
           
            if(round.contentEquals("Long National")) {
            	mMaxEnds = 12;

                // Change of distance after each round
            	mRoundsDistance.push(EIGHTY_YARDS);
            	mRoundsDistance.push(SIXTY_YARDS);
            	
            	mImperial = true;

                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(48));
            	mRoundsArrows.push(new Integer(24));
            	
            	              	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(418)); // FIRST
            	mClassification.push(new Integer(330)); // SECOND
            	mClassification.push(new Integer(202)); // THIRD
            }
            
            if(round.contentEquals("National")) {
            	mMaxEnds = 12;

                // Change of distance after each round
            	mRoundsDistance.push(SIXTY_YARDS);
            	mRoundsDistance.push(FIFTY_YARDS);
            	
            	mImperial = true;

                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(48));
            	mRoundsArrows.push(new Integer(24));
            	            	   
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(0)); 	// FIRST
            	mClassification.push(new Integer(436)); // SECOND
            	mClassification.push(new Integer(319)); // THIRD
            }
            
            if(round.contentEquals("Western")) {
            	mMaxEnds = 16;

                // Change of distance after each round
            	mRoundsDistance.push(SIXTY_YARDS);
            	mRoundsDistance.push(FIFTY_YARDS);
            	
            	mImperial = true;
            	
                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(48));
            	mRoundsArrows.push(new Integer(48));  
            	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(0)); 	// FIRST
            	mClassification.push(new Integer(595)); // SECOND
            	mClassification.push(new Integer(445)); // THIRD
            }
            
            if(round.contentEquals("Short Western")) {
            	mMaxEnds = 16;

                // Change of distance after each round
            	mRoundsDistance.push(FIFTY_YARDS);
            	mRoundsDistance.push(FOURTY_YARDS);
            	      
            	mImperial = true;
            	
                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(48));
            	mRoundsArrows.push(new Integer(48));  
            	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(0)); 	// FIRST
            	mClassification.push(new Integer(0)); 	// SECOND
            	mClassification.push(new Integer(556)); // THIRD
            }
           
            if(round.contentEquals("Long Western")) {
            	mMaxEnds = 16;

                // Change of distance after each round
            	mRoundsDistance.push(EIGHTY_YARDS);
            	mRoundsDistance.push(SIXTY_YARDS);
            	
            	mImperial = true;
            	
                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(48));
            	mRoundsArrows.push(new Integer(48));  
            	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(579)); // FIRST
            	mClassification.push(new Integer(468)); // SECOND
            	mClassification.push(new Integer(300)); // THIRD
            }
            
            if(round.contentEquals("American")) {
            	mMaxEnds = 15;

                // Change of distance after each round
            	mRoundsDistance.push(SIXTY_YARDS);
            	mRoundsDistance.push(FIFTY_YARDS);            	
            	mRoundsDistance.push(FOURTY_YARDS);            	
            	            	
            	mImperial = true;
            	
                // Number of arrows on each round
            	mRoundsArrows.push(new Integer(30));
            	mRoundsArrows.push(new Integer(30));
            	mRoundsArrows.push(new Integer(30));     
            	
            	mClassification.push(new Integer(0)); 	// BOWMAN
            	mClassification.push(new Integer(0)); 	// FIRST
            	mClassification.push(new Integer(594)); // SECOND
            	mClassification.push(new Integer(469)); // THIRD
            }
        }

        public void setScreenSize(int width, int height) {
        	mScreenWidth = width;
        	mScreenHeight = height;
        }
        
        // Draw the background with some nice trees. Created in Blender
        public void drawBackground() {
        	int scaley = mScreenHeight/5;
        	mMiddleX = mScreenWidth/2;
        	mMiddleY = (mScreenHeight/2)+scaley;
        	mSightX = mMiddleX;
            mSightY = mMiddleY;
            
            int xwStand=0;
            int ywStand=0;
            int xStandScale=0;
            int yStandScale=0;

            // Draw the stand and bodd target at different distances
            // depending on the game/round being played
            if(mCurrentDistance == FOURTY_YARDS) {
            	xwStand=+70;
            	ywStand=+62;
            	xStandScale=140;
            	yStandScale=140;
            	mDistanceString = "40 YARDS";
            }
            
            if(mCurrentDistance == FOURTY_METRES) {
            	xwStand=+62;
            	ywStand=+55;
            	xStandScale=125;
            	yStandScale=125;
            	if(mImperial) {
            		mDistanceString = "40 YARDS";
            	} else {
            		mDistanceString = "40 METRES";
            	}
            }
            
            if(mCurrentDistance == FIFTY_YARDS) {
            	xwStand=+55;
            	ywStand=+48;
            	xStandScale=110;
            	yStandScale=110;
            	if(mImperial) {
            		mDistanceString = "50 YARDS";
            	} else {
            		mDistanceString = "50 METRES";
            	}
            }
            
            if(mCurrentDistance == FIFTY_METRES) {
            	xwStand=+47;
            	ywStand=+42;
            	xStandScale=95;
            	yStandScale=95;
            	mDistanceString = "50 METRES";
            }
            
            if(mCurrentDistance == SIXTY_YARDS) {
            	xwStand=+40;
            	ywStand=+36;
            	xStandScale=80;
            	yStandScale=80;
            	mDistanceString = "60 YARDS";
            }
            
            if(mCurrentDistance == SEVENTY_METRES) {
            	xwStand=+35;
            	ywStand=+30;
            	xStandScale=70;
            	yStandScale=70;
            	mDistanceString = "70 METRES";
            }
            
            if(mCurrentDistance == EIGHTY_YARDS) {
            	xwStand=+30;
            	ywStand=+24;
            	xStandScale=55;
            	yStandScale=55;
            	mDistanceString = "80 YARDS";
            }
            
            // Draw the actual background
            Resources res = mContext.getResources();
            mBackground = new BitmapDrawable();
            Canvas offscreen = new Canvas();
            scratch = Bitmap.createBitmap(mScreenWidth,mScreenHeight,Bitmap.Config.ARGB_8888);
            
            offscreen = new Canvas();
    
            offscreen.setBitmap(scratch);        
            offscreen.drawColor(Color.GRAY);

            mBackgroundImage = mBackgroundImage.createScaledBitmap(
                    mBackgroundImage, mScreenWidth, mScreenHeight, true);
            offscreen.drawBitmap(mBackgroundImage, 0, 0, null);
            
            Paint paint = new Paint();
            
            // Draw the stand for the boss target
            mStandImage = mStandImage.createScaledBitmap(
                    mStandImage, xStandScale, yStandScale, true);
          
            offscreen.drawBitmap(mStandImage,mMiddleX-xwStand, mMiddleY-ywStand, null);
            mTargetX = mScreenWidth/2;
            mTargetY = mScreenHeight/2;

            // Draw the boss target  
            mTarget = new Target(offscreen, mMiddleX, mMiddleY, true, mCurrentDistance);
            mTarget.draw(offscreen);
        }
        
        public void drawScore(Canvas c, int fade) {
    		Paint paint = new Paint();
    		paint.setColor(Color.YELLOW);
    		paint.setAntiAlias(true);
    		paint.setTextSize(40);
    		paint.setColor(Color.YELLOW);
    		paint.setAlpha(fade);
    		
    		c.drawText(String.valueOf(arrow.mCurrentScore), arrow.last_arrow_x-8,arrow.last_arrow_y, paint);
        }  
        
        // Start the game
        public void doStart() {
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
                setState(STATE_RUNNING);
            }
        }

        @Override
        public void run() {
            // Get the game parameter sele4cted by the player
        	mCurrentDistance = (Integer)mRoundsDistance.pop();
        	mTotalArrowsForDistance = (Integer)mRoundsArrows.pop();
        	
        	mThirdClassScore = (Integer)mClassification.pop();
        	mSecondClassScore = (Integer)mClassification.pop();
        	mFirstClassScore = (Integer)mClassification.pop();
        	mBowmanScore = (Integer)mClassification.pop();

            // Display the background
        	drawBackground();
        	
            Canvas canvas = null;
            long lastRedraw = System.currentTimeMillis();
            while (true) {

            	if(canvas == null) {
            		mRun=true;
            	}

            	if(mRun) {
            		if(System.currentTimeMillis()-lastRedraw > 10) {
                        canvas = mSurfaceHolder.lockCanvas();
                        if (canvas != null) {
                		    doDraw(canvas);
                		    lastRedraw = System.currentTimeMillis();
                            mSurfaceHolder.unlockCanvasAndPost(canvas);
                        }
            		}
            	}
            }
          
        }

        // Draw the sight bow and arrow
        private void doDraw(Canvas canvas) {
        	Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            
            // Move the sight as the phone is tilted by accelerometer
            if(game_state != ROUND_END) { 
            	mSightX+=mAccelX;
            	mSightY+=mAccelY;
            }
        	
            // Draw the sight
            canvas.drawBitmap(this.scratch, 0, 0, null);
            drawSight(mSightX,mSightY,canvas);

            // Create a wind object if we dont already have one
            if(wind == null) {
            	wind = new Wind(canvas,mContext);
            } else {
                // draw an arrow to show the winds changing direction
            	int wind_centre = mScreenHeight/2;
            	wind.drawWind(mScreenWidth-20, wind_centre-20, canvas);
            	
                if(game_state!=ROUND_END) {
            		wind.run(); // Simulate the wind
            	}

            	String shortWindValue = String.format("%2.2f", wind.mSpeed);
            	b.putString("wind_speed", shortWindValue + " mph");
            }
            
            // Create a arrow object if we dont already have one
            if(arrow==null) {
            	arrow = new Arrow(canvas,mSightX, mSightY-42, mTarget, mImperial);
            } else {
            	arrow.draw(mSightX,mSightY-42, wind);
            	
            	if(arrow.arrow_state==arrow.ARROW_SHOT || arrow.arrow_state==arrow.ARROW_READY_FOR_RELOAD) {
            		arrow.run(wind); // Simulate an arrow being shot and apply wind
            	}
            
            	// Reload the arrow if we hit and scored the point
                if(arrow.mScored) {
                 	if((System.currentTimeMillis()-mLastTime)>3600) {
                 		arrow.mScored = false;
                 		arrow.arrow_state=arrow.ARROW_READY_FOR_RELOAD;
                 	}
                } else {
                	 mLastTime = System.currentTimeMillis();
                	 mFade=255;
                }
                
                // Draw the score point on the screen and fade over shot period
                if(arrow.mScored) {
              	   drawScore(canvas, mFade);
              	   arrow.last_arrow_y--;
              	   mFade-=2;
                }
                 
                // If we hit target play the number sound and calculate score 
                if(arrow.arrow_state == arrow.ARROW_HOLDING_SCORE) {                	
                	int hit_score = arrow.get_score();
                	playSound(HIT_SOUND,1);
                	playSound(hit_score,1);
                	mRunningTotal += hit_score;
                	arrow.arrow_state = arrow.ARROW_SCORED;
                	mArrowCount += 1;
                	mTotalDistanceArrowCount += 1;
                	mDozenScore += hit_score;
                	mDozenCount += 1;                	                 
                	 
                    // Move through to the next round if needed
                	if(mTotalDistanceArrowCount == mTotalArrowsForDistance) {
                		mTotalDistanceArrowCount = 0;
                		if(mCurrentEnd+1<mMaxEnds) {                			
                			mCurrentDistance = (Integer)mRoundsDistance.pop();                			 
                			mTotalArrowsForDistance = (Integer)mRoundsArrows.pop();
                		}
                     	arrow=null;
                     	drawBackground(); 
                	 }
                	 
                	 
                	 // Inform player of the scored dozen
                	 if(mDozenCount == 12) {
                		 String dozenMsg = String.valueOf(mDozenScore) + " Dozen";
                		 mDozenScore = 0;
                		 mDozenCount = 0;
                		 playSound(MSG_SOUND,1);
                		 card = new Card(canvas,System.currentTimeMillis(), mScreenWidth, mScreenHeight, dozenMsg, tf, thread);
                	 }
                	 
                	// Inform player of the scored end
                	 if(mDozenCount == 6) {
                		 String dozenMsg = String.valueOf(mDozenScore) + " End";                		                 		 
                		 card = new Card(canvas,System.currentTimeMillis(), mScreenWidth, mScreenHeight, dozenMsg, tf, thread);
                		 playSound(MSG_SOUND,1);
                	 }
                	 
                     // Display Archers score and classification
                	 if(mRunningTotal>=mThirdClassScore && mCurrentArchersClass.contains("NOVICE") && mThirdClassScore!=0) {
                		String lastClass = mCurrentArchersClass;                		 
                		mCurrentArchersClass = "THIRD";
                		String msgAward = mCurrentArchersClass + " CLASS";
                		if(lastClass == "NOVICE") {
                			card = new Card(canvas,System.currentTimeMillis(), mScreenWidth, mScreenHeight, msgAward, tf, thread);
                			playSound(MSG_SOUND,1);
                		}
                	}

                    // Display Archers score and classification 
                	if(mRunningTotal>=mSecondClassScore && mCurrentArchersClass.contains("THIRD") && mSecondClassScore!=0) {
                		String lastClass = mCurrentArchersClass;
                		mCurrentArchersClass = "SECOND";
                		String msgAward = mCurrentArchersClass + " CLASS";
                		if(lastClass == "THIRD") {
                			card = new Card(canvas,System.currentTimeMillis(), mScreenWidth, mScreenHeight, msgAward, tf, thread);
                			playSound(MSG_SOUND,1);
                		}
                	}
                	 
                    // Display Archers score and classification
                	if((mBowmanScore == 0) && mRunningTotal >= mFirstClassScore && (mFirstClassScore != 0)) {
                		String lastClass = mCurrentArchersClass;
                		mCurrentArchersClass = "FIRST";
                		String msgAward = mCurrentArchersClass + " CLASS";
                		if(lastClass == "SECOND") {
                			card = new Card(canvas,System.currentTimeMillis(), mScreenWidth, mScreenHeight, msgAward, tf, thread);
                			playSound(MSG_SOUND,1);
                		}
                	}
                	 
                	// Little strange but stop player getting Bownman when Bowman is not available on that round.
                	if((mBowmanScore!=0) && mRunningTotal>=mFirstClassScore && mRunningTotal<mBowmanScore) {
                		mCurrentArchersClass = "FIRST";                		
                	}

                    // Display Archers score and classification	 
                	if((mBowmanScore!=0) && mRunningTotal>=mBowmanScore) {
                		String lastClass = mCurrentArchersClass;
                		mCurrentArchersClass = "BOWMAN";
                		String msgAward = mCurrentArchersClass + " CLASS";
                		if(lastClass == "FIRST") {
                			playSound(MSG_SOUND,1);
                			card = new Card(canvas,System.currentTimeMillis(), mScreenWidth, mScreenHeight, msgAward, tf, thread);
                		}
                	}
                }
                                  
                String paddedScore = String.format("%04d", mRunningTotal);
                b.putString("new_score", paddedScore);
                b.putString("game_ends", String.valueOf(mCurrentEnd) + "/" + String.valueOf(mMaxEnds));
                b.putString("archers_class", mCurrentArchersClass);
                b.putString("distance", mDistanceString);
                msg.setData(b);
                // Reset archers quiver as they can only ever have 6 arrows
            	if(mArrowCount > 5) {
            		mArrowCount=0;
            		
                    // Check for archery round being completed
            		if(mCurrentEnd+1 > mMaxEnds) {
            			// GAME COMPLETED
            			game_state = ROUND_END;
            			String msgGameOver = "Game Over";
            			card = new Card(canvas,System.currentTimeMillis(), mScreenWidth, mScreenHeight, msgGameOver, tf, thread);
            			playSound(MSG_SOUND,1);	 
            		} else {
            			mCurrentEnd+=1;
            		}	 
            	}
                 
                 mHandler.sendMessage(msg);
            }

            // Draw the riser. This is the archers bow         
            mRiserImage.setBounds((int) mSightX-52, (int)mSightY-400, (int)mSightX+78, (int)mSightY + 465);
            mRiserImage.draw(canvas);
            
            // Create a bowstring object if not already created
            if(bowstring==null) {
            	bowstring = new BowString(canvas,arrow, thread);
            } else {
            	bowstring.draw();
            }

            canvas.save();
            
            // Tell the player the round is about to start
            if(game_state == ROUND_START) {
           	    card = new Card(canvas,System.currentTimeMillis(), mScreenWidth, mScreenHeight, "Get Ready", tf, thread);
           	    game_state=ROUND_STARTED;
            }
            
            if(card!=null) {
           	    card.draw();
            }
            
            // Allow the player to submit their score
            if(card!=null) {
           	    if((System.currentTimeMillis()-card.getTime())>3500) {
           		    card=null;    
           		    if(game_state==ROUND_END) {
           			    submit_score(mRunningTotal, mContext);
                        }
                }
            }
            
            // END OF SHOW CARD
            canvas.restore();
        }
        
        public String md5(String s) {
        	MessageDigest digest;
        	try {
        	    digest = MessageDigest.getInstance("MD5");
        	    digest.reset();
        	    digest.update(s.getBytes());
        	    byte[] a = digest.digest();
        	    int len = a.length;
        	    StringBuilder sb = new StringBuilder(len << 1);
        	    for (int i = 0; i < len; i++) {
        	        sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
        	        sb.append(Character.forDigit(a[i] & 0x0f, 16));
        	    }
        	    return sb.toString();
        	} catch (NoSuchAlgorithmException e) { 
                e.printStackTrace(); 
            }
        	
            return null;
        }

        
        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	//set up dialog
                final Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.submit_scores);
                dialog.setTitle("Submit Score");
                dialog.setCancelable(true);
        
                //set up button
                final EditText edtName = (EditText) dialog.findViewById(R.id.name_scores);
                TextView txtInfo = (TextView) dialog.findViewById(R.id.submit_score_text);
                final TextView txtTag = (TextView) dialog.findViewById(R.id.submit_score_tag);
                final TextView txtTagNote = (TextView) dialog.findViewById(R.id.tag_note);
                Button btnCancel = (Button) dialog.findViewById(R.id.cancel_scores);
                Button btnSubmit = (Button) dialog.findViewById(R.id.send_scores);
                txtInfo.setText("Please view the ProArchery Scoreboard at http://proarcheryscores.evansdev.co.uk");
                txtTagNote.setText("Please note your unique ID to see your entry on scoreboard.");
                final String tag;
                
                String oldtag = md5(strPhoneID);               
                
                // convert tag
                tag = oldtag.substring(0, 10);

                txtTag.setText(tag);

                // Submit the score to the pro archery score site
                btnCancel.setOnClickListener(new OnClickListener() {
                @Override
                    public void onClick(View v) {
                       dialog.dismiss();
                       try {
                       		write_score_data();
                       		wl.release();
							System.exit(1);
						} catch (Throwable e) {
							e.printStackTrace();
						}
                    }
                });

                btnSubmit.setOnClickListener(new OnClickListener() {
                    @Override
                        public void onClick(View v) {    	
                    	    String strName = edtName.getText().toString();
                    	    HttpParams httpParameters = new BasicHttpParams();
                            HttpConnectionParams.setConnectionTimeout(httpParameters, 5 * 1000); // wait 5 seconds
                            HttpConnectionParams.setSoTimeout(httpParameters, 30 * 1000); // wait 30 seconds
                            HttpClient client = new DefaultHttpClient(httpParameters);

                            String url;
                            String baseURL="http://";
                         
                            String md5 = md5(strName + tag + "oscarminty");
                         
                 		    url = baseURL.concat("proarcheryscores.evansdev.co.uk?name=" + strName + "&tag=" + tag + "&score=" + mRunningTotal + "&round=" + mCurrentRound + "&classification=" + mCurrentArchersClass + "&secret=" + md5);
                 		 
                 		    url = url.replaceAll(" ", "%20");
                            HttpGet request = new HttpGet(url);
                            request.addHeader("User-Agent", "ProArchery Mobile App");
                            HttpResponse httpResponse;
                            int responseCode=404;
                            try {
                 				httpResponse = client.execute(request);
                 				responseCode = httpResponse.getStatusLine().getStatusCode();
                 				if(responseCode!=200) {
                 					
                 					final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                 					alertDialog.setTitle("Internet Connection");
                 					alertDialog.setMessage("Sorry, Could not submit your score\nTry again or Cancel.");
                 					alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                 					      public void onClick(DialogInterface dialog, int which) {
                 					    	  alertDialog.dismiss();
                 					    } }); 
                 					alertDialog.show();
                 				}
                 			} catch (ClientProtocolException e) {
                 				e.printStackTrace();
                 			} catch (IOException e) {
                 				e.printStackTrace();
                 			}
                 		   if(responseCode == 200) {
                 			   dialog.dismiss();
                 			   try {
                 				   write_score_data();
                 				   System.exit(1);
                 			   } catch (Throwable e) {
                 				   e.printStackTrace();
                 			   }
                 		   }
                 		}
                    });
                //now that the dialog is set up, it's time to show it    
                dialog.show();
            }
        };

        private void submit_score(int score, Context context) {
        	handler.sendEmptyMessage(1);
		}

        // Save the score to the players phone
        public void write_score_data() {
        	FileReader in;
        	File sdDir = new File(Environment.getExternalStorageDirectory().getPath());
        	File proarcheryDir = new File(sdDir.getAbsolutePath() + "/com.proarchery");
    		File filetst = new File(proarcheryDir + "/data.txt");
    		
    		if(filetst.exists()) {
    			try {
    				Integer [] sc = new Integer[3];
    				in = new FileReader(new File(proarcheryDir + "/data.txt"));
    				BufferedReader br = new BufferedReader(in);
    				String s;
    				s = br.readLine();
    				String[] ss = s.split(",");
    				sc[0] = Integer.valueOf(ss[0]);
    				sc[1] = Integer.valueOf(ss[1]);
    				sc[2] = Integer.valueOf(ss[2]);
    			
    				if(mCurrentArchersClass.contains("FIRST")) {
    					sc[0]+=1;
    				}
    				if(mCurrentArchersClass.contains("SECOND")) {
    					sc[1]+=1;
    				}
    				if(mCurrentArchersClass.contains("THIRD")) {
    					sc[2]+=1;
    				}
    				
    				in.close();
    				
    				FileWriter out;		       		      
		        	try {
		        		out = new FileWriter(new File(proarcheryDir + "/data.txt"));
					
		        		out.write(String.valueOf(sc[0] + "," + String.valueOf(sc[1]) + "," + String.valueOf(sc[2])));
					
		        		out.close();
		        		
		        	} catch (IOException e) {
		        		// TODO Auto-generated catch block
		        		e.printStackTrace();		        		
		        	}
    				
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();				
    			}
    		} 

        }

        // Tell the thread to run or not
        public void setRunning(boolean b) {
            mRun = b;
        }

        // Set the game state
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        // Set the state of the game
        public void setState(int mode, CharSequence message) {
            synchronized (mSurfaceHolder) {
                mMode = mode;

                if (mMode == STATE_RUNNING) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                   
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                } else {            
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
               
                    mHandler.sendMessage(msg);
                }
            }
        }

      
        
        // Surface dimension change
        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                mScreenWidth = width;
                mScreenHeight = height;               

                // Resize the background image
                mBackgroundImage = mBackgroundImage.createScaledBitmap(
                        mBackgroundImage, width, height, true);
            }
        }

        // Resume from Pause
        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
            }
            setState(STATE_RUNNING);
        }
        
       

    }

    // Handle to the application context
    public Context mContext;

    // The thread that actually draws the animation
    private GameThread thread;
    private SensorManager mSensorManager;
    private final SensorListener mSensorAccelerometer = new SensorListener() {
    	// accelerometer sensor values.
        private float mAccelX;
        private float mAccelY;
        private float mAccelZ; // this is never used but just in-case future
        private float xacc = 1;
        private float yacc = 1;
        // accelerometer buffer, currently set to 0 so even the slightest movement
        // will roll the marble.
        private float mSensorBuffer = 0;
    	
        // method called whenever new sensor values are reported.
        public void onSensorChanged(int sensor, float[] values) {
            // grab the values required to respond to user movement.
        	float yaw = values[0];
            float pitch = values[1];
            float roll = values[2];
            	// Tilt
            	if (roll>0) {
            		thread.mAccelX=-0.8;
            	}
            	if(roll<0) {
            		thread.mAccelX=+0.8;
            	}
                
            	if (pitch>-30) {
            		thread.mAccelY=-0.8;
            	}
            	if(pitch<-30) {
            		thread.mAccelY=+0.8;
            	}
            	 
            	int scalex = thread.mScreenWidth/5;
            	int scaley = thread.mScreenHeight/5;
            	int left = scalex;
            	int right = thread.mScreenWidth-scalex;
            	int top = scaley;
            	int bottom = thread.mScreenHeight;
            	// Bounce
            	if(thread.mSightX<left) {
            		thread.mAccelX=+0.8;
            	}
            	if(thread.mSightX>right) {
            		thread.mAccelX=-0.8;
            	}
            	if(thread.mSightY<top) {
            		thread.mAccelY=+0.8;
            	}
            	if(thread.mSightY>bottom) {
            		thread.mAccelY=-0.8;
            	}
                            
        }

        // reports when the accuracy of sensor has change
        // SENSOR_STATUS_ACCURACY_HIGH = 3
        // SENSOR_STATUS_ACCURACY_LOW = 1
        // SENSOR_STATUS_ACCURACY_MEDIUM = 2
        // SENSOR_STATUS_UNRELIABLE = 0 //calibration required.
        public void onAccuracyChanged(int sensor, int accuracy) {
            // currently not used
        }
    };
    
    private TextView mActualScore;
    private TextView mGameEnds;
    private TextView mArchersClass;
    private TextView mWindSpeedValue;
    private TextView mDistance;
    private PowerManager.WakeLock wl;
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        wl.acquire();
        
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        
        // setup accelerometer sensor manager.
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        
        // register our accelerometer so we can receive values.
        // SENSOR_DELAY_GAME is the recommended rate for games
        mSensorManager.registerListener(mSensorAccelerometer, SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_GAME);
        
        try {
        // create thread
        thread = new GameThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                mActualScore.setText(m.getData().getString("new_score"));
                mGameEnds.setText(m.getData().getString("game_ends"));
                mArchersClass.setText(m.getData().getString("archers_class"));
                mWindSpeedValue.setText(m.getData().getString("wind_speed"));
                mDistance.setText(m.getData().getString("distance"));
            }
        });
        } catch(NullPointerException e) {
        	
        }
       
        setFocusable(true); // make sure we get key events
    }
 
    // Get the game thread
    public GameThread getThread() {
        return thread;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    { 
    	thread.bowstring.HandleTouch(event, this.getContext());
        return true;
    }

    // Pause thread on focus lost
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
    }

    // Set the surface size if the surface is changed 
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    	thread.setSurfaceSize(width, height);
    }

    public void setTextViewScore(TextView textView) {
        mActualScore = textView;
    }
    
    public void setTextViewEnds(TextView textView) {
        mGameEnds = textView;
    }
    
    public void setTextViewClass(TextView textView) {
        mArchersClass = textView;
    }
    
    public void setTextViewWindSpeed(TextView textView) {
        mWindSpeedValue = textView;
    }
    
    public void setTextViewDistance(TextView textView) {
        mDistance = textView;
    }
    
    // Set the thread running when surface created
    public void surfaceCreated(SurfaceHolder holder) {
    	if (!thread.isAlive()) {
            thread.setRunning(true);
            thread.start();
        }
    	
    	if(thread.isAlive()) {
    		thread.setRunning(true);
    	}
    }

    /// Stop the thread if the surface has been destroyed
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        
        if (thread.isAlive()) {
            thread.setRunning(false);
        }
     
    }
    
    protected void onPause() {
            wl.release();
            if (thread.isAlive()) {
                thread.setRunning(false);
                thread.pause();
            }         
    }

    protected void onResume() {
    	if (!thread.isAlive()) {
            thread.setRunning(true);
            thread.start();
        }
            wl.acquire();        
    }
}
