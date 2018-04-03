package net.cognitics.navapp;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
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

//todo: Add support for named routes/poi/cnp (e.g. route_tampa/cnp_tampa and route_meridian,cnp_meridian in the same db
    // todo: Attributes for routes

public class FeatureManager {

    public ArrayList<PointFeature> getCnpFeatures() {
        return cnpFeatures;
    }

    public ArrayList<PointFeature> cnpFeatures;
    public ArrayList<PointFeature> poiPointFeatures;

    public ArrayList<PointFeature> getAoiPointFeatures() {
        return aoiPointFeatures;
    }

    public ArrayList<PointFeature> aoiPointFeatures;
    public ArrayList<LineStringFeature> routeFeatures;
    private GeoPackage gpkgDb;
    private GeoPackageManager manager;
    private Point geoCenter;

    public RouteManager getRouteManager() {
        return routeManager;
    }

    private RouteManager routeManager;
    public FeatureManager(Context context)
    {

        cnpFeatures = new ArrayList<PointFeature>();
        poiPointFeatures = new ArrayList<PointFeature>();
        aoiPointFeatures = new ArrayList<PointFeature>();
        routeFeatures = new ArrayList<LineStringFeature>();
        gpkgDb = null;
        manager = GeoPackageFactory.getManager(context);
        geoCenter = new Point();
        routeManager = null;
    }

    private void processRoute(LineString ls, String table,HashMap<String, String> attributes,int fid)
    {
        int numVerts = 0;
        double totalX = 0;
        double totalY = 0;
        ArrayList<Point> routePoints = new ArrayList<Point>();
        for (Point pt : ls.getPoints()) {
            totalX += pt.getX();
            totalY += pt.getY();
            routePoints.add(pt);
            numVerts++;
        }
        if (table.startsWith("route_")) {
            routeManager = new RouteManager(routePoints,attributes,fid);
        }
        geoCenter.setX(totalX / numVerts);
        geoCenter.setY(totalY / numVerts);
    }


    public Boolean open(String fileName) {
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
                        int fid = 0;
                        FeatureRow featureRow = featureCursor.getRow();
                        String[] colNames = featureCursor.getColumnNames();
                        HashMap<String, String> attributes = new HashMap<String, String>();
                        // Just convert everything to a string for now.
                        for (String colName : colNames) {
                            if (!colName.equalsIgnoreCase(featureRow.getGeometryColumn().getName())) {

                                String attrName = featureRow.getColumn(colName).getName();
                                Object attrValue = featureRow.getValue(colName);
                                if(attrValue!=null)
                                    attributes.put(attrName, attrValue.toString());
                                if(colName.equalsIgnoreCase("fid"))
                                {
                                    // Weird double conversion here to make sure the
                                    // fid is parsed no matter what it's type
                                    if(attrValue!=null)
                                        fid = Integer.valueOf(attrValue.toString());
                                }
                            }
                        }


                        GeoPackageGeometryData geometryData = featureRow.getGeometry();
                        Geometry geometry = geometryData.getGeometry();
                        if (geometry instanceof LineString) {
                            LineString line = (LineString)geometry;
                            processRoute(line,table,attributes,fid);

                        } else if(geometry instanceof MultiLineString) {
                            MultiLineString mls = (MultiLineString)geometry;
                            for(LineString line: mls.getGeometries())
                            {
                                processRoute(line,table,attributes,fid);
                            }

                        } else if (geometry instanceof Point) {
                            Point pt = (Point) geometry;
                            PointFeature feature = new PointFeature(new WGS84(pt.getY(), pt.getX()), fid);
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
                                PointFeature feature = new PointFeature(new WGS84(pt.getY(), pt.getX()), fid);
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


        } else {
            return FALSE;
        }
        return FALSE;
    }

    public Point getGeoCenter() {
        return geoCenter;
    }
}
