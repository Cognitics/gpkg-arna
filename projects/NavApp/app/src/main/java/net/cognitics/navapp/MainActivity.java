package net.cognitics.navapp;
import android.net.Uri;
import android.view.View;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.net.URL;
import java.util.List;

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
                            if(!openGeoPackage(files[0])); {
                                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
                                dlgAlert.setMessage("Unable to open " + files[0]);
                                dlgAlert.setTitle("Error");
                                dlgAlert.setPositiveButton("OK", null);
                                dlgAlert.setCancelable(true);
                                dlgAlert.create().show();
                            }
                        }
                    }
                });
                dialog.show();

            }
        });

    }

    public Boolean openGeoPackage(String path)
    {
        // Open a GeoPackage
        GeoPackageManager manager = GeoPackageFactory.getManager(this);

        manager.importGeoPackage("test",new File(path));
        gpkgDb = manager.open("test");
        if(gpkgDb != null) {
            StringBuilder message = new StringBuilder();
            message.append("Tables:\n");
            List<String> tables = gpkgDb.getFeatureTables();
            for(String table : tables){
                message.append(table).append("\n");
            }
            final EditText msgText = findViewById(R.id.messages);
            msgText.setText(message.toString());
            return Boolean.TRUE;
        }
        return Boolean.FALSE;

    }
}
