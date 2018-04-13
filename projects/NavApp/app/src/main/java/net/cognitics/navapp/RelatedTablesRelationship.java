package net.cognitics.navapp;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kbentley on 3/10/2018.
 *
 * This class holds the information about a GeoPackage Related Tables Extensions relationship
 */

public class RelatedTablesRelationship {
    public String baseTableName;
    public String baseTableColumn;
    public String relatedTableName;
    public String relatedTableColumn;
    public String relationshipName;
    public String mappingTableName;

    //public HashMap<Integer,ArrayList<Integer>> mediaRelationships = new HashMap<Integer,ArrayList<Integer>>();
}
