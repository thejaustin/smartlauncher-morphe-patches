package com.autocat.morphe.smartlauncher.helpers

import android.content.Context
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

    /**
     * Checks whether the current device/OS supports native app archiving APIs.
     */
    @JvmStatic
    fun isNativeArchiveSupported(): Boolean {
        // Official PackageInstaller / LauncherApps archive APIs were standardized in API 35 (Android 15)
        // and supported on updated Samsung One UI device platforms.
        return Build.VERSION.SDK_INT >= 35
    }

    /**
     * Triggers the official system device app archive dialog/request for a specific package.
     *
     * @param context Host Context.
     * @param packageName Target package to request system archiving for.
     */
    @JvmStatic
    fun requestNativeArchive(context: Context, packageName: String): Boolean {
        if (!isNativeArchiveSupported()) {
            Toast.makeText(context, "Native archiving is not supported on this Android version", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val getPackageInstallerMethod = context.packageManager.javaClass.getMethod("getPackageInstaller")
            val packageInstaller = getPackageInstallerMethod.invoke(context.packageManager)
            
            // Reflectively invoke requestArchive on PackageInstaller (API 35+)
            val requestArchiveMethod = packageInstaller.javaClass.getMethod(
                "requestArchive",
                String::class.java,
                IntentSender::class.java
            )

            requestArchiveMethod.invoke(packageInstaller, packageName, null)
            Log.i(TAG, "Native archive requested for $packageName")
            true
        } catch (e: NoSuchMethodException) {
            try {
                val launcherApps = context.getSystemService("launcherapps") as? LauncherApps
                if (launcherApps != null) {
                    val archiveAppMethod = launcherApps.javaClass.getMethod(
                        "archiveApp",
                        String::class.java,
                        IntentSender::class.java
                    )
                    archiveAppMethod.invoke(launcherApps, packageName, null)
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
}
