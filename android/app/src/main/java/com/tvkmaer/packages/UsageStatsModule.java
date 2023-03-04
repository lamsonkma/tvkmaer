package com.tvkmaer.packages;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UsageStatsModule extends ReactContextBaseJavaModule {
    private static final String TAG = "UsageStatsModule";

    public UsageStatsModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "UsageStatsModule";
    }

    @ReactMethod
    public void getRecentUsageStats(int numDays, Promise promise) {
        ReactContext reactContext = getReactApplicationContext();

        UsageStatsManager usageStatsManager = (UsageStatsManager) reactContext.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            Log.w(TAG, "Usage stats manager is null");
            promise.reject("Error", "Usage stats manager is null");
            return;
        }

        long endTime = System.currentTimeMillis();
        long startTime = endTime - (1000 * 60 * 60 * 24 * numDays); // numDays days

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);

        WritableArray appUsageArray = Arguments.createArray();

        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                String packageName = event.getPackageName();
                long timeUsed = event.getTimeStamp() ;
                WritableMap appUsageMap = Arguments.createMap();
                appUsageMap.putString("packageName", packageName);
                appUsageMap.putString("date", getDate(event.getTimeStamp()));
                appUsageMap.putDouble("timeUsed", timeUsed);
                appUsageArray.pushMap(appUsageMap);

                Log.d(TAG, "Package name: " + packageName + ", Time used: " + timeUsed);
            }
        }

        promise.resolve(appUsageArray);
    }

    private String getDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return sdf.format(date);
    }
}