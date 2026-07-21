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
 * Integrates Shizuku support to archive apps directly from the launcher context popup menu.
 */
@Patch(
    name = "Shizuku App Archiving",
    description = "Adds a context menu item to archive apps via Shizuku (`pm archive`) in Smart Launcher 6.",
    compatiblePackages = [CompatiblePackage(name = "gin.com.it.smartlauncher")]
)
@app.morphe.patcher.annotation.Patch(
    name = "Shizuku App Archiving",
    description = "Adds a context menu item to archive apps via Shizuku (`pm archive`) in Smart Launcher 6.",
    compatiblePackages = [app.morphe.patcher.annotation.CompatiblePackage(name = "gin.com.it.smartlauncher")]
)
class ShizukuArchivePatch : BasePatch(
    name = "Shizuku App Archiving",
    description = "Adds a context menu item to archive apps via Shizuku (`pm archive`) in Smart Launcher 6.",
    targetPackage = "gin.com.it.smartlauncher"
) {

    fun matchesContextMenuMethod(method: MethodNode): Boolean {
        val isStatic = (method.access and Opcodes.ACC_STATIC) != 0
        return !isStatic && (
            method.desc.contains("Ljava/lang/String;") ||
            method.desc.contains("Landroid/content/Context;") ||
            method.desc.contains("Landroid/view/View;")
        )
    }

    override fun transform(classNode: ClassNode) {
        for (method in classNode.methods) {
            if (matchesContextMenuMethod(method)) {
                val instructions = InsnList().apply {
                    add(VarInsnNode(Opcodes.ALOAD, 0))
                    add(VarInsnNode(Opcodes.ALOAD, 1))
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "com/autocat/morphe/smartlauncher/helpers/ShizukuArchiveHelper",
                            "archiveAppWithShizuku",
                            "(Ljava/lang/Object;Ljava/lang/String;)Z",
                            false
                        )
                    )
                    add(InsnNode(Opcodes.POP))
                }

                method.instructions.insert(instructions)
            }
        }
    }
}
