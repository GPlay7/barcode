package app.solution.barcode.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class RuntimePermissionClass {

    public static final int CAMERA_REQUEST_CODE = 101;
    private Context mContext;

    public RuntimePermissionClass(Context context){
        this.mContext = context;
    }

    public boolean checkPermission(){

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            return false;
        }

        return true;
    }

}
