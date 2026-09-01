extension {
    name = "extensions/extension.mpe"
}

android {
    namespace = "com.autocat.morphe.smartlauncher.extension"
    defaultConfig {
        minSdk = 24
        consumerProguardFiles("proguard-rules.pro")
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.rikka.shizuku.api)
}
