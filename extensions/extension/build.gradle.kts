extension {
    name = "extensions/extension.mpe"
}

android {
    namespace = "com.autocat.morphe.smartlauncher.extension"
    defaultConfig {
        minSdk = 24
    }
}

dependencies {
    implementation(libs.rikka.shizuku.api)
}
