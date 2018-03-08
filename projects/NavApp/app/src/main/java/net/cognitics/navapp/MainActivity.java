package net.cognitics.navapp;
import android.view.View;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import java.io.File;

import mil.nga.geopackage.*;
import mil.nga.geopackage.factory.GeoPackageFactory;
import com.github.angads25.filepicker.*;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

public class MainActivity extends AppCompatActivity {
    GeoPackage gpkgDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = findViewById(R.id.openGpkg);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.FILE_SELECT;
                properties.root = new File(DialogConfigs.DEFAULT_DIR);
                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                properties.extensions = null;
                FilePickerDialog dialog = new FilePickerDialog(MainActivity.this,properties);
                dialog.setTitle("Select a File");
                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        //files is an array of the paths of files selected by the Application User.
                        if(files.length > 0) {
                            openGeoPackage(files[0]);
                        }
                    }
                });
                dialog.show();
                // Open a GeoPackage
                //GeoPackageManager manager = GeoPackageFactory.getManager(this);
                //gpkgDb = manager.open("");
            }
        });

    }

    public void openGeoPackage(String path)
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("File selected: " + path);
        dlgAlert.setTitle("Selected File");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
        // Open a GeoPackage
        //GeoPackageManager manager = GeoPackageFactory.getManager(this);
        //gpkgDb = manager.open("");


        return;
    }
}
