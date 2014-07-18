/*
package com.lenovo.powertester.app.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;

public class MyActivity extends Activity implements View.OnClickListener {


    private Button btn_reset;
    private EditText edit_interval;
    private int interval;
    private Toast mToast;
    private PendingIntent sender;
    private AlarmManager am;
    private TextView interval_show;
    private Button stop;
    private Button showlist;
    private Button updatelist;
    private TextView alarmlog;
    private AlarmManagerAdapter managerAdapter;
    private TextView history;
    private StringBuffer appendhistory;

    */
/**
 * Called when the activity is first created.
 *//*

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        edit_interval = (EditText) findViewById(R.id.edit_interval);
        btn_reset = (Button) findViewById(R.id.btn_reset);
        showlist = (Button) findViewById(R.id.showlist);
        updatelist = (Button) findViewById(R.id.updatelist);
        interval_show = (TextView) findViewById(R.id.interval_show);
        history = (TextView) findViewById(R.id.history);
        alarmlog = (TextView) findViewById(R.id.alarmlog);
        stop = (Button) findViewById(R.id.stop);


        Intent intent = new Intent(this, RepeatingAlarm.class);
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        sender = PendingIntent.getBroadcast(this, 0,
                intent, 0);
        edit_interval.setOnClickListener(this);
        btn_reset.setOnClickListener(this);
        showlist.setOnClickListener(this);
        updatelist.setOnClickListener(this);
        stop.setOnClickListener(this);
        managerAdapter = new AlarmManagerAdapter((AlarmManager) this.getSystemService(ALARM_SERVICE), this);
        clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appendhistory = MyData.getInstance().stringBuffer;
        if (appendhistory != null && appendhistory.length() > 1) {
            history.setText(appendhistory.toString());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reset:
                if (!(edit_interval.getText().toString()).equals("")) {
                    interval = Integer.parseInt(edit_interval.getText().toString());
                    interval_show.setText(interval + "秒");
                    clear();
                    repeat(interval);
                }
                break;
            case R.id.stop:
                clear();
                break;

            case R.id.showlist:
                updateTextView(managerAdapter.getAlarmStatus());
                break;
            case R.id.updatelist:
                HashMap<String, Integer> map = new HashMap<String, Integer>();
                map.put("com.example.myapp5", AlarmManagerAdapter.POLICY_FORBIDDEN);
                managerAdapter.updateAlarmList(map);
                updateTextView(managerAdapter.getAlarmStatus());
                break;
            case R.id.enablelist:
                HashMap<String, Integer> map2 = new HashMap<String, Integer>();
                map2.put("com.example.myapp5", AlarmManagerAdapter.POLICY_TRUSTED);
                managerAdapter.updateAlarmList(map2);
                updateTextView(managerAdapter.getAlarmStatus());
                break;
            default:
                break;

        }

    }

    private void updateTextView(HashMap<String, AbnormalInfo> alarmStatus) {
        if (alarmStatus == null) {
            return;
        }
        alarmlog.setText(Arrays.toString(alarmStatus.entrySet().toArray()));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void clear() {
        if (am != null && sender != null) {
            am.cancel(sender);
        }
    }

    private void repeat(int interval) {

        long firstTime = SystemClock.elapsedRealtime();

        am.setRepeating(AlarmManager.RTC_WAKEUP, firstTime, interval * 1000, sender);
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, "repeating_scheduled",
                Toast.LENGTH_LONG);
        mToast.show();
    }

}
*/
