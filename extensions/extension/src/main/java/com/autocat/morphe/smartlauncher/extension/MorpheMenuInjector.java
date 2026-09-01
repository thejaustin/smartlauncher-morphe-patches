package com.autocat.morphe.smartlauncher.extension;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

/**
 * Injects Morphe Actions into Smart Launcher's contextual long-press menus and settings.
 */
public final class MorpheMenuInjector {

    private MorpheMenuInjector() {}

    /**
     * Called when an app is long-pressed in the App Drawer or Categories screen.
     */
    public static void onAppLongPress(final Context context, final String packageName) {
        if (context == null || packageName == null || packageName.isEmpty()) {
            return;
        }

        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            CharSequence label = appInfo.loadLabel(pm);
            final String appName = label != null ? label.toString() : packageName;
            final boolean isArchived = (appInfo.flags & 0x40000000) != 0;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("📦 " + appName);

            String[] options = isArchived
                    ? new String[]{"♻️ Restore / Unarchive App", "⚙️ Morphe Mod Settings"}
                    : new String[]{"📦 Archive App (Save Storage)", "⚙️ Morphe Mod Settings"};

            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        if (isArchived) {
                            boolean ok = ShizukuArchiveHelper.unarchivePackage(packageName);
                            if (!ok) {
                                ok = NativeArchiveHelper.unarchivePackage(context, packageName);
                            }
                            Toast.makeText(context, ok ? "Unarchiving " + appName + "..." : "Failed to unarchive", Toast.LENGTH_SHORT).show();
                        } else {
                            boolean ok = ShizukuArchiveHelper.archivePackage(packageName);
                            if (!ok) {
                                ok = NativeArchiveHelper.archivePackage(context, packageName);
                            }
                            Toast.makeText(context, ok ? "Archiving " + appName + "..." : "Failed to archive", Toast.LENGTH_SHORT).show();
                        }
                    } else if (which == 1) {
                        MorpheSettingsDialog.show(context);
                    }
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        } catch (Throwable t) {
            Toast.makeText(context, "Morphe action error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when Smart Launcher preferences / experimental menu is loaded.
     */
    public static void openMorpheSettings(Context context) {
        MorpheSettingsDialog.show(context);
    }
}
