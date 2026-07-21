package com.autocat.morphe.smartlauncher.helpers

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import rikka.shizuku.Shizuku

/**
 * Injected helper class for archiving apps via Shizuku on Smart Launcher 6.
 * Supports Samsung Galaxy S22 Ultra (One UI) and standard Android environments.
 */
object ShizukuArchiveHelper {

    private const val TAG = "SmartLauncherMorphe_ShizukuArchive"

    /**
     * Checks if Shizuku service is running and permission is granted.
     */
    @JvmStatic
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            Log.w(TAG, "Shizuku availability check failed", e)
            false
        }
    }

    /**
     * Requests Shizuku permission if not already granted.
     */
    @JvmStatic
    fun requestShizukuPermission(requestCode: Int) {
        try {
            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to request Shizuku permission", e)
        }
    }

    /**
     * Archives the given package using Shizuku privileges via `pm archive <packageName>`.
     *
     * @param context Host application context.
     * @param packageName Target package name to archive.
     */
    @JvmStatic
    fun archiveAppWithShizuku(context: Context, packageName: String): Boolean {
        if (!isShizukuAvailable()) {
            Toast.makeText(context, "Shizuku is not running or permission denied", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            Log.i(TAG, "Attempting Shizuku archive for package: $packageName")
            
            // Execute `pm archive <packageName>` via Shizuku process executor
            val process = Shizuku.newProcess(
                arrayOf("pm", "archive", packageName),
                null,
                null
            )
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                Toast.makeText(context, "App $packageName successfully archived", Toast.LENGTH_SHORT).show()
                true
            } else {
                val errorOutput = process.errorStream.bufferedReader().readText()
                Log.e(TAG, "Shizuku pm archive failed (exit code $exitCode): $errorOutput")
                Toast.makeText(context, "Failed to archive app: $errorOutput", Toast.LENGTH_LONG).show()
                false
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error executing Shizuku archive command for $packageName", e)
            Toast.makeText(context, "Shizuku archive error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
