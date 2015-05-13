package edu.wisc.engr.enlight;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;


public class ControllerActivity extends FountainActivity {
    private int state; //Current state of the activity (see Utilities for values)
    boolean[] valveStates = new boolean[24];
    Button sendButton;
    TextView refreshTime;
    TextView statusText;
    Spinner patternSpinner;
    ProgressBar reloadProgress;
    FountainView mView;
    FountainViewCanvas leftView;
    FountainViewCanvas rightView;
    boolean dialogShowing = false;
    CircularTimer timerView;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setValve(int id, boolean pressed) {
        super.mFountain.setValveStates(id, pressed);
    }

    /**
     * This method is called to setup everything unique to the activity. Set up your buttons and
     * other widgets here
     */
    @Override
    public void doSetup() {
        Log.e("Setup", "Setup");
        setContentView(R.layout.activity_controller);
        // Instantiate our fountain view
        addCanvas();
        statusText = (TextView) findViewById(R.id.text_control);
        refreshTime = (TextView) findViewById(R.id.text_refresh);
        sendButton = (Button) findViewById(R.id.button_send);
        patternSpinner = (Spinner) findViewById(R.id.spinner_pattern);
        reloadProgress = (ProgressBar) findViewById(R.id.progress_reload);
        //timerView = new CircularTimer(this);

        // Fake empty container layout
        RelativeLayout lContainerLayout = (RelativeLayout) findViewById(R.id.layout_timerWrap);
        RelativeLayout.LayoutParams timerParams = new RelativeLayout.LayoutParams(500, 500);
        timerParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        timerParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        //lContainerLayout.addView(timerView, timerParams);
        //timerView.setTimeS(5);
        //timerView.setRadius(200);
        //timerView.start();
        //mView.unlock();


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFountain.getState() == Utilities.HAS_CONTROL) {
                    mFountain.releaseControl();
                } else if (mFountain.getState() == Utilities.IN_QUEUE) {
                    mFountain.releaseControl();
                } else if (mFountain.getState() == Utilities.NO_REQUESTS) {
                    mFountain.requestControl();
                }
            }
        });

    }

    /**
     * Callback for when the user enters the queue to control the fountain.
     */
    @Override
    public void onQueueEntered() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                patternSpinner.setVisibility(View.GONE);
                mView.lock();
                statusText.setText("Waiting for Control");
                refreshTime.setText("Another user may have control. Please wait.");
                sendButton.setText("Leave Queue");
            }
        });
    }

    /**
     * Callback for when the user gains control of the fountain
     */
    @Override
    public void onControlGained() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                patternSpinner.setVisibility(View.VISIBLE);
                mView.unlock();
                statusText.setText("You Have Control");
                refreshTime.setText("Tap a valve to activate it.");
                sendButton.setText("Release Control");
            }
        });

    }

    /**
     * Callback for when the user is no longer in control and is not in queue
     */
    @Override
    public void onNoRequest() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                patternSpinner.setVisibility(View.GONE);
                mView.lock();
                statusText.setText("Fountain Status");
                refreshTime.setText("Request control to gain access.");
                sendButton.setText("Request Control");
            }
        });

    }

    /**
     * Callback for when the valve states of the fountain changed.
     */
    @Override
    public void onValvesChanged(final boolean[] states) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //we want to update our fountain view
                mView.setValves(states);
            }
        });

    }

    /**
     * Callback for when we're making a webcall.
     *
     * @param hide If true, the webcall isn't being made from the timer method (might want to hide UI aspects)
     */
    @Override
    public void onLoadStarted(final boolean hide) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (hide) {
                    sendButton.setVisibility(View.GONE);
                    reloadProgress.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    /**
     * Callback for when the loading period had ended
     */
    @Override
    public void onLoadEnded() {
        onLastLoad();
    }

    /**
     * Callback for when the fountain class encounters an error
     */
    @Override
    public void onError(final Utilities.ERROR_STATE error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (error) {
                    case INTERNET:
                        // An internet connection error has occured, alert the user with a dialog
                        if (!dialogShowing) {
                            new AlertDialog.Builder(ControllerActivity.this)
                                    .setTitle("Connection error")
                                    .setMessage("Error connecting to server. Are you connected to the internet?")
                                    .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // delete or something?
                                            dialogShowing = false;
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                            dialogShowing = true;
                            break;
                        }
                }
            }
        });

    }

    /**
     * Callback for when the last load is done (might get rid of this for onLoadEnded)
     */
    @Override
    public void onLastLoad() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendButton.setVisibility(View.VISIBLE);
                reloadProgress.setVisibility(View.GONE);
            }
        });

    }

    /**
     * Magic voodoo code to make our fountain view less crappy to work with. Don't change the order of this please
     */
    private void addCanvas(){
        mView = new FountainView(this, this);
        leftView = new FountainViewCanvas(this, true, mView);
        rightView = new FountainViewCanvas(this, false, mView);
        mView.setViews(leftView, rightView);
        LinearLayout fountainCanvasLeft = (LinearLayout) findViewById(R.id.layout_canvas_left);
        LinearLayout fountainCanvasRight = (LinearLayout) findViewById(R.id.layout_canvas_right);
        fountainCanvasLeft.addView(leftView);
        fountainCanvasRight.addView(rightView);
    }


}
