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

    // All available Routes. We'll use the first one for now.
    private ArrayList<LineStringFeature> routes;
    private LineStringFeature currentRoute;
    // Position along the current route

    // Index on current route vert (integer referencing a vert 0-n)
    int nextRouteVert = 0;

    RouteManager(ArrayList<LineStringFeature> routes)
    {
        this.routes = routes;
        if(routes.size()>0)
            currentRoute = routes.get(0);
    }

    //Get the geographic coordinates nearest the current position
    Point getNearestRoutePointGeo(Point currentPosition)
    {
        return currentRoute.getNearestLinePointGeo(currentPosition);
    }

    //double getNearestBearing()

}
