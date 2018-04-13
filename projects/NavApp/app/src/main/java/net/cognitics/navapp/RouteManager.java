package net.cognitics.navapp;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mil.nga.wkb.geom.Point;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by kbentley on 3/13/2018.
 * <p>
 * Reference to the current route (i.e linestring)
 * Holds the current position (geographic coordinates)
 * Accessors for bearing and distance to next route vert
 */

public class RouteManager {

    private Context context;
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

    private int fid = 0;//feature id from the geopackage. Should always be 0

    private HashMap<String, String> attributes;

    private Boolean offRoute = FALSE;
    private Point routeInterceptPoint;
    private Boolean autoRouteMode = TRUE;//If false, waypoints have to be set manually
    // This maps an index into the route to a Point feature (CNP)
    // The association is made by finding the closest vert in the route
    // to each CNP. There is a field in the sample data that has an id
    // which might be doing the same thing, but instead of assuming that it is,
    // we'll just use distance.
    private Map<Integer,PointFeature> criticalNavigationPoints;

    // each route vert will end up with an association of the closest CNP (only one)
    public void associateCriticalNavigationPoints(ArrayList<PointFeature> points)
    {
        if(points.size()==0)
            return;

        for(int i=0;i<currentRouteUTM.size();i++) {
            int closestIdx = 0;
            PointFeature closestCNP = points.get(0);
            double closestDistance = Double.MAX_VALUE;
            for(int j=0;j<points.size();j++) {
                PointFeature cnp = points.get(j);
                Point cnpPoint = new Point(cnp.getUtmCoordinates().getEasting(), cnp.getUtmCoordinates().getNorthing());
                double dist = pointToPointDistance(currentRouteUTM.get(i),cnpPoint);
                if(dist < closestDistance) {
                    closestDistance = dist;
                    closestIdx = i;
                    closestCNP = cnp;
                }
            }
            criticalNavigationPoints.put(closestIdx,closestCNP);
        }
    }

    /**
     * Gets the nearest CNP associated with the current position on the route.
     * @return
     */
    public PointFeature getCurrentCNP()
    {
        if(criticalNavigationPoints.containsKey(nextIndex)) {
            return criticalNavigationPoints.get(nextIndex);
        }
        else
        {
            return null;
        }
    }

    public int getCurrentCNPID()
    {
        return nextIndex;
    }

    RouteManager(ArrayList<Point> route, HashMap<String, String> attributes, int fid, Context context) {
        currentRouteUTM = new ArrayList<Point>();
        currentRoute = new ArrayList<Point>();
        setRoute(new ArrayList<Point>(route));
        currentBearing = 0;
        currentDistance = 0;
        needsUpdate = TRUE;
        this.attributes = attributes;
        this.fid = fid;
        this.context = context;
        criticalNavigationPoints = new HashMap<>();
    }

    public Boolean getAutoRouteMode() {
        return autoRouteMode;
    }

    public void setAutoRouteMode(Boolean autoRouteMode) {
        this.autoRouteMode = autoRouteMode;
    }

    private void updateRoute() {
        if (needsUpdate) {
            if (currentPositionGeo == null || currentPositionUTM == null)
                return;
            if (autoRouteMode) {
                // gets the point and the distance
                calculateNearestLinePointGeo();
            }
            //Calculate the bearing after the next best point is found
            calculateNearestBearing();
            needsUpdate = FALSE;
        }
    }

    public ArrayList<Point> getRoute() {
        return currentRoute;
    }

    public void setRoute(ArrayList<Point> route) {
        for (Point pt : route) {
            currentRoute.add(pt);
        }

        currentRouteUTM.clear();
        for (Point pt : currentRoute) {
            WGS84 geo = new WGS84(pt.getX(), pt.getY());
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
     *
     * @return index into route
     */
    public int getNextIndex() {
        return nextIndex;
    }

    /**
     * Finds the point on a line segment (lineA->lineB) that is nearest to pt.
     *
     * @param lineA The first vert on the segment
     * @param lineB The second vert on the segment
     * @param pt    The vert to start from
     * @return A point on the segment (lineA->lineB) that is nearest to pt.
     */
    private Point findInterceptPoint(Point lineA, Point lineB, Point pt) {
        Point outputPt;
        lineA.setZ(0.0);
        lineB.setZ(0.0);
        pt.setZ(0.0);

        Point lineDelta = PointMath.subtract(lineB, lineA);
        double mag2 = PointMath.length2(lineDelta);

        double u = (((pt.getX() - lineA.getX()) * (lineB.getX() - lineA.getX())) +
                ((pt.getY() - lineA.getY()) * (lineB.getY() - lineA.getY()))) / mag2;

        if (u < 0.0f) {
            outputPt = lineA;
        } else if (u > 1.0f) {
            outputPt = lineB;
        } else {
            outputPt = PointMath.add(lineA, (PointMath.multiply(lineDelta, u)));
        }

        // get the distance
        //Point distvec = (PointMath.subtract(lineA,outputPt);
        //double dist = PointMath.length2(distvec);
        return outputPt;
    }

    private double pointToPointDistance(Point a, Point b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double pointToSegmentDistance(Point a, Point p1, Point p2) {
        double x21 = p2.getX() - p1.getX();
        double x01 = a.getX() - p1.getX();
        double y21 = p2.getY() - p1.getY();
        double y01 = a.getY() - p1.getY();

        double tn = x21 * x01 + y21 * y01;
        double td = x21 * x21 + y21 * y21;

        //	Negative t
        if (td < 0 != tn < 0) return Math.sqrt(x01 * x01 + y01 * y01);
        //	Less than 1
        if (td > tn) {
            double t = tn / td;
            double x = a.getX() - (p1.getX() + t * x21);
            double y = a.getY() - (p1.getY() + t * y21);
            return Math.sqrt(x * x + y * y);
        } else return pointToPointDistance(a, p2);
    }

    /**
     * Calculates the position (in geographic coordinates) of the nearest point on the line from
     * the current position
     */
    private void calculateNearestLinePointGeo() {
        if (currentPositionGeo == null || currentPositionUTM == null)
            return;

        // The distance you are allowed to be off the route before it puts you on a path to the nearest point on the route from your current location
        int numPoints = currentRoute.size();
        if (numPoints < 2)
            return;

        double testValue = 0;

        double distanceToSegment;
        if (nextIndex == 0 && !MainActivity.prefRouteFromStart) {
            double closestSegmentDistance = Double.MAX_VALUE;
            // Special case when starting a route...find the nearest segment to the current position and set the route there,
            // so the user isn't always navigated to the start of the route.
            for (int i = 0; i < (numPoints - 1); i++) {
                double dist = pointToSegmentDistance(currentPositionUTM, currentRouteUTM.get(i), currentRouteUTM.get(i + 1));
                if (dist < closestSegmentDistance) {
                    nextIndex = i + 1;
                    closestSegmentDistance = dist;
                }

            }
            distanceToSegment = closestSegmentDistance;
        } else if(nextIndex==0) {
            // We're in always start at 0 mode, and at index 0, so we use that navpint as the route.
            distanceToSegment = pointToPointDistance(currentPositionUTM,currentRouteUTM.get(0));
        }
        else
            distanceToSegment = pointToSegmentDistance(currentPositionUTM, currentRouteUTM.get(nextIndex - 1), currentRouteUTM.get(nextIndex));
        if (distanceToSegment > MainActivity.prefCNPOffRouteDistance) {
            offRoute = TRUE;
            if (nextIndex == 0)
                routeInterceptPoint = findInterceptPoint(currentRouteUTM.get(nextIndex), currentRouteUTM.get(nextIndex + 1), currentPositionUTM);
            else
                routeInterceptPoint = findInterceptPoint(currentRouteUTM.get(nextIndex - 1), currentRouteUTM.get(nextIndex), currentPositionUTM);
            currentDistance = pointToPointDistance(currentPositionUTM, routeInterceptPoint);
            return;
        } else {
            routeInterceptPoint = null;
            offRoute = FALSE;
            currentDistance = pointToPointDistance(currentPositionUTM, currentRouteUTM.get(nextIndex));
        }


        //double closestVertDistance = pointToPointDistance(currentPositionUTM,currentRouteUTM.get(0));

        // 'current segment' here means the segment ending at 'nextIndex'
        // Starting with the segment ending at the current index, compare the distance
        // to each segment. If any of the segments have a shorter distance than the current segment distance, then advance to that segment
        // Special case: If within xxx meters of the 'nextIndex' vertex, keep going to the next vert until the distance is greated than the 'snap' distance
        Boolean snappedToVert = false;
        for (int i = nextIndex; i < numPoints; i++) {
            double vertDistance = pointToPointDistance(currentPositionUTM, currentRouteUTM.get(i));
            if (vertDistance < MainActivity.prefCNPArrivalDistance) {
                snappedToVert = TRUE;
                currentDistance = vertDistance;
                nextIndex = i;
                for (int j = nextIndex; j < numPoints; j++) {
                    // Continue along the route until the next point is further away than the snap distance
                    vertDistance = pointToPointDistance(currentPositionUTM, currentRouteUTM.get(i));
                    if (vertDistance < currentDistance) {
                        currentDistance = vertDistance;
                        nextIndex = i;
                    }
                }
            }
        }
        if (snappedToVert) {
            // If we just snapped, advance to the next point
            nextIndex += 1;

            if (nextIndex > numPoints)
                Toast.makeText(context, "You have arrived at the destination!", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "Arrived at waypoint: " + (nextIndex - 1), Toast.LENGTH_LONG).show();
        }
    }

    public int rewindRoutePoint() {
        if (nextIndex != 0)
            nextIndex -= 1;
        return nextIndex;
    }

    public int advanceRoutePoint() {
        if ((nextIndex + 1) < currentRoute.size())
            nextIndex += 1;
        return nextIndex;
    }

    // If returns the next waypoint. If in autoroute mode, this will be either the next navigation
    // point along the route, or the nearest intercept point along that route.
    // When autoroute is disabled, this will be the current waypoint selected bby the user.
    private Point getCurrentWaypoint() {
        if (currentRoute.size() < 2)
            return null;
        if (this.autoRouteMode) {
            if (nextIndex >= 0 && nextIndex < currentRoute.size()) {
                if (!offRoute || (routeInterceptPoint != null))
                    return currentRoute.get(nextIndex);
                else
                    return routeInterceptPoint;

            } else {
                return currentRoute.get(currentRoute.size() - 1);
            }
        }

        return currentRoute.get(nextIndex);

    }

    private void calculateNearestBearing() {
        if (currentPositionGeo == null || currentPositionUTM == null || currentRoute.size() < 2)
            return;
        Point nextPoint = getCurrentWaypoint();
        currentBearing = GreatCircle.getBearing(currentPositionGeo, nextPoint);
    }

    //Get the geographic coordinates nearest the current position
    public Point getNearestRoutePointGeo() {
        updateRoute();
        return currentRoute.get(nextIndex);
    }

    //The nearest distance and bearing are only calculated here, so call this before getting those
    public void setCurrentPositionAndBearing(double latitude, double longitude, double elevation, double bearing) {
        currentPositionGeo = new Point(longitude, latitude);
        WGS84 geo = new WGS84(longitude, latitude);
        // Project to UTM
        UTM utm = new UTM(geo);
        currentPositionUTM = new Point(utm.getEasting(), utm.getNorthing());
        needsUpdate = TRUE;
    }

    double getNearestBearing() {
        updateRoute();

        return currentBearing;
    }

    double getNearestDistance() {
        updateRoute();
        return currentDistance;//Meters
    }

}
