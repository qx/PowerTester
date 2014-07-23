package com.lenovo.powertester.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;
import com.lenovo.powertester.app.alarm.AbnormalInfo;
import com.lenovo.powertester.app.alarm.AlarmManagerAdapter;
import com.lenovo.powertester.app.alarm.MyData;
import com.lenovo.powertester.app.alarm.RepeatingAlarm;
import com.lenovo.powertester.app.infos.Infos;
import com.lenovo.powertester.app.wakelock.PowerManagerInvacation;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Process;
import java.util.*;


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
            case 4:
                mTitle = getString(R.string.title_wakelock);
                break;
            case 5:
                mTitle = getString(R.string.title_appinfo);
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

        @Optional
        @InjectView(R.id.brightvalue)
        TextView brightvalue;
        //        @Optional
//        @InjectView(R.id.sliplinechart)
//        SlipLineChart sliplinechart;
        @Optional
        @InjectView(R.id.linearlayout)
        LinearLayout linearlayout;

        @Optional
        @InjectView(R.id.text_wakelock)
        TextView text_wakelock;
        @Optional
        @InjectView(R.id.lesafelist)
        TextView lesafelist;
        @Optional
        @InjectView(R.id.systeminfo)
        TextView systeminfo;

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
                    rootView = inflater.inflate(R.layout.fragment_smartscreen, container, false);
                    break;
                case 4:
                    rootView = inflater.inflate(R.layout.fragment_wakelock, container, false);
                    break;
                case 5:
                    rootView = inflater.inflate(R.layout.fragment_appinfo, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_alarm, container, false);
                    break;

            }
            Log.i("Mytester", "onCreateView()");
            ButterKnife.inject(this, rootView);

            return rootView;
        }

        private String getWakelockInfo() {
            Set<String> hs = PowerManagerInvacation.getWakelockApps(getActivity());
            Log.d("mywakelock", hs.toString());
            return hs.toString();
        }

        private void listenBrightness() {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    String value = getRealBrightness();
                    int curBrightness = Integer.parseInt(value);// Settings.System.getInt(mContext.getContentResolver(),
                    Log.i("BRIGHTNESS", "current lcd-backlight:" + curBrightness);

                    Message message = new Message();
                    message.arg1 = curBrightness;
                    mHandler.sendMessage(message);
                }
            };
            timer = new Timer();
            timer.schedule(timerTask, 0, 5000);

            //生成图表
            chart = ChartFactory.getTimeChartView(getActivity(), getDateDemoDataset(), getDemoRenderer(), "hh:mm:ss");
            linearlayout.addView(chart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 380));
        }

        private int addY = -1;
        private long addX;
        /**
         * 曲线数量
         */
        private static final int SERIES_NR = 1;
        private static final String TAG = "message";
        private TimeSeries series1;
        private XYMultipleSeriesDataset dataset1;
        private Handler handler;
        private Random random = new Random();
        /**
         * 时间数据
         */
        Date[] xcache = new Date[20];
        /**
         * 数据
         */
        int[] ycache = new int[20];
        private GraphicalView chart;
        Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                brightvalue.setText("" + msg.arg1);

                paintChart(msg.arg1);

            }


        };

        private void paintChart(int arg1) {
            //设定长度为20
            int length = series1.getItemCount();
            if (length >= 20) length = 20;
//            addY=random.nextInt()%10;
//            addY=random.nextInt()%10;
            addY = arg1;
            addX = new Date().getTime();

            //将前面的点放入缓存
            for (int i = 0; i < length; i++) {
                xcache[i] = new Date((long) series1.getX(i));
                ycache[i] = (int) series1.getY(i);
            }
            series1.clear();
            series1.add(new Date(addX), addY);
            for (int k = 0; k < length; k++) {
                series1.add(xcache[k], ycache[k]);
            }
            //在数据集中添加新的点集
            dataset1.removeSeries(series1);
            dataset1.addSeries(series1);
            //曲线更新
            chart.invalidate();
        }

        Timer timer;
        TimerTask timerTask;


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
            if (timer != null) {
                timer.cancel();
            }
            ButterKnife.reset(this);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            currentid = getArguments().getInt(ARG_SECTION_NUMBER);
            ((MainActivity) activity).onSectionAttached(currentid);
            Log.i("Mytester", "onAttach()");

        }

        @Override
        public void onResume() {
            super.onResume();
            switch (currentid) {
                case 1:
                    AsyncTaskThreadPoolExecutorHelper.execute(new AsyncTask<Object, Object, Object>() {
                        @Override
                        protected Object doInBackground(Object... objects) {
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            initAlarmEvent();
                        }
                    });
                    break;
                case 2:
                    AsyncTaskThreadPoolExecutorHelper.execute(new AsyncTask<Object, Object, Object>() {
                        @Override
                        protected Object doInBackground(Object... objects) {
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            initSystemInfo();
                        }
                    });
                    break;
                case 3:
                    AsyncTaskThreadPoolExecutorHelper.execute(new AsyncTask<Object, Object, Object>() {
                        @Override
                        protected Object doInBackground(Object... objects) {
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            listenBrightness();
                            super.onPostExecute(o);
                        }
                    });
                    break;
                case 4:
                    AsyncTaskThreadPoolExecutorHelper.execute(new AsyncTask<Object, Object, Object>() {
                        String wakelockinfo = "";
//adb logcat -s "mywakelock"
                        @Override
                        protected Object doInBackground(Object... objects) {
                            wakelockinfo = getWakelockInfo();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            text_wakelock.setText(wakelockinfo);
                        }
                    });
                    break;
                case 5:
                    AsyncTaskThreadPoolExecutorHelper.execute(new AsyncTask<Object, Object, Object>() {
                        String sysinfo = "";
                        String leinfo = "";

                        @Override
                        protected Object doInBackground(Object... objects) {
                            sysinfo = Infos.getSystemApp(getActivity()).toString();
                            leinfo = Infos.getSafeWhiteList(getActivity()).toString();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            systeminfo.setText(sysinfo);
                            lesafelist.setText(leinfo);
                            super.onPostExecute(o);
                        }
                    });
                    break;
                default:
                    break;
            }
            appendhistory = MyData.getInstance().stringBuffer;
            if (history != null && appendhistory != null && appendhistory.length() > 1) {

                history.setText(appendhistory.toString());
            }
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

        private static final int BUFFER_SIZE = 24;
        private static final String CMD_CAT = "/system/bin/cat";
        private String mSysBrightnessPath = "";

        /**
         * 亮度实际生效值
         *
         * @return
         */
        private String getRealBrightness() {
            InputStream in = null;
            try {
                if (TextUtils.isEmpty(mSysBrightnessPath)) {
                    mSysBrightnessPath = getSysBrightnessPath();
                    if (TextUtils.isEmpty(mSysBrightnessPath)) {
                        return "";
                    }
                }
                String result = "";

                final ProcessBuilder cmd = new ProcessBuilder(new String[]{CMD_CAT, mSysBrightnessPath});
                final Process process = cmd.start();
                in = process.getInputStream();

                final byte[] re = new byte[BUFFER_SIZE];
                while (in.read(re) != -1) {
                    result = result + new String(re);
                }
                if (TextUtils.isEmpty(result)) {
                    return "";
                }
                return result.trim();
            } catch (IOException ex) {
//            LogUtils.e(PERSONALTAG_SMARTAPP_SCREEN_BRIGHTNESS, ex.getMessage());
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
//                LogUtils.excep(e.toString());
                }
            }
            return "";
        }

        public static final String BRIGHTNESS_PATH_GERNERIC = "/sys/class/leds/lcd-backlight/brightness";
        public static final String BRIGHTNESS_PATH_SAMSUNG = "/sys/class/backlight/pwm-backlight.0/brightness";
        public static final String BRIGHTNESS_PATH_SAMSUNG2 = "/sys/class/backlight/pwm-backlight/brightness";
        public static final String BRIGHTNESS_PATH_SAMSUNG3 = "/sys/class/backlight/s6e8aa0/brightness";
        public static final String BRIGHTNESS_PATH_INTEL = "/sys/class/backlight/psb-bl/brightness";

        private String getSysBrightnessPath() {
            File sysFile = new File(ComUtils.convertObsolutePath(BRIGHTNESS_PATH_GERNERIC));
            if (sysFile.exists()) {
                return BRIGHTNESS_PATH_GERNERIC;
            }
            sysFile = new File(ComUtils.convertObsolutePath(BRIGHTNESS_PATH_SAMSUNG));
            if (sysFile.exists()) {
                return BRIGHTNESS_PATH_SAMSUNG;
            }
            sysFile = new File(ComUtils.convertObsolutePath(BRIGHTNESS_PATH_SAMSUNG2));
            if (sysFile.exists()) {
                return BRIGHTNESS_PATH_SAMSUNG2;
            }
            sysFile = new File(ComUtils.convertObsolutePath(BRIGHTNESS_PATH_SAMSUNG3));
            if (sysFile.exists()) {
                return BRIGHTNESS_PATH_SAMSUNG3;
            }
            sysFile = new File(ComUtils.convertObsolutePath(BRIGHTNESS_PATH_INTEL));
            if (sysFile.exists()) {
                return BRIGHTNESS_PATH_INTEL;
            }
            return "";
        }

        private XYMultipleSeriesRenderer getDemoRenderer() {
            XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
            renderer.setChartTitle("实时曲线");//标题
            renderer.setChartTitleTextSize(20);
            renderer.setXTitle("时间");    //x轴说明
            renderer.setAxisTitleTextSize(16);
            renderer.setAxesColor(Color.BLACK);
            renderer.setLabelsTextSize(15);    //数轴刻度字体大小
            renderer.setLabelsColor(Color.BLACK);
            renderer.setLegendTextSize(15);    //曲线说明
            renderer.setXLabelsColor(Color.BLACK);
            renderer.setYLabelsColor(0, Color.BLACK);
            renderer.setShowLegend(false);
            renderer.setMargins(new int[]{20, 30, 20, 0});
            XYSeriesRenderer r = new XYSeriesRenderer();

            r.setColor(Color.BLUE);
            r.setChartValuesTextSize(15);
            r.setChartValuesSpacing(3);
            r.setPointStyle(PointStyle.CIRCLE);
            r.setFillBelowLine(true);
            r.setFillBelowLineColor(Color.WHITE);
            r.setFillPoints(true);
            renderer.addSeriesRenderer(r);
            renderer.setMarginsColor(Color.WHITE);
            renderer.setPanEnabled(false, false);
            renderer.setShowGrid(true);
            renderer.setYAxisMax(300);
            renderer.setYAxisMin(10);
            renderer.setInScroll(true);  //调整大小
            return renderer;
        }

        /**
         * 数据对象
         *
         * @return
         */
        private XYMultipleSeriesDataset getDateDemoDataset() {
            dataset1 = new XYMultipleSeriesDataset();
            final int nr = 10;
            long value = new Date().getTime();
            Random r = new Random();
            for (int i = 0; i < SERIES_NR; i++) {
                series1 = new TimeSeries("Demo series " + (i + 1));
                for (int k = 0; k < nr; k++) {
                    series1.add(new Date(value + k * 1000), 20 + r.nextInt() % 10);
                }
                dataset1.addSeries(series1);
            }
            Log.i(TAG, dataset1.toString());
            return dataset1;
        }
    }
}
