package edu.wisc.engr.enlight;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SAM-DESK on 3/15/2015.
 */
public class CircularTimer extends View {
    private Context mContext;
    private float radius = 30.f; //defaults to 30 pixels
    private int fillColor = 0xFFFF0000; //defaults to red
    private int borderColor = 0xFF000000; //defaults to black
    private int maxTimeMS = 1000; //defaults to 1 second
    private int remainingMS = 1000;
    private float percentRemaining = 1.f;
    private boolean showText;
    private boolean showBorder;
    private boolean defaultTextColor;
    private int height;
    private int width;
    private float centerX;
    private float centerY;
    private Paint paintBorder;
    private Paint paintFill;
    private Timer frameTimer;
    private TextView timeText;
    private RelativeLayout textLayout;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public CircularTimer(Context context) {
        super(context);
        this.mContext = context;
        paintBorder = new Paint();
        paintFill = new Paint();
        paintBorder.setAntiAlias(true);
        paintFill.setAntiAlias(true);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintFill.setStyle(Paint.Style.FILL);
        paintBorder.setStrokeWidth(4.f);
        paintFill.setColor(fillColor);
        paintBorder.setColor(borderColor);

        this.frameTimer = new Timer();
        this.timeText = new TextView(context);
        this.timeText.setVisibility(View.VISIBLE);
        this.timeText.setTextColor(0xFF000000);
        this.timeText.setTextSize(30.f);
        this.timeText.setText("Hello world!");


        textLayout = new RelativeLayout(context);


        textLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDraw(Canvas canvas) {
        height = getHeight();
        width = getWidth();
        centerX = width / 2.f;
        centerY = height / 2.f;
        RectF fill = new RectF();
        fill.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(fill, -90.f + (1.f - percentRemaining) * 360.f, 360.f * percentRemaining, true, paintFill);


        textLayout.measure(canvas.getWidth(), canvas.getHeight());
        textLayout.layout(0, 0, canvas.getWidth(), canvas.getHeight());

        if (timeText.getParent() == null) {
            textLayout.addView(timeText);
        }
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        this.timeText.setLayoutParams(textParams);
        textLayout.draw(canvas);

    }

    private void timerMethod() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                remainingMS = remainingMS - 1000 / 60;
                percentRemaining = (float) remainingMS / maxTimeMS;
                if (percentRemaining <= 0.f) {
                    percentRemaining = 1.f;
                    remainingMS = maxTimeMS;
                }
                timeText.setText("" + (int) remainingMS / 1000);
                invalidate();
            }
        });
    }

    public void setRadius(float px) {
        this.radius = px;
    }

    public void setRadiusDP(int dp) {
        setRadius(Utilities.convertDpToPixel(dp, mContext));
    }

    public void setFillColor(int color) {
        this.fillColor = color;
        paintFill.setColor(color);
    }

    public void setBorderColor(int color) {
        this.borderColor = color;
        paintBorder.setColor(color);
    }

    public void showBorder(boolean show) {
        this.showBorder = show;
    }

    public void showText(boolean show) {
        this.showText = show;
    }

    public void setTimeMS(int milliseconds) {
        this.maxTimeMS = milliseconds;
        this.remainingMS = this.maxTimeMS;
        this.timeText.setText("" + (int) this.remainingMS / 1000);
    }

    public void setTimeS(int seconds) {
        setTimeMS(seconds * 1000);
    }

    public void start() {
        if (frameTimer == null) {
            frameTimer = new Timer();
        }
        this.frameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerMethod();
            }
        }, 0, 1000 / 60);
    }

    public void reset() {
        this.remainingMS = this.maxTimeMS;
        this.percentRemaining = 1.f;
    }

    public void pause() {
        this.frameTimer.cancel();
        this.frameTimer = null;
    }

    public void resume() {
        if (this.frameTimer == null) {
            //recreate the timer
            this.frameTimer = new Timer();
            this.frameTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timerMethod();
                }
            }, 0, 1000 / 60);
        }
    }
}
