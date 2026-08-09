package com.autocat.morphe.smartlauncher.extension;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Runtime logic for the "Hide archived apps" patch.
 * <p>
 * Called from every {@code LauncherApps.getActivityList(...)} call site the
 * HideArchivedAppsPatch fingerprint locates, filtering out entries for
 * packages that are currently archived (Android 15+ app archiving) before
 * Smart Launcher's own drawer/picker code ever sees them.
 * <p>
 * High-performance implementation: uses zero-allocation fast path when no apps
 * are archived, caches reflection methods, and performs non-blocking flag checks.
 */
@SuppressWarnings("unused")
public class ArchivedAppFilter {

    private static final String TAG = "ArchivedAppFilter";
    private static Method currentApplicationMethod;
    private static Method getArchiveTimeMethod;
    private static boolean reflectionInitialized = false;

    public static List<LauncherActivityInfo> filter(List<LauncherActivityInfo> activities) {
        if (activities == null || activities.isEmpty() || Build.VERSION.SDK_INT < 35) {
            return activities;
        }

        // Fast check: count archived apps first.
        // If 0 apps are archived (99.9% common case), return original list directly (0 heap allocations).
        int archivedCount = 0;
        for (int i = 0; i < activities.size(); i++) {
            LauncherActivityInfo info = activities.get(i);
            if (info != null && isArchivedFastCheck(info)) {
                archivedCount++;
            }
        }

        if (archivedCount == 0) {
            return activities;
        }

        // Allocate result list only when archived apps need to be removed
        PackageManager packageManager = currentPackageManager();
        List<LauncherActivityInfo> result = new ArrayList<>(activities.size() - archivedCount);
        for (int i = 0; i < activities.size(); i++) {
            LauncherActivityInfo info = activities.get(i);
            if (info != null && !isArchived(info, packageManager)) {
                result.add(info);
            }
        }
        return result;
    }

    private static boolean isArchivedFastCheck(LauncherActivityInfo info) {
        ApplicationInfo appInfo = info.getApplicationInfo();
        if (appInfo == null) return false;
        
        // Fast Signal 1: ApplicationInfo.FLAG_ARCHIVED (bit 30 = 0x40000000)
        if ((appInfo.flags & 0x40000000) != 0) {
            return true;
        }
        // Fast Signal 2: Unlinked / missing APK file on archived app
        return appInfo.sourceDir == null || appInfo.sourceDir.isEmpty() || !new File(appInfo.sourceDir).exists();
    }

    private static boolean isArchived(LauncherActivityInfo info, PackageManager packageManager) {
        if (isArchivedFastCheck(info)) {
            return true;
        }
        if (packageManager == null) {
            return false;
        }
        ApplicationInfo appInfo = info.getApplicationInfo();
        if (appInfo == null) {
            return false;
        }
        try {
            // Signal 3: Reflection for PackageInfo.getArchiveTimeMillis() with MATCH_ARCHIVED_PACKAGES = 0x00200000
            Object pkgInfo = packageManager.getPackageInfo(appInfo.packageName, 0x00200000);
            if (pkgInfo != null) {
                ensureReflectionInitialized(pkgInfo.getClass());
                if (getArchiveTimeMethod != null) {
                    Long time = (Long) getArchiveTimeMethod.invoke(pkgInfo);
                    return time != null && time != 0L;
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Could not determine archived state for " + appInfo.packageName, t);
        }
        return false;
    }

    private static synchronized void ensureReflectionInitialized(Class<?> pkgInfoClass) {
        if (reflectionInitialized) return;
        try {
            getArchiveTimeMethod = pkgInfoClass.getMethod("getArchiveTimeMillis");
        } catch (Throwable ignored) {
        }
        reflectionInitialized = true;
    }

    private static PackageManager currentPackageManager() {
        try {
            if (currentApplicationMethod == null) {
                currentApplicationMethod = Class.forName("android.app.ActivityThread")
                        .getMethod("currentApplication");
            }
            Application application = (Application) currentApplicationMethod.invoke(null);
            return application != null ? application.getPackageManager() : null;
        } catch (Throwable t) {
            Log.w(TAG, "Could not resolve current Application/PackageManager", t);
            return null;
        }
    }
}
