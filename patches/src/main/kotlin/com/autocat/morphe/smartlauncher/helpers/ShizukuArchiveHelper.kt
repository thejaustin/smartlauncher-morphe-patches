package com.autocat.morphe.smartlauncher.helpers

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import rikka.shizuku.Shizuku

/**
 * Injected helper class for archiving apps via Shizuku on Smart Launcher 6.
 * Asynchronous process execution to prevent ANRs on the main UI thread.
 */
object ShizukuArchiveHelper {

    private const val TAG = "SmartLauncherMorphe_ShizukuArchive"

    @JvmStatic
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            Log.w(TAG, "Shizuku availability check failed", e)
            false
        }
    }

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
     * Asynchronously archives the given package using Shizuku privileges via `pm archive <packageName>`.
     */
    @JvmStatic
    fun archiveAppWithShizuku(context: Context, packageName: String): Boolean {
        if (!isShizukuAvailable()) {
            Toast.makeText(context, "Shizuku is not running or permission denied", Toast.LENGTH_SHORT).show()
            return false
        }

        val mainHandler = Handler(Looper.getMainLooper())
        Toast.makeText(context, "Archiving $packageName via Shizuku...", Toast.LENGTH_SHORT).show()

        // Execute Shizuku process on a background thread to prevent UI thread ANR
        Thread {
            try {
                Log.i(TAG, "Attempting Shizuku archive for package: $packageName")
                
                val process = Shizuku.newProcess(
                    arrayOf("pm", "archive", packageName),
                    null,
                    null
                )

                // Read output & error streams to prevent process buffer deadlocks
                val outputText = process.inputStream.bufferedReader().readText()
                val errorText = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                mainHandler.post {
                    if (exitCode == 0) {
                        Toast.makeText(context, "App $packageName successfully archived", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Shizuku pm archive failed (exit code $exitCode): $errorText")
                        Toast.makeText(context, "Failed to archive app: ${errorText.ifEmpty { outputText }}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error executing Shizuku archive command for $packageName", e)
                mainHandler.post {
                    Toast.makeText(context, "Shizuku archive error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()

        return true
    }
}
