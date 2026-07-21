package com.autocat.morphe.smartlauncher.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/**
 * Real applicationId confirmed via `aapt dump badging` against the actual
 * Smart Launcher 6 APK (v6.6 build 002) - it is "ginlemon.flowerfree", not
 * "gin.com.it.smartlauncher" as every patch in this repo previously assumed.
 * That wrong package name alone would have kept every patch from ever
 * matching the installed app, independent of any other issue.
 */
internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Smart Launcher 6",
        packageName = "ginlemon.flowerfree",
        targets = listOf(AppTarget("6.6 build 002"))
    )
}
