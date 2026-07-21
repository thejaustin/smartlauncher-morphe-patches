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
    fun archiveAppWithShizuku(targetObj: Any?, packageName: String): Boolean {
        val context = ArchivedAppFilterHelper.resolveContext(targetObj)
        if (context == null) {
            Log.e(TAG, "Unable to resolve Context from targetObj: $targetObj")
            return false
        }

        if (!isShizukuAvailable()) {
            safeShowToast(context, "Shizuku is not running or permission denied", Toast.LENGTH_SHORT)
            return false
        }

        val mainHandler = Handler(Looper.getMainLooper())
        safeShowToast(context, "Archiving $packageName via Shizuku...", Toast.LENGTH_SHORT)

        Thread {
            try {
                Log.i(TAG, "Attempting Shizuku archive for package: $packageName")
                
                val process = Shizuku.newProcess(
                    arrayOf("pm", "archive", packageName),
                    null,
                    null
                )

                val outputText = process.inputStream.bufferedReader().readText()
                val errorText = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                mainHandler.post {
                    if (exitCode == 0) {
                        safeShowToast(context, "App $packageName successfully archived", Toast.LENGTH_SHORT)
                    } else {
                        Log.e(TAG, "Shizuku pm archive failed (exit code $exitCode): $errorText")
                        safeShowToast(context, "Failed to archive app: ${errorText.ifEmpty { outputText }}", Toast.LENGTH_LONG)
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error executing Shizuku archive command for $packageName", e)
                mainHandler.post {
                    safeShowToast(context, "Shizuku archive error: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                }
            }
        }.start()

        return true
    }

    private fun safeShowToast(context: Context, text: String, duration: Int) {
        try {
            Toast.makeText(context, text, duration).show()
        } catch (e: Throwable) {
            Log.w(TAG, "Toast display failed: $text", e)
        }
    }
}
