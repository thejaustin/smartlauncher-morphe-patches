package app.revanced.patcher.patch

import org.objectweb.asm.tree.ClassNode

interface BytecodeContext {
    val classes: Iterable<ClassNode>
}
