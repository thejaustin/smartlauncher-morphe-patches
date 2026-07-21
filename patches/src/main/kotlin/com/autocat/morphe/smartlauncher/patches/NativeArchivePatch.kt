package com.autocat.morphe.smartlauncher.patches

import app.revanced.patcher.annotation.CompatiblePackage
import app.revanced.patcher.annotation.Patch
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * Morphe patch for Smart Launcher 6:
 * Enables official device support (Android 15+ / Samsung One UI 7) for archiving apps directly through the launcher.
 */
@Patch(
    name = "Official Device App Archiving",
    description = "Enables system PackageInstaller/LauncherApps native archiving on supported devices (e.g. S22 Ultra with One UI / Android 15+).",
    compatiblePackages = []
)
@app.morphe.patcher.annotation.Patch(
    name = "Official Device App Archiving",
    description = "Enables system PackageInstaller/LauncherApps native archiving on supported devices (e.g. S22 Ultra with One UI / Android 15+).",
    compatiblePackages = []
)
class NativeArchivePatch : BasePatch() {

    override val name: String = "Official Device App Archiving"
    override val description: String = "Enables system PackageInstaller/LauncherApps native archiving on supported devices (e.g. S22 Ultra with One UI / Android 15+)."
    override val targetPackage: String = "gin.com.it.smartlauncher"

    /**
     * Target fingerprint for app action handlers in Smart Launcher 6.
     */
    fun matchesAppActionMethod(method: MethodNode): Boolean {
        return method.name.contains("performAppAction") ||
               method.name.contains("handleAppMenuSelection")
    }

    /**
     * Transforms app action bytecode to route native archiving requests.
     */
    override fun transform(classNode: ClassNode) {
        for (method in classNode.methods) {
            if (matchesAppActionMethod(method)) {
                val instructions = InsnList().apply {
                    add(VarInsnNode(Opcodes.ALOAD, 0))
                    add(VarInsnNode(Opcodes.ALOAD, 1))
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "com/autocat/morphe/smartlauncher/helpers/NativeArchiveHelper",
                            "requestNativeArchive",
                            "(Landroid/content/Context;Ljava/lang/String;)Z",
                            false
                        )
                    )
                }

                method.instructions.insert(instructions)
            }
        }
    }
}
