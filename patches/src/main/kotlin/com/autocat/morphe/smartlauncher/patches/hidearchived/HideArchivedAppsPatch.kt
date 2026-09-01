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
 */
@Suppress("unused")
val hideArchivedAppsPatch = bytecodePatch(
    name = "Hide archived apps",
    description = "Filters archived apps out of the app drawer, the add-to-home-screen picker, and the shortcut picker.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val matches = GetActivityListFingerprint.matchAllOrNull()
            ?: throw PatchException("Could not find any LauncherApps.getActivityList() call sites")

        matches.forEach { match ->
            val method = match.method
            // Process match instruction occurrences in reverse order to preserve preceding instruction offsets
            match.instructionMatches.reversed().forEach { insnMatch ->
                val invokeIndex = insnMatch.index

                // The instruction immediately after the invoke is move-result-object <reg>
                val resultRegister = method.getInstruction<OneRegisterInstruction>(invokeIndex + 1).registerA

                method.addInstructions(
                    invokeIndex + 2,
                    """
                        invoke-static/range {v$resultRegister .. v$resultRegister}, Lcom/autocat/morphe/smartlauncher/extension/ArchivedAppFilter;->filter(Ljava/util/List;)Ljava/util/List;
                        move-result-object v$resultRegister
                    """.trimIndent(),
                )
            }
        }
    }
}
