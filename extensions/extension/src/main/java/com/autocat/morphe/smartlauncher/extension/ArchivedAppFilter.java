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
 * Bulletproof, crash-resilient runtime filter for Smart Launcher 6 app archiving.
 * <p>
 * Guarantee: This filter will NEVER throw any exception to Smart Launcher.
 * If any error, unexpected data structure, or runtime exception occurs, it
 * immediately falls back to returning the original activities list safely.
 */
@SuppressWarnings("unused")
public class ArchivedAppFilter {

    private static final String TAG = "ArchivedAppFilter";
    private static final int FLAG_ARCHIVED = 0x40000000; // Bit 30 in ApplicationInfo.flags
    private static final int MATCH_ARCHIVED_PACKAGES = 0x00200000;

    private static Method currentApplicationMethod;
    private static Method getArchiveTimeMethod;
    private static volatile boolean reflectionInitialized = false;

    /**
     * Primary entry point called from bytecode hooks.
     */
    public static List<LauncherActivityInfo> filter(List<LauncherActivityInfo> activities) {
        try {
            if (activities == null || activities.isEmpty()) {
                return activities;
            }

            // Android 15+ archiving is only active on API 35+
            if (Build.VERSION.SDK_INT < 35) {
                return activities;
            }

            // Quick pass: check if any app is actually archived
            int total = activities.size();
            int archivedCount = 0;
            for (int i = 0; i < total; i++) {
                LauncherActivityInfo info = activities.get(i);
                if (info != null && isArchived(info)) {
                    archivedCount++;
                }
            }

            // If no apps are archived, return the original list with 0 allocations
            if (archivedCount == 0) {
                return activities;
            }

            // Filter out archived apps safely
            List<LauncherActivityInfo> filtered = new ArrayList<>(total - archivedCount);
            for (int i = 0; i < total; i++) {
                LauncherActivityInfo info = activities.get(i);
                if (info != null && !isArchived(info)) {
                    filtered.add(info);
                }
            }
            return filtered;
        } catch (Throwable t) {
            // Absolute fail-safe: Never crash Smart Launcher!
            Log.e(TAG, "Safe filter catch: preserving original activity list", t);
            return activities;
        }
    }

    private static boolean isArchived(LauncherActivityInfo info) {
        try {
            ApplicationInfo appInfo = info.getApplicationInfo();
            if (appInfo == null) {
                return false;
            }

            // Signal 1: FLAG_ARCHIVED bit 30
            if ((appInfo.flags & FLAG_ARCHIVED) != 0) {
                return true;
            }

            // Signal 2: Check if APK base file was stripped
            if (appInfo.sourceDir == null || appInfo.sourceDir.isEmpty()) {
                return true;
            }

            return false;
        } catch (Throwable t) {
            return false;
        }
    }
}
