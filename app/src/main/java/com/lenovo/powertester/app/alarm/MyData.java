package com.lenovo.powertester.app.alarm;

/**
 * Created by Administrator on 2014/5/16.
 */
public class MyData {
    private static MyData ourInstance = new MyData();

    public static MyData getInstance() {
        return ourInstance;
    }

    public StringBuffer stringBuffer;
    private MyData() {
        stringBuffer = new StringBuffer();
    }
}
