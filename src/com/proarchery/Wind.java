
/*
 * ProArchery (c) 2011
 *
 * Author: Karl Evans
 * Wind class to simulate wind that gets applied to an arrow when flying towards the target 
 *
 */
package com.proarchery;

import java.util.Random;
import com.proarchery.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

// Wind class
public class Wind {
	private Canvas c;
	public Bitmap mWindImage;
	public float mAngle;
	public float mSpeed;

	// Wind constructor
	public Wind (Canvas canvas, Context context) {
		c = canvas;
		
		Random randomGenerator = new Random();
		mAngle = randomGenerator.nextInt(360);
    	mSpeed = randomGenerator.nextInt(10);
		
		Resources res = context.getResources();
		mWindImage = BitmapFactory.decodeResource(res,
                R.drawable.wind);
	}
	
	// Draw wind
    public void drawWind(float x, float y, Canvas c) {
    	Matrix matrix = new Matrix();
        // resize the bit map
        // rotate the Bitmap
        matrix.postRotate(mAngle);
        
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(mWindImage, 0, 0,
                          23, 38, matrix, true); 

        Paint paint = new Paint();
    	c.drawBitmap(resizedBitmap,x-40, y, paint);
    } 

    // Simulate the wind    
    public void run() {
    	Random randomGenerator = new Random();
    	int direction = randomGenerator.nextInt(2);
    	int speed_direction = randomGenerator.nextInt(2);
    	float ammount = randomGenerator.nextInt(20);
    	float speed_increase = randomGenerator.nextInt(20);

    	speed_increase = speed_increase/25;
    	ammount = ammount/15;

        // Decrease the wind speed
    	if(speed_direction == 0) {
    		if(mSpeed > 0) {
    			mSpeed-=speed_increase/10;
    			if(mSpeed < 0) {
    				mSpeed = 0;
    			}
    		}
    	}

        // Increase the wind speed
    	if(speed_direction == 1) {
    		if(mSpeed < 10) {
    			mSpeed += speed_increase/10;
    			if(mSpeed > 10) {
    				mSpeed = 10;
    			}
    		}
    	}
    	
		// Increment the angle of wind clockwise
    	if(direction == 0) {
    		mAngle += ammount;
    	}

        // Decrement the angle of wind anti clockwise
    	if(direction == 1) {
    		mAngle -= ammount;
    	}
    }
}
