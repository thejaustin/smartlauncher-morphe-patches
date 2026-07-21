package com.autocat.morphe.smartlauncher.extension;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

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
 * {@link LauncherActivityInfo} and {@link ApplicationInfo} carry no direct
 * "is archived" accessor - the only documented signal is
 * {@code PackageInfo.getArchiveTimeMillis()} (non-zero once archived), which
 * requires a separate {@code PackageManager.getPackageInfo} lookup per app.
 * <p>
 * The patched call sites are Kotlin coroutine continuations with no
 * Context/PackageManager reliably reachable from a local register, so this
 * resolves one itself via the same {@code ActivityThread.currentApplication()}
 * hidden-API lookup used throughout the Xposed/LSPosed/ReVanced ecosystem for
 * exactly this situation, rather than guessing at the injection site's
 * register layout.
 */
@SuppressWarnings("unused")
public class ArchivedAppFilter {

    private static final String TAG = "ArchivedAppFilter";

    public static List<LauncherActivityInfo> filter(List<LauncherActivityInfo> activities) {
        if (activities == null || activities.isEmpty() || Build.VERSION.SDK_INT < 35) {
            return activities;
        }
        PackageManager packageManager = currentPackageManager();
        if (packageManager == null) {
            return activities;
        }
        List<LauncherActivityInfo> result = new ArrayList<>(activities.size());
        for (LauncherActivityInfo info : activities) {
            if (!isArchived(info, packageManager)) {
                result.add(info);
            }
        }
        return result;
    }

    private static PackageManager currentPackageManager() {
        try {
            Method currentApplication = Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication");
            Application application = (Application) currentApplication.invoke(null);
            return application != null ? application.getPackageManager() : null;
        } catch (Throwable t) {
            Log.w(TAG, "Could not resolve current Application/PackageManager", t);
            return null;
        }
    }

    private static boolean isArchived(LauncherActivityInfo info, PackageManager packageManager) {
        ApplicationInfo appInfo = info.getApplicationInfo();
        if (appInfo == null) {
            return false;
        }
        try {
            PackageManager.PackageInfoFlags flags = PackageManager.PackageInfoFlags.of(PackageManager.MATCH_ARCHIVED_PACKAGES);
            return packageManager.getPackageInfo(appInfo.packageName, flags).getArchiveTimeMillis() != 0L;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (Throwable t) {
            // Defensive: getArchiveTimeMillis() is gated behind the archiving
            // feature flag and may not exist on every OEM build. Never let a
            // launcher-critical call site crash the drawer over this.
            Log.w(TAG, "Could not determine archived state for " + appInfo.packageName, t);
            return false;
        }
    }
}
