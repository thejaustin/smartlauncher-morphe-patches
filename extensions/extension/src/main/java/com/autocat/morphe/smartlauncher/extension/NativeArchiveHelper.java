package com.autocat.morphe.smartlauncher.extension;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInstaller;
import android.os.Build;

/**
 * Real, verified implementation of native (no-root) app archiving via the
 * public {@code PackageInstaller.requestArchive(String, IntentSender)} API
 * (Android 15 / API 35+). This is genuinely callable and requires no special
 * permission beyond being the requesting app.
 * <p>
 * Not yet wired to a bytecode injection point - see NativeArchivePatch.kt
 * for why (the app-icon long-press menu appears to be a Jetpack Compose
 * screen; a safe injection point was not confirmed against the decompiled
 * APK in the time available).
 */
@SuppressWarnings("unused")
public class NativeArchiveHelper {

    public static boolean isSupported() {
        return Build.VERSION.SDK_INT >= 35;
    }

    public static void requestArchive(Context context, String packageName, PendingIntent statusReceiver) {
        if (!isSupported()) {
            return;
        }
        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        installer.requestArchive(packageName, statusReceiver.getIntentSender());
    }
}
