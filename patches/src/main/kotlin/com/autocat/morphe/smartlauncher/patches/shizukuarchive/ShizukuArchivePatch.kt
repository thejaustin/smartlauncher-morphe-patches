package com.autocat.morphe.smartlauncher.patches.shizukuarchive

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.autocat.morphe.smartlauncher.shared.Constants

/**
 * NOT YET IMPLEMENTED - kept as a real, discoverable (but disabled-by-default)
 * patch definition rather than deleted, so the next session has a concrete
 * starting point instead of another guess.
 *
 * What's confirmed against the real, decompiled Smart Launcher 6 v6.6 build 002
 * APK (ginlemon.flowerfree):
 *  - The app-icon long-press action menu (Uninstall / Rename / Hide / ...) is
 *    built by a Jetpack Compose composable. One candidate was located: a method
 *    in an R8-merged utility class referencing R.string.uninstall
 *    (resource id 0x7f13066b) with a signature shape of
 *    `(CoroutineScope, Context, View, <2 obfuscated types>) -> Unit`.
 *  - Injecting a *new* menu entry into compiled Compose UI (recomposition
 *    scopes, slot tables, synthetic lambda classes) is a materially different
 *    and much higher-risk kind of bytecode patch than a plain-View popup menu,
 *    and was not something to do blind without on-device Morphe testing.
 *  - Separately, the privileged execution mechanism this patch needs
 *    (running "pm archive <package>" via Shizuku) is not available through
 *    the current Shizuku API's public surface - see ShizukuArchiveHelper.
 *
 * Next steps for whoever picks this up: use jadx-gui interactively to confirm
 * the exact Compose menu-item list construction, add the new item there
 * (likely appending to a List<MenuAction>-style data list rather than
 * patching the composable tree directly, if such a data layer exists), and
 * implement a real Shizuku UserService for the privileged call.
 */
@Suppress("unused")
val shizukuArchivePatch = bytecodePatch(
    name = "Shizuku app archiving",
    description = "Not yet implemented - see source comments for what was verified and what's still needed.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        throw PatchException(
            "Shizuku app archiving is not implemented yet. The context-menu injection point and the " +
                "Shizuku privileged-execution path both need further work - see ShizukuArchivePatch.kt.",
        )
    }
}
