plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    compileOnly("com.google.android:android:4.1.1.4")
    compileOnly("org.ow2.asm:asm-tree:9.6")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

tasks.named<Jar>("jar") {
    archiveFileName.set("smartlauncher-morphe-patches.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output) {
        exclude("android/**")
        exclude("rikka/**")
    }
    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Patch-Bundle-Name" to "Smart Launcher 6 Morphe Patches",
            "Patch-Bundle-Version" to "1.0.0",
            "Patch-Bundle-Author" to "AutoCat Development",
            "Main-Class" to "com.autocat.morphe.smartlauncher.SmartLauncherPatchBundle"
        )
    }
}

val createMppPackage = tasks.register<Jar>("mppPackage") {
    archiveFileName.set("smartlauncher-morphe-patches.mpp")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output) {
        exclude("android/**")
        exclude("rikka/**")
    }
    manifest {
        attributes(
            "Manifest-Version" to "1.0",
            "Patch-Bundle-Name" to "Smart Launcher 6 Morphe Patches",
            "Patch-Bundle-Version" to "1.0.0",
            "Patch-Bundle-Author" to "AutoCat Development",
            "Main-Class" to "com.autocat.morphe.smartlauncher.SmartLauncherPatchBundle"
        )
    }
}

tasks.register("buildAndroid") {
    dependsOn(createMppPackage)
}
