package com.autocat.morphe.smartlauncher.patches.hidearchived

import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.autocat.morphe.smartlauncher.shared.Constants

/**
 * Filters archived apps (Android 15+ app archiving) out of every app list
 * Smart Launcher builds, by replacing `LauncherApps.getActivityList()` call sites
 * in-place with `ArchivedAppFilter.getActivityList()`.
 *
 * In-place 1-to-1 instruction replacement preserves exact bytecode instruction offsets,
 * try-catch boundaries, and coroutine jump targets.
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
            match.instructionMatches.forEach { insnMatch ->
                val invokeIndex = insnMatch.index
                val smali = try {
                    val insn = method.getInstruction<FiveRegisterInstruction>(invokeIndex)
                    val regA = insn.registerC
                    val regB = insn.registerD
                    val regC = insn.registerE
                    "invoke-static {v$regA, v$regB, v$regC}, Lcom/autocat/morphe/smartlauncher/extension/ArchivedAppFilter;->getActivityList(Landroid/content/pm/LauncherApps;Ljava/lang/String;Landroid/os/UserHandle;)Ljava/util/List;"
                } catch (t: Throwable) {
                    val insn = method.getInstruction<RegisterRangeInstruction>(invokeIndex)
                    val start = insn.startRegister
                    val end = start + insn.registerCount - 1
                    "invoke-static/range {v$start .. v$end}, Lcom/autocat/morphe/smartlauncher/extension/ArchivedAppFilter;->getActivityList(Landroid/content/pm/LauncherApps;Ljava/lang/String;Landroid/os/UserHandle;)Ljava/util/List;"
                }

                method.replaceInstruction(invokeIndex, smali)
            }
        }
    }
}
