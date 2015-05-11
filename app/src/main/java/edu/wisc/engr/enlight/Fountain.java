package edu.wisc.engr.enlight;

import android.content.Context;
import android.os.PowerManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SAM-DESK on 2/14/2015.
 */
public class Fountain {
    FountainActivity mActivity;
    FountainControlHandler mHandler;
    StateController mState;
    boolean[] valveStates = new boolean[24];
    private boolean repeat = false;
    private final int REFRESHTIME = 1000;
    private Timer refreshTimer;
    private boolean isRunning = false;

    /**
     * Constructor for our Fountain object
     * @param a The activity that needs this fountain object. It binds the activity's callbacks to this.
     */
    public Fountain(FountainActivity a) {
        this.mActivity = a;
        this.mState = new StateController(this);
        this.mHandler = new FountainControlHandler(this);
        //TODO make the timer and the timer methods
    }

    /**
     * Start/resume the fountain. Enter the queue
     *
     * @param repeat if true the fountain will try to re-enter the queue when it leaves
     */
    public void start(boolean repeat) {
        this.repeat = repeat;
        this.refreshTimer = new Timer();
        this.refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, REFRESHTIME);
        isRunning = true;
    }

    /**
     * Stop the fountain, leave queue and set repeat to false
     */
    public void stop() {
        if (mState.getState() == StateController.HAS_CONTROL || mState.getState() == StateController.IN_QUEUE) {
            mHandler.releaseControl();
        }
        this.repeat = false;
        this.refreshTimer.cancel();
        isRunning = false;
    }

    /**
     * Request control from the server
     */
    public void requestControl(){
        if (mState.getState() == StateController.NO_REQUESTS) {
            mActivity.onLoadStarted(true);
            mHandler.requestControl();
        }
    }

    /**
     * Leave the queue
     */
    public void leaveQueue(){
        if (mState.getState() == StateController.IN_QUEUE) {
            mActivity.onLoadStarted(true);
            mHandler.releaseControl();
        }
    }

    /**
     * Release control that we currently have
     */
    public void releaseControl(){
        if (mState.getState() == StateController.HAS_CONTROL) {
            mActivity.onLoadStarted(true);
            mHandler.releaseControl();
        }
    }

    /**
     * Set a single valve on the fountain
     * @param id the id of the valve
     * @param on true set it to spraying, false sets it to off
     */
    public void setValveStates(int id, boolean on){
        mActivity.onLoadStarted(false);
        mHandler.setSingleValve(id, on);
    }

    /**
     * Set all the valves
     * @param bitmask The bitmask for all the valves (1 means spraying, 0 means off) msb = highest id
     */
    public void setAllValves(int bitmask){
        mActivity.onLoadStarted(false);
        mHandler.setAllValves(bitmask);
    }

    /**
     * Callback from the fountain control handler when we query who has control currently
     * @param reqControl
     * @param hasControl
     */
    public void controlChanged(boolean hasControl, boolean reqControl) {
        // See if the state has actually changed
        if (mState.tick(hasControl, reqControl)) {
            // See what state we just entered
            switch (mState.getState()) {
                case StateController.NO_STATE:
                    mActivity.onError();
                    break;
                case StateController.NO_REQUESTS:
                    mHandler.currID = 0;
                    mActivity.onNoRequest();
                    if (repeat) {
                        mHandler.requestControl();
                        mActivity.onLoadStarted(true);
                    }
                    break;
                case StateController.IN_QUEUE:
                    mActivity.onQueueEntered();
                    break;
                case StateController.HAS_CONTROL:
                    mActivity.onControlGained();
                    break;
                default:
                    mActivity.onError();
                    break;
            }
        }
    }

    /**
     * Callback from the fountain handler when we query the valve states
     * @param states
     */
    public void valvesChanged(boolean[] states) {
        this.valveStates = states;
        mActivity.onValvesChanged(states);
    }

    /**
     * Sets a single valve in our variable (internal use by the handler only)
     * @param id valve id
     * @param spraying true means on, false means off
     */
    public void setSingleValve(int id, boolean spraying) {
        this.valveStates[id] = spraying;
    }

    /**
     * See if a valve is currently on
     * @param id valve id
     * @return Returns true if the valve is on, false if it is off
     */
    public boolean isSpaying(int id) {
        return this.valveStates[id];
    }

    /**
     * Callback from the fountain handler when it has finished all operations in a series
     */
    public void lastOpFinished() {
        mActivity.onLastLoad();
    }

    /**
     * Callback from the fountain handler when a request doesn't succeed
     */
    public void badRequest() {
        mActivity.onError();
    }

    /**
     * Timer method. Sends the control/valve state requests to the fountain control handler
     */
    private void TimerMethod() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //add a controller. method to refresh shit
                PowerManager pm = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
                int currState = mState.getState();
                if (pm.isScreenOn() && isRunning) {
                    //TODO is this statement correct???
                    if (currState == StateController.IN_QUEUE || currState == StateController.HAS_CONTROL || mHandler.currID != 0) {
                        mHandler.queryPosition();
                        mActivity.onLoadStarted(false);
                    }

                    if (currState != StateController.HAS_CONTROL) {
                        mActivity.onLoadStarted(false);
                        mHandler.queryAllValves();
                    }
                }
            }
        });
    }

    /**
     * Get the current state of the fountain (not in queue, in queue, or has control)
     * @return Returns the current state of the fountain on this user's app (see Utilities for values)
     */
    public int getState(){
        return mState.getState();
    }

    /**
     * Set the "repeat" value of the fountain, which will automatically re-enter the queue if true.
     * @param r If set to true, the fountain will attempt to re-enter the queue when it leaves.
     */
    public void setRepeat(boolean r){
        this.repeat = r;
    }
}
