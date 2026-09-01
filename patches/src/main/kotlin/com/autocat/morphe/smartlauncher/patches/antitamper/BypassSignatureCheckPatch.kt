package com.autocat.morphe.smartlauncher.patches.antitamper

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import com.autocat.morphe.smartlauncher.shared.Constants

object SignatureCheckFingerprint : Fingerprint(
    filters = listOf(
        string("Not genuine apk. This may not stop humans but may stop machines."),
        methodCall(
            smali = "Ljava/lang/System;->exit(I)V",
        ),
    ),
)

/**
 * Neutralizes Smart Launcher's internal anti-tamper signature verification check.
 * Smart Launcher checks its APK signing certificate against hardcoded hashes and
 * executes System.exit(0) when signed with a custom key.
 */
@Suppress("unused")
val bypassSignatureCheckPatch = bytecodePatch(
    name = "Bypass signature check",
    description = "Neutralizes Smart Launcher's internal anti-tamper signature verification to prevent instant shutdown on launch.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val match = SignatureCheckFingerprint.matchOrNull()
            ?: throw PatchException("Could not find Smart Launcher signature verification call site")

        val method = match.method
        match.instructionMatches.forEach { insnMatch ->
            val idx = insnMatch.index
            method.replaceInstruction(idx, "nop")
        }
    }
}
