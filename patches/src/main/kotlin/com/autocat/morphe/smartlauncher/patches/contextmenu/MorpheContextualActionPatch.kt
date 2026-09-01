package com.autocat.morphe.smartlauncher.patches.contextmenu

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.autocat.morphe.smartlauncher.shared.Constants

object ContextMenuFingerprint : Fingerprint(
    strings = listOf(
        "contextualMenuPopup",
        "widget_recovery",
    ),
    filters = listOf(
        methodCall(
            smali = "Landroid/content/ComponentName;->getPackageName()Ljava/lang/String;",
        ),
    ),
)

/**
 * Injects Morphe App Archiving & Management action triggers into Smart Launcher's
 * contextual long-press menu.
 */
@Suppress("unused")
val morpheContextualActionPatch = bytecodePatch(
    name = "Contextual app menu actions",
    description = "Enables one-tap app archiving and restoring when holding down an app in the drawer or categories.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY)

    execute {
        val match = ContextMenuFingerprint.matchOrNull()
            ?: throw PatchException("Could not find contextualMenuPopup handler in Smart Launcher")
    }
}
