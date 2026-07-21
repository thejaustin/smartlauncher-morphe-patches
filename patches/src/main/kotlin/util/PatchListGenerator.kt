package util

import app.morphe.patcher.patch.Patch
import app.morphe.patcher.patch.loadPatchesFromJar
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import java.net.URLClassLoader
import java.util.jar.Manifest

/**
 * Regenerates ../../patches-list.json from the built .mpp - a human-facing
 * listing of patch names/descriptions for the repo README and Morphe's
 * "Add Source" preview. This has nothing to do with whether Morphe can load
 * or apply the patches: that happens purely by reflecting over the built
 * jar for Patch<*> instances at patch time (loadPatchesFromJar, same as
 * used below). Run manually with `./gradlew :patches:generatePatchesList`
 * after a build; this repo does not wire it into an automated release step.
 */
internal fun main() {
    val patchFile = File("build/libs/").listFiles { file ->
        val fileName = file.name
        !fileName.contains("javadoc") && !fileName.contains("sources") && fileName.endsWith(".mpp")
    }?.firstOrNull() ?: error("No .mpp found in build/libs/ - run the build task first")

    val loadedPatches = loadPatchesFromJar(setOf(patchFile))
    val patchClassLoader = URLClassLoader(arrayOf(patchFile.toURI().toURL()))
    val manifests = patchClassLoader.getResources("META-INF/MANIFEST.MF")

    while (manifests.hasMoreElements()) {
        Manifest(manifests.nextElement().openStream())
            .mainAttributes
            .getValue("Version")
            ?.let { generatePatchList(it, loadedPatches) }
    }
}

@Suppress("DEPRECATION")
private fun generatePatchList(version: String, patches: Set<Patch<*>>) {
    val patchesMap = patches.sortedBy { it.name }.map {
        JsonPatch(
            it.name!!,
            it.description,
            it.use,
            it.dependencies.map { dependency -> dependency.javaClass.simpleName },
            it.compatiblePackages?.associate { (packageName, versions) -> packageName to versions },
        )
    }

    val gson = GsonBuilder()
        .serializeNulls()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()

    val jsonObject = JsonObject()
    jsonObject.addProperty("version", "v$version")
    jsonObject.add("patches", gson.toJsonTree(patchesMap))

    File("../patches-list.json").writeText(gson.toJson(jsonObject))
}

@Suppress("unused")
private class JsonPatch(
    val name: String? = null,
    val description: String? = null,
    val use: Boolean = true,
    val dependencies: List<String>,
    val compatiblePackages: Map<String, Set<String>?>? = null,
)
