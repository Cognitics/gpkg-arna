package net.cognitics.navapp;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;


import com.karan.churi.PermissionManager.PermissionManager;
import com.karan.churi.PermissionManager.PermissionManager.statusArray;

import android.view.View;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;


import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class MainActivity extends AppCompatActivity implements SensorEventListener {


    Camera camera;
    FrameLayout cameraPreview;
    ShowCamera showCamera;
    PermissionManager permissionManager;

    MainViewModel mViewModel;

    //Sensor Variables
    TextView tvHeading;
    SensorManager mSensorManager;
    CustomGraphics customGraphics;
    private float bearing,pitch,roll;

    protected float[] gravSensorVals;
    protected float[] magSensorVals;
    float RTmp[] = new float[9];
    private float results[] = new float[3];
    private float I[] = new float[9];
    private float Rot[] = new float[9];
    int resumeCamera=0;
    static final float ALPHA = 0.05f;

    static final int ROUTE_SELECT_REQUEST = 1;

    static final int REQUEST_CAMERA = 1;
    static final int REQUEST_LOCATION = 2;
    static final int REQUEST_MEDIA_WRITE = 3;
    static final int REQUEST_MEDIA_READ = 4;

    Boolean haveCameraPermission = FALSE;
    Boolean haveLocationPermission = FALSE;
    Boolean haveReadMediaPermission = FALSE;
    Boolean haveWriteMediaPermission = TRUE;//enabled for now, not sure if we need this permission

    public MainActivity() {
        //NULL
    }

    /**
     * Called from onCreate() when the proper permissions exist
     */
    protected void onCreateWithPermissions()
    {
        // Get camera stuff
        camera = Camera.open();
        cameraPreview = (FrameLayout) findViewById(R.id.cameraPreview);
        showCamera = new ShowCamera(this, camera);
        cameraPreview.addView(showCamera);

        //Bring constraint layout to front
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.Constraint);
        constraintLayout.bringToFront();

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvSensor);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);



        //Toast.makeText(this, "onCreate()", Toast.LENGTH_LONG).show();
    }

    /*
     * Runs on app launch
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Basic app creation
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        TextView msgText = (TextView) findViewById(R.id.messages);
        msgText.setText(mViewModel.messageLog);

        if (savedInstanceState == null) {
            // Ask for runtime permission
            mViewModel.setCustomGraphics(new CustomGraphics(this));
            permissionManager = new PermissionManager() {};
            permissionManager.checkAndRequestPermissions(this);
        }
        makeRequest();

        if(haveReadMediaPermission && haveLocationPermission && haveReadMediaPermission && haveWriteMediaPermission) {
            // Initialize the app
            onCreateWithPermissions();
        }

    }

    /**
     * Verify the table name starts with route, and initialize it if it does
     * @param tableName a table name beginning with "route_"
     * @return
     */
    public Boolean parseAndInitializeRoute(String tableName)
    {
        String routePrefix = "route_";
        if(tableName.startsWith(routePrefix))
        {
            String routeName =  tableName.substring(routePrefix.length());
            Toast.makeText(this, "You selected route: " + routeName, Toast.LENGTH_LONG).show();
            return mViewModel.initializeRoute(routeName);

        }
        else
        {
            Toast.makeText(this, "Invalid Route: " + tableName, Toast.LENGTH_LONG).show();
            return FALSE;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String rowText = data.getStringExtra(TableDialogActivity.RESULT_TEXT);
            parseAndInitializeRoute(rowText);

        }
    }


    /*
     * Functionality for choosing gpkg from files
     * @param v View
     */
    public void onClick(View v) {
        if(!(haveReadMediaPermission && haveLocationPermission && haveReadMediaPermission && haveWriteMediaPermission)) {
            return;
        }
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.root = new File("/"); //DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties);
        dialog.setTitle("Select a File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                //files is an array of the paths of files selected by the Application User.
                if (files.length > 0) {
                    if (!openGeoPackage(files[0])) {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
                        dlgAlert.setMessage("Unable to open " + files[0]);
                        dlgAlert.setTitle("Error");
                        dlgAlert.setPositiveButton("OK", null);
                        dlgAlert.setCancelable(true);
                        dlgAlert.create().show();
                        TextView msgText = (TextView) findViewById(R.id.messages);
                        msgText.setText(mViewModel.messageLog);
                    }
                    TextView msgText = (TextView) findViewById(R.id.messages);
                    msgText.setText(mViewModel.messageLog);
                }
            }
        });
        dialog.show();
    }


    /*
     * Reads Geo Package
     * @param path filepath for gpkg
     */

    private Boolean openGeoPackage(String path) {
        mViewModel.openGeoPackage(path);
        ArrayList<String> tables = mViewModel.getFeatureManager().getRoutes();
        if(tables.size()>1) {
            // Popup a list of tables
            // we will find all of the tables starting with route_ and
            // present the user with the choice of which route to use
            final Intent intent = new Intent(this, TableDialogActivity.class);
            intent.putExtra(TableDialogActivity.TITLE_TEXT, "Select Route");
            ArrayList<String> tableRows = new ArrayList<String>(tables);
            intent.putExtra(TableDialogActivity.ROW_STRINGS, tableRows);
            startActivityForResult(intent, ROUTE_SELECT_REQUEST);
        }
        else if(tables.size()==1)
        {
            parseAndInitializeRoute(tables.get(00));
        }
        else
            return FALSE;
        return TRUE;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!(haveReadMediaPermission && haveLocationPermission && haveReadMediaPermission && haveWriteMediaPermission)) {
            return;
        }
        //camera = Camera.open();
        ///cameraPreview = (FrameLayout) findViewById(R.id.cameraPreview);
       // showCamera = new ShowCamera(this, camera);
        //cameraPreview.addView(showCamera);
        resumeCamera=1;
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!(haveReadMediaPermission && haveLocationPermission && haveReadMediaPermission && haveWriteMediaPermission)) {
            return;
        }
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent evt) {
        customGraphics=mViewModel.getCustomGraphics();
        if(!(haveReadMediaPermission && haveLocationPermission && haveReadMediaPermission && haveWriteMediaPermission)) {
            return;
        }
        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravSensorVals = lowPass(evt.values.clone(), gravSensorVals);
        } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magSensorVals = lowPass(evt.values.clone(), magSensorVals);
        }
        if (gravSensorVals != null && magSensorVals != null) {
            SensorManager.getRotationMatrix(RTmp, I, gravSensorVals, magSensorVals);
            int rotation = 1; //???
            if (rotation == 1) {
                SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, Rot);
            } else {
                SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, Rot);
            }
            SensorManager.getOrientation(Rot, results);
            bearing = (float)(((results[0] * 180) / Math.PI) + 180);
            pitch = (float)(((results[1] * 180 / Math.PI)) + 90);
            roll = (float)(((results[2] * 180 / Math.PI)));
            customGraphics.setViewModel(bearing,pitch);


            //hotfix for camera being upside down in reverse landscape mode
            showCamera.updateRoll((int)roll);
            //hotfix for camera appearing black/crashing when app paused
            if (resumeCamera==1){
               // showCamera.resume();
                resumeCamera=0;
            }
            tvHeading.setText(" "+(int)bearing);

            //customGraphics.clearPoints();
            ArrayList<PointFeature> cnpFeatures = mViewModel.getFeatureManager().getCnpFeatures();
            for(PointFeature pointFeature : cnpFeatures)
            {
                double b = pointFeature.getBearing(mViewModel.getGps().getLatitude(), mViewModel.getGps().getLongitude(),mViewModel.getGps().getElevation());
                //todo: add different colors for cnp point to differentiate from the next route point
                //todo: make them clickable?
                //todo: When I enable the following line, the screen doesn't refresh on my phone
                customGraphics.addPoint(cameraPreview, (float) b, 90,Integer.toString(pointFeature.getFid()));
            }
            RouteManager rm = mViewModel.getRouteManager();
            if(rm!=null) {
                rm.setCurrentPositionAndBearing(mViewModel.getGps().getLatitude(), mViewModel.getGps().getLongitude(), mViewModel.getGps().getElevation(), bearing);

                double b = rm.getNearestBearing();
                double d = rm.getNearestDistance();
                int idx = rm.getNextIndex();
                mViewModel.setMessageLog(String.format("Distance: %.4fkm\nBearing: %.4f\nIndex: %d",d/1000.0,b,idx));
                TextView msgText = (TextView) findViewById(R.id.messages);
                msgText.setText(mViewModel.messageLog);
                customGraphics.addPoint(cameraPreview, (float) b, 90,"route_point");
                customGraphics.updatePositions();
            }
        }
    }


    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!(haveReadMediaPermission && haveLocationPermission && haveReadMediaPermission && haveWriteMediaPermission)) {
            return;
        }
        // On some devices the camera preview is lost when the activity is
        // restarted (i.e. onStart() is called but not onCreate())
        // Get camera stuff
        camera = Camera.open();
        cameraPreview = (FrameLayout) findViewById(R.id.cameraPreview);
        showCamera = new ShowCamera(this, camera);
        cameraPreview.addView(showCamera);
    }

    /**
     * Request permissions
     * take pictures and video
     * location
     * access photos and media
     */
    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_MEDIA_READ);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_MEDIA_WRITE);


        //makeRequest()
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[]
                                                   grantResults) {

        permissionManager.checkResult(requestCode, permissions, grantResults);
        ArrayList<statusArray> sa = permissionManager.getStatus();
        //System.out.println("xx" + sa.size());
        for(String permission : sa.get(0).granted) {
            switch(permission) {
                case "android.permission.CAMERA":
                    haveCameraPermission = TRUE;
                    break;
                case "android.permission.ACCESS_FINE_LOCATION":
                    haveLocationPermission = TRUE;
                    break;
                case "android.permission.READ_EXTERNAL_STORAGE":
                    haveReadMediaPermission = TRUE;
                    break;
                case "android.permission.WRITE_EXTERNAL_STORAGE":
                    haveWriteMediaPermission = TRUE;
                    break;
            }
        }

        if(haveReadMediaPermission && haveLocationPermission && haveReadMediaPermission && haveWriteMediaPermission) {
            // Initialize the app
            onCreateWithPermissions();
        }
    }


}