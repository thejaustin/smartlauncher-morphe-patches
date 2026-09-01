package com.autocat.morphe.smartlauncher.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/**
 * Target compatibility configuration for Smart Launcher 6 (ginlemon.flowerfree).
 * Covers Smart Launcher 6 builds (6.6 through 6.7+).
 */
internal object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Smart Launcher 6",
        packageName = "ginlemon.flowerfree",
        targets = listOf(
            AppTarget("6.6 build 002"),
            AppTarget("6.6 build 003"),
            AppTarget("6.6 build 004"),
            AppTarget("6.6 build 005"),
            AppTarget("6.6 build 006"),
            AppTarget("6.6 build 007"),
            AppTarget("6.6 build 008"),
            AppTarget("6.6 build 009"),
            AppTarget("6.6 build 010"),
            AppTarget("6.7 build 001"),
            AppTarget("6.7 build 002"),
            AppTarget("6.7 build 003"),
            AppTarget("6.7 build 004"),
            AppTarget("6.7 build 005")
        )
    )
}
