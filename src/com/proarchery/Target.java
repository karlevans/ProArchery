/*
 * ProArchery (c) 2011
 *
 * Author: Karl Evans
 * Target class - creates a archery target 
 *
 */
package com.proarchery;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

// Target class
public class Target {
	Canvas c;
	int mTargetX;
	int mTargetY;
	boolean mMetric;
	int mDistance;
	float mTargetSize;
	int [] mScores;
	int [] mMinSeg;
	int [] mMaxSeg;
	
	float mSegDiv;
	
	// Target constructor
	public Target (Canvas canvas, int x, int y, boolean metric, int distance) {
		mTargetX = x;
		mTargetY = y;
		mMetric = metric;
		mDistance = distance;
		mScores = new int[10];
	}
	
	// Create a target image
    public void draw(Canvas offscreen) {
		// Initialise the colours of a archery target in order
    	int colours [] = { Color.WHITE,
    			 			Color.BLACK,
    			 			Color.BLUE,
    			 			Color.RED,
    			 			Color.YELLOW };
    	 
		// Calculate the segment sizes based on rounds distance
    	float size = mDistance;
    	float original_size = size;
    	mSegDiv = original_size/10;
    	mTargetSize = original_size;
    	int colour=0;
    	int count=0;

		// Create each of the segments of the target
    	for(int seg = 0;seg < 10;seg++) {
    		mScores[seg] = seg+1;
    		Paint paint = new Paint();
    		paint.setAntiAlias(true);
    		paint.setStyle(Paint.Style.FILL_AND_STROKE);
    		paint.setColor(colours[colour]);
    		offscreen.drawCircle(mTargetX, mTargetY, size, paint);

			// Fix for the black segment
    		if(seg == 3) {
    			paint.setColor(Color.DKGRAY);
    		} else {
    			paint.setColor(Color.BLACK);
    		}

    		paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
    		offscreen.drawCircle(mTargetX, mTargetY, size, paint);
    		
    		size-=mSegDiv;
    		count++;

    		if(count > 1) {
    			colour++;
    			count = 0;
    		}
    	}    	
    }
    
}
