package com.autocat.morphe.smartlauncher.extension;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Injects Morphe Actions into Smart Launcher's contextual long-press popup menus and settings.
 */
public final class MorpheMenuInjector {

    private static final String TAG = "MorpheMenuInjector";
    private static String sLastPackageName = null;
    private static Context sLastContext = null;

    private MorpheMenuInjector() {}

    /**
     * Records the active package when a long-press popup is opened.
     */
    public static void setLastTarget(Context context, String packageName) {
        sLastContext = context;
        sLastPackageName = packageName;
    }

    /**
     * Injects a dedicated "Archive App" item into Smart Launcher's contextual popup menu list.
     */
    @SuppressWarnings("rawtypes")
    public static void injectArchiveItem(List items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        try {
            Object sampleItem = null;
            for (Object obj : items) {
                if (obj != null && obj.getClass().getName().endsWith("q36")) {
                    sampleItem = obj;
                    break;
                }
            }

            if (sampleItem == null) {
                return;
            }

            Class<?> q36Class = sampleItem.getClass();
            Field stringField = null;
            Field actionField = null;
            Field intFieldA = null;
            Field intFieldB = null;

            for (Field f : q36Class.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == String.class && stringField == null) {
                    stringField = f;
                } else if (f.getName().equals("f") || f.getType().getName().contains("b34") || f.getType().getName().contains("Function")) {
                    actionField = f;
                } else if (f.getType() == int.class) {
                    if (intFieldA == null) intFieldA = f;
                    else if (intFieldB == null) intFieldB = f;
                }
            }

            // Create a proxy onClick handler for Kotlin Function1 (b34)
            Class<?> function1Class = Class.forName("b34");
            Object clickProxy = Proxy.newProxyInstance(
                    q36Class.getClassLoader(),
                    new Class<?>[]{function1Class},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if ("invoke".equals(method.getName())) {
                                onArchiveClicked();
                                return null;
                            }
                            return null;
                        }
                    }
            );

            // Instantiate a new q36 entry via reflection
            Constructor<?>[] constructors = q36Class.getDeclaredConstructors();
            if (constructors.length > 0) {
                Constructor<?> ctor = constructors[0];
                ctor.setAccessible(true);
                Class<?>[] paramTypes = ctor.getParameterTypes();
                Object[] initArgs = new Object[paramTypes.length];

                for (int i = 0; i < paramTypes.length; i++) {
                    if (paramTypes[i] == int.class) initArgs[i] = 0;
                    else if (paramTypes[i] == boolean.class) initArgs[i] = false;
                    else if (paramTypes[i] == String.class) initArgs[i] = "Archive App";
                    else if (paramTypes[i].isAssignableFrom(function1Class)) initArgs[i] = clickProxy;
                    else initArgs[i] = null;
                }

                Object archiveItem = ctor.newInstance(initArgs);
                if (stringField != null) stringField.set(archiveItem, "Archive");
                if (actionField != null) actionField.set(archiveItem, clickProxy);

                // Add to the popup list right next to other app actions
                items.add(archiveItem);
                Log.i(TAG, "Successfully injected Archive App entry into popup menu");
            }
        } catch (Throwable t) {
            Log.w(TAG, "Safe popup item injection catch: " + t.getMessage());
        }
    }

    private static void onArchiveClicked() {
        Context context = sLastContext;
        String packageName = sLastPackageName;

        if (context == null || packageName == null) {
            return;
        }

        try {
            boolean ok = ShizukuArchiveHelper.archivePackage(packageName);
            if (!ok) {
                ok = NativeArchiveHelper.archivePackage(context, packageName);
            }
            Toast.makeText(context, ok ? "Archiving app..." : "Failed to archive app", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(context, "Archive error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Intercepts the Uninstall action triggered from the long-press popup menu.
     */
    public static void handleUninstallOrArchive(final Context context, final Intent uninstallIntent) {
        if (context == null || uninstallIntent == null) {
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
