package com.autocat.morphe.smartlauncher.helpers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.util.Log
import java.io.File

/**
 * Robust runtime helper for Smart Launcher 6.
 * Dynamic context/app resolution to prevent runtime ClassCastException during bytecode injection.
 */
object ArchivedAppFilterHelper {

    private const val TAG = "SmartLauncherMorphe_Filter"
    private const val PREFS_NAME = "smartlauncher_morphe_prefs"
    private const val KEY_HIDE_ARCHIVED_APPS = "experimental_hide_archived_apps"

    // Flag constant for ApplicationInfo.FLAG_ARCHIVED (API 35 / Backported)
    private const val FLAG_ARCHIVED = 1 shl 30 // 0x40000000

    @JvmStatic
    fun isHideArchivedAppsEnabled(context: Context?): Boolean {
        if (context == null) return false
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getBoolean(KEY_HIDE_ARCHIVED_APPS, false)
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Polymorphic evaluation function safe for ASM injection.
     * Accepts generic Objects for target and item to prevent receiver/argument mismatch crashes.
     */
    @JvmStatic
    fun shouldHideApp(targetObj: Any?, appObj: Any?): Boolean {
        val context = resolveContext(targetObj) ?: return false
        if (!isHideArchivedAppsEnabled(context)) return false

        val appInfo = resolveApplicationInfo(appObj) ?: return false
        return isAppArchived(appInfo)
    }

    @JvmStatic
    fun isAppArchived(appInfo: ApplicationInfo): Boolean {
        return try {
            val isArchivedFlag = (appInfo.flags and FLAG_ARCHIVED) != 0
            val isZeroLengthApk = appInfo.sourceDir.isNullOrEmpty() || !File(appInfo.sourceDir).exists()
            isArchivedFlag || isZeroLengthApk
        } catch (e: Throwable) {
            false
        }
    }

    fun resolveContext(obj: Any?): Context? {
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

    private fun resolveApplicationInfo(obj: Any?): ApplicationInfo? {
        if (obj == null) return null
        if (obj is ApplicationInfo) return obj
        if (obj is LauncherActivityInfo) return obj.applicationInfo

        return try {
            val method = obj.javaClass.methods.firstOrNull { 
                it.name == "getApplicationInfo" || it.name == "getAppInfo" || it.name == "getInfo"
            }
            (method?.invoke(obj) as? ApplicationInfo) ?: run {
                // Check for field access if getter is obfuscated
                val field = obj.javaClass.declaredFields.firstOrNull { 
                    ApplicationInfo::class.java.isAssignableFrom(it.type)
                }
                field?.isAccessible = true
                field?.get(obj) as? ApplicationInfo
            }
        } catch (e: Throwable) {
            null
        }
    }
}
