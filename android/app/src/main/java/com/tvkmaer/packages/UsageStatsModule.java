package com.tvkmaer.packages;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
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
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.WritableNativeArray;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

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

    // app sử dụng gần đây
    @ReactMethod
    public void getRecentUsageStats(int numDays, Promise promise) {
        ReactContext reactContext = getReactApplicationContext();

        UsageStatsManager usageStatsManager = (UsageStatsManager) reactContext
                .getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            Log.w(TAG, "Usage stats manager is null");
            promise.reject("Error", "Usage stats manager is null");
            return;
        }

        long endTime = System.currentTimeMillis();
        long startTime = endTime - (1000 * 60 * 60 * 24 * numDays); // numDays days

        UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);

        Map<String, Long> appUsageMap = new HashMap<>();

        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                String packageName = event.getPackageName();
                long timeUsed = event.getTimeStamp();

                if (appUsageMap.containsKey(packageName)) {
                    long existingTimeUsed = appUsageMap.get(packageName);
                    appUsageMap.put(packageName, existingTimeUsed + (endTime - timeUsed));
                } else {
                    appUsageMap.put(packageName, endTime - timeUsed);
                }

                Log.d(TAG, "Package name: " + packageName + ", Time used: " + (endTime - timeUsed));
            }
        }

        WritableArray appUsageArray = Arguments.createArray();
        for (Map.Entry<String, Long> entry : appUsageMap.entrySet()) {
            WritableMap appUsageMapEntry = Arguments.createMap();
            appUsageMapEntry.putString("packageName", entry.getKey());
            appUsageMapEntry.putDouble("timeUsed", entry.getValue() / (1000 * 60));
            appUsageArray.pushMap(appUsageMapEntry);
        }

        promise.resolve(appUsageArray);
    }

    // query theo thời gian bắt đầu và kết thúc
    @ReactMethod
    public void queryUsageStats(int interval, double startTime, double endTime, Promise promise) {
        WritableMap result = new WritableNativeMap();
        ReactContext reactContext = getReactApplicationContext();
        UsageStatsManager usageStatsManager = (UsageStatsManager) reactContext
                .getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(interval, (long) startTime,
                (long) endTime);
        for (UsageStats us : queryUsageStats) {
            Log.d("UsageStats", us.getPackageName() + " = " + us.getTotalTimeInForeground());
            WritableMap usageStats = new WritableNativeMap();
            usageStats.putString("packageName", us.getPackageName());
            usageStats.putDouble("totalTimeInForeground", us.getTotalTimeInForeground());
            usageStats.putDouble("firstTimeStamp", us.getFirstTimeStamp());
            usageStats.putDouble("lastTimeStamp", us.getLastTimeStamp());
            usageStats.putDouble("lastTimeUsed", us.getLastTimeUsed());
            usageStats.putInt("describeContents", us.describeContents());
            result.putMap(us.getPackageName(), usageStats);
        }
        promise.resolve(result);
    }

    // query theo tuần hiện tại
    @ReactMethod
    public void queryUsageStatsForWeek(Promise promise) {
        ReactContext reactContext = getReactApplicationContext();
        UsageStatsManager usageStatsManager = (UsageStatsManager) reactContext
                .getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = calendar.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        Map<String, UsageStats> usageStats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime);
        WritableArray result = new WritableNativeArray();

        for (Map.Entry<String, UsageStats> entry : usageStats.entrySet()) {
            String packageName = entry.getKey();
            UsageStats stats = entry.getValue();
            long totalTimeInForeground = stats.getTotalTimeInForeground();
            String appName = getAppNameFromPackageName(packageName, reactContext);

            if (totalTimeInForeground > 0) {
                WritableMap usageStatsMap = new WritableNativeMap();
                usageStatsMap.putString("packageName", packageName);
                usageStatsMap.putString("appName", appName);
                usageStatsMap.putDouble("totalTimeInForeground", (double) totalTimeInForeground / 1000);
                result.pushMap(usageStatsMap);
            }
        }
        promise.resolve(result);
    }

    // lấy thông tin app
    private String getAppNameFromPackageName(String packageName, Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : packageName);
    }

    // lấy thời gian của ứng dụng theo tuần
    @ReactMethod
    public void queryWeeklyUsageStats(Promise promise) {
        ReactContext reactContext = getReactApplicationContext();
        UsageStatsManager usageStatsManager = (UsageStatsManager) reactContext
                .getSystemService(Context.USAGE_STATS_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();

        Map<String, UsageStats> usageStats;
        WritableArray weeklyUsageStats = new WritableNativeArray();

        for (int i = 0; i < 7; i++) {
            String dayOfWeekName = getDayOfWeekName(calendar.get(Calendar.DAY_OF_WEEK));
            calendar.add(Calendar.DATE, 1);
            long endTime = calendar.getTimeInMillis();

            usageStats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime);
            WritableMap dailyUsageStats = new WritableNativeMap();

            for (Map.Entry<String, UsageStats> entry : usageStats.entrySet()) {
                String packageName = entry.getKey();
                UsageStats stats = entry.getValue();
                long totalTimeInForeground = stats.getTotalTimeInForeground();
                String appName = getAppNameFromPackageName(packageName, reactContext);

                if (totalTimeInForeground > 0) {
                    WritableMap usageStatsMap = new WritableNativeMap();
                    usageStatsMap.putString("packageName", packageName);
                    usageStatsMap.putString("appName", appName);
                    usageStatsMap.putDouble("totalTimeInForeground", (double) totalTimeInForeground / 1000);
                    dailyUsageStats.putMap(packageName, usageStatsMap);
                }
            }

            WritableMap dailyStatsMap = new WritableNativeMap();
            dailyStatsMap.putMap("usageStats", dailyUsageStats);
            dailyStatsMap.putString("dayOfWeek", dayOfWeekName);
            weeklyUsageStats.pushMap(dailyStatsMap);
        }

        promise.resolve(weeklyUsageStats);
    }

    // chuyển các ngày trong tuần sang dạng string
    private String getDayOfWeekName(int dayOfWeek) {
        switch (dayOfWeek) {
            case 0:
                return "Sunday";
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            default:
                return "";
        }
    }

    // lấy thời gian của ứng dụng theo tháng
    private long calculateTotalTimeInForeground(Map<String, UsageStats> usageStats) {
        long totalTimeInForeground = 0;
        for (Map.Entry<String, UsageStats> entry : usageStats.entrySet()) {
            UsageStats stats = entry.getValue();
            totalTimeInForeground += stats.getTotalTimeInForeground();
        }
        return totalTimeInForeground;
    }

    // format date
    private String getDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return sdf.format(date);
    }

    // lấy thông tin những app đã cài đặt
    @ReactMethod
    public void getInstalledApps(Promise promise) {
        PackageManager pm = getReactApplicationContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        WritableArray result = new WritableNativeArray();

        for (ApplicationInfo appInfo : apps) {
            String appName = pm.getApplicationLabel(appInfo).toString();
            String packageName = appInfo.packageName;

            // Get the app icon
            Drawable appIcon = pm.getApplicationIcon(appInfo);
            Bitmap bitmap = drawableToBitmap(appIcon);

            // Convert the bitmap to base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            // String encodedBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Create a map for the app data
            WritableMap appData = new WritableNativeMap();
            appData.putString("name", appName);
            appData.putString("package", packageName);
            // appData.putString("icon", encodedBitmap);

            result.pushMap(appData);
        }

        promise.resolve(result);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}