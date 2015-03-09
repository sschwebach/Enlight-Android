package edu.wisc.enlight.enlight_wear;

import android.content.Context;
import android.widget.RelativeLayout;


/**
 * Created by SAM-DESK on 2/17/2015.
 */
public class FountainViewWear {
    FountainViewCanvasWear leftView;
    FountainViewCanvasWear rightView;
    RelativeLayout container;
    private Context mContext;
    ControllerActivityWear mActivity;
    boolean[] states = new boolean[24];
    static int id = 1;

    public FountainViewWear(Context context, ControllerActivityWear a) {
        this.mContext = context;
        this.mActivity = a;
        // finally insert our views

    }

    public void setViews(FountainViewCanvasWear left, FountainViewCanvasWear right) {
        leftView = left;
        rightView = right;
    }

    /**
     * Callback from the underlying views when a valve is pressed
     *
     * @param valve   the id of the valve
     * @param pressed true means it was turned on
     */
    public void onValvePressed(int valve, boolean pressed) {
        states[valve - 1] = pressed;
        //mActivity.setValve(valve - 1, pressed);
    }

    /**
     * Set the valves in the drawing to the states from the server
     *
     * @param states Array of states that represent the valves
     */
    public void setValves(boolean[] states) {
        this.states = states;
        leftView.setValves(states);
        rightView.setValves(states);
    }

    public void lock() {
        leftView.lock();
        rightView.lock();
    }

    public void unlock() {
        leftView.unlock();
        rightView.unlock();
    }

}


