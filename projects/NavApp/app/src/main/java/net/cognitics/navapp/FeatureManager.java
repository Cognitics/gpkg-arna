package net.cognitics.navapp;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.MultiLineString;
import mil.nga.wkb.geom.MultiPoint;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by kbentley on 3/11/2018.
 */

public class FeatureManager {

    public ArrayList<PointFeature> cnpFeatures;
    public ArrayList<PointFeature> poiPointFeatures;
    public ArrayList<PointFeature> aoiPointFeatures;
    public ArrayList<LineStringFeature> routeFeatures;
    private GeoPackage gpkgDb;
    private GeoPackageManager manager;
    private Point geoCenter;
    FeatureManager(Context context)
    {

        cnpFeatures = new ArrayList<PointFeature>();
        poiPointFeatures = new ArrayList<PointFeature>();
        aoiPointFeatures = new ArrayList<PointFeature>();
        routeFeatures = new ArrayList<LineStringFeature>();
        gpkgDb = null;
        manager = GeoPackageFactory.getManager(context);
        geoCenter = new Point();
    }

    void ProcessPoint(Point pt, HashMap<String, String> attributes)
    {

    }

    void ProcessMultiPoint(MultiPoint mpt, HashMap<String, String> attributes)
    {
        for (Point pt: mpt.getPoints() ) {
            ProcessPoint(pt, attributes);
        }
    }

    Boolean open(String fileName) {
        File f = new File(fileName);
        String geoPackageName = f.getName();
        // Open a GeoPackage
        if (!manager.exists(geoPackageName)) {
            manager.importGeoPackage(geoPackageName, f);
        }
        gpkgDb = manager.open(geoPackageName);
        if (gpkgDb != null) {
            int numVerts = 0;
            double totalY = 0;
            double totalX = 0;

            List<String> tables = gpkgDb.getFeatureTables();

            for (String table : tables) {
                FeatureDao featureDao = gpkgDb.getFeatureDao(table);

                FeatureCursor featureCursor = featureDao.queryForAll();
                int numFeatures = featureCursor.getCount();

                try {
                    while (featureCursor.moveToNext()) {
                        FeatureRow featureRow = featureCursor.getRow();
                        String[] colNames = featureCursor.getColumnNames();
                        HashMap<String, String> attributes = new HashMap<String, String>();
                        // Just convert everything to a string for now.
                        for (String colName : colNames) {
                            if (colName != featureRow.getGeometryColumn().getName()) {
                                String attrName = featureRow.getColumn(colName).getName();
                                Object attrValue = featureRow.getValue(colName);
                                if(attrValue!=null)
                                    attributes.put(attrName, attrValue.toString());
                            }
                        }

                        GeoPackageGeometryData geometryData = featureRow.getGeometry();
                        Geometry geometry = geometryData.getGeometry();
                        if (geometry instanceof LineString) {
                            LineString line = (LineString) geometry;
                            for (Point pt : line.getPoints()) {
                                totalX += pt.getX();
                                totalY += pt.getY();
                                numVerts++;
                            }
                            LineStringFeature lineFeature = new LineStringFeature(line, attributes);
                            routeFeatures.add(lineFeature);
                        } else if(geometry instanceof MultiLineString) {
                            MultiLineString mls = (MultiLineString)geometry;
                            for(LineString line: mls.getGeometries())
                            {
                                for (Point pt : line.getPoints()) {
                                    totalX += pt.getX();
                                    totalY += pt.getY();
                                    numVerts++;
                                }
                                LineStringFeature lineFeature = new LineStringFeature(line, attributes);
                                routeFeatures.add(lineFeature);
                            }

                        } else if (geometry instanceof Point) {
                            Point pt = (Point) geometry;
                            PointFeature feature = new PointFeature(new WGS84(pt.getY(), pt.getX()), 0);
                            feature.setAttributes(attributes);
                            if (table.startsWith("cnp_")) {
                                cnpFeatures.add(feature);
                            } else if (table.startsWith("poi_")) {
                                poiPointFeatures.add(feature);
                            } else if (table.startsWith("aoi_")) {
                                aoiPointFeatures.add(feature);
                            }
                            totalX += pt.getX();
                            totalY += pt.getY();
                            numVerts++;
                        } else if (geometry instanceof  MultiPoint)
                        {
                          MultiPoint mp = (MultiPoint)geometry;
                          for(Point pt : mp.getGeometries()) {
                                PointFeature feature = new PointFeature(new WGS84(pt.getY(), pt.getX()), 0);
                                feature.setAttributes(attributes);
                                if (table.startsWith("cnp_")) {
                                    cnpFeatures.add(feature);
                                } else if (table.startsWith("poi_")) {
                                    poiPointFeatures.add(feature);
                                } else if (table.startsWith("aoi_")) {
                                    aoiPointFeatures.add(feature);
                                }
                                totalX += pt.getX();
                                totalY += pt.getY();
                                numVerts++;
                            }
                        } else if (geometry instanceof Polygon) {
                            Polygon poly = (Polygon) geometry;
                            for (LineString line : poly.getRings()) {
                                for (Point pt : line.getPoints()) {
                                    totalX += pt.getX();
                                    totalY += pt.getY();
                                    numVerts++;
                                }
                            }
                        }
                    }
                } finally {
                    featureCursor.close();
                }

            }
            geoCenter.setX(totalX / numVerts);
            geoCenter.setY(totalY / numVerts);

        } else {
            return FALSE;
        }
        return FALSE;
    }

    public Point getGeoCenter() {
        return geoCenter;
    }
}
