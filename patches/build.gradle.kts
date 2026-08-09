group = "com.autocat.morphe.smartlauncher"
version = "1.0.6"

base {
    archivesName.set("smartlauncher-morphe-patches")
}

patches {
    about {
        name = "Smart Launcher 6 Morphe Patches"
        description = "Custom patches for Smart Launcher 6, focused on app-archiving support."
        source = "git@github.com:thejaustin/smartlauncher-morphe-patches.git"
        author = "thejaustin"
        contact = "na"
        website = "https://github.com/thejaustin/smartlauncher-morphe-patches"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath: Configuration by configurations.creating

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    // Optional convenience: regenerates ../patches-list.json (a human-facing
    // listing of patch names/descriptions for the repo README / Morphe's
    // "Add Source" preview). Not required for Morphe to load or apply the
    // .mpp - that happens purely via reflection over the built jar, see
    // util/PatchListGenerator.kt. This repo doesn't use semantic-release,
    // so unlike the official Morphe template this isn't wired to a
    // "publish" task - run manually with `./gradlew :patches:generatePatchesList`.
    register<JavaExec>("generatePatchesList") {
        description = "Regenerate patches-list.json from the built patch bundle"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }
}
