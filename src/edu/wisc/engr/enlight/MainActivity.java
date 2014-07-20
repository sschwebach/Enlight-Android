package edu.wisc.engr.enlight;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
	TextView statusText;
	Spinner patternSpinner;
	Timer refreshTimer;
	ProgressBar reloadProgress;
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
		refreshTimer = new Timer();
		refreshTimer.schedule(new TimerTask() {
			@Override
			public void run(){
				TimerMethod();
			}
		}, 0, 2000);
		doSetup();
	}

	private void TimerMethod(){
		this.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				//add a controller. method to refresh shit
				controller.queryControl();
				if (!hasControl){
					controller.queryAllValves();
				}
			}
		});
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
		//Refresh the button checks
		//Let the user know if they have control or not
		Log.e("REFRESH", "REFRESH");


		//get and set the patterns
		if (patterns.size() == 0){
			patterns.add(new Pattern(0, "Manual", true));
		}
		PatternSpinnerAdapter<Pattern> adapter = new PatternSpinnerAdapter<Pattern>(this, android.R.layout.simple_spinner_item, patterns);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		patternSpinner.setAdapter(adapter);
	}

	private void doSetup(){
		statusText = (TextView) findViewById(R.id.text_control);
		pDialog = new ProgressDialog(this);
		controller = new FountainControlHandler(this);
		refreshTime = (TextView)findViewById(R.id.text_refresh);
		sendButton = (Button) findViewById(R.id.button_send);
		patternSpinner = (Spinner) findViewById(R.id.spinner_pattern);
		reloadProgress = (ProgressBar) findViewById(R.id.progress_reload);
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
					controller.releaseControl();
					Log.e("Valve", "" + sum);
					task.setValveRequest(sum);
					task.execute("Hello");
				}else if (!hasControl && !controlRequested){
					controller.requestControl();
				}

			}

		});
		
		patternSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				//do nothing I suppose
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