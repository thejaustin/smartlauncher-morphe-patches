package com.autocat.morphe.smartlauncher.patches

import app.revanced.patcher.annotation.CompatiblePackage
import app.revanced.patcher.annotation.Patch
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * Morphe patch for Smart Launcher 6:
 * Adds an experimental setting to hide all archived apps from the app drawer.
 */
@Patch(
    name = "Hide Archived Apps Toggle",
    description = "Adds an experimental setting to Smart Launcher 6 to filter out archived apps from the app drawer.",
    compatiblePackages = [CompatiblePackage(name = "gin.com.it.smartlauncher")]
)
@app.morphe.patcher.annotation.Patch(
    name = "Hide Archived Apps Toggle",
    description = "Adds an experimental setting to Smart Launcher 6 to filter out archived apps from the app drawer.",
    compatiblePackages = [app.morphe.patcher.annotation.CompatiblePackage(name = "gin.com.it.smartlauncher")]
)
class HideArchivedAppsPatch : BasePatch(
    name = "Hide Archived Apps Toggle",
    description = "Adds an experimental setting to Smart Launcher 6 to filter out archived apps from the app drawer.",
    targetPackage = "gin.com.it.smartlauncher"
) {

    /**
     * Target fingerprint criteria for Smart Launcher app drawer filter methods.
     */
    fun matchesFilterMethod(method: MethodNode): Boolean {
        return method.desc.contains("Landroid/content/pm/ApplicationInfo;") ||
               method.desc.contains("Landroid/content/pm/LauncherActivityInfo;")
    }

    /**
     * Executes bytecode injection into the matched app filter method.
     */
    override fun transform(classNode: ClassNode) {
        for (method in classNode.methods) {
            if (matchesFilterMethod(method)) {
                val skipLabel = LabelNode()
                val instructions = InsnList().apply {
                    add(VarInsnNode(Opcodes.ALOAD, 0))
                    add(VarInsnNode(Opcodes.ALOAD, 1))
                    add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "com/autocat/morphe/smartlauncher/helpers/ArchivedAppFilterHelper",
                            "shouldHideApp",
                            "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                            false
                        )
                    )
                    add(JumpInsnNode(Opcodes.IFEQ, skipLabel))
                    if (method.desc.endsWith("Z")) {
                        add(InsnNode(Opcodes.ICONST_0))
                        add(InsnNode(Opcodes.IRETURN))
                    } else {
                        add(InsnNode(Opcodes.RETURN))
                    }
                    add(skipLabel)
                }
                
                method.instructions.insert(instructions)
            }
        }
    }
}
