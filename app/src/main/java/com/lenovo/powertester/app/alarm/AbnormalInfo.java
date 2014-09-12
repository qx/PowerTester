package com.lenovo.powertester.app.alarm;

import android.app.AlarmManager;

import java.util.Arrays;

/**
 * Created by Administrator on 2014/5/15.
 */
public class AbnormalInfo {
    public static final int ALARMTYPE = AlarmManager.RTC;
    private String appNameFromPkgName;
    private int[] anInts;

    public AbnormalInfo(String appNameFromPkgName, int[] anInts) {

        this.appNameFromPkgName = appNameFromPkgName;
        this.anInts = anInts;
    }

    @Override
    public String toString() {
        if (anInts.length == 2) {
            return
                    "\n包名 =" + appNameFromPkgName +
                            "\n" + "类型 RTCWAKEUP:" + anInts[0] +
                            "\n" + "类型 ELAPSED_REALTIME_WAKEUP:" + anInts[1] +
                            "\n";

        } else {
            return "\n" + "\n包名 =" + appNameFromPkgName +
                    "\n" + "类型 RTCWAKEUP+ELAPSED_REALTIME_WAKEUP:" + Arrays.toString(anInts) + "\n";
        }
    }
}
