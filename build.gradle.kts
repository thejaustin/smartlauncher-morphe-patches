plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("buildAndroid") {
    dependsOn(":patches:buildAndroid")
}
