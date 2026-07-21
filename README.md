# 🚀 Smart Launcher 6 - Morphe Patch Suite

[![Add to Morphe](https://img.shields.io/badge/Add%20to-Morphe-7C3AED?style=for-the-badge&logo=android&logoColor=white)](https://morphe.software/add-source?github=thejaustin/smartlauncher-morphe-patches)
[![Download .mpp](https://img.shields.io/badge/Download-.mpp%20Package-10B981?style=for-the-badge&logo=android&logoColor=white)](https://github.com/thejaustin/smartlauncher-morphe-patches/releases/latest/download/smartlauncher-morphe-patches.mpp)
[![Build Status](https://img.shields.io/github/actions/workflow/status/thejaustin/smartlauncher-morphe-patches/release.yml?branch=main&style=for-the-badge&logo=github)](https://github.com/thejaustin/smartlauncher-morphe-patches/actions)

Custom **Morphe Patch Package (`.mpp`)** suite for **Smart Launcher 6**, specially designed for devices like the **Samsung Galaxy S22 Ultra** (One UI / Android 14+ / Android 15+).

---

## 📲 Quick Installation

### Method 1: Add Source to Morphe Manager (Recommended)
Choose any of the options below on your Android device:

- 🌐 [**Option A: Add Source via Morphe Web Portal**](https://morphe.software/add-source?github=thejaustin/smartlauncher-morphe-patches)
- 📱 [**Option B: Add Source via Morphe App Deep Link (`morphe-manager://`)**](morphe-manager://add-source?url=https://raw.githubusercontent.com/thejaustin/smartlauncher-morphe-patches/main/patches-bundle.json)
- ⚡ [**Option C: Add Source via Morphe Protocol (`morphe://`)**](morphe://add-source?github=thejaustin/smartlauncher-morphe-patches)

---

### Method 2: Manual Import
1. Download the latest **[`smartlauncher-morphe-patches.mpp`](https://github.com/thejaustin/smartlauncher-morphe-patches/releases/latest/download/smartlauncher-morphe-patches.mpp)** release file.
2. Open **Morphe Manager** on your Android device.
3. On the **Home Screen**, tap **Import Patches** (or **+**).
4. Select `smartlauncher-morphe-patches.mpp` from your **Downloads** folder.
5. Select **Smart Launcher 6** APK and apply your desired patches!

---

## 🌟 Included Patches

### 1. 🙈 Hide archived apps (`hideArchivedAppsPatch`) - implemented
- Filters archived apps (Android 15+ app archiving) out of the app drawer, the add-to-home-screen picker, and the shortcut picker.
- Applying the patch is the toggle - there's no separate in-app setting.
- Not yet verified on-device; see the "Development status" section below.

### 2. ⚡ Shizuku app archiving (`shizukuArchivePatch`) - not implemented
### 3. 📱 Native app archiving (`nativeArchivePatch`) - not implemented

Both throw a clear error if selected. See the doc comments in
`ShizukuArchivePatch.kt` / `NativeArchivePatch.kt` for exactly what was
verified against the real APK and what's still missing (a safe injection
point into Smart Launcher's Compose-based long-press menu, and for Shizuku
specifically, a bound UserService since `Shizuku.newProcess` is private in
the current API).

## 🚧 Development status

This repo was rebuilt from scratch to use the real `app.morphe.patches`
Gradle plugin and `app.morphe:morphe-patcher` API (fingerprints against
dexlib2/smali) - the version before this used hand-written stub annotations
and plain-JVM ASM bytecode editing, which is not what Morphe's patch loader
or Android's dex format actually use, so no patch it produced was ever
discoverable by Morphe regardless of how it was bug-fixed. The compatible
package was also wrong (`gin.com.it.smartlauncher` instead of the real
`ginlemon.flowerfree`, confirmed via `aapt dump badging` against the actual
APK).

`hideArchivedAppsPatch`'s fingerprint and injection point are based on
decompiling the real Smart Launcher 6 v6.6 build 002 APK (jadx + apktool),
not guessed - but it has not been applied to a real APK and run on a device
yet. Building the .mpp and testing it in Morphe Manager against a real
install is the next step.

---

## 🛠️ Local Development & Building

Building the `.mpp` patch container locally:

```bash
# Clone the repository
git clone https://github.com/thejaustin/smartlauncher-morphe-patches.git
cd smartlauncher-morphe-patches

# Build Morphe Patch Package (.mpp)
./gradlew buildAndroid
```

The output `.mpp` file will be generated at:
`patches/build/libs/smartlauncher-morphe-patches.mpp`
