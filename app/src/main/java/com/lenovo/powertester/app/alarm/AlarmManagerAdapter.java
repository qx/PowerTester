package com.lenovo.powertester.app.alarm;

import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2014/4/24.
 */
public class AlarmManagerAdapter {
    public static final int POLICY_FORBIDDEN = 2;
    public static final int POLICY_TRUSTED = 0;
    public AlarmManagerAdapter(AlarmManager manager, Context mContext) {
        this.manager = manager;
        this.mContext = mContext;
    }

    private AlarmManager manager;
    private Context mContext;

    /**
     * get alarmStatus from system
     *
     * @return
     */
    public HashMap<String, AbnormalInfo> getAlarmStatus() {

        HashMap<String, AbnormalInfo> alarmMap = new HashMap<String, AbnormalInfo>();
            Class<?> alarm_class = manager.getClass();
            //data
            Bundle resBundle;

            try {
                Class partypes[] = null;
                Method getStatsBundle = alarm_class.getDeclaredMethod("getStatsBundle", partypes);
                getStatsBundle.setAccessible(true);
                resBundle = (Bundle) getStatsBundle.invoke(manager, null);
                if (resBundle == null) {
                    return alarmMap;
                }

                ToStringBuilder.reflectionToString(resBundle, ToStringStyle.SIMPLE_STYLE);

                Set<String> keySet = resBundle.keySet();




                for (String key : keySet) {
                    AbnormalInfo abnormalInfo = new AbnormalInfo(getAppNameFromPkgName(mContext, key),
                            resBundle.getIntArray(key));
                    alarmMap.put(key, abnormalInfo);
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
//        System.out.println(Arrays.toString(alarmMap.entrySet().toArray()));
        return alarmMap;
    }

    /**
     * update alarmList
     *
     * @param app_policys
     */
    public void updateAlarmList(HashMap<String, Integer> app_policys) {
            Class<?> alarm_class = manager.getClass();
            Bundle policies = new Bundle();//app,policy
            for (Object o : app_policys.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String mkey = (String) entry.getKey();
                Integer mValue = (Integer) entry.getValue();
                policies.putInt(mkey, mValue);
            }
            try {
                Class<?> partypes[] = new Class[2];
                partypes[0] = Bundle.class;  //Bundle.class ??
                partypes[1] = boolean.class;
                Method setAlarmAuthority_bundle = alarm_class.getDeclaredMethod("updatePolicyBundle", partypes);
                setAlarmAuthority_bundle.setAccessible(true);
                setAlarmAuthority_bundle.invoke(manager, policies, false);

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
    }
    public static String getAppNameFromPkgName(Context mContext, String pkg) {

        PackageManager packageManager = mContext.getPackageManager();

        ApplicationInfo info;
        try {
            info = packageManager.getApplicationInfo(pkg, PackageManager.GET_META_DATA);
            return (String) info.loadLabel(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();  //TODO
        }
        return null;
    }
}

