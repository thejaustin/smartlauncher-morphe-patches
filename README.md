# 🚀 Smart Launcher 6 - Morphe Patch Suite

[![Add to Morphe](https://img.shields.io/badge/Add%20to-Morphe-7C3AED?style=for-the-badge&logo=android&logoColor=white)](morphe-manager://add-source?url=https://raw.githubusercontent.com/YOUR_GITHUB_USERNAME/smartlauncher-morphe-patches/main/patches-bundle.json)
[![Download .mpp](https://img.shields.io/badge/Download-.mpp%20Package-10B981?style=for-the-badge&logo=android&logoColor=white)](https://github.com/YOUR_GITHUB_USERNAME/smartlauncher-morphe-patches/releases/latest/download/smartlauncher-morphe-patches.mpp)
[![Build Status](https://img.shields.io/github/actions/workflow/status/YOUR_GITHUB_USERNAME/smartlauncher-morphe-patches/release.yml?branch=main&style=for-the-badge&logo=github)](https://github.com/YOUR_GITHUB_USERNAME/smartlauncher-morphe-patches/actions)

Custom **Morphe Patch Package (`.mpp`)** suite for **Smart Launcher 6**, specially designed for devices like the **Samsung Galaxy S22 Ultra** (One UI / Android 14+ / Android 15+).

---

## 📲 Quick Installation

### Method 1: Direct "Add to Morphe" Link (Recommended)
Click the badge above or use the deep link on your Android device with Morphe Manager installed:
[👉 **Click here to add patch source to Morphe Manager**](morphe-manager://add-source?url=https://raw.githubusercontent.com/YOUR_GITHUB_USERNAME/smartlauncher-morphe-patches/main/patches-bundle.json)

---

### Method 2: Manual Import
1. Download the latest **[`smartlauncher-morphe-patches.mpp`](https://github.com/YOUR_GITHUB_USERNAME/smartlauncher-morphe-patches/releases/latest/download/smartlauncher-morphe-patches.mpp)** release file.
2. Open **Morphe Manager** on your Android device.
3. On the **Home Screen**, tap **Import Patches** (or **+**).
4. Select `smartlauncher-morphe-patches.mpp` from your **Downloads** folder.
5. Select **Smart Launcher 6** APK and apply your desired patches!

---

## 🌟 Included Patches

### 1. 🙈 Toggle Archived Apps as Hidden (`HideArchivedAppsPatch`)
- Adds an experimental preference setting (`experimental_hide_archived_apps`).
- Filters out all archived apps (`ApplicationInfo.FLAG_ARCHIVED` / zero-byte package entries) from the launcher app drawer.

### 2. ⚡ Shizuku App Archiving (`ShizukuArchivePatch`)
- Integrates **Shizuku** ADB binder support directly into Smart Launcher's context popup menu.
- Executes `pm archive <package>` with elevated privileges on Samsung Galaxy S22 Ultra and all Shizuku-supported devices.

### 3. 📱 Official Device App Archiving (`NativeArchivePatch`)
- Enables native system app archiving via `PackageInstaller.requestArchive()` / `LauncherApps.archiveApp()`.
- Optimized for Android 15+ / Samsung One UI 7.

---

## 🛠️ Local Development & Building

Building the `.mpp` patch container locally:

```bash
# Clone the repository
git clone https://github.com/YOUR_GITHUB_USERNAME/smartlauncher-morphe-patches.git
cd smartlauncher-morphe-patches

# Build Morphe Patch Package (.mpp)
./gradlew buildAndroid
```

The output `.mpp` file will be generated at:
`patches/build/libs/smartlauncher-morphe-patches.mpp`
