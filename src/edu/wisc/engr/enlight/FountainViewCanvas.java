package edu.wisc.engr.enlight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class FountainViewCanvas extends View{
	Paint paint = new Paint();
	Paint paintCyan = new Paint();
	Paint paintBlack = new Paint();
	int badgerGold = 0xffe7d9c1;
	int badgerRed = 0xffb70101;
	int cream = 0xFF6E6A5B;
	int darkGrey = 0xFFC8C5BB;
	Canvas mCanvas;
	int height;
	int width;
	int length;
	float centerX;
	float centerY;
	boolean setup = false;
	float radiusOuter;
	float radiusInner;
	boolean left;
	RectF rectOuter;
	RectF rectInner;
	public boolean[] buttonPressed;

	public FountainViewCanvas(Context context, boolean left){
		super(context);
		this.left = left;
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(4.5f);
		paintCyan.setAntiAlias(true);
		paintCyan.setColor(badgerRed);

		paintCyan.setStyle(Paint.Style.FILL);
		//TODO set the color to the same one as the xml layout
		paintBlack.setAntiAlias(true);
		paintBlack.setColor(Color.WHITE);
		paintBlack.setStyle(Paint.Style.FILL);
		buttonPressed = new boolean[12];


	}

	@Override
	public void onDraw(Canvas canvas){
		if (!setup){
			mCanvas = canvas;
			height = getHeight();
			width = getWidth();
			if (left){
				centerX = (float) (width * .85);
			}else{
				centerX = (float) (width * .15);
			}
			centerY = height/2;
			if (left){
				if (centerY < centerX){
					radiusOuter = centerY;
				}else{
					radiusOuter = centerX;
				}
			}else{
				if (centerY < (width - centerX)){
					radiusOuter = centerY;
				}else{
					radiusOuter = width - centerX;
				}
			}
			radiusInner = radiusOuter*2/6;
			rectOuter = new RectF();
			rectInner = new RectF();
			rectOuter.set(centerX - radiusOuter, centerY - radiusOuter, centerX + radiusOuter, centerY + radiusOuter);
			rectInner.set(centerX - radiusInner, centerY - radiusInner, centerX + radiusInner, centerY + radiusInner);
			setup = true;
		}
		//draw the circles
		if (left){
			canvas.drawArc(rectOuter, 90, 180, false, paint);
			canvas.drawArc(rectInner, 90, 180, false, paint);

		}else{
			canvas.drawArc(rectOuter, -90, 180, false, paint);
			canvas.drawArc(rectInner, -90, 180, false, paint);

		}

		//draw any views that are filled in
		for (int i = 1; i <= 12; i++){
			if (left){
				//compute the starting and ending angles for the segment we're checking
				float angleStart = (float) (Math.PI/2 + (Math.PI/12) * (i-1));
				float angleEnd = (float) (Math.PI/2 + (Math.PI/12) * (i));
				RectF fill = new RectF();
				fill.set(centerX - radiusOuter, centerY - radiusOuter, centerX + radiusOuter, centerY + radiusOuter);
				if (buttonPressed[i-1]){
					paintCyan.setColor(badgerRed);
				}else{
					paintCyan.setColor(darkGrey);
				}
				canvas.drawArc(fill, (float) (angleStart*180/Math.PI), (float) ((angleEnd - angleStart)*180/Math.PI), true, paintCyan);


			}else{
				//compute the starting and ending angles for the segment we're checking
				float angleStart = (float) (Math.PI/2 - (Math.PI/12) * (i-1));
				float angleEnd = (float) (Math.PI/2 - (Math.PI/12) * (i));
				RectF fill = new RectF();
				fill.set(centerX - radiusOuter, centerY - radiusOuter, centerX + radiusOuter, centerY + radiusOuter);
				if (buttonPressed[i-1]){
					paintCyan.setColor(badgerRed);
				}else{
					paintCyan.setColor(darkGrey);
				}
				canvas.drawArc(fill, (float) (angleStart*180/Math.PI), (float) ((angleEnd - angleStart)*180/Math.PI), true, paintCyan);

			}
		}

		//draw the rest of the outline
		canvas.drawLine(centerX, centerY - radiusOuter, centerX, centerY - radiusInner, paint);
		canvas.drawLine(centerX, centerY + radiusInner, centerX, centerY + radiusOuter, paint);
		//Draw the division lines TIME FOR MATH
		for (int i = 1; i < 12; i++){
			float angle;
			float startX;
			float startY;
			float endX; 
			float endY;
			//calculate the angle
			if (left){
				angle = (float) (Math.PI/2 + (Math.PI/12) * i);
				startX = (float) (centerX + Math.cos(angle)*radiusInner);
				startY = (float) (centerY + Math.sin(angle) * radiusInner);
				endX = (float) (centerX + Math.cos(angle)*radiusOuter);
				endY = (float) (centerY + Math.sin(angle) * radiusOuter);
				canvas.drawLine(startX, startY, endX, endY, paint);
			}else{
				angle = (float) (Math.PI/2 - (Math.PI/12) * i);
				startX = (float) (centerX + Math.cos(angle)*radiusInner);
				startY = (float) (centerY + Math.sin(angle) * radiusInner);
				endX = (float) (centerX + Math.cos(angle)*radiusOuter);
				endY = (float) (centerY + Math.sin(angle) * radiusOuter);
				canvas.drawLine(startX, startY, endX, endY, paint);
			}

		}
		//Lastly, draw an arc in the center to get rid of a few things...
		if (left){
			canvas.drawArc(rectInner, 89, 182, true, paintBlack);

		}else{
			canvas.drawArc(rectInner, -91, 182, true, paintBlack);
		}



	}

	public int detectButton(float x, float y){
		int buttonNum = 0;
		//compute the distance from the center
		float rad = (float) Math.sqrt(Math.pow((y-centerY), 2) + Math.pow((x-centerX), 2));
		float angle = (float) (Math.atan((y - centerY)/(x - centerX)));
		if (left){
			angle += Math.PI;
		}
		if (left && x > centerX){
			return 0;
		}
		if (!left && x < centerX){
			return 0;
		}
		Log.e("ANGLE", "" + angle);
		for (int i = 1; i <= 12; i++){
			//TODO make sure we're not getting the angle 180 degrees from what we think we're getting
			if (left){
				//compute the starting and ending angles for the segment we're checking
				float angleStart = (float) (Math.PI/2 + (Math.PI/12) * (i-1));
				float angleEnd = (float) (Math.PI/2 + (Math.PI/12) * (i));
				Log.e("ANGLE RANGE", "From " + angleStart + " to " +angleEnd);

				if (angle > angleStart && angle < angleEnd){
					if (rad > radiusInner && rad < radiusOuter){
						buttonPressed[i-1] = !buttonPressed[i-1];
						return i; //this makes it easier to line up the number with the actual valve number
					}
				}
			}else{
				//compute the starting and ending angles for the segment we're checking
				float angleStart = (float) (Math.PI/2 - (Math.PI/12) * (i-1));
				float angleEnd = (float) (Math.PI/2 - (Math.PI/12) * (i));
				Log.e("ANGLE RANGE", "From " + angleStart + " to " +angleEnd);

				if (angle < angleStart && angle > angleEnd){
					if (rad > radiusInner && rad < radiusOuter){
						buttonPressed[i-1] = !buttonPressed[i-1];
						return i;
					}
				}
			}
		}
		//first detect if the button press angle is in the current segment
		//if the button press is within the segment, check the radius
		return buttonNum;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e){
		if ((e.getAction() & MotionEvent.ACTION_DOWN) == MotionEvent.ACTION_DOWN){
			float xLoc = e.getX();
			float yLoc = e.getY();
			int buttonNum = detectButton(xLoc, yLoc);
			if (buttonNum > 0){
				Log.e("BUTTON", "" + buttonNum);
				this.invalidate();
			}
		}
		return super.onTouchEvent(e);

	}

}
