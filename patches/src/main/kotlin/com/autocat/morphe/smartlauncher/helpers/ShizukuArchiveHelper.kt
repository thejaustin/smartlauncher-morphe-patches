package com.autocat.morphe.smartlauncher.helpers

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import rikka.shizuku.Shizuku

/**
 * Injected helper class for archiving apps via Shizuku on Smart Launcher 6.
 * Accepts polymorphic target instances (View/Context) to prevent bytecode type mismatch VerifyErrors.
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
     * Asynchronously archives the given package using Shizuku privileges.
     * Accepts generic target object (View, Context, or Fragment) and resolves Context safely.
     */
    @JvmStatic
    fun archiveAppWithShizuku(targetObj: Any?, packageName: String): Boolean {
        val context = resolveContext(targetObj)
        if (context == null) {
            Log.e(TAG, "Unable to resolve Context from targetObj: $targetObj")
            return false
        }

        if (!isShizukuAvailable()) {
            Toast.makeText(context, "Shizuku is not running or permission denied", Toast.LENGTH_SHORT).show()
            return false
        }

        val mainHandler = Handler(Looper.getMainLooper())
        Toast.makeText(context, "Archiving $packageName via Shizuku...", Toast.LENGTH_SHORT).show()

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

    private fun resolveContext(obj: Any?): Context? {
        if (obj == null) return null
        if (obj is Context) return obj

        return try {
            val method = obj.javaClass.methods.firstOrNull { 
                it.name == "getContext" || it.name == "getApplicationContext" 
            }
            method?.invoke(obj) as? Context
        } catch (e: Throwable) {
            null
        }
    }
}
