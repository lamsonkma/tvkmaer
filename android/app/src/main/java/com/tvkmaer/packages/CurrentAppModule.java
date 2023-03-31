package com.tvkmaer.packages;

import android.content.pm.PackageManager.NameNotFoundException;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import android.app.Activity;
import android.content.ComponentName;
import java.util.List;
import android.util.Log;


import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Promise;

public class CurrentAppModule extends ReactContextBaseJavaModule {
    private ReactApplicationContext reactContext;
    private BroadcastReceiver broadcastReceiver;

    public CurrentAppModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Activity currentActivity = getCurrentActivity();
                if (currentActivity != null) {
                    String packageName = intent.getData().getEncodedSchemeSpecificPart();
                    sendEvent(packageName);
                }
            }
        };
        this.reactContext.registerReceiver(broadcastReceiver, filter);
    }

    @ReactMethod
    public void getRunningApps(Promise promise) {
        ActivityManager activityManager = (ActivityManager) getReactApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager packageManager = getReactApplicationContext().getPackageManager();

        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager
                .getRunningAppProcesses();

        if (runningAppProcesses == null) {
            promise.reject(new RuntimeException("Could not get running app processes."));
            return;
        }

        WritableArray appsArray = Arguments.createArray();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(
                        runningAppProcessInfo.processName, PackageManager.GET_META_DATA);
                WritableMap appMap = Arguments.createMap();
                appMap.putString("packageName", appInfo.packageName);
                appMap.putString("label", appInfo.loadLabel(packageManager).toString());
                appMap.putString("icon", appInfo.icon != 0 ? Integer.toString(appInfo.icon) : null);
                appsArray.pushMap(appMap);
            } catch (NameNotFoundException e) {
                Log.e("getRunningApps",
                        "Could not find application info for process " +
                                runningAppProcessInfo.processName,
                        e);
            }
        }

        promise.resolve(appsArray);
    }

    @Override
    public String getName() {
        return "CurrentAppModule";
    }

    @ReactMethod
    public void getCurrentAppInfo(Callback callback) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getCurrentActivity().getSystemService(Context.USAGE_STATS_SERVICE);

        // Lấy thời gian hiện tại
        long currentTime = System.currentTimeMillis();
    
        // Lấy thông tin ứng dụng chạy cuối cùng
        UsageStats lastAppStats = null;
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 , currentTime);
        if (usageStatsList != null && !usageStatsList.isEmpty()) {
            for (UsageStats usageStats : usageStatsList) {
                if (lastAppStats == null || lastAppStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                    lastAppStats = usageStats;
                }
            }
        }
    
        if (lastAppStats != null) {
            String packageName = lastAppStats.getPackageName();
            PackageManager packageManager = getCurrentActivity().getPackageManager();
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                String label = (String) packageManager.getApplicationLabel(applicationInfo);
    
                WritableMap appInfo = Arguments.createMap();
                appInfo.putString("label", label);
                appInfo.putString("packageName", packageName);
    
                callback.invoke(null, appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                callback.invoke("Unable to get current app info", null);
            }
        } else {
            callback.invoke("Unable to get current app info", null);
        }
    }

    private void sendEvent(String packageName) {
        WritableMap map = Arguments.createMap();
        map.putString("packageName", packageName);
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onAppChange", map);
    }

    // @ReactMethod
    // public void getRunningApps(Promise promise) {
    // ActivityManager am = (ActivityManager)
    // reactContext.getSystemService(Context.ACTIVITY_SERVICE);
    // List<ActivityManager.RunningAppProcessInfo> runningApps =
    // am.getRunningAppProcesses();

    // if (runningApps != null) {
    // WritableArray appList = Arguments.createArray();
    // for (ActivityManager.RunningAppProcessInfo app : runningApps) {
    // try {
    // ApplicationInfo ai =
    // reactContext.getPackageManager().getApplicationInfo(app.processName, 0);
    // String appName = (String) ai.loadLabel(reactContext.getPackageManager());
    // appList.pushString(appName);
    // } catch (PackageManager.NameNotFoundException e) {
    // // Do nothing
    // }
    // }
    // promise.resolve(appList);
    // } else {
    // promise.reject("NO_APPS_RUNNING", "No apps are currently running");
    // }
    // }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        if (this.broadcastReceiver != null) {
            this.reactContext.unregisterReceiver(this.broadcastReceiver);
        }
    }
}
