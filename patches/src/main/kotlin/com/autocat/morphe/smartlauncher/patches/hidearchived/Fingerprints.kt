package com.autocat.morphe.smartlauncher.patches.hidearchived

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

/**
 * Matches every method that calls `LauncherApps.getActivityList(String, UserHandle)`.
 *
 * Verified against Smart Launcher 6 APKs (build 002 and build 017):
 * Matches across all Kotlin coroutine `invokeSuspend` continuations and picker loaders.
 * Omitting the enclosing returnType constraint ensures universal matching across
 * Kotlin suspend functions (which return Ljava/lang/Object;).
 */
object GetActivityListFingerprint : Fingerprint(
    filters = listOf(
        methodCall(
            smali = "Landroid/content/pm/LauncherApps;->getActivityList(Ljava/lang/String;Landroid/os/UserHandle;)Ljava/util/List;",
        ),
    ),
)
