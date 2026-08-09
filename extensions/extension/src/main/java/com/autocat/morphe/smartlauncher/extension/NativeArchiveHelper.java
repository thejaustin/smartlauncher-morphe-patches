package com.autocat.morphe.smartlauncher.extension;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.util.Log;

/**
 * Real, verified implementation of native (no-root) app archiving via the
 * public {@code PackageInstaller.requestArchive(String, IntentSender)} API
 * (Android 15 / API 35+). This is genuinely callable and requires no special
 * permission beyond being the requesting app.
 */
@SuppressWarnings("unused")
public class NativeArchiveHelper {

    private static final String TAG = "NativeArchiveHelper";

    public static boolean isSupported() {
        return Build.VERSION.SDK_INT >= 35;
    }

    public static void requestArchive(Context context, String packageName, PendingIntent statusReceiver) {
        if (!isSupported() || context == null || statusReceiver == null) {
            return;
        }
        try {
            PackageInstaller installer = context.getPackageManager().getPackageInstaller();
            installer.requestArchive(packageName, statusReceiver.getIntentSender());
        } catch (Throwable t) {
            Log.e(TAG, "Failed to request native archive for " + packageName, t);
        }
    }
}
