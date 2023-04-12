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

    public CurrentAppModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "CurrentAppModule";
    }

    @ReactMethod
    public void getCurrentAppInfo(Callback callback) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getCurrentActivity().getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        
        UsageStats lastAppStats = null;
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000, currentTime);
        
        while (lastAppStats == null) {
            for (UsageStats usageStats : usageStatsList) {
                if (lastAppStats == null || lastAppStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                    lastAppStats = usageStats;
                }
            }
        
            if (lastAppStats == null) {
                // Chờ một khoảng thời gian nữa để lấy danh sách mới hơn
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        
                usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000, currentTime);
            }
        }
        
        // Lấy thông tin ứng dụng cuối cùng
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
    }

}
