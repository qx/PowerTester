package com.lenovo.powertester.app.wakelock;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import com.lenovo.powertester.app.root.RootTools;

import java.io.*;
import java.util.*;

/**
 * @author zhaogf1
 */
public class PowerManagerInvacation {

    private static final String TAG = "mywakelock";

    public static Set<String> getWakelockApps(Context context) {
        RootTools.dumpWakelockDetail(context);

        Set<String> mSet = new HashSet<String>();
        final String keyUid = "uid=";
        final String keyTag = "tag=";
        File dumpFile = new File(context.getFilesDir(), "wakelocks");
        Log.d(TAG, "dumpFile = " + dumpFile.getAbsolutePath());
        boolean exits = dumpFile.exists();
        Log.d(TAG, "dumpFile exists = " + exits);
        if (exits) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(dumpFile));
                String readLine;
                int uid = 0;
                while ((readLine = br.readLine()) != null) {
                    String[] infos = readLine.split(";");
                    try {
                        uid = Integer.parseInt(infos[0].replace(keyUid, ""));
                    } catch (NumberFormatException ignored) {
                    }
                    String[] pkgName = context.getPackageManager().getPackagesForUid(uid);
                    infos[1].replace(keyTag, "");

                    if (pkgName != null && pkgName.length > 0) {
                        mSet = rmSetNotInBase(new HashSet<String>(Arrays.asList(pkgName)), getCleanPkgListSet4Wakelock(context));
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mSet;
    }

    public static List<String> getLauncherAppList(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        List<String> appsInLauncher = new ArrayList<String>();
        Intent it = new Intent(Intent.ACTION_MAIN);
        it.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(it, 0);
        for (ResolveInfo f : list) {
            appsInLauncher.add(f.activityInfo.packageName);
        }
        return appsInLauncher;
    }

    public static Set<String> getCleanPkgListSet4Wakelock(Context context) {
        List<String> appsInLauncher = getLauncherAppList(context);
        PackageManager pkgManager = context.getPackageManager();
        List<ApplicationInfo> installedApps = pkgManager.getInstalledApplications(0);
        Set<String> pkgs = new HashSet<String>();

        for (ApplicationInfo appInfo : installedApps) {

            String pkgName = appInfo.packageName;

            if (!appsInLauncher.contains(pkgName)) {
                continue;
            }

            if (PackageDetector.isPkgDisabled(context, pkgName)) {
                continue;
            }
            pkgs.add(pkgName);
        }
        return pkgs;

    }

    public static Set<String> rmSetNotInBase(Set<String> mSet, Set<String> pkgListSet) {
        Iterator<String> iterator = mSet.iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            if (!pkgListSet.contains(element)) {
                iterator.remove();
            }
        }
        return mSet;
    }
}
