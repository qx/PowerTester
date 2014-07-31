package com.lenovo.powertester.app.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;
import com.lenovo.powertester.app.MainActivity;
import com.lenovo.powertester.app.R;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2014/5/15.
 */
public class RepeatingAlarm extends BroadcastReceiver {
    private StringBuffer str;
    private Context mContext;
    private MainActivity.PlaceholderFragment AlarmInfo;

    @Override
    public void onReceive(Context context, Intent intent) {
        str = new StringBuffer();
        str.append("aaaaaaaaaaaaaaa");
        this.mContext = context;
        Toast.makeText(context, "repeating_received", Toast.LENGTH_SHORT)
                .show();
        ioOperationForTest();

        System.out.println("****repeat");
        MyData.getInstance().stringBuffer.append("repeat<" + getNow4AlarmCheck() + ">"+"| AlarmType:"+ AbnormalInfo.ALARMTYPE + "\n");
    }

    public static void excu(Runnable runnable) {
        // Create a factory that produces daemon threads with a naming pattern and
        // a priority
        BasicThreadFactory factory = new BasicThreadFactory.Builder()
                .namingPattern("powersetting thread-%d")
                .daemon(true)
                .priority(Thread.MAX_PRIORITY)
                .build();
        // Create an executor service for single-threaded execution
        ExecutorService exec = Executors.newSingleThreadExecutor(factory);
        exec.submit(runnable);
    }

    private void ioOperationForTest() {
        excu(new Runnable() {
            @Override
            public void run() {
//                get();
                read();
            }
        });
    }

    private void get() {
        try {
          /*
           * //---Internal Storage--- FileOutputStream fOut =
           * openFileOutput("textfile.txt", MODE_WORLD_READABLE);
           *
           */
            File sdCard = Environment.getExternalStorageDirectory();
//            File directory = new File("file:///android_asset/test");
            File directory = new File(sdCard.getAbsolutePath()
                    + "/MyFiles");
            directory.mkdirs();
            System.out.println("path" + directory.getPath());
            File file = new File(directory, "textfile.txt");
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write(str.toString());
            osw.flush();
            osw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        str = null;
    }

    private static final int READ_BLOCK_SIZE = 100;

    private void read() {
        try {

            // ---SD Storage---
//            File sdCard = Environment.getExternalStorageDirectory();
//            File directory = new File(sdCard.getAbsolutePath()
//                    + "/MyFiles");
//            File file = new File(directory, "textfile.txt");
//            String path = "file:///android_raw/test.zip";
//            File file = new File(path);
//            FileInputStream fIn = new FileInputStream(file);
//            InputStreamReader isr = new InputStreamReader( mContext.getResources().getAssets().open("test") );

            InputStream myfile = mContext.getResources().openRawResource(R.raw.test);

            InputStreamReader isr = new InputStreamReader(myfile);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String s = "";
            int charRead;
            while ((charRead = isr.read(inputBuffer)) > 0) {

                String readString = String.copyValueOf(inputBuffer, 0,
                        charRead);
                s += readString;

                inputBuffer = new char[READ_BLOCK_SIZE];
            }
            System.out.println(s);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static String getNow4AlarmCheck() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        return fdf.format(new Date());
    }
}