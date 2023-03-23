package com.tvkmaer.packages;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.tvkmaer.packages.UsageStatsModule;
import com.tvkmaer.packages.CurrentAppModule;
import com.tvkmaer.packages.PermissionModule;
import com.tvkmaer.packages.AppUsageLimitModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsageStasPackage implements ReactPackage {
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @Override
    public List<NativeModule> createNativeModules(
            ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();

        modules.add(new UsageStatsModule(reactContext));
        modules.add(new PermissionModule(reactContext));
        modules.add(new CurrentAppModule(reactContext));
        modules.add(new AppUsageLimitModule(reactContext));
        return modules;
    }
}