package net.cognitics.navapp;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Map;

import mil.nga.wkb.geom.Point;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;


/**
 * Created by kbentley on 3/11/2018.
 */

public class PointFeature {
    // Layer name (table in the geopackage file)
    private String layerName;
    // Lat/lon
    private WGS84 geoCoordinates;
    // utm coords
    private UTM utmCoordinates;

    public int getFid() {
        return fid;
    }

    // Feature ID
    private int fid;
    //attributes (key/value pairs, stored as a dictionary)
    private Map<String, String> attributes;

    // Vertices for opengl rendering
    // Buffer for vertex-array
    private FloatBuffer vertexBuffer;
    // Buffer for index-array
    private ByteBuffer indexBuffer;

    PointFeature(WGS84 geoCoordinates, int fid)
    {
        this.fid = fid;
        this.geoCoordinates = new WGS84(geoCoordinates.getLatitude(),geoCoordinates.getLongitude());
        // Project to UTM
        utmCoordinates = new UTM(geoCoordinates);

        // todo: Generate a diamond or something to act as a placemark
    }

    public String getAttribute(String attributeName)
    {
        if(attributes.containsKey(attributeName))
        {
            return attributes.get(attributeName);
        }
        return null;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public WGS84 getGeoCoordinates() {
        return geoCoordinates;
    }

    public void setGeoCoordinates(WGS84 geoCoordinates) {
        this.geoCoordinates = geoCoordinates;
        utmCoordinates = new UTM(geoCoordinates);
    }

    public UTM getUtmCoordinates() {
        return utmCoordinates;
    }

    public void setUtmCoordinates(UTM utmCoordinates) {
        this.utmCoordinates = utmCoordinates;
        this.geoCoordinates = new WGS84(utmCoordinates);

    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public void render(GL10 gl)
    {

    }

    /**
     *  Returns the bearing from a specified point to this point feature
     * @param latitude The latitude of the position to get the bearing from
     * @param longitude
     * @param elevation
     * @return The bearing from the specified position to the point feature
     */
    public double getBearing(double latitude, double longitude, double elevation)
    {
        double bearing = 0;
        Point pta = new Point(longitude,latitude);
        Point ptb = new Point(geoCoordinates.getLongitude(),geoCoordinates.getLatitude());
        bearing = GreatCircle.getBearing(pta,ptb);

        return bearing;
    }

    public double getDistance(double latitude, double longitude, double elevation)
    {
        Point pta = new Point(longitude,latitude);
        Point ptb = new Point(geoCoordinates.getLongitude(),geoCoordinates.getLatitude());
        return GreatCircle.getDistanceMeters(pta,ptb);
    }
}
