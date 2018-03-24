package net.cognitics.navapp;

import java.util.ArrayList;

import mil.nga.wkb.geom.Point;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by kbentley on 3/13/2018.
 *
 * Reference to the current route (i.e linestring)
 * Holds the current position (geographic coordinates)
 * Accessors for bearing and distance to next route vert
 *  */

public class RouteManager {

    private double currentBearing;
    private double currentDistance;
    // All available Routes. We'll use the first one for now.
    private ArrayList<Point> currentRoute;
    // Position along the current route
    private Point currentPositionGeo;
    // Index on current route vert (integer referencing a vert 0-n)
    private int nextIndex = 0;

    private boolean needsUpdate;

    private void updateRoute()
    {
        if(needsUpdate)
        {
            calculateNearestBearing();
            // gets the point and the distance
            calculateNearestLinePointGeo();
            needsUpdate = FALSE;
        }
    }

    public ArrayList getRoute() {
        return currentRoute;
    }

    public void setRoute(ArrayList<Point> route) {
        this.currentRoute = route;
        nextIndex = 0;
    }

    /**
     * Retrieves the index to the next point on the route, based on the current position
     * specified in setCurrentPositionAndBearing()
     * @return index into route
     */
    public int getNextIndex() {
        return nextIndex;
    }

    RouteManager(ArrayList<Point> route)
    {
        currentRoute = new ArrayList<Point>(route.size());
        for (Point pt: route){
            currentRoute.add(pt);
        }
        currentBearing = 0;
        currentDistance = 0;
        needsUpdate = TRUE;
    }

    /**
     *
     * @return  The position (in geographic coordinates) of the nearest point on the line from
     *          the current position
     */
    private void calculateNearestLinePointGeo()
    {
        int numPoints = currentRoute.size();
        if(numPoints<2)
            return;
        currentDistance =
                GreatCircle.getDistanceMeters(currentPositionGeo, currentRoute.get(0));
        for(int i=nextIndex;i<numPoints;i++)
        {
            double dist = GreatCircle.getDistanceMeters(currentPositionGeo, currentRoute.get(i));
            if(dist < currentDistance)
            {
                currentDistance = dist;
                nextIndex = i;
            }
        }
    }

    private void calculateNearestBearing()
    {
        currentBearing = GreatCircle.getBearing(currentPositionGeo,currentRoute.get(nextIndex));
    }

    //Get the geographic coordinates nearest the current position
    public Point getNearestRoutePointGeo()
    {
        updateRoute();
        return currentRoute.get(nextIndex);
    }

    //The nearest distance and bearing are only calculated here, so call this before getting those
    public void setCurrentPositionAndBearing(double latitude, double longitude, double elevation, double bearing)
    {
        currentPositionGeo = new Point(longitude,latitude);
        needsUpdate = TRUE;
    }

    double getNearestBearing()
    {
        updateRoute();
        return currentBearing;
    }

    double getNearestDistance()
    {
        updateRoute();
        return currentDistance;
    }

}
