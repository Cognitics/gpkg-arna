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

    // Feature ID
    private int fid;
    //attributes (key/value pairs, stored as a dictionary)
    private Map<String, String> attributes;

    // Will convert to UTM
    LineStringFeature(LineString line, Map<String, String> attributes)
    {
        utmLine = new LineString();
        this.attributes = attributes;
        for(Point pt : line.getPoints())
        {
            WGS84 geo = new WGS84(pt.getX(),pt.getY());
            // Project to UTM
            UTM utm = new UTM(geo);
            Point utmPoint = new Point();
            utmPoint.setX(utm.getEasting());
            utmPoint.setX(utm.getNorthing());
            utmPoint.setZ(pt.getZ());
            utmLine.addPoint(utmPoint);
        }

    }
}
