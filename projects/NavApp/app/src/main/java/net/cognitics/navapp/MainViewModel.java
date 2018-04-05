package net.cognitics.navapp;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.widget.TextView;

import mil.nga.geopackage.GeoPackage;
import mil.nga.wkb.geom.Point;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by kbentley on 3/12/2018.
 */

public class MainViewModel extends AndroidViewModel {

    public Boolean haveCameraPermission = FALSE;
    public Boolean haveLocationPermission = FALSE;
    public Boolean haveReadMediaPermission = FALSE;
    public Boolean haveWriteMediaPermission = TRUE;//enabled for now, not sure if we need this permission

    private GeoPackage gpkgDb;

    public GPSTracker getGps() {
        return gps;
    }

    private GPSTracker gps;

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    private FeatureManager featureManager;

    public void setMessageLog(String messageLog) {
        this.messageLog = messageLog;
    }

    public String messageLog = new String();

    private CustomGraphics customGraphics;

    public MainViewModel(@NonNull Application application) {
        super(application);
        featureManager = new FeatureManager(application.getApplicationContext());
        gps = new GPSTracker(application.getApplicationContext());
    }

    public Boolean initializeRoute(String route)
    {
        //todo: initialize route in feature manager
        if(!featureManager.initializeRoute(route))
        {
            return FALSE;
        }
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Routes points: ");
        messageBuilder.append(featureManager.getRouteManager().getRoute().size());
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
            double bearing = 99;
            Point currPt = new Point(gps.getLongitude(), gps.getLatitude());
            RouteManager rm = featureManager.getRouteManager();
            Point nextRoutePoint = currPt;
            if(rm != null)
            {
                rm.setCurrentPositionAndBearing(nextRoutePoint.getY(),nextRoutePoint.getX(),0,0);//todo: get bearing and altitude
                nextRoutePoint = rm.getNearestRoutePointGeo();
                bearing = rm.getNearestBearing();
            }
            double distance = GreatCircle.getDistanceMiles(nextRoutePoint, currPt);
            messageBuilder.append("Distance from here: ");
            messageBuilder.append(distance);
            messageBuilder.append(" miles\n");
            messageBuilder.append("Bearing: " + Double.valueOf(bearing).toString() + "\n");
        }
        messageLog = messageBuilder.toString();
        return TRUE;
    }

    public Boolean openGeoPackage(String path) {
        return featureManager.open(path);

    }
    public CustomGraphics getCustomGraphics(){return customGraphics;}
    public void setCustomGraphics(CustomGraphics customGraphics){this.customGraphics=customGraphics;}

    RouteManager getRouteManager()
    {
        return featureManager.getRouteManager();
    }
}
