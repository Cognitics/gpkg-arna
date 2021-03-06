package net.cognitics.navapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.db.GeoPackageDatabase;
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

    public ArrayList<PointFeature> getPoiPointFeatures() {
        return poiPointFeatures;
    }

    public ArrayList<PointFeature> poiPointFeatures;
    public ArrayList<PointFeature> aoiPointFeatures;
    public ArrayList<LineStringFeature> routeFeatures;

    public ArrayList<RelatedTablesRelationship> getCnpRelationships() {
        return cnpRelationships;
    }

    public ArrayList<RelatedTablesRelationship> getPoiRelationships() {
        return poiRelationships;
    }

    public ArrayList<RelatedTablesRelationship> getAoiRelationships() {
        return aoiRelationships;
    }

    public ArrayList<RelatedTablesRelationship> getRouteRelationships() {
        return routeRelationships;
    }

    public GeoPackageRelatedTables getRelatedTablesManager() {
        return relatedTablesManager;
    }

    public ArrayList<RelatedTablesRelationship> cnpRelationships;
    public ArrayList<RelatedTablesRelationship> poiRelationships;
    public ArrayList<RelatedTablesRelationship> aoiRelationships;
    public ArrayList<RelatedTablesRelationship> routeRelationships;

    private GeoPackage geopackage;
    private GeoPackageDatabase gpkgDb;
    private SQLiteDatabase sqliteDb;
    private GeoPackageManager manager;
    private Point geoCenter;
    private Context context;
    private GeoPackageRelatedTables relatedTablesManager;
    private final int CNP_TEST_REQUEST = 1;
    private File geoPackageFile;

    public ArrayList<PointFeature> getAoiPointFeatures() {
        return aoiPointFeatures;
    }
    public RouteManager getRouteManager() {
        return routeManager;
    }

    private RouteManager routeManager;

    public FeatureManager(Context context) {
        this.context = context;
        cnpFeatures = new ArrayList<PointFeature>();
        poiPointFeatures = new ArrayList<PointFeature>();
        aoiPointFeatures = new ArrayList<PointFeature>();
        routeFeatures = new ArrayList<LineStringFeature>();
        geopackage = null;
        manager = GeoPackageFactory.getManager(context);
        geoCenter = new Point();
        routeManager = null;
    }

    private void processRoute(LineString ls, String table, HashMap<String, String> attributes, int fid) {
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
            routeManager = new RouteManager(routePoints, attributes, fid, context);
        }
        geoCenter.setX(totalX / numVerts);
        geoCenter.setY(totalY / numVerts);
    }

    public ArrayList<String> getRoutes() {
        ArrayList<String> tables = new ArrayList<String>();
        if (geopackage != null) {
            SQLiteDatabase sqliteDb = gpkgDb.getDb();

            Cursor cursor = sqliteDb.rawQuery("select * from gpkg_contents where table_name like 'route_%'", null);
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    int idx = cursor.getColumnIndex("table_name");
                    tables.add(cursor.getString(idx));
                    cursor.moveToNext();
                }
            } finally {
                cursor.close();
            }

        }
        return tables;
    }

    private void addMediaRelationship(String route, String table)
    {
        // Add a media table and a relationship for photos
        String mediaTable = route + "_photos";
        relatedTablesManager.addMediaTable(mediaTable);
        // Add the relationship table
        RelatedTablesRelationship relationship = new RelatedTablesRelationship();
        relationship.baseTableName = table;
        relationship.baseTableColumn = "fid";
        relationship.relatedTableName = mediaTable;
        relationship.relatedTableColumn = "id";
        relationship.relationshipName = "media";
        relationship.mappingTableName = table + "_photos";
        relatedTablesManager.addRelationship(relationship);
        save();
        String msg = String.format("Created media relationship for " + route);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public Boolean initializeRoute(String route) {
        if (geopackage != null) {
            gpkgDb = geopackage.getConnection().getDb();
            int numVerts = 0;
            double totalY = 0;
            double totalX = 0;

            // clear old route features
            aoiPointFeatures.clear();
            cnpFeatures.clear();
            routeFeatures.clear();
            poiPointFeatures.clear();
            cnpRelationships = new ArrayList<>();
            poiRelationships = new ArrayList<>();
            aoiRelationships = new ArrayList<>();
            routeRelationships = new ArrayList<>();

            List<String> tables = geopackage.getFeatureTables();


            for (String table : tables) {
                // Only load tables matching the route name (E.g. aoi_xxx where xxx is the route name)
                int offset = table.length() - route.length();
                if (offset < 1) {
                    continue;
                }
                String tableRouteName = table.substring(offset);
                if (!tableRouteName.equalsIgnoreCase(route))
                    continue;

                if (table.startsWith("cnp_")) {
                    cnpRelationships = relatedTablesManager.getRelationships(table);
                    if(cnpRelationships.size()>0) {
                        String msg = String.format(Locale.US,"Found %d relationships for %s", cnpRelationships.size(), table);
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        addMediaRelationship(route,table);
                    }
                } else if (table.startsWith("poi_")) {
                    poiRelationships = relatedTablesManager.getRelationships(table);
                    if(poiRelationships.size()>0) {
                        String msg = String.format(Locale.US,"Found %d relationships for %s", poiRelationships.size(), table);
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        addMediaRelationship(route,table);
                    }
                } else if (table.startsWith("aoi_")) {
                    aoiRelationships = relatedTablesManager.getRelationships(table);
                    if(aoiRelationships.size()>0) {
                        String msg = String.format("Found %d relationships for %s", aoiRelationships.size(), table);
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }else
                    {
                        addMediaRelationship(route,table);
                    }
                } else if (table.startsWith("route_")) {
                    routeRelationships = relatedTablesManager.getRelationships(table);
                    if(routeRelationships.size()>0) {
                        String msg = String.format("Found %d relationships for %s", routeRelationships.size(), table);
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }
                }

                FeatureDao featureDao = geopackage.getFeatureDao(table);

                FeatureCursor featureCursor = featureDao.queryForAll();
                //int numFeatures = featureCursor.getCount();

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
                                if (attrValue != null)
                                    attributes.put(attrName, attrValue.toString());
                                if (colName.equalsIgnoreCase("fid")) {
                                    // Weird double conversion here to make sure the
                                    // fid is parsed no matter what it's type
                                    if (attrValue != null)
                                        fid = Integer.valueOf(attrValue.toString());
                                }
                            }
                        }


                        GeoPackageGeometryData geometryData = featureRow.getGeometry();
                        Geometry geometry = geometryData.getGeometry();
                        if (geometry instanceof LineString) {
                            LineString line = (LineString) geometry;
                            processRoute(line, table, attributes, fid);

                        } else if (geometry instanceof MultiLineString) {
                            MultiLineString mls = (MultiLineString) geometry;
                            for (LineString line : mls.getGeometries()) {
                                processRoute(line, table, attributes, fid);
                            }

                        } else if (geometry instanceof Point) {
                            Point pt = (Point) geometry;
                            PointFeature feature = new PointFeature(new WGS84(pt.getY(), pt.getX()), fid, table);
                            feature.setAttributes(attributes);
                            if (table.startsWith("cnp_")) {
                                cnpFeatures.add(feature);
                                if(cnpRelationships.size()>0) {
                                    //ArrayList<Integer> fids = relatedTablesManager.getRelatedFIDs(cnpRelationships.get(0),fid);

                                    /*
                                    The use case is that when the CNP changes, the MainActivity needs to update the current display
                                    ...so when nav point changes...
                                    ......get the media from the point feature
                                    .........point feature needs to know where the media is (Relationship and fid)
                                    feature manage has a get media (PointFeature) method

                                    table_name (e.g. cnp_meridian) -> relationship (contains HashMap<int,ArrayList<int>>
                                    which means that each relationship object contains the entire map of related table entries


                                     */

                                }
                            } else if (table.startsWith("poi_")) {
                                poiPointFeatures.add(feature);
                            } else if (table.startsWith("aoi_")) {
                                aoiPointFeatures.add(feature);
                            }
                            totalX += pt.getX();
                            totalY += pt.getY();
                            numVerts++;
                        } else if (geometry instanceof MultiPoint) {
                            MultiPoint mp = (MultiPoint) geometry;
                            for (Point pt : mp.getGeometries()) {
                                PointFeature feature = new PointFeature(new WGS84(pt.getY(), pt.getX()), fid, table);
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
            if(cnpFeatures.size() > 0) {
                routeManager.associateCriticalNavigationPoints(cnpFeatures);
            }

        } else {
            return FALSE;
        }
        return FALSE;
    }

    public Boolean open(String fileName) {

        geoPackageFile = new File(fileName);
        //geoPackageFile.getPath();
        String geoPackageName = geoPackageFile.getName();
        // Open a GeoPackage, we won't use cache for now (during development
        // in case it's been updated, and because there isn't a big advantage to doing so at the moment
        if (manager.exists(geoPackageName)) {
            manager.delete(geoPackageName);
        }
        manager.importGeoPackage(geoPackageName, geoPackageFile);

        geopackage = manager.open(geoPackageName);
        if(geopackage!=null) {
            gpkgDb = geopackage.getConnection().getDb();
            sqliteDb = gpkgDb.getDb();
            relatedTablesManager = new GeoPackageRelatedTables(geopackage);
            return TRUE;
        }
        return FALSE;
    }

    public Boolean save()
    {
        try {
            String myParent = geoPackageFile.getParent();
            manager.exportGeoPackage(geoPackageFile.getName(), new File(myParent));
        }
        catch (mil.nga.geopackage.GeoPackageException ex) {
            Toast.makeText(context,"Unable to save GeoPackage. This session will be read-only.",Toast.LENGTH_LONG);
        }
        return TRUE;
    }
    public Point getGeoCenter() {
        return geoCenter;
    }

    public class FeatureMedia {
        public RelatedTablesRelationship relationship; // where it came from
        public String contentType; // from the media table
        public byte[] blob;
    }
    // create an inner class for related media
    // fid, byte array, content type, relationship....?

    public ArrayList<FeatureMedia> getRelatedMedia(PointFeature feature) {
        ArrayList<FeatureMedia> relatedMedia = new ArrayList<>();
        ArrayList<RelatedTablesRelationship> relationships =
            relatedTablesManager.getRelationships(feature.getLayerName());
        ArrayList<FeatureMedia> media = new ArrayList<>();
        for(RelatedTablesRelationship relationship : relationships) {
            relatedMedia.addAll(getMediaBlobs(relationship,feature.getFid()));
        }
        return relatedMedia;
    }

    public ArrayList<FeatureMedia> getMediaBlobs(ArrayList<RelatedTablesRelationship> relationships, String contentType, int featureFid) {
        ArrayList<FeatureMedia> mediaArrayList = new ArrayList<>();
        for(RelatedTablesRelationship relationship : relationships) {
            //select * from photos left join cnp_tampa_photos on cnp_tampa_photos.related_id=photos.id where cnp_tampa_photos.base_id=4 AND content_type='image/jpeg'
            String queryString = String.format("select * from %s left join %s on %s.related_id=%s.%s where %s.base_id=%d AND content_type='%s'",
                    relationship.relatedTableName,
                    relationship.mappingTableName,
                    relationship.mappingTableName,
                    relationship.relatedTableName,
                    relationship.relatedTableColumn,
                    relationship.mappingTableName,
                    featureFid,
                    contentType);

            /*String queryString = String.format(Locale.US ,"select %s.*,%s.%s from %s left join %s on %s.%s=%s.%s where %s.%s=%d AND content_type='%s'",
                    relationship.relatedTableName,
                    relationship.baseTableName,
                    relationship.baseTableColumn,
                    relationship.relatedTableName,
                    relationship.baseTableName,
                    relationship.baseTableName,
                    relationship.baseTableColumn,
                    relationship.relatedTableName,
                    relationship.relatedTableColumn,
                    relationship.baseTableName,
                    relationship.baseTableColumn,
                    featureFid,
                    contentType
            );*/

            SQLiteDatabase sqliteDb = gpkgDb.getDb();

            Cursor cursor = sqliteDb.rawQuery(queryString, null);
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    //int fididx = cursor.getColumnIndex("fid");
                    int blobidx = cursor.getColumnIndex("data");
                    int contentidx = cursor.getColumnIndex("content_type");
                    if (blobidx != -1 && contentidx != -1) {
                        String rowContentType = cursor.getString(contentidx);
                        if(rowContentType.equals(contentType)) {
                            byte[] blob = cursor.getBlob(blobidx);
                            FeatureMedia media = new FeatureMedia();
                            media.blob = blob;
                            media.relationship = relationship;
                            media.contentType = contentType;
                            mediaArrayList.add(media);
                        }
                    }
                    cursor.moveToNext();
                }
            } finally {
                cursor.close();
            }
        }
        return mediaArrayList;
    }

    public ArrayList<FeatureMedia> getMediaBlobs(RelatedTablesRelationship relationship, int featureFid) {
        ArrayList<FeatureMedia> mediaArrayList = new ArrayList<>();
        // For now, just show relationships for the first type of relationship there is.

        //GeoPackageRelatedTables gpkgRTE = new GeoPackageRelatedTables(geopackage);

        //select photos.*,cnp_tampa.fid from photos left join cnp_tampa on cnp_tampa.fid=photos.id
        String queryString = String.format(Locale.US ,"select %s.*,%s.%s from %s left join %s on %s.%s=%s.%s where %s.%s=%d",
                relationship.relatedTableName,
                relationship.baseTableName,
                relationship.baseTableColumn,
                relationship.relatedTableName,
                relationship.baseTableName,
                relationship.baseTableName,
                relationship.baseTableColumn,
                relationship.relatedTableName,
                relationship.relatedTableColumn,
                relationship.baseTableName,
                relationship.baseTableColumn,
                featureFid
        );

        SQLiteDatabase sqliteDb = gpkgDb.getDb();

        Cursor cursor = sqliteDb.rawQuery(queryString, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int fididx = cursor.getColumnIndex("fid");
                int blobidx = cursor.getColumnIndex("data");
                int contentidx = cursor.getColumnIndex("content_type");
                if (fididx != -1 && blobidx != -1) {
                    String contentType = cursor.getString(contentidx);
                    int fid = cursor.getInt(fididx);
                    byte[] blob = cursor.getBlob(blobidx);
                    FeatureMedia media = new FeatureMedia();
                    media.blob = blob;
                    media.relationship = relationship;
                    media.contentType = contentType;
                    mediaArrayList.add(media);
                }
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return mediaArrayList;
    }

    // This is not very generic for now, it's adding a photo to a table. It could
    // be that the specific table we choose has something other than images, but this should still work
    // Future enhancements will make it more generic.
    public int addRelatedMedia(PointFeature feature,byte[] blob)
    {
        ArrayList<RelatedTablesRelationship> relationships =
                relatedTablesManager.getRelationships(feature.getLayerName());
        //which relationship to use? For now we'll assume there is only one.
        GeoPackageRelatedTables gpkgRTE = new GeoPackageRelatedTables(geopackage);
        int mediaID = gpkgRTE.addMedia(relationships.get(0).relatedTableName,blob,"image/png");
        gpkgRTE.addFeatureRelationship(relationships.get(0),feature.getFid(),mediaID);
        // Bad for performance, but we use it here to make it easy to push changes
        // back to the SD card without worrying about when the app exits, etc.
        // todo: Make export/write to SD smarter, or possibly put a export button on
        save();
        return mediaID;
    }

    public ArrayList<RelatedTablesImageDialog.Row> relatedFeaturesTest() {
        ArrayList<RelatedTablesImageDialog.Row> rows = new ArrayList<>();
        // For now, just show relationships for the first type of relationship there is.
        if (cnpRelationships.size() > 0) {
            RelatedTablesRelationship relationship = cnpRelationships.get(0);

            GeoPackageRelatedTables gpkgRTE = new GeoPackageRelatedTables(geopackage);


            for (PointFeature pt : cnpFeatures) {
                //ArrayList<Integer> fids = gpkgRTE.getRelatedFIDs(relationship, pt.getFid());
                // Query for the data the hard way. It would be cleaner and probably faster
                // to do a join against the tables
                //select photos.*,cnp_tampa.fid from photos left join cnp_tampa on cnp_tampa.fid=photos.id
                String queryString = String.format(Locale.US ,"select %s.*,%s.%s from %s left join %s on %s.%s=%s.%s where %s.%s=%d",
                        relationship.relatedTableName,
                        relationship.baseTableName,
                        relationship.baseTableColumn,
                        relationship.relatedTableName,
                        relationship.baseTableName,
                        relationship.baseTableName,
                        relationship.baseTableColumn,
                        relationship.relatedTableName,
                        relationship.relatedTableColumn,
                        relationship.baseTableName,
                        relationship.baseTableColumn,
                        pt.getFid()
                );

                SQLiteDatabase sqliteDb = gpkgDb.getDb();

                Cursor cursor = sqliteDb.rawQuery(queryString, null);
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        int fididx = cursor.getColumnIndex("fid");
                        int blobidx = cursor.getColumnIndex("data");
                        if (fididx != -1 && blobidx != -1) {
                            int fid = cursor.getInt(fididx);
                            byte[] blob = cursor.getBlob(blobidx);
                            RelatedTablesImageDialog.Row row = new RelatedTablesImageDialog.Row(fid, blob);
                            rows.add(row);
                            return rows;
                        }
                        cursor.moveToNext();
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return rows;
    }

    public void addMediaTable(String layerName)
    {
        // If not exist...
        String query = "CREATE TABLE IF NOT EXISTS '" + layerName + "' ( id INTEGER PRIMARY KEY AUTOINCREMENT, data BLOB NOT NULL, content_type TEXT NOT NULL )";
        SQLiteDatabase sqliteDb = gpkgDb.getDb();

        sqliteDb.execSQL(query);

    }


}
