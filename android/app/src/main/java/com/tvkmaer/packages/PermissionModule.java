package com.tvkmaer.packages;
import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.ArrayList;
import java.util.List;

public class PermissionModule extends ReactContextBaseJavaModule {
    private static final String TAG = "PermissionModule";
    private static final int REQUEST_PACKAGE_USAGE_STATS = 100;
    private static final int REQUEST_FOREGROUND_SERVICE = 101;

    public PermissionModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "PermissionModule";
    }

    @ReactMethod
    public void requestPermissions(Promise promise) {
        List<String> permissionsToRequest = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasPermissionForPackageUsageStats()) {
                permissionsToRequest.add(Manifest.permission.PACKAGE_USAGE_STATS);
            }
        }

        if (!hasPermissionForForegroundService()) {
            permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE);
        }

        if (permissionsToRequest.isEmpty()) {
            promise.resolve(true);
            return;
        }

        promise.resolve(false);

        // String[] permissionsArray = new String[permissionsToRequest.size()];
        // permissionsToRequest.toArray(permissionsArray);
        // ActivityCompat.requestPermissions(getCurrentActivity(), permissionsArray, REQUEST_FOREGROUND_SERVICE);
    }

    private boolean hasPermissionForPackageUsageStats() {
        AppOpsManager appOpsManager = (AppOpsManager) getReactApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getReactApplicationContext().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean hasPermissionForForegroundService() {
        return ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_FOREGROUND_SERVICE) {
            boolean granted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                Log.d(TAG, "Foreground service permission granted");
            } else {
                Log.d(TAG, "Foreground service permission denied");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @ReactMethod
    public void openUsageAccessSettings() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Activity activity = getCurrentActivity();
        if (activity != null) {
            activity.startActivityForResult(intent, REQUEST_PACKAGE_USAGE_STATS);
        }
    }
}
