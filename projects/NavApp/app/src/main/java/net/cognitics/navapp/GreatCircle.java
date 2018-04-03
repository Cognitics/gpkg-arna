package net.cognitics.navapp;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.util.GeometryUtils;

/**
 * Created by kbentley on 3/8/2018.
 */

public class GreatCircle {
    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    public static double getDistanceMiles(Point ptA, Point ptB)
    {
        return getDistanceMeters(ptA,ptB)/1609.34;
    }

    public static double getDistanceMeters(Point pt1, Point pt2)
    {
        Point ptA = new Point(pt1);
        Point ptB = new Point(pt2);
        ptA.setX(Math.toRadians(ptA.getX()));
        ptA.setY(Math.toRadians(ptA.getY()));
        ptB.setX(Math.toRadians(ptB.getX()));
        ptB.setY(Math.toRadians(ptB.getY()));
        double deltaLat  = ptB.getY() - ptA.getY();
        double deltaLong = ptB.getX() - ptA.getX();

        double startLat = ptA.getY();
        double endLat  = ptB.getY();

        double a = haversin(deltaLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(deltaLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371000 * c;
    }

    /*
     * Calculate the initial bearing from ptA to ptB. As one point moves this needs to be
     * recalculated because in the great circle, the bearing changes as you follow the shortest path
     */
    public static double getBearing(Point ptA, Point ptB)
    {
        if(getDistanceMeters(ptA,ptB)<10.0)
            return 0;//It doesn't make much sense to calculate a bearing when they are this close
        return _initial(ptA,ptB);
    }

    public static double _initial(Point ptA, Point ptB)
    {
        return (_bearing(ptA,ptB) + 360.0) % 360;

    }

    public static double _final(Point ptA, Point ptB)
    {
        return (_bearing(ptA,ptB) + 180.0) % 360;
    }

    public static double _bearing(Point ptA, Point ptB)
    {
        double phi1 = Math.toRadians(ptA.getY());
        double phi2 = Math.toRadians(ptB.getY());

        double lam1 = Math.toRadians(ptA.getX());
        double lam2 = Math.toRadians(ptB.getX());
        return Math.atan2(
                Math.sin(lam2-lam1)*Math.cos(phi2),
                Math.cos(phi1)*Math.sin(phi2) - Math.sin(phi1)*Math.cos(phi2)*Math.cos(lam2-lam1)
        ) * 180/Math.PI;


    }

}
