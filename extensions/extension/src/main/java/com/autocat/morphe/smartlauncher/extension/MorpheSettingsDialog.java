package com.autocat.morphe.smartlauncher.extension;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

            CheckBox swHide = toggleRow(context, root,
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

            CheckBox swNative = toggleRow(context, root,
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

            CheckBox swShizuku = toggleRow(context, root,
                    "Shizuku Archiving",
                    "Uses Shizuku for privileged archiving. Works across Android 14/15/16.",
                    MorphePreferences.isShizukuEnabled(context),
                    colorPrimary, colorSecondary, dp16, dp8, dp4);
            swShizuku.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton b, boolean checked) {
                    MorphePreferences.setShizukuEnabled(context, checked);
                }
            });

            // Shizuku Service Status Row
            ShizukuArchiveHelper.Status status = ShizukuArchiveHelper.getStatus();
            String statusText;
            int statusColor;
            if (status == ShizukuArchiveHelper.Status.ACTIVE) {
                statusText = "⚡ Shizuku: Running & Authorized";
                statusColor = 0xFF2E7D32; // Green
            } else if (status == ShizukuArchiveHelper.Status.PERMISSION_REQUIRED) {
                statusText = "⚠️ Shizuku: Running (Tap to authorize)";
                statusColor = 0xFFE65100; // Orange
            } else {
                statusText = "🔌 Shizuku: Not Running";
                statusColor = colorSecondary;
            }

            TextView tvStatus = new TextView(context);
            tvStatus.setText(statusText);
            tvStatus.setTextSize(13f);
            tvStatus.setTextColor(statusColor);
            tvStatus.setPadding(dp16, dp8, dp16, dp8);
            if (status == ShizukuArchiveHelper.Status.PERMISSION_REQUIRED) {
                tvStatus.setTypeface(null, Typeface.BOLD);
                tvStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShizukuArchiveHelper.requestPermissionWithFeedback(context);
                    }
                });
            }
            root.addView(tvStatus);

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

    private static CheckBox toggleRow(Context context, LinearLayout parent,
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

        CheckBox cb = new CheckBox(context);
        cb.setChecked(checked);
        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cbParams.leftMargin = padV;
        cb.setLayoutParams(cbParams);
        row.addView(cb);

        parent.addView(row);
        return cb;
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

    private static class AppEntry {
        final String packageName;
        final String label;

        AppEntry(String packageName, String label) {
            this.packageName = packageName;
            this.label = label;
        }
    }

    private static void showAppArchivePicker(final Context context, final boolean unarchiveMode) {
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> installed = pm.getInstalledPackages(0);
            List<AppEntry> entries = new ArrayList<>();

            for (PackageInfo pi : installed) {
                if (pi.applicationInfo != null
                        && (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    boolean isArchived = ArchivedAppFilter.isAppArchived(pi.applicationInfo);
                    if (unarchiveMode == isArchived) {
                        CharSequence label = pi.applicationInfo.loadLabel(pm);
                        String labelStr = label != null ? label.toString() : pi.packageName;
                        entries.add(new AppEntry(pi.packageName, labelStr));
                    }
                }
            }

            if (entries.isEmpty()) {
                Toast.makeText(context,
                        unarchiveMode ? "No archived apps found" : "No user apps available to archive",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Sort alphabetically by app name
            Collections.sort(entries, new Comparator<AppEntry>() {
                @Override
                public int compare(AppEntry o1, AppEntry o2) {
                    return o1.label.compareToIgnoreCase(o2.label);
                }
            });

            final List<String> packageNames = new ArrayList<>(entries.size());
            final List<String> appLabels = new ArrayList<>(entries.size());
            for (AppEntry e : entries) {
                packageNames.add(e.packageName);
                appLabels.add(e.label);
            }

            AlertDialog.Builder picker = new AlertDialog.Builder(context);
            picker.setTitle(unarchiveMode ? "Restore Archived App" : "Archive App");
            picker.setItems(appLabels.toArray(new CharSequence[0]),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String pkg = packageNames.get(which);
                            MorpheMenuInjector.performArchiveOrRestoreAsync(context, pkg, unarchiveMode);
                        }
                    });
            picker.setNegativeButton("Cancel", null);
            picker.show();
        } catch (Throwable t) {
            Toast.makeText(context, "Error loading apps: " + t.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
