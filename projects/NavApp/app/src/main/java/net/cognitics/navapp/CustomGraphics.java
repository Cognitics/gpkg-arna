package net.cognitics.navapp;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomGraphics extends View
{


    float bearing=-1;
    float pitch;
    public Context context;
    FrameLayout camera;
    private Activity mainActivity;
    //private ArrayList<NavPoint> navList=new ArrayList<NavPoint>();
    private HashMap<String,NavPoint> navMap=new HashMap<String,NavPoint>();
    private ArrayList<NavLine> lineList = new ArrayList<NavLine>();
    Paint paint = new Paint();
    public CustomGraphics(Context con)

    {
        super(con);
        this.context=con;
        paint.setColor(Color.WHITE);
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
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(10);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (NavLine n : lineList){
            if (n.getPoints()!=null){
                NavPoint p1=n.getPoints()[0];
                NavPoint p2=n.getPoints()[1];
            if (p1!=null&&p2!=null) {

               // if ((p1.getX()>0&&p1.getX()<camera.getWidth()-p1.getWidth())&&(p2.getX()>0&&p2.getX()<camera.getWidth()-p2.getWidth()))
                //{


                    c.drawLine(p1.getX() + (p1.getWidth() / 2), p1.getY() + (p1.getHeight() / 2), p2.getX() + (p2.getWidth() / 2), p2.getY() + (p2.getHeight() / 2), paint);
               // }
            }else if (n.getPoints()!=null&&p1!=null) {
                //if (p1.getX()>0&&p1.getX()<camera.getWidth()-p1.getWidth()){
                c.drawLine(p1.getX() + (p1.getWidth() / 2), p1.getY() + (p1.getHeight() / 2), camera.getMeasuredWidth() / 2, camera.getMeasuredHeight(), paint);
            //}
            }
            }
        }
    }


    public void updatePositions(){
        int width = camera.getMeasuredWidth();
        int height = camera.getMeasuredHeight();
        for (NavPoint n : navMap.values()){

            //Set X and Y to center of screen
            int tX = (width/2)-(n.getWidth()/2);
            int tY = (height/2)-(n.getHeight()/2);
            TextView text = n.getText();
            text.setVisibility(View.GONE);
            text.measure(0, 0);
            boolean showText = true;
            //Offset X and Y based on deltaPitch and deltaBearing
            if (Math.abs(n.getBearing()-bearing)>180){
                if (bearing>180) {
                    //Bearing around 330, target around 30, should be a positive result(60?)
                    tX += 12 * (Math.abs(bearing-360) + (n.getBearing()));
                }else{
                    //Bearing around 30, target around 330, should be negative result(-60?)
                    tX -= 12 * (bearing + Math.abs((n.getBearing())-360));
                }
            }else {
                tX += 12 * ((n.getBearing()) - (bearing));
            }

            int spread = 30; //Hardcoded currently, should be set as a representation of the spread of the route's distance
            int pRatio = ((spread*(1000/((n.getDistance())+1)))-(spread*2));
            if (pRatio>120){
                pRatio=120;
            }

            tY-=16*(n.getPitch()-(pitch))-pRatio;


            //Keep on screen if out of screen

            if (tX<0){
                //CASE: Off screen to left
                //tY-=tX; //TOO CONFUSING?
                tX=0;
                showText=false;

            }else if (tX>width-n.getWidth()){
                //CASE: Off screen to right
                //tY+=tX-n.getWidth();
                tX=width-(n.getWidth());
                showText=false;

            }
            if (tY<0){
                //CASE: Off screen up
                tY=0;
                showText=false;
            }else if (tY>height-n.getHeight()){
                //CASE: Off screen down
                tY=height-(n.getHeight());
                showText=false;
            }
            //Apply X and Y
            if (showText){
                text.setVisibility(View.VISIBLE);
                text.setX((float)tX-(text.getMeasuredWidth()/4));
                text.setY((float)tY+70);
            }
            n.setX((float)tX);
            n.setY((float)tY);
        }
        invalidate();

    }


    public void setViewModel(float bearing, float pitch){
        this.bearing=bearing;
        this.pitch=pitch;
    }


    public void addPoint(FrameLayout camera, float bearing, float pitch, String id, float distance, String title, PointFeature feature){

        if(navMap.containsKey(id))
        {
            if(title!=null)
                navMap.get(id).getText().setText(String.format("%s:\n %.3fkm",title,distance/1000.0));
                navMap.get(id).setDistance((int)distance);
            updatePoint(bearing,pitch,id);
            return;
        }
        this.camera=camera;
        final NavPoint newPoint = new NavPoint(context,bearing,pitch);
        newPoint.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_action_name));
        if(title!=null && title=="*")
            newPoint.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_action_name));
        else
            newPoint.setBackgroundDrawable(getResources().getDrawable(R.drawable.cnpicon));

        newPoint.setOnClickListener(new AppCompatImageButton.OnClickListener() {
                                        public void onClick(View v) {

                                            //Get the PointFeature associated with this NavPoint
                                            //We can use this to attach a new photo
                                            PointFeature pt = (PointFeature)v.getTag();
                                          //  if(pt==null)
                                            {
                                                // The NavPoint for the waypoint will be null
                                            //}
                                            //else {

                                                PopupMenu popupMenu = new PopupMenu(context, newPoint);
                                                popupMenu.getMenuInflater().inflate(R.menu.navpoint_select, popupMenu.getMenu());
                                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                    public boolean onMenuItemClick(MenuItem item) {
                                                        switch (item.getItemId()) {
                                                            case R.id.snap:
                                                                /*String file = "Picturetest.jpg";
                                                                File newfile = new File(file);
                                                                try {
                                                                    newfile.createNewFile();
                                                                } catch (IOException e) {
                                                                }

                                                                Uri outputFileUri = Uri.fromFile(newfile);

                                                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

                                                                ((Activity) context).startActivityForResult(cameraIntent, MainActivity.REQUEST_TAKE_PHOTO);
                                                                */
                                                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                                // Because of how the camera code works, it assumes that if there
                                                                // are any extras, then there must be an EXTRA_OUTPUT entry, so it doesn't
                                                                // appear that we can asssociate any other extras along for the ride.
                                                                // Because of that we'll have to use a static member to track which
                                                                // point feature to associate with the picture for now.
                                                                //takePictureIntent.putExtra("kevin","washere");
                                                                if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                                                                    ((Activity)context).startActivityForResult(takePictureIntent, MainActivity.REQUEST_TAKE_PHOTO);
                                                                }
                                                                break;

                                                            case R.id.select:
                                                                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                                // Start the Intent
                                                                ((Activity) context).startActivityForResult(galleryIntent, MainActivity.REQUEST_PICK_PHOTO);
                                                                break;


                                                            default:
                                                                break;

                                                        }
                                                        return true;


                                                    }
                                                });
                                                popupMenu.show();
                                            }
                                        }
                                    });

        newPoint.setTag(feature);
        navMap.put(id,newPoint);
        camera.addView(newPoint);
        newPoint.setDistance((int)distance);
        camera.addView(newPoint.getText());
        if(title!=null)
            navMap.get(id).getText().setText(String.format("%s:\n %.3fkm",title,distance/1000.0));
    }

    public void addLine(NavPoint navOne, NavPoint navtwo){
        lineList.add(new NavLine(navOne,navtwo));
    }
    public void clearLines(){
        lineList.clear();
    }

    public void updatePoint(float bearing, float pitch, String id)
    {
        if(navMap.containsKey(id))
        {
           navMap.get(id).setBearing(bearing);
           navMap.get(id).setPitch(pitch);
        }
    }
    public void clearPoints(){
        for(View v : navMap.values())
        {
            camera.removeView(v);
        }
        navMap.clear();
    }
    public NavPoint getPoint(String id){
        return navMap.get(id);
    }


}