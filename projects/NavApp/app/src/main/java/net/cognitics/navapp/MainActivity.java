package net.cognitics.navapp;
import android.view.View;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import java.io.File;

import mil.nga.geopackage.*;
import mil.nga.geopackage.factory.GeoPackageFactory;

public class MainActivity extends AppCompatActivity {
    GeoPackage gpkgDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = findViewById(R.id.openGpkg);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(v.getContext());
                dlgAlert.setMessage("This is an alert with no consequence");
                dlgAlert.setTitle("App Title");
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();
                // Open a GeoPackage
                //GeoPackageManager manager = GeoPackageFactory.getManager(this);
                //gpkgDb = manager.open("");
            }
        });

    }

    public void openGeoPackage()
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("This is an alert with no consequence");
        dlgAlert.setTitle("App Title");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
        // Open a GeoPackage
        GeoPackageManager manager = GeoPackageFactory.getManager(this);
        gpkgDb = manager.open("");


        return;
    }
}
