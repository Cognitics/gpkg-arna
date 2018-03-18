package net.cognitics.navapp;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Chris on 3/3/2018.
 */

public class ShowCamera extends SurfaceView implements SurfaceHolder.Callback {
    Camera.Parameters params;
    Camera camera;
    SurfaceHolder holder;
    int roll=-90;
    public ShowCamera(Context context, Camera camera){
        super(context);
        this.camera=camera;
        holder = getHolder();
        holder.addCallback(this);
        params = camera.getParameters();

    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        camera.stopPreview();
    try {
        camera.startPreview();
    }catch(Exception e){

    }

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
try {
    if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
        params.set("orientation", "portrait");
        camera.setDisplayOrientation(90);
        params.setRotation(90);
    } else {
        if (roll > 0) {
            params.set("orientation", "landscape");
            camera.setDisplayOrientation(180);
            params.setRotation(180);
        } else {

            params.set("orientation", "landscape");
            camera.setDisplayOrientation(0);
            params.setRotation(0);
        }
    }


    camera.setParameters(params);
    try {
        camera.setPreviewDisplay(holder);
        camera.startPreview();
    } catch (IOException e) {
        e.printStackTrace();
    }
}catch(Exception e){

}
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    public void updateRoll(int roll){
        //this.roll=roll;
        if (Math.abs(this.roll-roll)>40) {
            //Save battery
            this.roll=roll;
            surfaceCreated(holder);
        }
    }
    public void resume(){
        surfaceCreated(holder);
    }

}
