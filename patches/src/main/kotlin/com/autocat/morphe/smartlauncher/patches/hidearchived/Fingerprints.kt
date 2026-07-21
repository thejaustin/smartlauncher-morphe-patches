package com.autocat.morphe.smartlauncher.patches.hidearchived

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

/**
 * Matches every method that calls `LauncherApps.getActivityList(String, UserHandle)`.
 *
 * Verified against the real Smart Launcher 6 v6.6 build 002 APK (ginlemon.flowerfree)
 * via jadx + apktool: three call sites exist in that build (one each for the app
 * drawer, the add-to-home-screen app picker, and the shortcut picker - all compiled
 * as separate Kotlin coroutine `invokeSuspend` continuations). Obfuscated class and
 * method names are expected to change every release, so this fingerprint matches
 * purely on the framework API call being invoked, never on an app-internal
 * identifier, per the Morphe fingerprinting guidance.
 */
object GetActivityListFingerprint : Fingerprint(
    returnType = "Ljava/util/List;",
    filters = listOf(
        methodCall(
            smali = "Landroid/content/pm/LauncherApps;->getActivityList(Ljava/lang/String;Landroid/os/UserHandle;)Ljava/util/List;",
        ),
    ),
)
