package com.example.focuslauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Everything that talks to PackageManager: listing, labelling, icons, launching. */
final class AppRepository {
    private AppRepository() {}

    /** All apps that have a launcher icon, sorted A-Z, excluding this launcher itself. */
    static List<AppInfo> loadLaunchableApps(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        List<AppInfo> apps = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ResolveInfo ri : infos) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(context.getPackageName())) continue; // hide ourselves
            if (!seen.add(pkg)) continue;                       // one row per app
            apps.add(new AppInfo(pkg, ri.loadLabel(pm).toString()));
        }
        Collections.sort(apps, (a, b) -> a.label.compareToIgnoreCase(b.label));
        return apps;
    }

    /** App label for a package, or the package name if it can't be read. */
    static String labelOf(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    /** App icon for a package, or null if the app is no longer installed. */
    static Drawable loadIcon(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /** Opens an app. Returns false if it has no launch intent (e.g. uninstalled). */
    static boolean launch(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) return false;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
        return true;
    }

    /**
     * First-run helper: finds the phone's default dialer, SMS app, camera, maps app
     * and browser, so the home screen isn't empty. Order = tile order.
     */
    static List<String> guessDefaultApps(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent[] probes = {
                new Intent(Intent.ACTION_DIAL, Uri.parse("tel:")),
                new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=cafe")),
                new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com"))
        };

        List<String> packages = new ArrayList<>();
        for (Intent probe : probes) {
            String pkg = resolvePackage(pm, probe);
            if (pkg != null && !packages.contains(pkg) && !pkg.equals(context.getPackageName())) {
                packages.add(pkg);
            }
        }
        return packages;
    }

    /** Which package would handle this intent? Null if none (or it can't be launched). */
    private static String resolvePackage(PackageManager pm, Intent intent) {
        ResolveInfo best = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (best == null || best.activityInfo == null) return null;

        String pkg = best.activityInfo.packageName;
        if ("android".equals(pkg)) {
            // The system picked its "choose an app" dialog; take the first real handler instead.
            List<ResolveInfo> all = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (all.isEmpty()) return null;
            pkg = all.get(0).activityInfo.packageName;
        }
        return pm.getLaunchIntentForPackage(pkg) != null ? pkg : null;
    }
}
