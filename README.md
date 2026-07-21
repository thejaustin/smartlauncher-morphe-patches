# Smart Launcher 6 - Morphe Patch Suite

Custom Morphe / ReVanced-style patch project for **Smart Launcher 6**, optimized for devices like the **Samsung Galaxy S22 Ultra** (One UI / Android 14+ / Android 15+).

---

## 🌟 Features Included

### 1. 🙈 Toggle Archived Apps as Hidden (`HideArchivedAppsPatch`)
- Adds an experimental setting preference key (`experimental_hide_archived_apps`).
- Filters out all archived apps (`ApplicationInfo.FLAG_ARCHIVED` / zero-byte package entries) from the app drawer.

### 2. ⚡ Shizuku App Archiving (`ShizukuArchivePatch`)
- Integrates **Shizuku** support directly into Smart Launcher's app popup menu.
- Executes `pm archive <package>` with Shizuku elevated binder privileges.
- Allows user-triggered app archiving directly from the launcher context menu on Samsung Galaxy S22 Ultra and other Shizuku-supported devices.

### 3. 📱 Official Device App Archiving (`NativeArchivePatch`)
- Enables native OS app archiving support via `PackageInstaller.requestArchive()` / `LauncherApps.archiveApp()`.
- Built for official device archiving on Android 15+ / Samsung One UI 7.

---

## 🏗️ Project Architecture

```
smartlauncher-morphe-patches/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/libs.versions.toml
└── patches/
    └── src/main/kotlin/com/autocat/morphe/smartlauncher/
        ├── SmartLauncherPatchBundle.kt
        ├── helpers/
        │   ├── ArchivedAppFilterHelper.kt
        │   ├── ShizukuArchiveHelper.kt
        │   └── NativeArchiveHelper.kt
        └── patches/
            ├── HideArchivedAppsPatch.kt
            ├── ShizukuArchivePatch.kt
            └── NativeArchivePatch.kt
```

---

## 🛠️ Usage & Patching Instructions

1. **Building the Patch JAR:**
   ```bash
   bash ./gradlew assemble
   ```

2. **Applying with Morphe / ReVanced CLI:**
   ```bash
   java -jar morphe-cli.jar patch \
     --patch-bundle patches/build/libs/patches-all.jar \
     --apk smartlauncher6.apk \
     --out smartlauncher6-patched.apk
   ```

3. **Shizuku Setup on Galaxy S22 Ultra:**
   - Ensure Shizuku is running via Wireless Debugging or ADB (`sh /sdcard/Android/data/moe.shizuku.privileged.api/files/start.sh`).
   - Grant Shizuku permission when requested by Smart Launcher.
