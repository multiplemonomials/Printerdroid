package com.piedpiper.barchart;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.View;
public abstract class ChartView extends View{

    Activity activity;

    //Paint to draw things
    protected Paint paint;
    protected Canvas canvas;

    //Display size
    protected int display_height;
    protected int display_width;

    //Offset values
    protected float offset_top;
    protected float offset_bottom;
    protected float offset_left;
    protected float offset_right;

    //New calculated boundaries 
    protected float xtl;
    protected float ytl;
    protected float xtr;
    protected float ytr;
    protected float xbl;
    protected float ybl;
    protected float xbr;
    protected float ybr;

    //Chart size
    protected float chart_height;
    protected float chart_width;

    public ChartView(Activity activity) {
        super(activity);

        this.activity = activity;

        //First determining height of the display
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        display_height = displaymetrics.heightPixels;
        display_width = displaymetrics.widthPixels;

        //Now calculating Offset values to set padding from layout boundaries
        offset_top = display_height * 0.08f;
        offset_bottom = display_height * 0.1f;
        offset_left = display_width * 0.1f;
        offset_right = display_width * 0.08f;

        chart_width = display_width - offset_right - offset_left;
        chart_height = display_height - offset_top - offset_bottom;

        xtl = offset_left;
        ytl = offset_top;
        xtr = display_width-offset_right;
        ytr = offset_top;
        xbl = offset_left;
        ybl = display_height-offset_bottom;
        xbr = display_width-offset_right;
        ybr = display_height-offset_bottom;

        //Initialize Paint object
        paint = new Paint();
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        paint.setDither(true);

    }
}