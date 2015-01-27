package edu.wisc.engr.enlight;

import android.app.Activity;

/**
 * Created by SAM-DESK on 1/24/2015.
 */
public class StateController {

    public static final int NO_STATE = -1;
    public static final int NO_REQUESTS = 0;
    public static final int IN_QUEUE = 1;
    public static final int HAS_CONTROL = 2;

    public int state = NO_REQUESTS;
    public boolean controlRequested = false;
    public boolean hasControl = false;
    //a pending request (NO_STATE) means there is no pending request
    public int pending = NO_STATE;
    //a boolean to wait for the UI to update
    public boolean wait = false;
    //a boolean to indicate a web call is going on
    public boolean busy = false;
    private MainActivity mActivity;
    //TODO add support for pending requests that couldn't be serviced due to a web call

    /*
    The goal of this class is to make an FSM-like design for the different states of the app
    Currently, the delay in network calls can make it seem incorrect.
    Goals:
    Make UI changes only happen when the state has changed, which is checked every time the timer
    goes off
    The inputs to the states (has control, control requested) will be updated once the web calls return
    Hopefully this means there will be no race conditions (we might need a transaction variable to be set)

    When the user presses a button to request control, the button won't be visible until the state has
    been confirmed (even if this is after the next tick)

    The idea here is that the state will be blind to what the user does, only to what the server returns

    What needs to be done:
    All changes to the UI should be moved here to some extent
    StateController changes in the fountain handler should come here by setting the two booleans
    Some sort of tick function will be defined here to actually change the state
     */

    /**
     * Binds the state controller to the activity (wow my code has a horrible layout)
     * @param a
     */
    public void bind(MainActivity a){
        mActivity = a;
    }

    /**
     * Checks if the state should be changed.
     * @return Returns the new state of the program.
     */
    public int tick(){
        int oldState = state;
        if (!wait) {
            switch (oldState) {
                //currently each case is the same, but I'll code them all out in case that changes
                case NO_REQUESTS:
                    //see if we should change to one of the other states
                    if (hasControl) {
                        state = HAS_CONTROL;
                    } else if (controlRequested && !hasControl) {
                        state = IN_QUEUE;
                    } else {
                        state = NO_REQUESTS;
                    }
                    break;
                case IN_QUEUE:
                    if (hasControl) {
                        state = HAS_CONTROL;
                    } else if (controlRequested && !hasControl) {
                        state = IN_QUEUE;
                    } else {
                        state = NO_REQUESTS;
                    }
                    break;
                case HAS_CONTROL:
                    if (hasControl) {
                        state = HAS_CONTROL;
                    } else if (controlRequested && !hasControl) {
                        //not sure how this can happen in the app's current state
                        state = IN_QUEUE;
                    } else {
                        state = NO_REQUESTS;
                    }
                    break;
                default:
                    //error state? return to the base state until things change
                    state = NO_REQUESTS;
                    controlRequested = false;
                    hasControl = false;
                    break;
            }
            //now that we've updated the state, alert the UI that it's been updated
            //TODO update the UI here
            //first see if the state even changes
            if (state != oldState){
                //if the state has changed, actually do something
            }
            //even if the state hasn't changed, we want to put the button back I suppose

        }
        return state;
    }


    /**
     * Pause the state machine (this will also help with transactions)
     */
    public void pause(){
        wait = true;
    }

    /**
     * Resume the state machine (or to signal that a transaction is complete)
     */
    public void resume(){
        wait = false;
    }

    /**
     * Resets the state machine to NO_REQUESTS
     */
    public void reset(){
        wait = false;
        state = NO_REQUESTS;
    }
}
