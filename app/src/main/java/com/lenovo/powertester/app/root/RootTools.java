package com.lenovo.powertester.app.root;

import android.app.ActivityManager;
import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RootTools {
    public static final class CommandType {
        public static final String AM_FORCE_STOP_APP = "am force-stop ";
        public static final String CLEAR_PKG = " -fs ";
        public static final String COMMAND_ADD_PREFERRED_ACTIVITY = " -add-activity ";
        public static final String COMMAND_CLEAR_PREFERRED_ACTIVITY = " -clear-activity ";
        public static final String COMMAND_CLEAR_RECENT_TASK = " -clear-recent-task ";
        public static final String COMMAND_DISABLE_ALL_RECEVIER = " -disable-receiver ";
        public static final String COMMAND_ENABLE_ALL_RECEVIER = " -enable-receiver ";
        public static final String COMMAND_START_SERVICE = " -start-service ";
        public static final String COMMAND_STOP_SERVICE = " -stop-service ";
        public static final String DUMP_SYSTEM_WAKELOCK = " -dp ";
        public static final String DUMP_WAKELOCK_DETAIL = " -dpwl ";
        public static final String DUMP_ALARMS = " -dpalarm ";
        public static final String KEY_CLEAR_DATA = " -cc ";
        public static final String PM_DISABLE = "pm disable ";
        public static final String PM_ENABLE = "pm enable ";
        public static final String PM_FORCE_INSTALL = "pm install -r ";
        public static final String PM_INSTALL = "pm install ";
        public static final String REMOVE_WAKELOCK = " -rmwl ";
        public static final String SHUT_DOWN = " -sd ";
        public static final String COMMAND_PKG_STATE_STOPPED = " -pss ";
        public static final String COMMAND_PKG_STATE_UNSTOPPED = " -punss ";
        public static final String COMMAND_COPY_FILE = " -cpfile ";
    }

    private static final class ExecCommand implements Callable<Boolean> {
        private Context mContext = null;
        private boolean mIsForceByNAC = false;
        private boolean mIsIgnoreRootType = false;
        private String mPara1 = null;
        private String mPara2 = null;
        private boolean mIsRunningBackground = false;

        public ExecCommand(Context context, String para1, String para2, boolean isRunningBackground) {
            mContext = context;
            mPara1 = para1;
            mPara2 = para2;
            mIsRunningBackground = isRunningBackground;
        }

        public ExecCommand(String para1, String para2) {
            mPara1 = para1;
            mPara2 = para2;
            mIsRunningBackground = true;
        }

        @Override
        public Boolean call() {
            boolean isSucess = false;
            /** lenovo-sw zhaogf1,2014.5.12 ,for app_process execute error,begin**/
            Log.d(TAG, "ExecCommand.call(), mPara1 :" + mPara1 + " ,mPara2" + mPara2);
            try {
                if (mPara2 != null && mPara2.contains("app_process")) {
                    if (mPara1 == null || mPara1.length() < 1) {
                        Log.d(TAG, "app_process execute, mPara1 == null ,return false ");
                        return false;
                    }
                }// end if
            } catch (Exception e) {
                Log.d(TAG, "app_process execute,exception:", e);
                return false;
            }
            /** lenovo-sw zhaogf1,2014.5.12 ,for app_process execute error,end**/
            if (mIsForceByNAC) {
                return executeByCustomizion(mContext, mPara1, mPara2, mIsRunningBackground);
            }
            printMemberValueLog();

            if (SuTools.IsSuRooted() || mIsIgnoreRootType) {
                isSucess = executeBySu(mPara1, mPara2);
                if (PRINT_DEBUG) {
                    Log.d(TAG, "SuRooted  isSucess = " + isSucess);
                }
            }
            Log.d("classic", "RootTools ExecCommand call time = " + System.currentTimeMillis() / 1000);
            if (!isSucess && (mIsNACRooted || mIsIgnoreRootType || mIsSystemRooted)) {
                return executeByCustomizion(mContext, mPara1, mPara2, mIsRunningBackground);
            }
            return isSucess;
        }

        public void setIgnoreRootType(boolean isIgnore) {
            mIsIgnoreRootType = isIgnore;
        }

        public void setOnlyUseNAC() {
            mIsForceByNAC = true;
        }

        private void printMemberValueLog() {
            if (PRINT_DEBUG) {
                Log.d(TAG, "IsNACRooted = " + mIsNACRooted + " isSuRooted = " + SuTools.IsSuRooted()
                        + " mIsIgnoreRooted = " + mIsIgnoreRootType);
            }
        }
    }

    public static final String EXECUTE_JAR = "power.jar";
    public static final String EXECUTE_ROOT_SERVICE_JAR = "rootservice.jar";
    public static final int ROOT_SERVICE_JAR_TYPE = 2;
    private static final String COMMAND_PATH = "/command_path_";
    private static final String EXECUTE_JAR_FULL_NAME = "com.lenovo.powercenter.LFS";
    private static final String EXECUTE_JAR_PARA_PROCESS = "app_process";
    private static final String EXECUTE_PARA_BIN = "/system/bin";
    private static final String EXECUTE_ROOT_SERVICE_JAR_FULL_NAME = "com.lenovo.powercenter.classicmode.service.RootServer";
    private static final String EXPORT_CLASSPATH = "export CLASSPATH=$CLASSPATH:%s\n";
    private static final String EXPORT_LIBRARY = "export LD_LIBRARY_PATH=/vendor/lib:/system/lib\n";
    private static final String LOCAL_ADDRESS = "127.0.0.1";
    private static final int LOCAL_PORT = 30001;
    private static final int MAX_THREAD_COUNTER = 10;
    private static final ExecutorService mExecutor = Executors.newFixedThreadPool(MAX_THREAD_COUNTER);
    private static boolean mIsNACRooted = false;
    private static boolean mIsSystemRooted = false;
    /*private static boolean mIsLesafeRunning = false;*/
    private static final String NAC_SERVER = "nac_server";
    private static final String SYS_SERVER = "supercmdlocalsocket";
    static final boolean PRINT_DEBUG = true;
    private static final String TAG = "RootTools";
    private static final String LENOVO_SAFE1 = "com.lenovo.safecenter";
    private static final String LENOVO_SAFE2 = "com.lenovo.safecenter.ww";
    private static final String LENOVO_SAFE3 = "com.lenovo.safecenterpad";
    private static final String LENOVO_SAFE4 = "com.lenovo.safecenter.hd";

    private static final String PACKAGE_DATA_ABSOLUTE_DIR = "/data/data/com.lenovo.powercenter";

    /**
     * Application first start, acquire root
     *
     * @param contxt
     */
    public static void acquireRoot(final Context contxt) {
        if (contxt == null) {
            throw new IllegalArgumentException();
        }
        /*mIsLesafeRunning = isLenovoSafeRunning(contxt);*/
        if (isObtainRoot()) return;/**lenovo-sw zhaogf1,2014.01.14,not repeat-execute acquireRoot command **/
        SuTools.init(contxt);
        if (PRINT_DEBUG) {
            Log.d(TAG, "SuRoot init result = " + SuTools.IsSuRooted());
        }

        String path = contxt.getFilesDir().getAbsolutePath();
        File file = new File(RootUtils.convertObsolutePath(path + "/" + "wlfile"));
        Log.d(TAG, "path = " + path);
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            Log.d(TAG, "wlfile delete");
        } else {
            Log.d(TAG, "wlfile not exist");
        }
        executeNACCommand(contxt, getExportedJar(contxt, EXECUTE_JAR),
                composeCommandPara(contxt.getFilesDir().getAbsolutePath(), CommandType.DUMP_SYSTEM_WAKELOCK, 1));
        file = new File(RootUtils.convertObsolutePath(path + "/" + "wlfile"));
        if (file.exists()) {
            Log.d(TAG, "wlfile exist, mIsNACRooted :" + mIsNACRooted + " , mIsSystemRooted:" + mIsSystemRooted);
            /*mIsNACRooted = true;*/
        } else {
            Log.d(TAG, "wlfile not exist, mIsNACRooted :" + mIsNACRooted + " , mIsSystemRooted:" + mIsSystemRooted);
            mIsNACRooted = false;
            mIsSystemRooted = false;
        }
        isObtainRoot();
    }

    public static void dumpWakelockDetail(final Context context) {
        executeNACCommand(context, getExportedJar(context, EXECUTE_JAR),
                composeCommandPara(context.getFilesDir().getAbsolutePath(), CommandType.DUMP_WAKELOCK_DETAIL, 1));
    }

    public static void copyAlarmWhiteList(final Context contxt) {
        executeNACCommand(contxt, getExportedJar(contxt, EXECUTE_JAR),
                composeCommandPara(contxt.getFilesDir().getAbsolutePath(), CommandType.COMMAND_COPY_FILE, 1));
    }

    public static void dumpAlarms(final Context contxt) {
        executeNACCommand(contxt, getExportedJar(contxt, EXECUTE_JAR),
                composeCommandPara(contxt.getFilesDir().getAbsolutePath(), CommandType.DUMP_ALARMS, 1));
    }

    private static boolean isLenovoSafeRunning(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> listRunning = am.getRunningAppProcesses();
        int len = 0;
        if (listRunning != null) {
            len = listRunning.size();
        }
        if (len < 1) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            String prcname = listRunning.get(i).processName;
            if (prcname.equals(LENOVO_SAFE1) ||
                    prcname.equals(LENOVO_SAFE2) ||
                    prcname.equals(LENOVO_SAFE3) ||
                    prcname.equals(LENOVO_SAFE4)) {
                Log.d("RootTools", "lenovo safecenter is running");
                return true;
            }
        }
        Log.d("RootTools", "lenovo safecenter is not running");
        return false;
    }

    /**
     * Execute user command
     *
     * @param context
     * @param para1   : command to be executed.
     * @param para2   : command to be executed.
     */
    public static void executeCommand(final Context context, final String para1, final String para2) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        mExecutor.submit(new ExecCommand(context, para1, para2, false));
    }

    /**
     * Execute user command
     *
     * @param context
     * @param para1   : command to be executed.
     * @param para2   : command to be executed.
     * @return true: success; false: failed
     */

    public static boolean executeCommandSyncResult(final Context context, final String para1, final String para2) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        Future<Boolean> result = mExecutor.submit(new ExecCommand(context, para1, para2, false));
        try {
            return result.get();
        } catch (Exception e) {
            printException(e);
        }
        return false;
    }


    /**
     * Execute command in power.jar
     *
     * @param context
     * @param para    : command to be executed.
     */
    public static void executeJarCommand(final Context context, final String para) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        mExecutor.submit(new ExecCommand(context, getExportedJar(context, EXECUTE_JAR), composeCommandPara(para,
                CommandType.CLEAR_PKG), false));
    }

    /**
     * Execute command in power.jar
     *
     * @param context
     * @param para    : command to be executed.
     * @param cmdFlag : command flag in power.jar
     */
    public static void executeJarCommand(final Context context, final String para, final String cmdFlag,
                                         final int jarType) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        boolean isRootServiceJar = (jarType == ROOT_SERVICE_JAR_TYPE);
        mExecutor.submit(new ExecCommand(context, isRootServiceJar ? getExportedJar(context, EXECUTE_ROOT_SERVICE_JAR)
                : getExportedJar(context, EXECUTE_JAR), composeCommandPara(para, cmdFlag, jarType),
                isRootServiceJar ? true : false));
    }

    /**
     * Execute command in power.jar
     *
     * @param context
     * @param para    : command to be executed.
     * @return true: success; false: failed
     */
    public static boolean executeJarSyncResult(final Context context, final String para) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        Future<Boolean> result = mExecutor.submit(new ExecCommand(context, getExportedJar(context, EXECUTE_JAR),
                composeCommandPara(para, CommandType.CLEAR_PKG), false));
        try {
            return result.get();
        } catch (Exception e) {
            printException(e);
        }
        return false;
    }

    /**
     * Execute command in power.jar
     *
     * @param context
     * @return true: success; false: failed
     */
    public static boolean executeJarSyncResult(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
       /* boolean isRootServiceJar = (RootTools.ROOT_SERVICE_JAR_TYPE == ROOT_SERVICE_JAR_TYPE);*/
        boolean isRootServiceJar = true;
        if (PRINT_DEBUG) {
            Log.d(TAG, "executeJarSyncResult time = " + System.currentTimeMillis() / 1000);
        }
        ExecCommand command = new ExecCommand(context, isRootServiceJar ? getExportedJar(context,
                EXECUTE_ROOT_SERVICE_JAR) : getExportedJar(context, EXECUTE_JAR), composeCommandPara("true",
                CommandType.COMMAND_START_SERVICE, RootTools.ROOT_SERVICE_JAR_TYPE), isRootServiceJar ? true : false);
        /*command.setOnlyUseNAC();*/ /** lenovo-sw zhaogf1,for bug [CROWDTEST-1063],git:147567 error **/
        Future<Boolean> result = mExecutor.submit(command);
        try {
            return result.get();
        } catch (Exception e) {
            printException(e);
        }
        return false;
    }

    /**
     * Only for rootservice.jar reconfiguration service environment.
     *
     * @param para
     * @param cmdFlag
     * @return
     */
    public static boolean executeJarSyncResultForNoContext(final String para, final String cmdFlag) {
        Future<Boolean> result = mExecutor.submit(new ExecCommand(getExportedJarForNoContext(),
                composeCommandPara(para, cmdFlag, ROOT_SERVICE_JAR_TYPE)));
        try {
            return result.get();
        } catch (Exception e) {
            printException(e);
        }
        return false;
    }

    /**
     * Do NOT use this methood
     *
     * @return
     */
    public static boolean isObtainRoot() {
        boolean isRootSuccess;
        isRootSuccess = (mIsNACRooted) || SuTools.IsSuRooted() || mIsSystemRooted;
        return isRootSuccess;
    }


    private static String composeCommandPara(final String param, final String flag) {
        Object[] arrayOfObject = new Object[5];
        arrayOfObject[0] = EXECUTE_PARA_BIN;
        arrayOfObject[1] = EXECUTE_JAR_PARA_PROCESS;
        arrayOfObject[2] = EXECUTE_PARA_BIN;
        arrayOfObject[3] = EXECUTE_JAR_FULL_NAME;
        arrayOfObject[4] = flag + param;
        String result = String.format("%s/%s %s %s %s\n", arrayOfObject);
        return result;
    }

    private static String composeCommandPara(final String param, final String flag, final int type) {
        Object[] arrayOfObject = new Object[5];
        arrayOfObject[0] = EXECUTE_PARA_BIN;
        arrayOfObject[1] = EXECUTE_JAR_PARA_PROCESS;
        arrayOfObject[2] = EXECUTE_PARA_BIN;

        if (type == ROOT_SERVICE_JAR_TYPE) {
            arrayOfObject[3] = EXECUTE_ROOT_SERVICE_JAR_FULL_NAME;
        } else {
            arrayOfObject[3] = EXECUTE_JAR_FULL_NAME;
        }
        arrayOfObject[4] = flag + param;
        String result = String.format("%s/%s %s %s %s\n", arrayOfObject);
        return result;
    }

    private static boolean execute(final String cmd, final boolean isRunningBkg) {
        if (executeByNACServer(cmd, isRunningBkg)) {
            return true;
        }

        if (executeBySysServer(cmd)) {
            return true;
        }
        /*if (executeByLocalAddress(cmd, isRunningBkg)) {
            return true;
        }*/
        return false;
    }


    public static String getCommandFile(final Context context, final String command) {
        if (context == null || TextUtils.isEmpty(command)) {
            throw new IllegalArgumentException();
        }
        String randomName = COMMAND_PATH + UUID.randomUUID().toString();
        String commandPath = context.getApplicationInfo().dataDir + randomName + ".sh";

        File cmdFile = new File(commandPath);
        try {
            //noinspection ResultOfMethodCallIgnored
            cmdFile.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
        if (!cmdFile.exists()) {
            return null;
        }
        if (writeSimpleCommand2File(commandPath, command)) {
            return cmdFile.getAbsolutePath();
        }
        return null;
    }

    private static boolean executeByCustomizion(final Context context, final String cmdPara1, final String cmdPara2,
                                                final boolean isRunningBkg) {
        File cmdFile = null;
        String randomName = COMMAND_PATH + UUID.randomUUID().toString();
        String commandPath;

        if (context != null) {
            String contextPath = context.getApplicationInfo().dataDir;
            if (TextUtils.isEmpty(contextPath) || !PACKAGE_DATA_ABSOLUTE_DIR.equals(contextPath)) {
                contextPath = PACKAGE_DATA_ABSOLUTE_DIR;
            }
            commandPath = contextPath + randomName + ".sh";
        } else {
            commandPath = PACKAGE_DATA_ABSOLUTE_DIR + randomName + ".sh";
        }
        try {
            cmdFile = new File(RootUtils.convertObsolutePath(commandPath));
            //noinspection ResultOfMethodCallIgnored
            cmdFile.createNewFile();
            if (!cmdFile.exists()) {
                if (PRINT_DEBUG) {
                    Log.d(TAG, "Failed to create tmp file " + commandPath);
                }
                return false;
            }
            boolean isSucess = writeCommand2File(commandPath, cmdPara1, cmdPara2);
            if (PRINT_DEBUG) {
                Log.d(TAG, "cmd1 = " + cmdPara1);
                Log.d(TAG, "cmd2 = " + cmdPara2);
                Log.d(TAG, "executeByCustomizion time = " + System.currentTimeMillis() / 1000);
            }
            if (isSucess) {
                return execute(commandPath, isRunningBkg);
            }
        } catch (IOException e) {
            printException(e);
        } finally {
            if (cmdFile != null && !cmdFile.delete()) {
                cmdFile.deleteOnExit();
            }
        }
        return false;
    }

    private static boolean executeByLocalAddress(final String filePath, final boolean isRunningBkg) {
        Socket client = null;
        PrintWriter socketWriter = null;
        BufferedReader socketReader = null;
        try {
            client = new Socket(LOCAL_ADDRESS, LOCAL_PORT);
            client.setReuseAddress(true);
            socketWriter = new PrintWriter(client.getOutputStream(), true);
            socketReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            if (isRunningBkg) {
                socketWriter.write(filePath + " & \n");
            } else {
                socketWriter.write(filePath);
            }
            socketWriter.flush();

            String result = socketReader.readLine();
            if (PRINT_DEBUG) {
                Log.d(TAG, "executeByLocalAddress exeCmd = " + filePath + "\nresult = " + result);
            }
            if (!TextUtils.isEmpty(result) && result.startsWith("success")) {
                if (!mIsNACRooted) {
                    mIsNACRooted = true;
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            printException(e);
        } finally {
            try {
                if (socketWriter != null) {
                    socketWriter.close();
                }
                if (socketReader != null) {
                    socketReader.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                printException(e);
            }
        }
        return false;
    }

    private static boolean executeBySysServer(final String filePath) {
        PrintWriter socketWriter = null;
        BufferedReader socketReader = null;
        LocalSocket localSocket = null;
        int read_count = 0;
        byte[] buffer = new byte[512];
        ResponseResult rr;

        try {
            LocalSocketAddress address = new LocalSocketAddress(SYS_SERVER);
            localSocket = new LocalSocket();
            localSocket.connect(address);

            socketWriter = new PrintWriter(localSocket.getOutputStream(), true);
            socketReader = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));

            DataInputStream din = new DataInputStream(
                    (localSocket.getInputStream()));

/*            if (false) {
                socketWriter.write(filePath + " & \n");
            } else {
                socketWriter.write(filePath);
            }*/
            socketWriter.write(filePath);
            socketWriter.flush();

            /*my get result part*/
            read_count = din.read(buffer);
            rr = new ResponseResult(buffer, read_count);


            // 返回的字节长度
            Log.v(TAG, "read_count  =" + read_count);

            // 返回命令执行结果
            Log.v(TAG, "result=" + rr.getResult());

            // 返回错误码
            Log.v(TAG, "error=" + rr.getErrNo());

            // 返回具体的错误信息
            Log.v(TAG, "msg=" + rr.getErrMsg());           
            /*my get result part*/
            String result = socketReader.readLine();
            int error = rr.getErrNo();
            if (PRINT_DEBUG) {
                Log.d(TAG, "executeThroughNAC exeCmd = " + filePath + "\nresult = " + result);
            }
            if (!TextUtils.isEmpty(result) && result.startsWith("success") || (error == 0)) {
                mIsSystemRooted = true;
                return true;
            }
            mIsSystemRooted = false;
            return false;
        } catch (IOException e) {
            printException(e);
        } finally {
            try {
                if (socketWriter != null) {
                    socketWriter.close();
                }
                if (socketReader != null) {
                    socketReader.close();
                }
                if (localSocket != null) {
                    localSocket.close();
                }
            } catch (IOException e) {
                printException(e);
            }
        }
        mIsSystemRooted = false;
        return false;
    }

    private static boolean executeByNACServer(final String filePath, final boolean isRunningBkg) {
        PrintWriter socketWriter = null;
        BufferedReader socketReader = null;
        LocalSocket localSocket = null;
        try {
            LocalSocketAddress address = new LocalSocketAddress(NAC_SERVER);
            localSocket = new LocalSocket();
            localSocket.connect(address);

            socketWriter = new PrintWriter(localSocket.getOutputStream(), true);
            socketReader = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
            if (isRunningBkg) {
                socketWriter.write(filePath + " & \n");
            } else {
                socketWriter.write(filePath);
            }
            socketWriter.flush();

            String result = socketReader.readLine();
            if (PRINT_DEBUG) {
                Log.d(TAG, "executeThroughNAC exeCmd = " + filePath + "\nresult = " + result);
            }
            if (!TextUtils.isEmpty(result) && result.startsWith("success")) {
                if (!mIsNACRooted) {
                    mIsNACRooted = true;
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            printException(e);
        } finally {
            try {
                if (socketWriter != null) {
                    socketWriter.close();
                }
                if (socketReader != null) {
                    socketReader.close();
                }
                if (localSocket != null) {
                    localSocket.close();
                }
            } catch (IOException e) {
                printException(e);
            }
        }
        return false;
    }

    private static boolean executeBySu(final String cmd1, final String cmd2) {
        DataOutputStream outputStream = null;
        Process process;
        try {
            process = Runtime.getRuntime().exec(SuTools.getSuCommand());
            outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes(EXPORT_LIBRARY);
            // outputStream.writeBytes(String.format(EXPORT_CLASSPATH,
            // EXECUTE_PARA_BIN));
            if (!TextUtils.isEmpty(cmd1)) {
                outputStream.writeBytes(cmd1 + " \n");
            }
            if (!TextUtils.isEmpty(cmd2)) {
                outputStream.writeBytes(cmd2 + " \n");
            }
            outputStream.flush();

            return true;
        } catch (Exception e) {
            printException(e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                printException(e);
            }
        }
        return false;
    }

    private static void executeNACCommand(final Context context, final String para1, final String para2) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        ExecCommand command = new ExecCommand(context, para1, para2, false);
        command.setOnlyUseNAC();
        Future<Boolean> result = mExecutor.submit(command);
        try {
            result.get();
        } catch (Exception e) {
            printException(e);
        }
    }

    private static String getExportedJar(final Context context, final String jarName) {
        String nbPath;
        String command = "";
        File nbFile = context.getFileStreamPath(jarName);
        if (nbFile != null && nbFile.exists()) {
            nbPath = nbFile.getAbsolutePath();
            command = String.format(EXPORT_CLASSPATH, nbPath);
        }
        return command;
    }

    private static String getExportedJarForNoContext() {
        String nbPath;
        String command = "";
        File nbFile = new File(RootUtils.convertObsolutePath(PACKAGE_DATA_ABSOLUTE_DIR) + "/files/" +
                RootUtils.convertObsolutePath(RootTools.EXECUTE_ROOT_SERVICE_JAR));
        if (nbFile.exists()) {
            nbPath = nbFile.getAbsolutePath();
            command = String.format(EXPORT_CLASSPATH, nbPath);
        }
        return command;
    }

    private static void printException(Exception e) {
        if (PRINT_DEBUG) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private static boolean writeSimpleCommand2File(final String commandPath, final String command) {
        if (TextUtils.isEmpty(commandPath)) {
            throw new IllegalArgumentException();
        }
        BufferedWriter bw = null;
        StringBuilder buffer = new StringBuilder();
        try {
            FileWriter fileWriter = new FileWriter(commandPath);
            if (!TextUtils.isEmpty(command)) {
                buffer.append(command).append("\n");
            }
            bw = new BufferedWriter(fileWriter);
            bw.write(buffer.toString());
            bw.flush();

            return true;
        } catch (IOException e) {
            printException(e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
            }
        }
        return false;
    }

    private static boolean writeCommand2File(final String commandPath, final String cmdPara1, final String cmdPara2) {
        if (TextUtils.isEmpty(commandPath)) {
            throw new IllegalArgumentException();
        }
        BufferedWriter bw = null;
        StringBuilder buffer = new StringBuilder();
        try {
            FileWriter fileWriter = new FileWriter(commandPath);

            if (!TextUtils.isEmpty(cmdPara1)) {
                buffer.append(cmdPara1).append("\n");
            }
            buffer.append(EXPORT_LIBRARY).append("\n");
            buffer.append(String.format(EXPORT_CLASSPATH, EXECUTE_PARA_BIN)).append("\n");
            if (!TextUtils.isEmpty(cmdPara2)) {
                buffer.append(cmdPara2).append("\n");
            }
            String shString = buffer.toString();
            bw = new BufferedWriter(fileWriter);
            bw.write(shString);
            bw.flush();

            return true;
        } catch (IOException e) {
            printException(e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
            }
        }
        return false;
    }

    private static class ResponseResult {

        /**
         * 调用成功 *
         */
        public static final int SUCCESS = 1;

        /**
         * 调用失败 *
         */
        public static final int FAIL = 0;

        private byte[] mRespBuff = null;
        private int mReadCount = 0;

        private int byteArrayToInt(byte[] b, int offset) {
            int value = 0;
            for (int i = 0; i < 4; i++) {
                int shift = (4 - 1 - i) * 8;
                value += (b[i + offset] & 0x000000FF) << shift;
            }
            return value;
        }

        /**
         * 构造函数
         *
         * @param respBuff  root通道返回的buffer
         * @param readCount 返回的长度
         */
        ResponseResult(byte[] respBuff, int readCount) {
            mRespBuff = respBuff;
            mReadCount = readCount;
        }

        /**
         * 获取调用结果
         *
         * @return 1成功 0失败
         */
        int getResult() {
            if (mRespBuff != null && mRespBuff.length >= 4) {
                return byteArrayToInt(mRespBuff, 0);
            } else {
                return -1;
            }
        }

        /**
         * 获取错误号
         *
         * @return
         */
        int getErrNo() {
            if (mRespBuff != null && mRespBuff.length >= 8) {
                return byteArrayToInt(mRespBuff, 4);
            } else {
                return -1;
            }
        }

        /**
         * 获取错误的具体信息
         *
         * @return 错误的具体信息
         */
        String getErrMsg() {
            String msg = "";
            if (mRespBuff != null && mRespBuff.length >= 8) {
                msg = new String(mRespBuff, 8, mReadCount - 8);
            }
            return msg;
        }
    }

    private RootTools() {
    }
}
