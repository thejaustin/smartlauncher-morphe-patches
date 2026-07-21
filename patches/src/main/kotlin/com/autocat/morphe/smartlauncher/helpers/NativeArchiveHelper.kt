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
 * Polymorphic target instance handling to prevent ASM type mismatch VerifyErrors.
 */
object NativeArchiveHelper {

    private const val TAG = "SmartLauncherMorphe_NativeArchive"
    private const val FLAG_IMMUTABLE = 0x04000000
    private const val FLAG_UPDATE_CURRENT = 0x08000000

    @JvmStatic
    fun isNativeArchiveSupported(): Boolean {
        return Build.VERSION.SDK_INT >= 35
    }

    /**
     * Triggers official system app archiving request.
     * Accepts generic target instance (View, Context, or Fragment).
     */
    @JvmStatic
    fun requestNativeArchive(targetObj: Any?, packageName: String): Boolean {
        val context = ArchivedAppFilterHelper.resolveContext(targetObj)
        if (context == null) {
            Log.e(TAG, "Unable to resolve Context from targetObj: $targetObj")
            return false
        }

        if (!isNativeArchiveSupported()) {
            safeShowToast(context, "Native archiving is not supported on this Android version", Toast.LENGTH_SHORT)
            return false
        }

        return try {
            val getPackageInstallerMethod = context.packageManager.javaClass.getMethod("getPackageInstaller")
            val packageInstaller = getPackageInstallerMethod.invoke(context.packageManager)
            val intentSender = createDummyIntentSender(context, packageName)

            val requestArchiveMethod = packageInstaller.javaClass.getMethod(
                "requestArchive",
                String::class.java,
                IntentSender::class.java
            )

            requestArchiveMethod.invoke(packageInstaller, packageName, intentSender)
            Log.i(TAG, "Native archive requested for $packageName")
            safeShowToast(context, "Requesting native archive for $packageName...", Toast.LENGTH_SHORT)
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
            safeShowToast(context, "Native archive API unavailable on this device build", Toast.LENGTH_SHORT)
            false
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to invoke native requestArchive for $packageName", e)
            safeShowToast(context, "Native archive error: ${e.localizedMessage}", Toast.LENGTH_SHORT)
            false
        }
    }

    private fun safeShowToast(context: Context, text: String, duration: Int) {
        try {
            Toast.makeText(context, text, duration).show()
        } catch (e: Throwable) {
            Log.w(TAG, "Toast display failed: $text", e)
        }
    }

    private fun createDummyIntentSender(context: Context, packageName: String): IntentSender? {
        return try {
            val dummyIntent = Intent("com.autocat.morphe.smartlauncher.ARCHIVE_CALLBACK").apply {
                setPackage(context.packageName)
                putExtra("archived_package", packageName)
            }
            val flags = FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            PendingIntent.getBroadcast(context, packageName.hashCode(), dummyIntent, flags)?.intentSender
        } catch (e: Throwable) {
            Log.w(TAG, "Failed to create dummy IntentSender", e)
            null
        }
    }
}
