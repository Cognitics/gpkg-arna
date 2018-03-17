package net.cognitics.navapp;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;

/**
 * Created by Chris on 3/16/2018.
 */

public class NavPoint extends AppCompatImageButton{
    private float bearing,pitch;

    public NavPoint(Context context) {
        super(context);
    }
    public NavPoint(Context context, float bearing, float pitch) {
        super(context);
        this.bearing=bearing;
        this.pitch=pitch;
        setScaleX(.1f);
        setScaleY(.1f);
    }

    public float getBearing(){
        return bearing;
    }
    public float getPitch(){
        return pitch;
    }
}
