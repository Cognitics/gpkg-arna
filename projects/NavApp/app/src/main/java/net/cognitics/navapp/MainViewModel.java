package net.cognitics.navapp;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.widget.TextView;

import mil.nga.geopackage.GeoPackage;
import mil.nga.wkb.geom.Point;

import static java.lang.Boolean.TRUE;

/**
 * Created by kbentley on 3/12/2018.
 */

public class MainViewModel extends AndroidViewModel {

    private GeoPackage gpkgDb;
    private GPSTracker gps;
    FeatureManager featureManager;
    public String messageLog = new String();

    public MainViewModel(@NonNull Application application) {
        super(application);
        featureManager = new FeatureManager(application.getApplicationContext());
        gps = new GPSTracker(application.getApplicationContext());
    }

    public Boolean openGeoPackage(String path) {
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

        return TRUE;
    }

}
