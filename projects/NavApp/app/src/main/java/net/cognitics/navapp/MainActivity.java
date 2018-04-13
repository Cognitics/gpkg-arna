package net.cognitics.navapp;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;


import com.karan.churi.PermissionManager.PermissionManager;
import com.karan.churi.PermissionManager.PermissionManager.statusArray;

import android.view.View;
import android.support.v7.app.AlertDialog;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;

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
    private float bearing, pitch, roll;

    protected float[] gravSensorVals;
    protected float[] magSensorVals;
    float RTmp[] = new float[9];
    private float results[] = new float[3];
    private float I[] = new float[9];
    private float Rot[] = new float[9];
    int resumeCamera = 0;
    static final float ALPHA = 0.05f;

    static final int ROUTE_SELECT_REQUEST = 1;
    static final int CNP_RT_TEST_REQUEST = 3;

    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_LOCATION = 2;
    public static final int REQUEST_MEDIA_WRITE = 3;
    public static final int REQUEST_MEDIA_READ = 4;
    public static final int REQUEST_TAKE_PHOTO = 5;
    public static final int REQUEST_PICK_PHOTO = 6;
    public static final int REQUEST_PREFERENCES = 7;

    public static Boolean prefRouteFromStart = TRUE;
    public static Boolean prefDisplayCNPPhotos = TRUE;
    public static Boolean prefGroupCNPMarkers = TRUE;
    public static Boolean prefDisplayPOI = TRUE;
    public static float prefCNPArrivalDistance = 10;
    public static float prefCNPOffRouteDistance = 25;

    public MainActivity() {
        //NULL
    }

    /**
     * Called from onCreate() when the proper permissions exist
     */
    protected void onCreateWithPermissions() {
        // Get camera stuff
        camera = Camera.open();
        cameraPreview = (FrameLayout) findViewById(R.id.cameraPreview);
        showCamera = new ShowCamera(this, camera);
        cameraPreview.addView(showCamera);
        ((RelativeLayout)findViewById(R.id.rLayout)).addView(mViewModel.getCustomGraphics());
        mViewModel.getCustomGraphics().bringToFront();
        //Bring constraint layout to front
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.Constraint);
        constraintLayout.bringToFront();

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvSensor);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Toast.makeText(this, "onCreate()", Toast.LENGTH_LONG).show();
        ImageButton btn = (ImageButton) findViewById(R.id.action_settings);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                startActivityForResult(intent, REQUEST_PREFERENCES);
            }
        });

        ImageButton toggleAutoRouteMode = (ImageButton) findViewById(R.id.autoNav);
        if(mViewModel.getRouteManager()!=null) {
            toggleAutoRouteMode.setVisibility(View.VISIBLE);
            if(mViewModel.getRouteManager().getAutoRouteMode())
                toggleAutoRouteMode.setImageResource(android.R.drawable.ic_media_pause);
            else
                toggleAutoRouteMode.setImageResource(android.R.drawable.ic_media_play);

        }
        ImageButton nextNavButton = (ImageButton) findViewById(R.id.nextNav);
        ImageButton previousNavButton = (ImageButton) findViewById(R.id.previousNav);

        if(mViewModel.getRouteManager()!=null && !mViewModel.getRouteManager().getAutoRouteMode()) {
            previousNavButton.setVisibility(View.VISIBLE);
            nextNavButton.setVisibility(View.VISIBLE);
        }
        previousNavButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int waypointId = mViewModel.getRouteManager().rewindRoutePoint();
                Toast.makeText(getBaseContext(), "Waypoint set to " + waypointId, Toast.LENGTH_LONG).show();
            }
        });
        //todo: enable and set the bitmap as the route CNP changes.
        ImageButton mImageView = (ImageButton) findViewById(R.id.imageButton);
        mImageView.setVisibility(View.INVISIBLE);
        //ImageButton minimizeBtn = (ImageButton) findViewById(R.id.showImage);
        //minimizeBtn.setVisibility(View.INVISIBLE;

        nextNavButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int waypointId = mViewModel.getRouteManager().advanceRoutePoint();
                Toast.makeText(getBaseContext(), "Waypoint set to " + waypointId, Toast.LENGTH_LONG).show();
            }
        });
        toggleAutoRouteMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mViewModel.getRouteManager().getAutoRouteMode()) {
                    ImageButton toggleAutoRouteMode = (ImageButton) findViewById(R.id.autoNav);
                    toggleAutoRouteMode.setImageResource(android.R.drawable.ic_media_play);
                    mViewModel.getRouteManager().setAutoRouteMode(FALSE);
                    ImageButton prevNavBtn = (ImageButton) findViewById(R.id.previousNav);
                    prevNavBtn.setVisibility(View.VISIBLE);
                    ImageButton nextNavBtn = (ImageButton) findViewById(R.id.nextNav);
                    nextNavBtn.setVisibility(View.VISIBLE);

                    //todo: change the button icon to something other than 'play'
                    Toast.makeText(getBaseContext(), "Navigation mode set to manual.", Toast.LENGTH_LONG).show();
                }
                else {
                    mViewModel.getRouteManager().setAutoRouteMode(TRUE);
                    //todo: change the button back
                    ImageButton toggleAutoRouteMode = (ImageButton) findViewById(R.id.autoNav);
                    toggleAutoRouteMode.setImageResource(android.R.drawable.ic_media_pause);
                    ImageButton prevNavBtn = (ImageButton) findViewById(R.id.previousNav);
                    prevNavBtn.setVisibility(View.INVISIBLE);
                    ImageButton nextNavBtn = (ImageButton) findViewById(R.id.nextNav);
                    nextNavBtn.setVisibility(View.INVISIBLE);
                    Toast.makeText(getBaseContext(), "Navigation mode set to auto.", Toast.LENGTH_LONG).show();
                }
            }
        });


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Map<String,?> allprefs = preferences.getAll();
        prefRouteFromStart = preferences.getBoolean("route_from_start", TRUE);
        prefDisplayCNPPhotos = preferences.getBoolean("display_cnp_photos", TRUE);
        prefGroupCNPMarkers = preferences.getBoolean("group_cnp_icons", TRUE);
        prefDisplayPOI = preferences.getBoolean("display_poi", TRUE);
        prefCNPOffRouteDistance = Float.parseFloat(preferences.getString("cnp_off_route_distance", "25"));
        prefCNPArrivalDistance = Float.parseFloat(preferences.getString("cnp_arrival_distance", "10"));
    }

    /*
     * Runs on app launch
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Basic app creation
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d("NAVAPP", "OnCreate()");
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        TextView msgText = (TextView) findViewById(R.id.messages);
        msgText.setText(mViewModel.messageLog);

        if (savedInstanceState == null || permissionManager == null) {
            // Ask for runtime permission
            permissionManager = new PermissionManager() {
            };
            permissionManager.checkAndRequestPermissions(this);
            mViewModel.setCustomGraphics(new CustomGraphics(this));
            ImageButton toggleAutoRouteMode = (ImageButton) findViewById(R.id.autoNav);
            toggleAutoRouteMode.setVisibility(View.INVISIBLE);
            ImageButton prevNavBtn = (ImageButton) findViewById(R.id.previousNav);
            prevNavBtn.setVisibility(View.INVISIBLE);
            ImageButton nextNavBtn = (ImageButton) findViewById(R.id.nextNav);
            nextNavBtn.setVisibility(View.INVISIBLE);
            makeRequest();
        }

        if (mViewModel.haveCameraPermission && mViewModel.haveLocationPermission && mViewModel.haveReadMediaPermission && mViewModel.haveWriteMediaPermission) {
            // Initialize the app
            onCreateWithPermissions();
        }

    }

    /**
     * Verify the table name starts with route, and initialize it if it does
     *
     * @param tableName a table name beginning with "route_"
     * @return
     */
    public Boolean parseAndInitializeRoute(String tableName) {
        String routePrefix = "route_";
        if (tableName.startsWith(routePrefix)) {
            String routeName = tableName.substring(routePrefix.length());
            Toast.makeText(this, "You selected route: " + routeName, Toast.LENGTH_LONG).show();
            return mViewModel.initializeRoute(routeName);
        } else {
            Toast.makeText(this, "Invalid Route: " + tableName, Toast.LENGTH_LONG).show();
            return FALSE;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("NAVAPP", "OnActivityResult(" + requestCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ROUTE_SELECT_REQUEST && resultCode == Activity.RESULT_OK) {
            String rowText = data.getStringExtra(TableDialogActivity.RESULT_TEXT);
            parseAndInitializeRoute(rowText);

        } else if ((requestCode == REQUEST_TAKE_PHOTO) && resultCode == Activity.RESULT_OK) {
            ImageButton mImageView = (ImageButton) findViewById(R.id.imageButton);
            Bundle extras = data.getExtras();
            mImageView.setVisibility(View.VISIBLE);
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            imageBitmap.recycle();
            mImageView.setImageBitmap(imageBitmap);
            recreate();

        } else if ((requestCode == REQUEST_PICK_PHOTO) && resultCode == Activity.RESULT_OK) {

        }
        // We don't check resultCode for preferences since there is only back/cancel to get out
        else if (requestCode == REQUEST_PREFERENCES) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            //Map<String,?> allprefs = preferences.getAll();
            Boolean new_prefRouteFromStart = preferences.getBoolean("route_from_start", TRUE);
            if (prefRouteFromStart != new_prefRouteFromStart) {
                Toast.makeText(this, "Reload the route to restart navigation.", Toast.LENGTH_LONG).show();
            }
            prefRouteFromStart = new_prefRouteFromStart;
            prefDisplayCNPPhotos = preferences.getBoolean("display_cnp_photos", TRUE);
            prefGroupCNPMarkers = preferences.getBoolean("group_cnp_icons", TRUE);
            prefDisplayPOI = preferences.getBoolean("display_poi", TRUE);
            prefCNPOffRouteDistance = Float.parseFloat(preferences.getString("cnp_off_route_distance", "25"));
            prefCNPArrivalDistance = Float.parseFloat(preferences.getString("cnp_arrival_distance", "10"));
        }
    }

    /*
     * Functionality for choosing gpkg from files
     * @param v View
     */
    public void onClick(View v) {
        Log.d("NAVAPP", "OnClick()");
        if (!(mViewModel.haveCameraPermission && mViewModel.haveLocationPermission && mViewModel.haveReadMediaPermission && mViewModel.haveWriteMediaPermission)) {
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
                    else
                    {
                        ImageButton toggleAutoRouteMode = (ImageButton) findViewById(R.id.autoNav);
                        toggleAutoRouteMode.setVisibility(View.VISIBLE);
                        //defaults to autoroute mode
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

        if (tables.size() > 1) {
            // Popup a list of tables
            // we will find all of the tables starting with route_ and
            // present the user with the choice of which route to use
            final Intent intent = new Intent(this, TableDialogActivity.class);
            intent.putExtra(TableDialogActivity.TITLE_TEXT, "Select Route");
            ArrayList<String> tableRows = new ArrayList<String>(tables);
            intent.putExtra(TableDialogActivity.ROW_STRINGS, tableRows);
            startActivityForResult(intent, ROUTE_SELECT_REQUEST);
        } else if (tables.size() == 1) {
            parseAndInitializeRoute(tables.get(00));
        } else
            return FALSE;
        return TRUE;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("NAVAPP", "OnResume()");
        if (!(mViewModel.haveCameraPermission && mViewModel.haveLocationPermission && mViewModel.haveReadMediaPermission && mViewModel.haveWriteMediaPermission)) {
            return;
        }
        //camera = Camera.open();
        ///cameraPreview = (FrameLayout) findViewById(R.id.cameraPreview);
        // showCamera = new ShowCamera(this, camera);
        //cameraPreview.addView(showCamera);
        resumeCamera = 1;
        if (mSensorManager == null)
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("NAVAPP", "OnPause()");
        if (!(mViewModel.haveCameraPermission && mViewModel.haveLocationPermission && mViewModel.haveReadMediaPermission && mViewModel.haveWriteMediaPermission)) {
            return;
        }
        // to stop the listener and save battery
        if (mSensorManager == null)
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent evt) {
        CustomGraphics customGraphics = mViewModel.getCustomGraphics();
        if (!(mViewModel.haveCameraPermission && mViewModel.haveLocationPermission && mViewModel.haveReadMediaPermission && mViewModel.haveWriteMediaPermission)) {
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
            bearing = (float) (((results[0] * 180) / Math.PI) + 180);
            pitch = (float) (((results[1] * 180 / Math.PI)) + 90);
            roll = (float) (((results[2] * 180 / Math.PI)));
            customGraphics.setViewModel(bearing, pitch);


            //hotfix for camera being upside down in reverse landscape mode
            if (showCamera != null)
                showCamera.updateRoll((int) roll);
            //hotfix for camera appearing black/crashing when app paused
            if (resumeCamera == 1) {
                // showCamera.resume();
                resumeCamera = 0;
            }
            if (tvHeading != null)
                tvHeading.setText("Heading:\n" + (int) bearing);

            //customGraphics.clearPoints();
/*
            ArrayList<PointFeature> cnpFeatures = mViewModel.getFeatureManager().getCnpFeatures();
            for(PointFeature pointFeature : cnpFeatures)
            {
                double b = pointFeature.getBearing(mViewModel.getGps().getLatitude(), mViewModel.getGps().getLongitude(),mViewModel.getGps().getElevation());
                double d = pointFeature.getDistance(mViewModel.getGps().getLatitude(), mViewModel.getGps().getLongitude(),mViewModel.getGps().getElevation());
                //todo: add different colors for cnp point to differentiate from the next route point
                //todo: make them clickable?
                //todo: When I enable the following line, the screen doesn't refresh on my phone
                String featureName = pointFeature.getAttribute("name");

                if(featureName==null)
                {
                    featureName = Integer.toString(pointFeature.getFid());
                }
                customGraphics.addPoint(cameraPreview, (float) b, 90,Integer.toString(pointFeature.getFid()),(float)d,featureName,pointFeature);
            }*/

            if(prefDisplayPOI) {
                ArrayList<PointFeature> poiFeatures = mViewModel.getFeatureManager().getPoiPointFeatures();
                for (PointFeature pointFeature : poiFeatures) {
                    double b = pointFeature.getBearing(mViewModel.getGps().getLatitude(), mViewModel.getGps().getLongitude(), mViewModel.getGps().getElevation());
                    double d = pointFeature.getDistance(mViewModel.getGps().getLatitude(), mViewModel.getGps().getLongitude(), mViewModel.getGps().getElevation());
                    //todo: add different colors for cnp point to differentiate from the next route point
                    //todo: make them clickable?
                    //todo: When I enable the following line, the screen doesn't refresh on my phone
                    String featureName = pointFeature.getAttribute("name");

                    if (featureName == null) {
                        featureName = Integer.toString(pointFeature.getFid());
                    }
                    customGraphics.addPoint(cameraPreview, (float) b, 90, Integer.toString(pointFeature.getFid()), (float) d, featureName, pointFeature);
                }
            }


            //customGraphics.invalidate();



            RouteManager rm = mViewModel.getRouteManager();
            if (rm != null) {
                rm.setCurrentPositionAndBearing(mViewModel.getGps().getLatitude(), mViewModel.getGps().getLongitude(), mViewModel.getGps().getElevation(), bearing);

                double b = rm.getNearestBearing();
                double d = rm.getNearestDistance();
                int idx = rm.getNextIndex();
                mViewModel.setMessageLog(String.format("Distance: %.4fkm\nBearing: %.4f\nIndex: %d", d / 1000.0, b, idx));
                TextView msgText = (TextView) findViewById(R.id.messages);
                msgText.setText(mViewModel.messageLog);
                customGraphics.addPoint(cameraPreview, (float) b, 90, "route_point", (float) d, "", null);
                customGraphics.addLine(customGraphics.getPoint("route_point"),null);
                customGraphics.updatePositions();


                PointFeature cnp = rm.getCurrentCNP();
                int cnpID = rm.getCurrentCNPID();
                if(mViewModel.currentCNPID!=cnpID || mViewModel.cnpImage==null) {
                    // get image bitmap from cnp
                    ArrayList<RelatedTablesRelationship> relationships = mViewModel.getFeatureManager().getRelatedTablesManager().getRelationships(cnp.getLayerName());
                    if(relationships.size()>0) {
                        //We're only going to render the first relationship
                        ArrayList<FeatureManager.FeatureMedia> mediaBlobs = mViewModel.getFeatureManager().getMediaBlobs(relationships.get(0),cnp.getFid());
                        if(mediaBlobs.size()>0) {
                            // get a bitmap
                            ByteArrayInputStream is = new ByteArrayInputStream(mediaBlobs.get(0).blob); //stream pointing to your blob or file
                            ImageButton mImageView = (ImageButton) findViewById(R.id.imageButton);
                            mViewModel.cnpImage = BitmapFactory.decodeStream(is);
                            if(mViewModel.cnpImage!=null) {
                                mImageView.setImageBitmap(mViewModel.cnpImage);
                                mImageView.setVisibility(View.VISIBLE);
                            }
                            else {
                                mImageView.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
                double cnpBearing = cnp.getBearing(mViewModel.getGps().getLatitude(), mViewModel.getGps().getLongitude(), mViewModel.getGps().getElevation());
                double cnpDistance = cnp.getDistance(mViewModel.getGps().getLatitude(), mViewModel.getGps().getLongitude(), mViewModel.getGps().getElevation());
                // get cnp bearing/distance
                customGraphics.addPoint(cameraPreview, (float) cnpBearing, 90, "id", (float) d, String.format(Locale.US,"%.3fkm",cnpDistance), cnp);
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
        Log.d("NAVAPP", "OnStart()");
        if (!(mViewModel.haveCameraPermission && mViewModel.haveLocationPermission && mViewModel.haveReadMediaPermission && mViewModel.haveWriteMediaPermission)) {
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

        if (!mViewModel.haveCameraPermission)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        if (!mViewModel.haveLocationPermission)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        if (!mViewModel.haveReadMediaPermission)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_MEDIA_READ);
        if (!mViewModel.haveWriteMediaPermission)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_MEDIA_WRITE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[]
                                                   grantResults) {

        Log.d("NAVAPP", "OnRequestPermissionsResult()");

        permissionManager.checkResult(requestCode, permissions, grantResults);
        ArrayList<statusArray> sa = permissionManager.getStatus();
        //System.out.println("xx" + sa.size());
        for (String permission : sa.get(0).granted) {
            switch (permission) {
                case "android.permission.CAMERA":
                    mViewModel.haveCameraPermission = TRUE;
                    break;
                case "android.permission.ACCESS_FINE_LOCATION":
                    mViewModel.haveLocationPermission = TRUE;
                    break;
                case "android.permission.READ_EXTERNAL_STORAGE":
                    mViewModel.haveReadMediaPermission = TRUE;
                    break;
                case "android.permission.WRITE_EXTERNAL_STORAGE":
                    mViewModel.haveWriteMediaPermission = TRUE;
                    break;
            }
        }

        if (mViewModel.haveCameraPermission && mViewModel.haveLocationPermission && mViewModel.haveReadMediaPermission && mViewModel.haveWriteMediaPermission) {
            // Initialize the app
            recreate();
        }
    }

    // public void createPMenu(NavPoint navPoint, AppCompatImageButton button){

    // }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d("NAVAPP", "OnStop()");
    }
}