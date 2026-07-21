package app.morphe.patcher.patch

import app.morphe.patcher.patch.annotation.CompatiblePackage

abstract class BytecodePatch(
    val name: String = "",
    val description: String? = null,
    val compatiblePackages: Set<CompatiblePackage> = emptySet(),
    val use: Boolean = true
) {
    abstract fun execute(context: BytecodeContext)
}
