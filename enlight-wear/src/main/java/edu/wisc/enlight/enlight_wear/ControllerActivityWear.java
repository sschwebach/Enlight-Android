package edu.wisc.enlight.enlight_wear;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.LinearLayout;


public class ControllerActivityWear extends Activity {

    FountainViewWear mView;
    FountainViewCanvasWear leftView;
    FountainViewCanvasWear rightView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                doSetup(stub);
            }
        });
    }

    public void doSetup(WatchViewStub stub){
        addCanvas(stub);
    }

    /**
     * Magic voodoo code to make our fountain view less crappy to work with. Don't change the order of this please
     */
    private void addCanvas(WatchViewStub stub){
        mView = new FountainViewWear(this, this);
        leftView = new FountainViewCanvasWear(this, true, mView);
        rightView = new FountainViewCanvasWear(this, false, mView);
        mView.setViews(leftView, rightView);
        LinearLayout fountainCanvasLeft = (LinearLayout) stub.findViewById(R.id.layout_canvas_left);
        LinearLayout fountainCanvasRight = (LinearLayout) stub.findViewById(R.id.layout_canvas_right);
        fountainCanvasLeft.addView(leftView);
        fountainCanvasRight.addView(rightView);
    }
}
