package com.autocat.morphe.smartlauncher.patches

import app.revanced.patcher.patch.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import org.objectweb.asm.tree.ClassNode

/**
 * Common base patch extending ReVanced & Morphe patcher framework standards.
 */
abstract class BasePatch(
    name: String,
    description: String,
    targetPackage: String
) : BytecodePatch(
    name = name,
    description = description,
    compatiblePackages = setOf(CompatiblePackage(name = targetPackage))
) {
    abstract fun transform(classNode: ClassNode)

    override fun execute(context: BytecodeContext) {
        context.classes.forEach { classNode ->
            transform(classNode)
        }
    }
}
