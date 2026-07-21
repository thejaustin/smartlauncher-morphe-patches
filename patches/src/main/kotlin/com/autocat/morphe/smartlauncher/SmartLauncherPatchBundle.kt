package com.autocat.morphe.smartlauncher

import com.autocat.morphe.smartlauncher.patches.HideArchivedAppsPatch
import com.autocat.morphe.smartlauncher.patches.NativeArchivePatch
import com.autocat.morphe.smartlauncher.patches.ShizukuArchivePatch

/**
 * Registry bundle for Smart Launcher 6 Morphe patches.
 */
object SmartLauncherPatchBundle {

    val patches = listOf(
        HideArchivedAppsPatch(),
        ShizukuArchivePatch(),
        NativeArchivePatch()
    )

    fun printSummary() {
        println("==================================================")
        println(" Smart Launcher 6 - Morphe Patch Suite")
        println(" Target Device Focus: Samsung Galaxy S22 Ultra (One UI)")
        println("==================================================")
        patches.forEachIndexed { index, patch ->
            println("${index + 1}. [${patch.name}]")
            println("   Description: ${patch.description}")
            println("   Target: ${patch.compatiblePackages.firstOrNull()?.name ?: "gin.com.it.smartlauncher"}")
        }
        println("==================================================")
    }
}
