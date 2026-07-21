package com.autocat.morphe.smartlauncher.patches

import app.revanced.patcher.annotation.CompatiblePackage
import app.revanced.patcher.annotation.Patch
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
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
    compatiblePackages = [CompatiblePackage(name = "gin.com.it.smartlauncher")]
)
@app.morphe.patcher.annotation.Patch(
    name = "Official Device App Archiving",
    description = "Enables system PackageInstaller/LauncherApps native archiving on supported devices (e.g. S22 Ultra with One UI / Android 15+).",
    compatiblePackages = [app.morphe.patcher.annotation.CompatiblePackage(name = "gin.com.it.smartlauncher")]
)
class NativeArchivePatch : BasePatch(
    name = "Official Device App Archiving",
    description = "Enables system PackageInstaller/LauncherApps native archiving on supported devices (e.g. S22 Ultra with One UI / Android 15+).",
    targetPackage = "gin.com.it.smartlauncher"
) {

    /**
     * Target fingerprint for app action handlers in Smart Launcher 6.
     */
    fun matchesAppActionMethod(method: MethodNode): Boolean {
        val isStatic = (method.access and Opcodes.ACC_STATIC) != 0
        return !isStatic && (
            method.desc.contains("Landroid/content/Context;Ljava/lang/String;") ||
            method.desc.contains("Landroid/view/View;Ljava/lang/String;")
        )
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
                    // Pop returned boolean off stack to prevent stack imbalance VerifyError
                    add(InsnNode(Opcodes.POP))
                }

                method.instructions.insert(instructions)
            }
        }
    }
}
