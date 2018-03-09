package net.cognitics.navapp;
import mil.nga.wkb.geom.Point;
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

    public static double getDistanceMeters(Point ptA, Point ptB)
    {
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
        //6,371,000 meter radius
        return 6371000 * c;
    }

    public static double getBearing(Point ptA, Point ptB)
    {
        //todo: implement bearing
        return 0;
    }
}
