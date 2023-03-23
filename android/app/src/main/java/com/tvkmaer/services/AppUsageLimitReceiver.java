package com.tvkmaer.services;

import android.content.BroadcastReceiver;
import android.widget.Toast;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class AppUsageLimitReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String packageName = intent.getStringExtra("packageName");

        PackageManager packageManager = context.getPackageManager();

        try {
            // Stop the app from running by launching its main activity
            Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
            context.startActivity(launchIntent);

            // Get the current time
            long currentTime = System.currentTimeMillis();

            long startTime = intent.getLongExtra("startTime", 0);

            // Get the end time from the intent extra data
            long endTime = intent.getLongExtra("endTime", 0);

            // Check if the current time is after the end time
            if (currentTime >= endTime) {
                // Show a toast message to inform the user that the usage limit has been reached
                Toast.makeText(context,
                        "Bạn đã đạt giới hạn sử dụng cho gói_" + startTime + "_" + packageName + endTime,
                        Toast.LENGTH_SHORT).show();
            } else if (currentTime < startTime) {
                // Show a toast message to inform the user that the usage limit has been reached
                Toast.makeText(context, "Bạn chưa đạt giới hạn sử dụng của gói " + startTime + packageName + endTime,
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}