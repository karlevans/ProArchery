/*
 *ProArchery (c) 2011
 *
 * Author: Karl Evans
 * Arrow class to simulate shooting an arrow against wind and checking if we hit a circular target 
 *
 */
package com.proarchery;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;

/* Class for the arrow */
public class Arrow {
	public static int ARROW_NOT_LOADED 	= 0;
	public static int ARROW_LOADED 	= 1;
	public static int ARROW_SHOT = 2;
	public static int ARROW_HIT	= 3;
	public static int ARROW_MISSED	= 4;
	public static int ARROW_MAX_DISTANCE = 160;
	public static int ARROW_LIFE_ENDED	= 5;
	public static int ARROW_HOLDING_SCORE  = 6;
	public static int ARROW_READY_FOR_RELOAD = 7;
	public static int ARROW_SCORED 	= 8;
	public static int ARROW_SIZE = 15;
	public static int ARROW_SPEED = 2;
	public static int arrow_state;
	public static double arrow_distance;
	public static float arrow_x;
	public static float arrow_y;
	public static float last_arrow_x;
	public static float last_arrow_y;
	private static float arrow_diameter;
	private static float targetx;
	private static float targety;
	static Canvas c = null;
	private static int [] curve;
	static int curve_pos=0;
	static public float targetSize;
	static float segDiv;
	static int mCurrentScore;
	static boolean mScored;
	static int segment;
	Thread mMainThread;
	static boolean mImperial;
	
	/* Constructor for the arrow */
	public Arrow (Canvas canvas, float sight_x, float sight_y, Target target, boolean scoring_system) {
		arrow_state = ARROW_LOADED;
		arrow_distance = 0.0;
		arrow_x = sight_x;
		arrow_y = sight_y;		
		arrow_diameter = 10;
		targetx = target.mTargetX;
		targety = target.mTargetY;
		targetSize = target.mTargetSize;
		c = canvas;
		curve = new int[] {+5,+4,+3,+2,+1,-1,-2,-3,-4,-5};
		segDiv = target.mSegDiv;
		mScored = false;
		mImperial = scoring_system;
		
	}
	
	/* Draw the arrow on the screen */
	public void draw(float sightx, float sighty, Wind wind) {
		if(arrow_state == ARROW_SHOT) {	
			Paint paint = new Paint();
			paint.setColor(Color.rgb(100,100,100));

			/* Not bothered about granualirty so make is 1 */
			if(arrow_diameter<2) {
				arrow_diameter = 1;
			}

			/* Draw the arrow */
			c.drawCircle(arrow_x, arrow_y, arrow_diameter, paint);
			arrow_x+=((wind.mSpeed/40)*(Math.cos(Math.toRadians(wind.mAngle-90))));
			arrow_y+=((wind.mSpeed/40)*(Math.sin(Math.toRadians(wind.mAngle-90))));
		}

		/* Align the arrow with the Archers Bow */
		if(arrow_state==ARROW_LOADED) {
			arrow_x = sightx;
			arrow_y = sighty;
			
		}
		
		/* If the arrow is very small make it easier to see against target boss */
		if(arrow_diameter<2) {
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			if(arrow_diameter<2) {
				arrow_diameter = 1;
			}
			c.drawCircle(arrow_x, arrow_y, arrow_diameter, paint);
		}
		
	}
	
	/* Run the wind simulation */
	public void run(Wind wind) {
		if(arrow_state==ARROW_SHOT) {
			arrow_diameter-=0.2;
			arrow_distance+=ARROW_SPEED;
			double cpos = arrow_distance/(ARROW_MAX_DISTANCE/10);
			arrow_y-=curve[(int)cpos];				
			float x =(float) (Math.sqrt((arrow_x-targetx)*(arrow_x-targetx)+(arrow_y-targety)*(arrow_y-targety)));						
		}
		
		/* Calculate using the distance from point of origin where the arrow has hit */
		/* This calculation will determine if we have hit to area of a circle, which looks like a Archers boss target */
		if(arrow_state==ARROW_SHOT && arrow_distance>=(ARROW_MAX_DISTANCE-ARROW_SPEED)) {
			if((float) (Math.sqrt((arrow_x-targetx)*(arrow_x-targetx)+(arrow_y-targety)*(arrow_y-targety)))<=targetSize) {
				float x =(float) (Math.sqrt((arrow_x-targetx)*(arrow_x-targetx)+(arrow_y-targety)*(arrow_y-targety)));				
				checkHit(x);
			} else {
				checkMiss();
			}
			
		}
		
		/* Once we have hit a scored our shot the arrow can be reloaded */
		if(arrow_state==ARROW_READY_FOR_RELOAD) {
			reload_arrow();
		}
		
	}
	
	public void reload_arrow() {
		arrow_diameter = ARROW_SIZE;
		arrow_distance = 0.0;
		arrow_state = ARROW_LOADED;
		
	}
	
	protected void checkHit(float x) {
		
		int [] metric_scores = {10,9,8,7,6,5,4,3,2,1};
		int [] imperial_scores = {9,9,7,7,5,5,3,3,1,1};
		int score;
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.YELLOW);
		segment = (int)Math.floor((x/segDiv));
		if(mImperial == true) {
			score = imperial_scores[segment];
		} else {
			score = metric_scores[segment];
		}
	
		mCurrentScore = score;
		mScored = true;
		last_arrow_x = arrow_x;
		last_arrow_y = arrow_y;
		arrow_state=ARROW_HOLDING_SCORE;
      
    }
	
	public int get_score() {
		return mCurrentScore;
	}
	
	public void set_arrow_state(int state) {
		arrow_state=state;
	}
	
	protected void checkMiss() {				
		int score = 0;
		       
		mCurrentScore = score;
		mScored = true;
		last_arrow_x = arrow_x;
		last_arrow_y = arrow_y;
		arrow_state=ARROW_HOLDING_SCORE;
	}
}
