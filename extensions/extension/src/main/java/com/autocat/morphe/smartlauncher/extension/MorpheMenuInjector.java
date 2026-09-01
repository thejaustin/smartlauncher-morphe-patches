package com.autocat.morphe.smartlauncher.extension;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

/**
 * Injects Morphe Actions into Smart Launcher's contextual long-press menus and settings.
 */
public final class MorpheMenuInjector {

    private MorpheMenuInjector() {}

    /**
     * Intercepts the Uninstall action triggered from the long-press popup menu.
     * Offers the user the choice between Archiving (save space, keep data) and Complete Uninstallation.
     */
    public static void handleUninstallOrArchive(final Context context, final Intent uninstallIntent) {
        if (context == null) {
            return;
        }
        if (uninstallIntent == null) {
            return;
        }

        try {
            Uri data = uninstallIntent.getData();
            final String packageName = (data != null) ? data.getSchemeSpecificPart() : null;

            if (packageName == null || packageName.isEmpty()) {
                context.startActivity(uninstallIntent);
                return;
            }

            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            CharSequence label = appInfo.loadLabel(pm);
            final String appName = label != null ? label.toString() : packageName;
            final boolean isArchived = (appInfo.flags & 0x40000000) != 0;

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("📦 " + appName);

            if (isArchived) {
                builder.setMessage("This application is currently archived. Choose an action:");
                builder.setPositiveButton("♻️ Restore / Unarchive", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean ok = ShizukuArchiveHelper.unarchivePackage(packageName);
                        if (!ok) {
                            ok = NativeArchiveHelper.unarchivePackage(context, packageName);
                        }
                        Toast.makeText(context, ok ? "Unarchiving " + appName + "..." : "Failed to unarchive", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNeutralButton("🗑️ Delete Completely", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(uninstallIntent);
                    }
                });
            } else {
                builder.setMessage("Choose an action for " + appName + ":");
                builder.setPositiveButton("📦 Archive App (Save Space)", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean ok = ShizukuArchiveHelper.archivePackage(packageName);
                        if (!ok) {
                            ok = NativeArchiveHelper.archivePackage(context, packageName);
                        }
                        Toast.makeText(context, ok ? "Archiving " + appName + "..." : "Failed to archive", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNeutralButton("🗑️ Uninstall", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(uninstallIntent);
                    }
                });
            }

            builder.setNegativeButton("Cancel", null);
            builder.show();
        } catch (Throwable t) {
            try {
                context.startActivity(uninstallIntent);
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Called when Smart Launcher preferences / experimental menu is loaded.
     */
    public static void openMorpheSettings(Context context) {
        MorpheSettingsDialog.show(context);
    }
}
