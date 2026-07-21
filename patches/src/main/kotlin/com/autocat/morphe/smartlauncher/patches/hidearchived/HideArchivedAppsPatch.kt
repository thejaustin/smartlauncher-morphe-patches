package com.autocat.morphe.smartlauncher.patches.hidearchived

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.autocat.morphe.smartlauncher.shared.Constants

/**
 * Filters archived apps (Android 15+ app archiving) out of every app list
 * Smart Launcher builds, by wrapping the result of every
 * `LauncherApps.getActivityList()` call site with
 * [com.autocat.morphe.smartlauncher.extension.ArchivedAppFilter.filter].
 *
 * Applying the patch is the toggle - there is no in-app setting, to avoid
 * also having to inject a new row into Smart Launcher's (Compose-based)
 * settings screen for a single boolean.
 */
@Suppress("unused")
val hideArchivedAppsPatch = bytecodePatch(
    name = "Hide archived apps",
    description = "Filters archived apps out of the app drawer, the add-to-home-screen picker, and the shortcut picker.",
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val matches = GetActivityListFingerprint.matchAllOrNull()
            ?: throw PatchException("Could not find any LauncherApps.getActivityList() call sites")

        matches.forEach { match ->
            val invokeIndex = match.instructionMatches.first().index
            val method = match.method

            // The instruction immediately after the invoke is always
            // move-result-object <reg> - verified against all 3 real call
            // sites in Smart Launcher 6 v6.6 build 002 (app drawer, app
            // picker, shortcut picker), each compiled as a distinct Kotlin
            // coroutine continuation with its own register allocation.
            val resultRegister = method.getInstruction<OneRegisterInstruction>(invokeIndex + 1).registerA

            method.addInstructions(
                invokeIndex + 2,
                """
                    invoke-static {v$resultRegister}, Lcom/autocat/morphe/smartlauncher/extension/ArchivedAppFilter;->filter(Ljava/util/List;)Ljava/util/List;
                    move-result-object v$resultRegister
                """.trimIndent(),
            )
        }
    }
}
