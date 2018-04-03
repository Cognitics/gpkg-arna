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

    private ArrayList<Point> currentRouteUTM;
    // Position along the current route
    private Point currentPositionGeo;
    private Point currentPositionUTM;
    // Index on current route vert (integer referencing a vert 0-n)
    private int nextIndex = 0;

    private boolean needsUpdate;

    private void updateRoute()
    {
        if(needsUpdate)
        {
            if(currentPositionGeo==null || currentPositionUTM==null)
                return;
            // gets the point and the distance
            calculateNearestLinePointGeo();
            //Calculate the bearing after the next best point is found
            calculateNearestBearing();
            needsUpdate = FALSE;
        }
    }

    public ArrayList<Point> getRoute() {
        return currentRoute;
    }

    public void setRoute(ArrayList<Point> route) {
        for (Point pt: route){
            currentRoute.add(pt);
        }

        currentRouteUTM.clear();
        for(Point pt : currentRoute)
        {
            WGS84 geo = new WGS84(pt.getX(),pt.getY());
            // Project to UTM
            UTM utm = new UTM(geo);
            Point utmPoint = new Point();
            utmPoint.setX(utm.getEasting());
            utmPoint.setY(utm.getNorthing());
            utmPoint.setZ(pt.getZ());
            currentRouteUTM.add(utmPoint);
        }
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
        currentRouteUTM = new ArrayList<Point>();
        currentRoute  = new ArrayList<Point>();
        setRoute(new ArrayList<Point>(route));
        currentBearing = 0;
        currentDistance = 0;
        needsUpdate = TRUE;

    }

    double pointToPointDistance(Point a, Point b)
    {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx*dx + dy*dy);
    }

    double pointToSegmentDistance(Point a, Point p1, Point p2)
    {
        double x21 = p2.getX() - p1.getX();
        double x01 = a.getX() - p1.getX();
        double y21 = p2.getY() - p1.getY();
        double y01 = a.getY() - p1.getY();

        double tn = x21*x01 + y21*y01;
        double td = x21*x21 + y21*y21;

        //	Negative t
        if (td < 0 != tn < 0) return Math.sqrt(x01*x01 + y01*y01);
        //	Less than 1
        if (td > tn)
        {
            double t = tn/td;
            double x = a.getX() - (p1.getX() + t*x21);
            double y = a.getY() - (p1.getY() + t*y21);
            return Math.sqrt(x*x + y*y);
        }
        else return pointToPointDistance(a,p2);
    }

    /**
     *
     * @return  The position (in geographic coordinates) of the nearest point on the line from
     *          the current position
     */
    private void calculateNearestLinePointGeo()
    {
        if(currentPositionGeo==null || currentPositionUTM==null)
            return;

        double SNAP_DISTANCE = 1;
        int numPoints = currentRoute.size();
        if(numPoints<2)
            return;

        //double closestVertDistance = pointToPointDistance(currentPositionUTM,currentRouteUTM.get(0));

        // 'current segment' here means the segment ending at 'nextIndex'
        // Starting with the segment ending at the current index, compare the distance
        // to each segment. If any of the segments have a shorter distance than the current segment distance, then advance to that segment
        // Special case: If within xxx meters of the 'nextIndex' vertex, keep going to the next vert until the distance is greated than the 'snap' distance
        for(int i=0;i<numPoints;i++)
        {
            double vertDistance = pointToPointDistance(currentPositionUTM,currentRouteUTM.get(i));
            if(vertDistance < SNAP_DISTANCE)
            {
                currentDistance = vertDistance;
                nextIndex = i;
                for(int j=i;j<numPoints;j++)
                {
                    // Continue along the route until the next point is further away than the snap distance
                    vertDistance = pointToPointDistance(currentPositionUTM,currentRouteUTM.get(i));
                    if(vertDistance >= SNAP_DISTANCE)
                    {
                        return;
                    }
                    else
                    {
                        currentDistance = vertDistance;
                        nextIndex = i;
                    }
                }
                // We've snapped to a vert, so skip the segment distance test
                return;
            }
        }

        // We're going to assume that we never go backwards. So keep navigating to the nextIndex until
        // the next segment length is closer


        if(nextIndex==0)
        {
            currentDistance = pointToPointDistance(currentPositionUTM,currentRouteUTM.get(0));
        }
        else
        {
            currentDistance = pointToSegmentDistance(currentPositionUTM,currentRouteUTM.get(nextIndex-1),currentRouteUTM.get(nextIndex));
        }

        for(int i=nextIndex;i<numPoints;i++)
        {
            if(i>0)
            {
                double distanceToSegment = pointToSegmentDistance(currentPositionUTM,currentRouteUTM.get(i-1),currentRouteUTM.get(i));
                if(currentDistance>distanceToSegment)
                {
                    nextIndex = i;
                    currentDistance = distanceToSegment;
                }
            }
        }

    }

    private void calculateNearestBearing()
    {
        if(currentPositionGeo==null || currentPositionUTM==null || currentRoute.size()<2)
            return;
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
        WGS84 geo = new WGS84(longitude,latitude);
        // Project to UTM
        UTM utm = new UTM(geo);
        currentPositionUTM = new Point(utm.getEasting(),utm.getNorthing());
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
        return currentDistance;//Meters
    }

}
