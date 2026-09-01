extension {
    name = "extensions/extension.mpe"
}

android {
    namespace = "com.autocat.morphe.smartlauncher.extension"
    defaultConfig {
        minSdk = 24
    }
}

configurations.all {
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains")
}
