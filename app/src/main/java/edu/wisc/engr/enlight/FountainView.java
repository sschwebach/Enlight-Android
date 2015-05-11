package edu.wisc.engr.enlight;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by SAM-DESK on 2/14/2015.
 */
public class FountainView{
    FountainViewCanvas leftView;
    FountainViewCanvas rightView;
    RelativeLayout container;
    private Context mContext;
    ControllerActivity mActivity;
    boolean[] states = new boolean[24];
    static int id = 1;

    public FountainView(Context context, ControllerActivity a) {
        this.mContext = context;
        this.mActivity = a;
        // finally insert our views

    }

    public void setViews(FountainViewCanvas left, FountainViewCanvas right){
        leftView = left;
        rightView = right;
    }

    /**
     * Callback from the underlying views when a valve is pressed
     * @param valve the id of the valve
     * @param pressed true means it was turned on
     */
    public void onValvePressed(int valve, boolean pressed){
        states[valve - 1] = pressed;
        mActivity.setValve(valve, pressed);
    }

    /**
     * Set the valves in the drawing to the states from the server
     * @param states Array of states that represent the valves
     */
    public void setValves(boolean[] states){
        this.states = states;
        leftView.setValves(states);
        rightView.setValves(states);
    }

    public void lock(){
        leftView.lock();
        rightView.lock();
    }

    public void unlock(){
        leftView.unlock();
        rightView.unlock();
    }

}
