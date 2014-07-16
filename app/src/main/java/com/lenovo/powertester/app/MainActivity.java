package com.lenovo.powertester.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import com.lenovo.powertester.app.alarm.AbnormalInfo;
import com.lenovo.powertester.app.alarm.AlarmManagerAdapter;
import com.lenovo.powertester.app.alarm.RepeatingAlarm;

import java.util.Arrays;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private static TelephonyManager tm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_alarm);
                break;
            case 2:
                mTitle = getString(R.string.info);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private int currentid;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }


        @Optional
        @InjectView(R.id.edit_interval)
        EditText edit_interval;

        @Optional
        @InjectView(R.id.btn_reset)
        Button btn_reset;

        @Optional
        @InjectView(R.id.showlist)
        Button showlist;

        @Optional
        @InjectView(R.id.updatelist)
        Button updatelist;

        public void updatelist() {

        }

        @Optional
        @InjectView(R.id.interval_show)
        TextView interval_show;

        @Optional
        @InjectView(R.id.history)
        TextView history;

        @Optional
        @InjectView(R.id.alarmlog)
        TextView alarmlog;

        @Optional
        @InjectView(R.id.stop)
        Button stop;


        private int interval;


        @Optional
        @InjectView(R.id.systemverison)
        TextView systemverison;
        @Optional
        @InjectView(R.id.androidid)
        TextView androidid;
        @Optional
        @InjectView(R.id.systemmodel)
        TextView systemmodel;
//        @Optional
//        @InjectView(R.id.section_label)
//        TextView section_label;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;
            switch (currentid) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_alarm, container, false);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_systeminfo, container, false);
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_wakelock, container, false);
                    break;
                case 4:
                    rootView = inflater.inflate(R.layout.fragment_alarm, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_alarm, container, false);
                    break;

            }
            Log.i("Mytester", "onCreateView()");
            ButterKnife.inject(this, rootView);
            switch (currentid) {
                case 1:
                    initAlarmEvent();
                    break;
                case 2:
                    initSystemInfo();
                    break;
                case 3:
                    break;
                default:
                    break;
            }
            return rootView;
        }

        private void initSystemInfo() {

            systemverison.setText(Build.DISPLAY + "");
            androidid.setText(Build.VERSION.SDK_INT + "");
            systemmodel.setText(android.os.Build.MODEL);
        }

        private PendingIntent sender;
        private AlarmManager am;
        private AlarmManagerAdapter managerAdapter;
        private StringBuffer appendhistory;
        private Toast mToast;

        private void initAlarmEvent() {
            Intent intent = new Intent(getActivity(), RepeatingAlarm.class);
            am = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
            sender = PendingIntent.getBroadcast(getActivity(), 0,
                    intent, 0);
            managerAdapter = new AlarmManagerAdapter((AlarmManager) getActivity().getSystemService(ALARM_SERVICE), getActivity());
            clear();
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
            mToast = Toast.makeText(getActivity(), "repeating_scheduled",
                    Toast.LENGTH_LONG);
            mToast.show();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            ButterKnife.reset(this);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            currentid = getArguments().getInt(ARG_SECTION_NUMBER);
            ((MainActivity) activity).onSectionAttached(currentid);
            Log.i("Mytester", "onAttach()");

        }


        @Optional
        @OnClick({R.id.btn_reset, R.id.stop, R.id.showlist, R.id.updatelist, R.id.enablelist})
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
    }

}
