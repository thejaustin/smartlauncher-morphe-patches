package com.autocat.morphe.smartlauncher.helpers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.LauncherApps
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import android.widget.Toast

/**
 * Injected helper class for invoking official system device app archiving APIs.
 * Supports official device archiving on Samsung Galaxy S22 Ultra (One UI) & Android 15+.
 */
object NativeArchiveHelper {

    private const val TAG = "SmartLauncherMorphe_NativeArchive"

    @JvmStatic
    fun isNativeArchiveSupported(): Boolean {
        return Build.VERSION.SDK_INT >= 35
    }

    /**
     * Triggers official system app archiving request.
     */
    @JvmStatic
    fun requestNativeArchive(context: Context, packageName: String): Boolean {
        if (!isNativeArchiveSupported()) {
            Toast.makeText(context, "Native archiving is not supported on this Android version", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val packageInstaller = context.packageManager.packageInstaller
            val intentSender = createDummyIntentSender(context, packageName)

            val requestArchiveMethod = packageInstaller.javaClass.getMethod(
                "requestArchive",
                String::class.java,
                IntentSender::class.java
            )

            requestArchiveMethod.invoke(packageInstaller, packageName, intentSender)
            Log.i(TAG, "Native archive requested for $packageName")
            Toast.makeText(context, "Requesting native archive for $packageName...", Toast.LENGTH_SHORT).show()
            true
        } catch (e: NoSuchMethodException) {
            try {
                val launcherApps = context.getSystemService("launcherapps") as? LauncherApps
                if (launcherApps != null) {
                    val intentSender = createDummyIntentSender(context, packageName)
                    val archiveAppMethod = launcherApps.javaClass.getMethod(
                        "archiveApp",
                        String::class.java,
                        IntentSender::class.java
                    )
                    archiveAppMethod.invoke(launcherApps, packageName, intentSender)
                    Log.i(TAG, "LauncherApps native archive requested for $packageName")
                    return true
                }
            } catch (ex: Throwable) {
                Log.e(TAG, "LauncherApps fallback archive failed", ex)
            }
            
            Log.e(TAG, "PackageInstaller requestArchive method not found", e)
            Toast.makeText(context, "Native archive API unavailable on this device build", Toast.LENGTH_SHORT).show()
            false
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to invoke native requestArchive for $packageName", e)
            Toast.makeText(context, "Native archive error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun createDummyIntentSender(context: Context, packageName: String): IntentSender {
        val dummyIntent = Intent("com.autocat.morphe.smartlauncher.ARCHIVE_CALLBACK").apply {
            setPackage(context.packageName)
            putExtra("archived_package", packageName)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, packageName.hashCode(), dummyIntent, flags).intentSender
    }
}
