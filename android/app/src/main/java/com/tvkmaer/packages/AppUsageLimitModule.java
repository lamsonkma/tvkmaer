package com.tvkmaer.packages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.AlarmManager;
import android.app.PendingIntent;
import java.util.Calendar;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.tvkmaer.services.AppUsageLimitReceiver;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import com.facebook.react.bridge.Callback;
import android.widget.Toast;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Date;

public class AppUsageLimitModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private AlarmManager alarmManager;

    public AppUsageLimitModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.alarmManager = (AlarmManager) reactContext.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public String getName() {
        return "AppUsageLimitModule";
    }

    @ReactMethod
    public void setUsageLimit(String packageName, String startTime, String endTime, Callback callback) {
        long startTimeMillis = Long.valueOf(startTime);
        long endTimeMillis = Long.valueOf(endTime);

        int startAlarmId = packageName.hashCode();
        int endAlarmId = packageName.hashCode() + 1;

        Context context = reactContext.getApplicationContext();
        Intent intent = new Intent(reactContext, AppUsageLimitReceiver.class);
        intent.putExtra("packageName", packageName);
        intent.putExtra("startTime", startTimeMillis);
        intent.putExtra("endTime", endTimeMillis);

        PendingIntent startPendingIntent = PendingIntent.getBroadcast(context, startAlarmId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent endPendingIntent = PendingIntent.getBroadcast(context, endAlarmId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(startPendingIntent);
        alarmManager.cancel(endPendingIntent);

        alarmManager.set(AlarmManager.RTC_WAKEUP, startTimeMillis, startPendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, endTimeMillis, endPendingIntent);

        Toast.makeText(context,
                "Giới hạn sử dụng đã được đặt cho gói vào lúc" + startTimeMillis + packageName,
                Toast.LENGTH_SHORT).show();

        callback.invoke("Success");
    }
}