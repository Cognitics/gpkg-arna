package net.cognitics.camera2;

import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.karan.churi.PermissionManager.PermissionManager;

public class MainActivity extends AppCompatActivity {

    Camera camera;
    FrameLayout cameraPreview;
    ShowCamera showCamera;

    PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionManager = new PermissionManager() {};
        permissionManager.checkAndRequestPermissions(this);

        cameraPreview = (FrameLayout) findViewById(R.id.cameraPreview);

        //open
        camera = Camera.open();

        showCamera = new ShowCamera(this,camera);
        cameraPreview.addView(showCamera);



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode, permissions, grantResults);
    }
}
