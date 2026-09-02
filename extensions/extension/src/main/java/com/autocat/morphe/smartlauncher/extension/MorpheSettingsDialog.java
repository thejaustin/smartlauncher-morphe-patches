package com.autocat.morphe.smartlauncher.extension;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public final class MorpheSettingsDialog {

    private MorpheSettingsDialog() {}

    public static void show(final Context context) {
        try {
            int[] colorAttrs = {android.R.attr.textColorPrimary, android.R.attr.textColorSecondary};
            TypedArray ta = context.getTheme().obtainStyledAttributes(colorAttrs);
            final int colorPrimary = ta.getColor(0, 0xFF212121);
            final int colorSecondary = ta.getColor(1, 0xFF757575);
            ta.recycle();

            int[] accentAttr = {android.R.attr.colorAccent};
            TypedArray ta2 = context.getTheme().obtainStyledAttributes(accentAttr);
            final int colorAccent = ta2.getColor(0, 0xFF4CAF50);
            ta2.recycle();

            float d = context.getResources().getDisplayMetrics().density;
            int dp4 = Math.round(4 * d);
            int dp8 = Math.round(8 * d);
            int dp12 = Math.round(12 * d);
            int dp16 = Math.round(16 * d);

            LinearLayout root = new LinearLayout(context);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(0, dp8, 0, dp16);

            // ── App Drawer ─────────────────────────────────────────────
            root.addView(sectionHeader(context, "App Drawer", colorSecondary, dp16, dp12, dp4));

            Switch swHide = toggleRow(context, root,
                    "Hide Archived Apps",
                    "Removes archived apps from the app drawer so they stay out of sight.",
                    MorphePreferences.isHideArchivedEnabled(context),
                    colorPrimary, colorSecondary, dp16, dp8, dp4);
            swHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton b, boolean checked) {
                    MorphePreferences.setHideArchivedEnabled(context, checked);
                    ArchivedAppFilter.setFilterEnabled(checked);
                }
            });

            root.addView(divider(context, dp16, dp4));

            // ── App Archiving ───────────────────────────────────────────
            root.addView(sectionHeader(context, "App Archiving", colorSecondary, dp16, dp12, dp4));

            Switch swNative = toggleRow(context, root,
                    "Native Archiving",
                    "Uses Android 15+ system PackageInstaller APIs. No extra permissions needed.",
                    MorphePreferences.isNativeEnabled(context),
                    colorPrimary, colorSecondary, dp16, dp8, dp4);
            swNative.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton b, boolean checked) {
                    MorphePreferences.setNativeEnabled(context, checked);
                }
            });

            Switch swShizuku = toggleRow(context, root,
                    "Shizuku Archiving",
                    "Uses Shizuku for privileged archiving. Requires Shizuku to be installed and running.",
                    MorphePreferences.isShizukuEnabled(context),
                    colorPrimary, colorSecondary, dp16, dp8, dp4);
            swShizuku.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton b, boolean checked) {
                    MorphePreferences.setShizukuEnabled(context, checked);
                }
            });

            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(root);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Morphe Settings");
            builder.setView(scrollView);

            builder.setPositiveButton("Archive App…", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showAppArchivePicker(context, false);
                }
            });

            builder.setNeutralButton("Restore App…", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showAppArchivePicker(context, true);
                }
            });

            builder.setNegativeButton("Done", null);
            builder.create().show();

        } catch (Throwable t) {
            Toast.makeText(context, "Morphe Settings error: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static TextView sectionHeader(Context context, String title,
                                          int color, int padH, int padTop, int padBottom) {
        TextView tv = new TextView(context);
        tv.setText(title.toUpperCase());
        tv.setTextSize(11f);
        tv.setTextColor(color);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setLetterSpacing(0.12f);
        tv.setPadding(padH, padTop, padH, padBottom);
        return tv;
    }

    private static Switch toggleRow(Context context, LinearLayout parent,
                                    String title, String description,
                                    boolean checked,
                                    int colorPrimary, int colorSecondary,
                                    int padH, int padV, int dp4) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(padH, padV, padH, padV);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(textParams);

        TextView tvTitle = new TextView(context);
        tvTitle.setText(title);
        tvTitle.setTextSize(15f);
        tvTitle.setTextColor(colorPrimary);
        textCol.addView(tvTitle);

        TextView tvDesc = new TextView(context);
        tvDesc.setText(description);
        tvDesc.setTextSize(12f);
        tvDesc.setTextColor(colorSecondary);
        tvDesc.setPadding(0, dp4, 0, 0);
        textCol.addView(tvDesc);

        row.addView(textCol);

        Switch sw = new Switch(context);
        sw.setChecked(checked);
        LinearLayout.LayoutParams swParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        swParams.leftMargin = padV;
        sw.setLayoutParams(swParams);
        row.addView(sw);

        parent.addView(row);
        return sw;
    }

    private static View divider(Context context, int padH, int dp4) {
        View v = new View(context);
        int dp1 = Math.max(1, Math.round(context.getResources().getDisplayMetrics().density));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp1);
        p.leftMargin = padH;
        p.rightMargin = padH;
        p.topMargin = dp4;
        p.bottomMargin = dp4;
        v.setLayoutParams(p);
        v.setBackgroundColor(0x1A808080);
        return v;
    }

    private static void showAppArchivePicker(final Context context, final boolean unarchiveMode) {
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> installed = pm.getInstalledPackages(0);
            final List<String> packageNames = new ArrayList<>();
            final List<String> appLabels = new ArrayList<>();

            for (PackageInfo pi : installed) {
                if (pi.applicationInfo != null
                        && (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    boolean isArchived = (pi.applicationInfo.flags & 0x40000000) != 0;
                    if (unarchiveMode == isArchived) {
                        packageNames.add(pi.packageName);
                        CharSequence label = pi.applicationInfo.loadLabel(pm);
                        appLabels.add(label != null ? label.toString() : pi.packageName);
                    }
                }
            }

            if (appLabels.isEmpty()) {
                Toast.makeText(context,
                        unarchiveMode ? "No archived apps found" : "No user apps available",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder picker = new AlertDialog.Builder(context);
            picker.setTitle(unarchiveMode ? "Restore Archived App" : "Archive App");
            picker.setItems(appLabels.toArray(new CharSequence[0]),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String pkg = packageNames.get(which);
                            String name = appLabels.get(which);
                            if (unarchiveMode) {
                                boolean ok = ShizukuArchiveHelper.unarchivePackage(pkg);
                                if (!ok && context instanceof Activity) {
                                    ok = NativeArchiveHelper.unarchivePackage((Activity) context, pkg);
                                }
                                Toast.makeText(context,
                                        ok ? "Restoring " + name + "…" : "Failed to restore " + name,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                boolean ok = ShizukuArchiveHelper.archivePackage(pkg);
                                if (!ok && context instanceof Activity) {
                                    ok = NativeArchiveHelper.archivePackage((Activity) context, pkg);
                                }
                                Toast.makeText(context,
                                        ok ? "Archiving " + name + "…" : "Failed to archive " + name,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            picker.setNegativeButton("Cancel", null);
            picker.show();
        } catch (Throwable t) {
            Toast.makeText(context, "Error loading apps: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
