package net.cognitics.navapp;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.karan.churi.PermissionManager.PermissionManager;

import android.view.View;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import java.io.File;

import mil.nga.geopackage.*;
import mil.nga.wkb.geom.Point;

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
    FeatureManager featureManager;
    PermissionManager permissionManager;

    private GeoPackage gpkgDb;
    private GPSTracker gps;
    private String messageLog = new String();


    //Sensor Variables
    TextView tvHeading;
    SensorManager mSensorManager;

    private float bearing,pitch,roll;

    protected float[] gravSensorVals;
    protected float[] magSensorVals;
    float RTmp[] = new float[9];
    private float results[] = new float[3];
    private float I[] = new float[9];
    private float Rot[] = new float[9];

    static final float ALPHA = 0.25f;

    //

    public MainActivity() {
        //NULL
    }

    /*
     * Runs on app launch
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Basic app creation
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // Ask for runtime permission
            permissionManager = new PermissionManager() {};
            permissionManager.checkAndRequestPermissions(this);
            featureManager = new FeatureManager(this);
            // Initalize Location tracker
            gps = new GPSTracker(this);
        }
        // Get camera stuff
        camera = Camera.open();
        cameraPreview = (FrameLayout) findViewById(R.id.cameraPreview);
        showCamera = new ShowCamera(this, camera);
        cameraPreview.addView(showCamera);

        //Bring constraint layout to front
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.Constraint);
        constraintLayout.bringToFront();
        TextView msgText = (TextView) findViewById(R.id.messages);
        msgText.setText(messageLog);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvSensor);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode, permissions, grantResults);
    }

    /*
     * Functionality for choosing gpkg from files
     * @param v View
     */
    public void onClick(View v) {
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
                    }
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
        featureManager.open(path);
        Point geoPackageCenter = featureManager.getGeoCenter();
        StringBuilder messageBuilder = new StringBuilder();
        /*
                message.append("Center = ");
                message.append(geoPackageCenter.getY());
                message.append(", ");
                message.append(geoPackageCenter.getX());
                message.append("\n");
        */
        messageBuilder.append("Routes: ");
        messageBuilder.append(featureManager.routeFeatures.size());
        messageBuilder.append("\nCNPs: ");
        messageBuilder.append(featureManager.cnpFeatures.size());
        messageBuilder.append("\nPOIs: ");
        messageBuilder.append(featureManager.poiPointFeatures.size());
        messageBuilder.append("\nAOIs: ");
        messageBuilder.append(featureManager.aoiPointFeatures.size());
        messageBuilder.append("\n");
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }
        if (!gps.canGetLocation()) {
            messageBuilder.append("GPS Not enabled.");
        } else {
            Point currPt = new Point(gps.getLongitude(), gps.getLatitude());
            double distance = GreatCircle.getDistanceMiles(geoPackageCenter, currPt);
            messageBuilder.append("Distance from here: ");
            messageBuilder.append(distance);
            messageBuilder.append(" miles\n");
        }
        messageLog = messageBuilder.toString();
        TextView msgText = (TextView) findViewById(R.id.messages);
        msgText.setText(messageLog);

        return TRUE;
    }
    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent evt) {
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
            tvHeading.setText(" "+(int)bearing);
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
}