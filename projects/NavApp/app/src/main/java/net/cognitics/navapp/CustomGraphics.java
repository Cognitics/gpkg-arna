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
    //public int height,width;
    final Paint circlePaint;
    float bearing=-1;
    float pitch;
    public Context context;

    final float TEST_BEARING=180f;
    final float TEST_PITCH=90f;

    private ArrayList<NavPoint> navList=new ArrayList<NavPoint>();
    //private ArrayList<Buttont> navList=new ArrayList<NavPoint>();
    {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(100);
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.FILL);
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
    {/*
        super.onDraw(c);
        int width = getWidth();
        int height = getHeight();
        int radius;
        radius = 80;
        int x= width/2;
        int y=height/2;
        if (bearing!=-1) {
            /*int x2=x;
            x+=3.5*(TEST_BEARING-(bearing));
            x2+=12*(TEST_BEARING-(bearing));
            int y2=y;
            y-=8*(TEST_PITCH-(pitch));
            y2-=16*(TEST_PITCH-(pitch));
            circlePaint.setColor(Color.parseColor("#CD5C5C"));
            c.drawCircle(x, y, radius, circlePaint);
            circlePaint.setColor(Color.parseColor("#0000ff"));
            c.drawCircle(x2, y2, radius/2, circlePaint);

            for (NavPoint n: navList){
                int tX = x;
                int tY = y;
                if (Math.abs(n.getBearing()-bearing)>180){
                    tX += 12 * ((n.getBearing()) - (bearing-360));
                }else {
                    tX += 12 * ((n.getBearing()) - (bearing));
                }
                tY-=16*(n.getPitch()-(pitch));
                Random rnd = new Random();
                circlePaint.setColor(Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
                c.drawCircle(tX, tY, radius, circlePaint);
            }


        }

*/
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
       // newPoint.setLayoutParams(new LinearLayout.LayoutParams(100,100));
        newPoint.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_action_name));

        newPoint.setOnClickListener(new AppCompatImageButton.OnClickListener() {
                                        public void onClick(View v) {
                                            Toast.makeText(context, "Hello World", Toast.LENGTH_LONG).show();
                                        }
                                    });

        //newPoint.setBackgroundResource(R.mipmap.waypoint1);
        navList.add(newPoint);
        //FrameLayout camera = (FrameLayout) findViewById(R.id.cameraPreview);
        camera.addView(newPoint);
    }
    public void clearPoints(){
        navList.clear();
    }


}