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

        Cursor cursor = sqliteDb.rawQuery("select * from gpkg_extensions WHERE extension_name='related_tables'",null);
        if(cursor.getCount()>0)
            return Boolean.TRUE;
        else
        {
            cursor = sqliteDb.rawQuery("select * from sqlite_master WHERE name='gpkgext_relations'",null);
            if(cursor.getCount()>0)
                return Boolean.TRUE;

        }
        return Boolean.FALSE;

    }

    /*
    // Get unique content types from related media, and return all the relationships
    // that have content of that type.
    ArrayList<RelatedTablesRelationship> getRelationships(String layer, String content_type) {
        ArrayList<RelatedTablesRelationship> contentSpecificRelationships = new ArrayList<>();
        ArrayList<RelatedTablesRelationship> relationships = getRelationships(layer);
        for(RelatedTablesRelationship relationship : relationships) {
            //if(relationship.relationshipName)
        }

        return contentSpecificRelationships;
    }
    */
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

    public void enableRelatedTablesExtension()
    {
        SQLiteDatabase sqliteDb = gpkgDb.getDb();
        // First make sure the table exists:
        String query = "CREATE TABLE IF NOT EXISTS 'gpkgext_relations' ( id INTEGER PRIMARY KEY AUTOINCREMENT, base_table_name TEXT NOT NULL, base_primary_column TEXT NOT NULL DEFAULT 'id', related_table_name TEXT NOT NULL, related_primary_column TEXT NOT NULL DEFAULT 'id', relation_name TEXT NOT NULL, mapping_table_name TEXT NOT NULL UNIQUE )";
        sqliteDb.execSQL(query);

    }
    public void addRelationship(RelatedTablesRelationship relationship)
    {
        enableRelatedTablesExtension();
        //todo: Do we assume the table does not exist, or do we make this work (not crash with a sql exception) if it does?
        SQLiteDatabase sqliteDb = gpkgDb.getDb();
        enableRelatedTablesExtension();

        // Create the mapping table
        String query = "CREATE TABLE IF NOT EXISTS '" + relationship.mappingTableName + "' ( base_id INTEGER NOT NULL, related_id INTEGER NOT NULL )";
        sqliteDb.execSQL(query);
        // Add to the gpkgext_relationships table
        query = "INSERT INTO gpkgext_relations (base_table_name, base_primary_column,related_table_name,related_primary_column,relation_name,mapping_table_name)";
        query += String.format("VALUES('%s','%s','%s','%s','%s','%s')",
                relationship.baseTableName,
                relationship.baseTableColumn,
                relationship.relatedTableName,
                relationship.relatedTableColumn,
                relationship.relationshipName,
                relationship.mappingTableName);
        Cursor cursor = sqliteDb.rawQuery(query, null);
        try {
            cursor.moveToFirst();
        } finally {
            cursor.close();
        }
        query = "INSERT INTO gpkg_extensions (table_name,extension_name,definition,scope) VALUES(";
        query += String.format("'%s','related_tables','Related Tables Mapping Table','read-write')",relationship.mappingTableName);
        cursor = sqliteDb.rawQuery(query, null);
        try {
            cursor.moveToFirst();
        } finally {
            cursor.close();
        }

    }
    // Get related tables for a given layer
    public ArrayList<Integer> getRelatedFIDs(RelatedTablesRelationship relationship, int fid)
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
    public void addFeatureRelationship(RelatedTablesRelationship relationship, int baseFID, int relatedFID)
    {
        String query = String.format("INSERT INTO %s (base_id,related_id) VALUES(%d,%d)");
        Cursor cursor = sqliteDb.rawQuery(query, null);
        try {
            cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // This table has a fixed schema, so add it in this class.
    public void addMediaTable(String name)
    {
        // Create the mapping table
        String query = "CREATE TABLE IF NOT EXISTS '" + name + "' ( id INTEGER PRIMARY KEY AUTOINCREMENT, data BLOB NOT NULL, content_type TEXT NOT NULL )";
        sqliteDb.execSQL(query);
    }

    /**
     *
     * @param mediaTable Name of the table to insert into
     * @param blob Binary data to insert into the 'data' column
     * @param contentType The type of content
     * @return
     */
    public int addMedia(String mediaTable, byte[] blob, String contentType)
    {
        String query = "INSERT INTO " + mediaTable + " (data,content_type) VALUES(?,?)";
        sqliteDb.execSQL(query, new Object[]{ blob, contentType });

        int fid = -1;
        Cursor cursor = sqliteDb.rawQuery("SELECT last_insert_rowid()", null);
        try {
            if(cursor.moveToFirst())
                fid = cursor.getInt(0);
        } finally {
            cursor.close();
        }
        return fid;
    }
}
