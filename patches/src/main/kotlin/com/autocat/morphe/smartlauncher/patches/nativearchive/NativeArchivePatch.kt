package com.autocat.morphe.smartlauncher.patches.nativearchive

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.autocat.morphe.smartlauncher.shared.Constants

/**
 * NOT YET IMPLEMENTED - see ShizukuArchivePatch.kt for the full writeup of
 * what was found. Summary for this one specifically: the runtime call this
 * patch needs, `PackageInstaller.requestArchive(String, IntentSender)`
 * (Android 15+, verified against the real AOSP source), is implemented and
 * ready in NativeArchiveHelper - what's missing is the same safe injection
 * point into Smart Launcher's Compose-based app-icon menu that
 * ShizukuArchivePatch also needs, which wasn't confirmed against the real
 * APK in the time available for this pass.
 */
@Suppress("unused")
val nativeArchivePatch = bytecodePatch(
    name = "Native app archiving",
    description = "Not yet implemented - see source comments for what was verified and what's still needed.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        throw PatchException(
            "Native app archiving is not implemented yet. NativeArchiveHelper.requestArchive() is ready " +
                "and verified, but the context-menu injection point still needs to be found - see NativeArchivePatch.kt.",
        )
    }
}
