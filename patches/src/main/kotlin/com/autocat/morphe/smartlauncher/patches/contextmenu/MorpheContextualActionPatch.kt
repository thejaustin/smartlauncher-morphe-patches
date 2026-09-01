package com.autocat.morphe.smartlauncher.patches.contextmenu

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.autocat.morphe.smartlauncher.shared.Constants

object ContextMenuFingerprint : Fingerprint(
    strings = listOf(
        "android.intent.action.DELETE",
        "android.intent.extra.USER",
    ),
    filters = listOf(
        methodCall(
            smali = "Landroid/content/Context;->startActivity(Landroid/content/Intent;)V",
        ),
    ),
)

/**
 * Injects one-tap Archiving & Unarchiving options directly into the long-press
 * app popup menu (the same menu used for uninstalling apps).
 */
@Suppress("unused")
val morpheContextualActionPatch = bytecodePatch(
    name = "Contextual app menu actions",
    description = "Allows archiving and unarchiving apps directly from the long-press popup menu (where Uninstall is located).",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val match = ContextMenuFingerprint.matchOrNull()
            ?: throw PatchException("Could not find popup menu uninstall handler in Smart Launcher")

        val method = match.method
        val matchIndex = match.instructionMatches.first().index

        val insn = method.getInstruction<FiveRegisterInstruction>(matchIndex)
        val regContext = insn.registerC
        val regIntent = insn.registerD

        method.replaceInstruction(
            matchIndex,
            "invoke-static {v$regContext, v$regIntent}, Lcom/autocat/morphe/smartlauncher/extension/MorpheMenuInjector;->handleUninstallOrArchive(Landroid/content/Context;Landroid/content/Intent;)V",
        )
    }
}
