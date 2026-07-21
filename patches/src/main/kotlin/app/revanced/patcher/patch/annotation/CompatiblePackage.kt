package app.revanced.patcher.patch.annotation

data class CompatiblePackage(
    val name: String,
    val versions: Set<String> = emptySet()
)
