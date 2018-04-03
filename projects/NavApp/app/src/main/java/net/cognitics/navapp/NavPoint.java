package net.cognitics.navapp;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;

/**
 * Created by Chris on 3/16/2018.
 */

public class NavPoint extends AppCompatImageButton{
    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    private float bearing,pitch;

    public NavPoint(Context context) {
        super(context);
    }
    public NavPoint(Context context, float bearing, float pitch) {
        super(context);
        this.bearing=bearing;
        this.pitch=pitch;
        //setScaleX(.1f);
        //setScaleY(.1f);
        setMinimumHeight(50);
        setMinimumWidth(50);

    }

    public float getBearing(){
        return bearing;
    }
    public float getPitch(){
        return pitch;
    }


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
