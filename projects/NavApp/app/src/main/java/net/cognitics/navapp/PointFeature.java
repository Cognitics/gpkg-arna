package net.cognitics.navapp;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Map;

import mil.nga.wkb.geom.Point;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

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
        this.geoCoordinates = new WGS84(geoCoordinates.getLongitude(),geoCoordinates.getLatitude());
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
}
