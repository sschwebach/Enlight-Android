package edu.wisc.engr.enlight;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

/**
 * Created by SAM-DESK on 2/14/2015.
 */
public abstract class FountainActivity extends Activity{
    //methods that the extended classes need to define
    protected Fountain mFountain;

    /**
     * This method is called to setup everything unique to the activity
     */
    public abstract void doSetup();

    /**
     * Callback for when the user enters the queue to control the fountain.
     */
    public abstract void onQueueEntered();

    /**
     * Callback for when the user gains control of the fountain
     */
    public abstract void onControlGained();

    /**
     * Callback for when the user is no longer in control and is not in queue
     */
    public abstract void onNoRequest();

    /**
     * Callback for when the valve states of the fountain changed.
     */
    public abstract void onValvesChanged(boolean[] states);

    /**
     * Callback for when we're making a webcall.
     * @param hide If true, the webcall isn't being made from the timer method (might want to hide UI aspects)
     */
    public abstract void onLoadStarted(boolean hide);

    /**
     * Callback for when the loading period had ended
     */
    public abstract void onLoadEnded();

    /**
     * Callback for when the fountain class encounters an error
     */
    public abstract void onError(Utilities.ERROR_STATE error);

    /**
     * Callback for when the last load is done (might get rid of this for onLoadEnded)
     */
    public abstract void onLastLoad();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        doSetup();
        mFountain = new Fountain(this);
        mFountain.start(false);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mFountain.stop();
    }

    @Override
    protected  void onResume(){
        super.onResume();
        mFountain.start(false);
    }

}
