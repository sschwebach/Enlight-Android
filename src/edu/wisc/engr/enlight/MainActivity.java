package edu.wisc.engr.enlight;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	FountainControlHandler controller;
	boolean hasControl = false;
	boolean controlRequested = false;
	boolean inQueue = false;
	boolean controlChanged = false;
	boolean[] valveStates = new boolean[24];
	ProgressDialog pDialog;
	Time lastRefresh;
	int bitmask;
	int userID;
	public UserQueue userQueue;
	public ArrayList<Pattern> patterns;
	Button sendButton;
	Button requestButton;
	TextView refreshTime;
	FountainViewCanvas leftFountain;
	FountainViewCanvas rightFountain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//TODO Load the last refresh from storage
		lastRefresh = new Time();
		patterns = new ArrayList<Pattern>();
		leftFountain = new FountainViewCanvas(this, true);
		rightFountain = new FountainViewCanvas(this, false);
		doSetup();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.clear();
		menu.add("Change API Key");
		return true;
	}

	/**
	 * Refreshes the layout to reflect the most recent data.
	 * This is often called after a server call is made to update the UI.
	 */
	public void refresh(){
		//TODO
		Toast toast = Toast.makeText(this, "Refresh", Toast.LENGTH_LONG);
		toast.show();
		LinearLayout buttonArea = (LinearLayout) findViewById(R.id.button_layout);
		//Refresh the button checks
//		for (int i = 0; i < 22; i++){
//			((Button) buttonArea.getChildAt(i)).setPressed(valveStates[i]);
//		}
		//Let the user know if they have control or not
		if (controlChanged && !hasControl){ //if they lost control since last refresh
			controlChanged = false;
			Toast controlToast = Toast.makeText(this, "You no longer have control", Toast.LENGTH_SHORT);
			controlToast.show();
			sendButton.setVisibility(View.GONE);
			requestButton.setText("Request Control");
		}
		if (hasControl){
			//Show the send button if it isn't already shown
			sendButton.setVisibility(View.VISIBLE);
			requestButton.setText("Release Control");
		}
		lastRefresh.setToNow();
		String currTime = lastRefresh.format("Last full refresh on %B %d, %Y at %I:%M %p");
		refreshTime.setText(currTime);
	}

	private void doSetup(){
		pDialog = new ProgressDialog(this);
		controller = new FountainControlHandler(this);
		final LinearLayout buttonArea = (LinearLayout) findViewById(R.id.button_layout);
		refreshTime = (TextView)findViewById(R.id.refresh_last);
		sendButton = (Button) findViewById(R.id.sendButton);
		//Draw the fountain images
		LinearLayout fountainCanvasLeft = (LinearLayout) findViewById(R.id.canvas_left);
		LinearLayout fountainCanvasRight = (LinearLayout) findViewById(R.id.canvas_right);
		fountainCanvasLeft.addView(leftFountain);
		fountainCanvasRight.addView(rightFountain);
		//Add the valve control buttons
		for (int i = 1; i <= 24; i++){
			ToggleButton newButton = new ToggleButton(this);
			newButton.setText("" + i);
			newButton.setTextOn("" + i);
			newButton.setTextOff("" + i);
			newButton.setTextSize(10);
			newButton.setPadding(0,  0, 10, 0);
			newButton.setTextColor(0xFFFFFFFF);
			buttonArea.addView(newButton);

		}
		//Add the submit button
		Button resetButton = (Button) findViewById(R.id.refresh_button);
		resetButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				ContactFountainTask task = new ContactFountainTask();
				int sum = 0;
				for (int i = 0; i < 22; i++){
					if (((ToggleButton) buttonArea.getChildAt(i)).isChecked() && i != 10 && i != 11){ //don't want the big valves
						sum = sum + (int) Math.pow(2, i);
					}
				}
				if (hasControl){
					controller.setAllValves(sum);
				}
				task.setValveRequest(sum);
				task.execute("Hello");
				Log.e("Button", "Button pressed!");
			}
		});

		requestButton = (Button) findViewById(R.id.reqControlButton);
		requestButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if (!hasControl){
					controller.requestControl();
				}else{
					controller.releaseControl();
				}
			}

		});
		//TODO once everything is set up, refresh the layout
		refresh();
	}
	
	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}
	
}