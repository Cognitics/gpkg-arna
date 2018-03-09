package net.cognitics.navapp;
import net.cognitics.navapp.GPSTracker;
import android.view.View;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.net.URL;
import java.util.List;

import mil.nga.geopackage.*;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;

import com.github.angads25.filepicker.*;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import static java.lang.Boolean.*;
import net.cognitics.navapp.GreatCircle;

public class MainActivity extends AppCompatActivity {
    private GeoPackage gpkgDb;
    GPSTracker gps;

    public MainActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gps = new GPSTracker(this);
        final Button button = findViewById(R.id.openGpkg);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.FILE_SELECT;
                properties.root = new File("/");//DialogConfigs.DEFAULT_DIR);
                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                properties.extensions = null;
                FilePickerDialog dialog = new FilePickerDialog(MainActivity.this,properties);
                dialog.setTitle("Select a File");
                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        //files is an array of the paths of files selected by the Application User.
                        if(files.length > 0) {
                            if(!openGeoPackage(files[0])) {
                                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
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
        });

    }

    private Boolean openGeoPackage(String path)
    {
        File f = new File(path);
        String geoPackageName = f.getName();
        // Open a GeoPackage
        GeoPackageManager manager = GeoPackageFactory.getManager(this);
        if(!manager.exists(geoPackageName)) {
            manager.importGeoPackage(geoPackageName,f);
        }
        gpkgDb = manager.open(geoPackageName);
        if(gpkgDb != null) {
            int numVerts = 0;
            double totalY = 0;
            double totalX = 0;
            StringBuilder message = new StringBuilder();
            message.append("Tables:\n");
            List<String> tables = gpkgDb.getFeatureTables();
            for(String table : tables){
                message.append(table);
                FeatureDao featureDao = gpkgDb.getFeatureDao(table);

                FeatureCursor featureCursor = featureDao.queryForAll();
                int numFeatures = featureCursor.getCount();
                message.append(": ");
                message.append(numFeatures);
                message.append("\n");

                try{
                    while(featureCursor.moveToNext()){
                        FeatureRow featureRow = featureCursor.getRow();
                        GeoPackageGeometryData geometryData = featureRow.getGeometry();
                        Geometry geometry = geometryData.getGeometry();

                        //GeometryType type = geometry.getGeometryType();
                        //if(type==GeometryType.POINT)
                        if(geometry instanceof LineString)
                        {
                            LineString line = (LineString)geometry;
                            for(Point pt:line.getPoints())
                            {
                                totalX += pt.getX();
                                totalY += pt.getY();
                                numVerts++;
                            }
                        }
                        else if(geometry instanceof Point)
                        {
                            Point pt = (Point)geometry;
                            totalX += pt.getX();
                            totalY += pt.getY();
                            numVerts++;
                        }
                        else if(geometry instanceof Polygon)
                        {
                            Polygon poly = (Polygon)geometry;
                            for(LineString line : poly.getRings())
                            {
                                for(Point pt:line.getPoints())
                                {
                                    totalX += pt.getX();
                                    totalY += pt.getY();
                                    numVerts++;
                                }
                            }
                        }
                    }
                }finally{
                    featureCursor.close();
                }

            }
            double avgX = totalX / numVerts;
            double avgY = totalY / numVerts;
            message.append("Center = ");
            message.append(avgY);
            message.append(", ");
            message.append(avgX);
            message.append("\n");
            Point destPt = new Point(avgX,avgY);
            if(!gps.canGetLocation()) {
                gps.showSettingsAlert();
            }
            if(!gps.canGetLocation()) {
                message.append("GPS Not enabled.");
            }
            else {
                Point currPt = new Point(gps.getLongitude(), gps.getLatitude());
                double distance = GreatCircle.getDistanceMiles(destPt, currPt);
                message.append("Distance from here: ");
                message.append(distance);
                message.append(" miles\n");
            }
            final EditText msgText = findViewById(R.id.messages);
            msgText.setText(message.toString());
            return TRUE;
        }
        else {
            return FALSE;
        }

    }
}
