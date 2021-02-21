/*
 * ProArchery (c) 2011
 *
 * Author: Karl Evans
 * Card class to  communicate to the player that the game is over.
 *
 */
package com.proarchery;

import com.proarchery.GameView.GameThread;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.widget.Toast;

/* Card class to communicate state of game to the user */
public class Card{
	private Canvas c;
	long lastTime;
	int mScreenWidth;
	int mScreenHeight;
	int mCenterX;
	int mCenterY;
	String mMsg;
	Typeface mTypeface;
	int mAlpha;
	GameThread mThread;

	/* Cards constructor */
	public Card(Canvas canvas, long currentTime, int width, int height, String message, Typeface tf, GameThread thread) {
		c = canvas;
		lastTime = currentTime;
		mScreenWidth = width;
		mScreenHeight = height;
		mCenterX=width/2;
		mCenterY=height/2;	
		mMsg = message;
		mTypeface = tf;
		mAlpha = 255;
		mThread=thread;
	}
	
	/* Draw the card */
	public void draw() {
		int msgLength = mMsg.length();
		int charSize=16;

		Paint paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setColor(Color.rgb(0,100,0));
		paint.setTextSize(charSize);	
		paint.setColor(Color.WHITE);
		paint.setAlpha(mAlpha);
		paint.setTextSize(70);
		paint.setTypeface(mTypeface);
		paint.setTextAlign(Paint.Align.CENTER);

		c.drawText(mMsg, mCenterX, mCenterY, paint);
		if(mMsg.contains("Game Over")) {
			c.drawText("You scored " + String.valueOf(mThread.mRunningTotal), mCenterX, mCenterY+50, paint);
		}

        /* Fade the card slowly after a second of being displayed */
		if(System.currentTimeMillis() - lastTime>1000) {
			if(mAlpha > 0) {
				mAlpha -= 3;
			} else {
				mAlpha = 0;
			}
		}
	}
	
	public long getTime() {
		return lastTime;
	}
}
