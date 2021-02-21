/*
 * ProArchery (c) 2011
 *
 * Author: Karl Evans
 * Bow class to simulate an archers bowstring.
 * The player would touch the string on the phone or tablet screen and pull down and release to shoot a arrow. 
 *
 */
package com.proarchery;

import com.proarchery.GameView.GameThread;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.os.Vibrator;
import android.view.MotionEvent;

/* BowStrign class */
public class BowString{
	private Canvas c;
	long lastTime;
	int mScreenWidth;
	int mScreenHeight;
	int mCenterX;
	int mCenterY;
	int mStringLength;
	static int mStartX;
	static int mStopX;
	int mStringCenterX;
	static int mStartY;
	static int mStopY;
	static int mDragY;
	String mMsg;
	Typeface mTypeface;
	static boolean mBowStringDrawn = false;
	static Arrow mArrow = null;
	static GameThread mThread;
	static float mTop;
	static float mBottom;
	static float mMidX;
	static float mMidY;

	/* BosString constructor */
	public BowString(Canvas canvas, Arrow arrow, GameThread thread) {
		c = canvas;	
		mThread = thread;
		mScreenWidth = thread.mScreenWidth;
		mScreenHeight = thread.mScreenHeight;
		
		/* Calculate the size of the bowstring and where it can get pulled down to */
		mStringLength=(mScreenWidth/6);	
		int stringHeight = (mScreenHeight/3);
		mStartX = mScreenWidth-(mStringLength)-20;
		mStopX = mStartX+mStringLength;
		mStartY = mScreenHeight-stringHeight;
		mStopY = mScreenHeight-stringHeight;
		mDragY = mStartY;
		mStringCenterX = mStartX+(mStringLength/2);
		mArrow = arrow;
	}
	
	/* Draw the bowstring */
	public void draw() {
		Paint paint = new Paint();
		paint.setStrokeWidth(3);
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		paint.setColor(Color.rgb(0,100,0));

		/* Change the string colour to indicate it is ready to shoot */	
		if((mDragY-mStartY)>40) {
			paint.setColor(Color.RED);
    	} else {
    		paint.setColor(Color.WHITE);
    	}
		
		/* Draw the bos string */
		c.drawLine(mStartX, mStartY, mStringCenterX, mDragY, paint);
		c.drawLine(mStringCenterX, mDragY, mStopX, mStopY, paint);
		paint.setColor(Color.BLACK);
		c.drawPoint(mStringCenterX, mDragY, paint);
		
		float mMidY = mThread.mSightY+23; 
		float mMidX = mThread.mSightX+35;

        /* Draw the arrow */
		if(mThread.arrow.arrow_state == mThread.arrow.ARROW_LOADED) {
			paint.setColor(Color.rgb(100,100,100));
			c.drawLine(mMidX+((mDragY-mStartY)/2), mMidY, mMidX , mMidY-10, paint);
		}

		paint.setColor(Color.BLACK);
		c.drawLine(mMidX+((mDragY-mStartY)/2)+4, mMidY, mMidX+8 , mThread.mSightY+483, paint);
		c.drawLine(mMidX+((mDragY-mStartY)/2)+2, mMidY, mMidX , mThread.mSightY-400, paint);	
	}	
	
	/* Touch screen handler */
	public static void HandleTouch(MotionEvent e, Context context)
	{
	    int eventaction = e.getAction(); 
	    try {
	        if(mThread.game_state==mThread.ROUND_END) {
		    	return;
		    }
	    
	     
	        switch (eventaction ) { 
	            case MotionEvent.ACTION_DOWN:{ // touch on the screen event	            	
	                int x = (int)e.getX();
	                int y = (int)e.getY();
	                if(x>=mStartX && x<=mStopX && y>=mStartY-40 && y<= mStartY+40) {   	
	                		mBowStringDrawn = true;
	                } else {
	                	return;
	                }
	            }

	            case MotionEvent.ACTION_MOVE:{ // move event
	               int x = (int)e.getX();
	               int y = (int)e.getY();
	               
	               if(mBowStringDrawn) {
	            	   if(y<mStartY+50 && y>=mStartY) {
	            		   mDragY = y;
	            	   }

	            	   if(mDragY>mStartY+8) {
	            		   mThread.playSound(mThread.DRAW_SOUND,(float)(mDragY-mStartY)/16);
	            	   }
	               }

	               break;
	            }

                /* Simulate an archer pulling a bow string and releasign it to shoot a arrow */
	            case MotionEvent.ACTION_UP:{  // finger up event
	            	if((mDragY-mStartY)>40) {
	            	    if(mBowStringDrawn && (mDragY>mStartY)) { // Bow string has moved
	            		    mDragY = mStartY;
	            		    mBowStringDrawn=false;

							/* Play a sound like a arrow has been shot */	            		
	            		    mThread.playSound(mThread.SHOOT_SOUND,1);	            		
	            		    if(mArrow.arrow_state==mArrow.ARROW_LOADED) {
	            			    mArrow.arrow_state=mArrow.ARROW_SHOT;
	            		    }
	            	    }
	            	} else {
	            		mDragY = mStartY;
	            		mBowStringDrawn=false;
	            	}
	                break;
	            }
	        }
	    } catch(NullPointerException exc) {
    		
	    }
	 }

}