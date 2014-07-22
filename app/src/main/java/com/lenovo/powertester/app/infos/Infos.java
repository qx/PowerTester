package com.lenovo.powertester.app.infos;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import com.lenovo.powertester.app.wakelock.PackageDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ok on 7/22/14.
 */
public class Infos {
    private static final String AUTHORITY = "com.lenovo.performancecenter.provider.querywhitelist";
    private static final String DB_COLUMN_PKGNAME = "pkgName";
    private static final String DB_COLUMN_STATE = "state";
    private static boolean sIsUseSafeWhiteList = false;
    // safe center add provider and configured white list exported=true
    private static final String TAG = "AppWhiteListUtility";
    private static final String URI_QUERY_WHITE_LIST = "content://" + AUTHORITY + "/queryWhitelistApps";
    private static final String URI_UPDATE_WHITE_LSIT = "content://" + AUTHORITY + "/updateWhitelistApp";
    private static Object obj = new Object();

    public static Map<String, String> getSafeWhiteList(final Context context) {
        synchronized (obj) {
            if (context == null) {
                throw new IllegalArgumentException();
            }
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.parse(URI_QUERY_WHITE_LIST);
            ContentProviderClient client = resolver.acquireContentProviderClient(uri);
            if (client == null) {
                return new HashMap<String, String>();
            }
            Cursor cursor = null;
            try {
                cursor = client.query(uri, null, null, null, null);
                if (cursor == null) {
                    return new HashMap<String, String>(0);
                }
                Map<String, String> whiteList = new HashMap<String, String>();

                while (cursor.moveToNext()) {
                    String pkg = cursor.getString(cursor.getColumnIndex(DB_COLUMN_PKGNAME));
                    whiteList.put(pkg, getAppName(context, pkg));
                }

                return whiteList;
            } catch (RemoteException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
                client.release();
            }
            return new HashMap<String, String>();
        }
    }

    public static String getAppName(Context ctx, String pkgName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
            String label = pm.getApplicationLabel(appInfo).toString();
            return label;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    public static Map<String, String> getSystemApp(Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        Map<String, String> whiteList = new HashMap<String, String>();
//        List<String> appsInLauncher = new ArrayList<String>();
        Intent it = new Intent(Intent.ACTION_MAIN);
        it.addCategory(Intent.CATEGORY_LAUNCHER);
//        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(it, 0);
//        for (ResolveInfo f : list) {
//            appsInLauncher.add(f.activityInfo.packageName);
//        }
        PackageManager pkgManager = context.getPackageManager();
        List<ApplicationInfo> installedApps = pkgManager.getInstalledApplications(0);
        for (ApplicationInfo appInfo : installedApps) {
            boolean isSystemApp = ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            if (PackageDetector.isPkgDisabled(context, appInfo.packageName)) {
                continue;
            }
            if (isSystemApp) {
                whiteList.put(appInfo.packageName, getAppName(context, appInfo.packageName));
            }
        }
        return whiteList;
    }
}
