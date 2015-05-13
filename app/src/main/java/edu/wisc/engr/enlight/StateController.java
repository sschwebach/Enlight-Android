package edu.wisc.engr.enlight;

import android.app.Activity;
import android.util.Log;

/**
 * Created by SAM-DESK on 1/24/2015.
 */
public class StateController {
    // I can't use Android studio's refactoring tool well, so we're doing this for now (the laze is real)
    public static final int NO_STATE = Utilities.NO_STATE;
    public static final int NO_REQUESTS = Utilities.NO_REQUESTS;
    public static final int IN_QUEUE = Utilities.IN_QUEUE;
    public static final int HAS_CONTROL = Utilities.HAS_CONTROL;

    public int state = NO_REQUESTS;
    //a pending request (NO_STATE) means there is no pending request
    public int pending = NO_STATE;
    //a boolean to wait for the UI to update
    public boolean wait = false;
    //a boolean to indicate a web call is going on
    public boolean busy = false;
    private Fountain mFountain;
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

    public StateController(Fountain f){
        this.mFountain = f;
    }


    /**
     * Checks if the state should be changed.
     * @return Returns the new state of the program.
     */
    public boolean tick(boolean hasControl, boolean reqControl){
        int oldState = state;
        // If we need to pause the state controller for some reason
        if (!wait) {
            switch (oldState) {
                //currently each case is the same, but I'll code them all out in case that changes
                case NO_REQUESTS:
                    //see if we should change to one of the other states
                    if (hasControl) {
                        state = HAS_CONTROL;
                    } else if (reqControl && !hasControl) {
                        state = IN_QUEUE;
                    } else {
                        state = NO_REQUESTS;
                    }
                    break;
                case IN_QUEUE:
                    if (hasControl) {
                        state = HAS_CONTROL;
                    } else if (reqControl && !hasControl) {
                        state = IN_QUEUE;
                    } else {
                        state = NO_REQUESTS;
                    }
                    break;
                case HAS_CONTROL:
                    if (hasControl) {
                        state = HAS_CONTROL;
                    } else if (reqControl && !hasControl) {
                        //not sure how this can happen in the app's current state
                        state = IN_QUEUE;
                    } else {
                        state = NO_REQUESTS;
                    }
                    break;
                default:
                    //error state? return to the base state until things change
                    state = NO_REQUESTS;
                    reqControl = false;
                    hasControl = false;
                    break;
            }
            //first see if the state even changes
            if (state != oldState){
                //if the state has changed, actually do something
                //Log.e("State", "State changed!");
                return true;
            }
            //even if the state hasn't changed, we want to put the button back I suppose

        }
        return false;
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

    /**
     * Get the current state of the fountain.
     * @return Returns the state according to the values in Utilities
     */
    public int getState(){
        return this.state;
    }
}
