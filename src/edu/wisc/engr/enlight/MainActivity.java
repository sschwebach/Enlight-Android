package edu.wisc.engr.enlight;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
	ImageButton refreshButton;
	Button sendButton;
	ImageButton abdicateButton;
	TextView refreshTime;
	FountainViewCanvas leftFountain;
	FountainViewCanvas rightFountain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_new);
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
		//Refresh the button checks
		//Let the user know if they have control or not
		if (controlChanged && !hasControl){ //if they lost control since last refresh
			controlChanged = false;
			Toast controlToast = Toast.makeText(this, "You no longer have control", Toast.LENGTH_SHORT);
			controlToast.show();
			sendButton.setVisibility(View.GONE);
			sendButton.setText("Request Control");
		}
		if (hasControl){
			//Show the send button if it isn't already shown
			sendButton.setVisibility(View.VISIBLE);
			sendButton.setText("Release Control");
		}
		lastRefresh.setToNow();
		String currTime = lastRefresh.format("Last full refresh on %B %d, %Y at %I:%M %p");
		refreshTime.setText(currTime);
	}

	private void doSetup(){

		pDialog = new ProgressDialog(this);
		controller = new FountainControlHandler(this);
		refreshTime = (TextView)findViewById(R.id.text_refresh);
		sendButton = (Button) findViewById(R.id.button_send);
		//Draw the fountain images
		LinearLayout fountainCanvasLeft = (LinearLayout) findViewById(R.id.layout_canvas_left);
		LinearLayout fountainCanvasRight = (LinearLayout) findViewById(R.id.layout_canvas_right);
		fountainCanvasLeft.addView(leftFountain);
		fountainCanvasRight.addView(rightFountain);

		//Add the submit button
		Button resetButton = (Button) findViewById(R.id.refresh_button);
		sendButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if (hasControl){
					ContactFountainTask task = new ContactFountainTask();
					int sum = 0;
					for (int i = 0; i < 12; i++){
						//start with the left valve
						//both valves are back asswards right now
						if (leftFountain.buttonPressed[i]){
							int valveNum = 11 - i;
							Log.e("Valvenum", "" + valveNum);
							sum = sum + (int) Math.pow(2, valveNum);
						}if (rightFountain.buttonPressed[i]){
							int valveNum = 23 - i;
							Log.e("Valvenum", "" + valveNum);
							sum = sum + (int) Math.pow(2, valveNum);
						}
					}
					controller.setAllValves(sum);
					Log.e("Valve", "" + sum);
					task.setValveRequest(sum);
					task.execute("Hello");
				}else{
					controller.requestControl();
				}

				Log.e("Button", "Button pressed!");
			}

		});

		sendButton.setOnClickListener(new OnClickListener(){

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