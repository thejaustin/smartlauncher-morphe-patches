package com.autocat.morphe.smartlauncher.patches

import org.objectweb.asm.tree.ClassNode

/**
 * Base bytecode patch implementation compatible with Morphe / ReVanced patcher engines.
 */
abstract class BasePatch : app.revanced.patcher.patch.BytecodePatch() {
    abstract val name: String
    abstract val description: String
    abstract val targetPackage: String
    
    abstract fun transform(classNode: ClassNode)

    override fun execute(context: Any) {
        if (context is ClassNode) {
            transform(context)
        }
    }
}
