package net.cognitics.navapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageDatabase;

/**
 * Created by kbentley on 3/10/2018.
 * Manages related tables data
 */

public class GeoPackageRelatedTables {

    private GeoPackage gpkg;
    private GeoPackageDatabase gpkgDb;
    private SQLiteDatabase sqliteDb;
    private Boolean extensionInstalled;
    GeoPackageRelatedTables(GeoPackage geopackage)
    {
        gpkgDb = geopackage.getConnection().getDb();
        gpkg = geopackage;
        sqliteDb = gpkgDb.getDb();
        extensionInstalled = IsRelatedTablesInstalled();
    }

    public Boolean IsRelatedTablesInstalled()
    {
        Cursor cursor = sqliteDb.rawQuery("select * from gpkg_extensions WHERE extension_name='related_tables'",null);
        if(cursor.getCount()>0)
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    ArrayList<RelatedTablesRelationship> getRelationships(String layer)
    {
        ArrayList<RelatedTablesRelationship> relationships = new ArrayList<RelatedTablesRelationship>();
        if(extensionInstalled) {
            StringBuilder sb = new StringBuilder();
            sb.append("select * from gpkgext_relations WHERE base_table_name='");
            sb.append(layer);
            sb.append("'");
            Cursor cursor = sqliteDb.rawQuery(sb.toString(), null);
            try {
                cursor.moveToFirst();
                while (cursor.moveToNext()) {

                }
            } finally {
                cursor.close();
            }
        }
        return relationships;
    }

    // Get related tables for a given layer
    ArrayList<Integer> getRelatedFIDs(RelatedTablesRelationship relationship, int fid)
    {
        ArrayList<Integer> fids = new ArrayList();
        if(extensionInstalled) {
            StringBuilder sb = new StringBuilder();
            sb.append("select related_id from ");
            sb.append(relationship.mappingTableName);
            sb.append(" where base_id=");
            sb.append(fid);

            Cursor cursor = sqliteDb.rawQuery(sb.toString(), null);
            try {
                cursor.moveToFirst();
                while (cursor.moveToNext()) {
                    fids.add(cursor.getInt(0));
                }

            } finally {
                cursor.close();
            }
        }
        return fids;
    }
}
