package net.cognitics.navapp;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class CustomGraphics extends View
{


    float bearing=-1;
    float pitch;
    public Context context;

    private ArrayList<NavPoint> navList=new ArrayList<NavPoint>();

    {
    }

    public CustomGraphics(Context con)
    {
        super(con);
        this.context=con;
    }

    public CustomGraphics(Context con, AttributeSet set)
    {
        super(con, set);
    }

    public CustomGraphics(Context con, AttributeSet set, int style)
    {
        super(con, set, style);
    }

    @Override
    protected void onDraw(Canvas c)
    {
    }


    public void updatePositions(){
        int width = getWidth();
        int height = getHeight();
       // int x= width/2;
       // int y=height/2;
             for (NavPoint n : navList){
                 int tX = 0;
                 int tY = 0;
                 if (Math.abs(n.getBearing()-bearing)>180){
                     tX += 12 * ((n.getBearing()) - (bearing-360));
                 }else {
                     tX += 12 * ((n.getBearing()) - (bearing));
                 }
                 tY-=16*(n.getPitch()-(pitch));
                 n.setX((float)tX);
                 n.setY((float)tY);
             }

    }




    public void setViewModel(float bearing, float pitch){
        this.bearing=bearing;
        this.pitch=pitch;
    }

    public void addPoint(FrameLayout camera, float bearing, float pitch){
        NavPoint newPoint = new NavPoint(context,bearing,pitch);
        newPoint.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_action_name));

        newPoint.setOnClickListener(new AppCompatImageButton.OnClickListener() {
                                        public void onClick(View v) {
                                            Toast.makeText(context, "Hello World", Toast.LENGTH_LONG).show();
                                        }
                                    });

        navList.add(newPoint);
        camera.addView(newPoint);
    }
    public void clearPoints(){
        navList.clear();
    }


}