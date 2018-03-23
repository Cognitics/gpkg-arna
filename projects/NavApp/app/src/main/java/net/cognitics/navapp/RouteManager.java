package net.cognitics.navapp;

import java.util.ArrayList;

import mil.nga.wkb.geom.Point;

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
        currentRoute = route;
        currentBearing = 0;
        currentDistance = 0;
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
        double nearestDistance =
                GreatCircle.getDistanceMeters(currentPositionGeo, currentRoute.get(0));
        for(int i=1;i<numPoints;i++)
        {
            double dist = GreatCircle.getDistanceMeters(currentPositionGeo, currentRoute.get(i));
            if(dist < nearestDistance)
            {
                nearestDistance = dist;
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
        return currentRoute.get(nextIndex);
    }

    //The nearest distance and bearing are only calculated here, so call this before getting those
    public void setCurrentPositionAndBearing(double latitude, double longitude, double elevation, double bearing)
    {
        currentPositionGeo = new Point(longitude,latitude);
        calculateNearestLinePointGeo();
        calculateNearestBearing();
    }

    double getNearestBearing()
    {
        return currentBearing;
    }

}
