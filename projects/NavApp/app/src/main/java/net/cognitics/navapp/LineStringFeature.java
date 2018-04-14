package net.cognitics.navapp;

import java.util.Map;

import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.Point;

/**
 * Created by kbentley on 3/11/2018.
 */

public class LineStringFeature {
    // Layer name (table in the geopackage file)
    private String layerName;
    private LineString utmLine;
    private LineString geoLine;

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        if(currentIndex <0)
            currentIndex = 0;
        if(currentIndex >= geoLine.numPoints())
            currentIndex = geoLine.numPoints();
        this.currentIndex = currentIndex;
    }

    /**
     * The index of the current position within the linestring. updated with calls to getNearestLinePointGeo()
     */
    private int currentIndex;
    // Feature ID
    private int fid;
    //attributes (key/value pairs, stored as a dictionary)
    private Map<String, String> attributes;

    /**
     *
     * @param line A LineString in geographic (latitude/longitude) coordinates.
     * @param attributes Key/Value pairs with any attributes needed by the client
     */
    LineStringFeature(LineString line, Map<String, String> attributes)
    {
        this.geoLine = line;
        utmLine = new LineString();
        this.attributes = attributes;
        for(Point pt : line.getPoints())
        {
            WGS84 geo = new WGS84(pt.getY(),pt.getX());
            // Project to UTM
            UTM utm = new UTM(geo);
            Point utmPoint = new Point();
            utmPoint.setX(utm.getEasting());
            utmPoint.setY(utm.getNorthing());
            utmPoint.setZ(pt.getZ());
            utmLine.addPoint(utmPoint);
        }

    }

    /**
     *
     * @param currentPosition The current position in geographic (latitude/longitude/elevation
     *                        coordinates. The currentIndex will be updated based on this
     *                        calculation
     * @return  The position (in geographic coordinates) of the nearest point on the line from
     *          the current position
     */
    Point getNearestLinePointGeo(Point currentPosition)
    {
        int numPoints = geoLine.getPoints().size();
        if(numPoints<2)
            return null;
        double nearestDistance =
                GreatCircle.getDistanceMeters(currentPosition, geoLine.getPoints().get(0));
        Point nearestPoint = geoLine.getPoints().get(0);
        for(int i=1;i<numPoints;i++)
        {
            double dist = GreatCircle.getDistanceMeters(currentPosition, geoLine.getPoints().get(i));
            if(dist < nearestDistance)
            {
                nearestDistance = dist;
                nearestPoint = geoLine.getPoints().get(i);
                currentIndex = i;
            }
        }
        return nearestPoint;
    }
}
