package net.cognitics.navapp;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatImageButton;
import android.widget.TextView;

/**
 * Created by Chris on 3/16/2018.
 */

public class NavPoint extends AppCompatImageButton{
    private float bearing,pitch;
    private int distance=1;
    private TextView textView;

    public NavPoint(Context context) {
        super(context);
    }
    public NavPoint(Context context, float bearing, float pitch) {
        super(context);
        this.bearing=bearing;
        this.pitch=pitch;
        //setScaleX(.1f);
        //setScaleY(.1f);
        textView = new TextView(context);
        setMinimumHeight(50);
        setMinimumWidth(50);
        textView.setTextColor(Color.parseColor("#e500ff"));

    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }


    public float getBearing(){
        return bearing;
    }
    public void setDistance(int distance){
        if (distance>0) {
            this.distance = distance;
        }
    }
    public int getDistance(){
        return distance;
    }
    public float getPitch(){
        return pitch;
    }
    public TextView getText(){return textView;};


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        int measuredHeight = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        // Ensure this view is always square.
        int min = Math.min(measuredHeight, measuredWidth);
        setMeasuredDimension(min/10, min/10);
    }








}
