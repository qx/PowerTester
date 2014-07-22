package com.lenovo.powertester.app.wakelock;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@SuppressWarnings("SameParameterValue")
public final class PackageDetector {
    private static final String MD5 = "MD5";
    private static char mHexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String[] sMD5 = {"5ABDF9B649C2B8AC6026B88313C3C332", "EBAA529685AAE3A1D299AC15F7710AEA",
            "B4BDCC6C9886781D05FB601649D02801", "B78A0EE7E80045B4F9A0C425703D0CA4",
            "388CF508552BC50ED26A8715D33312BA", "6B861FD954CE40EE4C560992DCCB35F0",
            "49FF2FEA9A214DCC2F7EF229488B1A54", "56B44C8B44C3A4064FE0667DC91E01AA",
            "CEA942B20A36347B3BF793BFF50F737D"};

    private static final String TAG = "PackageDetector";

    private PackageDetector(){}

    public static boolean isPkgDisabled(final Context context, final String pkgName) {
        if (context == null || pkgName == null) {
            throw new IllegalArgumentException();
        }
        try {
            int status = context.getPackageManager().getApplicationEnabledSetting(pkgName);
            return (status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    public static boolean isPreInstalledApp(final Context context, final String pkgName) {
        if (context == null || pkgName == null) {
            throw new IllegalArgumentException();
        }
        try {
            PackageManager pkgManager = context.getPackageManager();
            ApplicationInfo appInfo = pkgManager.getApplicationInfo(pkgName, PackageManager.GET_ACTIVITIES);

            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,e.toString());
        }

        return false;
    }

    public static boolean isProtectedApp(final Context context, final String pkgName) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            throw new IllegalArgumentException();
        }
        if (isCustomizedPkg(getCretMD5(context, pkgName))) {
            return true;
        }
        return false;
    }

    private static void appendHexPair(final byte bt, StringBuffer stringbuffer) {
        char c0 = mHexDigits[(bt & 0xf0) >> 4];
        char c1 = mHexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    private static String bufferToHex(final byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(final byte bytes[], final int m, final int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static String getCretMD5(final Context context, final String pkgName) {
        android.content.pm.Signature[] sigs;
        String md5 = "";
        try {
            sigs = context.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_SIGNATURES).signatures;
            for (Signature sig : sigs) {
                md5 = getMD5String(sig.toByteArray());
            }
            if (!TextUtils.isEmpty(md5)) {
                return md5.toUpperCase();
            }
        } catch (PackageManager.NameNotFoundException e) {
            // ignore}
        }
        return md5;
    }

    private static String getMD5String(final byte[] bytes) {
        try {
            MessageDigest messagedigest = MessageDigest.getInstance(MD5);
            messagedigest.update(bytes);

            return bufferToHex(messagedigest.digest());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return "";
    }

    private static boolean isCustomizedPkg(String pkg) {
        for (String packge : sMD5) {
            if (packge.equals(pkg)) {
                return true;
            }
        }
        return false;
    }
}
