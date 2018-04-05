package net.cognitics.navapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageDatabase;

import static java.lang.Boolean.TRUE;

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
        return TRUE; //the SOFWERX dataset doesn't have gpkg_extensions populated
        /*
        Cursor cursor = sqliteDb.rawQuery("select * from gpkg_extensions WHERE extension_name='related_tables'",null);
        if(cursor.getCount()>0)
            return Boolean.TRUE;
        return Boolean.FALSE;
        */
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
                while (!cursor.isAfterLast()) {
                    RelatedTablesRelationship relationship = new RelatedTablesRelationship();
                    int idx = cursor.getColumnIndex("base_primary_column");
                    if(idx!=-1)
                        relationship.baseTableColumn = cursor.getString((idx));
                    idx = cursor.getColumnIndex("related_table_name");
                    if(idx!=-1)
                        relationship.relatedTableName = cursor.getString((idx));
                    idx = cursor.getColumnIndex("related_primary_column");
                    if(idx!=-1)
                        relationship.relatedTableColumn = cursor.getString((idx));
                    idx = cursor.getColumnIndex("relation_name");
                    if(idx!=-1)
                        relationship.relationshipName = cursor.getString((idx));
                    idx = cursor.getColumnIndex("mapping_table_name");
                    if(idx!=-1)
                        relationship.mappingTableName = cursor.getString((idx));
                    relationship.baseTableName = layer;
                    relationships.add(relationship);
                    cursor.moveToNext();
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
                while (!cursor.isAfterLast()) {
                    fids.add(cursor.getInt(0));
                    cursor.moveToNext();
                }

            } finally {
                cursor.close();
            }
        }
        return fids;
    }
}
