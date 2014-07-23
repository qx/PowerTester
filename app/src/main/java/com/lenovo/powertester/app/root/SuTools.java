package com.lenovo.powertester.app.root;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;

class SuTools {
    private static final int BUFFER_SIZE = 1024;
    private static boolean isCmccSucess = false;
    private static final String KEY_CMCC_COMMAND = "/system/bin/cmcc_ps";
    private static final String KEY_SU_COMMAND = "su";
    private static boolean mIsSuRooted = false;
    private static final String MOUNT_COMMAND = "mount";
    private static final String TAG = "SuTools";

    public static boolean executeCmd(final String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            throw new IllegalArgumentException();
        }
        DataOutputStream outputStream = null;
        try {
            Process process = Runtime.getRuntime().exec(RootUtils.convertObsolutePath(KEY_SU_COMMAND));
            outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.write((cmd + "\n").getBytes());
            outputStream.flush();

            return true;
        } catch (Exception e) {
            if (RootTools.PRINT_DEBUG) {
                Log.e(TAG, e.getMessage(), e);
            }
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                }
            }
        }
        return false;
    }

    public static String getSuCommand() {
        return isCmccSucess ? KEY_CMCC_COMMAND : KEY_SU_COMMAND;
    }

    public static void init(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
//        isCmccSucess = isCmccSuccessed();
//        if (isCmccSucess) {
//            mIsSuRooted = true;
//            return;
//        }
        mIsSuRooted = isSuSuccessed();
        if (!mIsSuRooted) {
            return;
        }
//        isCmccSucess = updateCmcc(context);
    }

    private final static class CommandResultError {
        static final String NOT_FOUND = "not found";
        static final String ERROR = "error";
        static final String FAILED = "failed";
        static final String REFUSED = "refused";
        static final String UNKOWN = "Unknown";

        static boolean isInvalid(String result) {
            return result.contains(NOT_FOUND) || result.contains(ERROR) || result.contains(FAILED)
                    || result.contains(REFUSED);
        }
    }

    public static boolean isCmccSuccessed() {
        DataOutputStream outputStream = null;
        BufferedReader in = null;
        try {
            Process process = Runtime.getRuntime().exec(KEY_CMCC_COMMAND);
            outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("id" + "\n");
            outputStream.flush();

            InputStream inputStream = process.getInputStream();
            in = new BufferedReader(new InputStreamReader(inputStream));
            String result = null;
            while ((result = in.readLine()) != null) {
                Log.d(TAG, "cmcc result = " + result);
                return !CommandResultError.isInvalid(result);
            }
        } catch (Exception e) {
            if (RootTools.PRINT_DEBUG) {
                Log.e(TAG, e.getMessage(), e);
            }
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                	Log.d(TAG, e.toString());
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                	Log.d(TAG, e.toString());
                }
            }
        }
        return false;
    }

    public static boolean IsSuRooted() {
        return mIsSuRooted;
    }

    private static boolean copyFile(final Context context, final File toFile) {
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            fos = new FileOutputStream(toFile);
            is = context.getResources().getAssets().open("c");
            byte[] buf = new byte[BUFFER_SIZE];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();

            return true;
        } catch (Exception e) {
            if (RootTools.PRINT_DEBUG) {
                Log.e(TAG, e.getMessage(), e);
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            	Log.d(TAG, e.toString());
            }
        }
        return false;
    }

    private static String getSystemDevPath(final Context context) {
        String path = context.getFilesDir() + File.separator + MOUNT_COMMAND;
        File mountFile = new File(path);

        String system = "";
        BufferedReader bufReader = null;
        try {
            if (!writeMountInfoToFile(path)) {
                return "";
            }

            FileInputStream fis = new FileInputStream(mountFile);
            bufReader = new BufferedReader(new InputStreamReader(fis));

            String line = bufReader.readLine();
            while ((line = bufReader.readLine()) != null) {
                if (line.contains(" /system ")) {
                    system = line.substring(0, line.indexOf(' '));
                    break;
                }
            }
        } catch (Exception e) {
            if (RootTools.PRINT_DEBUG) {
                Log.e(TAG, e.getMessage(), e);
            }
        } finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException e) {
                	Log.d(TAG, e.toString());
                }
            }
            if (mountFile.exists() && !mountFile.delete()) {
                mountFile.deleteOnExit();
            }
        }
        return system;
    }

    private static boolean isSuSuccessed() {
        DataOutputStream outputStream = null;
        BufferedReader in = null;
        try {
            Process process = Runtime.getRuntime().exec(RootUtils.convertObsolutePath(KEY_SU_COMMAND));
            outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("id" + "\n");
            outputStream.flush();

            InputStream mInputStream = process.getInputStream();
            in = new BufferedReader(new InputStreamReader(mInputStream));
            String result = null;
            while ((result = in.readLine()) != null) {
                Log.d(TAG, "su result = " + result);
                if (CommandResultError.isInvalid(result)) {
                    return false;
                }
                return true;
            }
            // return true;
        } catch (Exception e) {
            if (RootTools.PRINT_DEBUG) {
                Log.e(TAG, e.getMessage(), e);
            }
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                	Log.d(TAG, e.toString());
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                	Log.d(TAG, e.toString());
                }
            }
        }
        return false;
    }

    private static boolean updateCmcc(final Context context) {
        String systemDev = getSystemDevPath(context);
        File targetFile = new File(context.getFilesDir() + File.separator + "tmp");
        boolean isSucess = copyFile(context, targetFile);
        if (!isSucess) {
            return false;
        }

        boolean result = executeCmd("mount -o remount,rw " + systemDev + " /system \n" + "cat " + targetFile.getAbsolutePath() + " > " + KEY_CMCC_COMMAND + " \n" + "chown root.root " + KEY_CMCC_COMMAND + " \n" + "chmod 6777 /system/bin/cmcc_ps \n" + "mount -o remount,ro " + systemDev + " /system \n");
        if (targetFile.exists() && !targetFile.delete()) {
            targetFile.deleteOnExit();
        }

        File cmccFile = new File(RootUtils.convertObsolutePath(KEY_CMCC_COMMAND));
        if (cmccFile.exists() && cmccFile.length() == 0) {
            cmccFile.delete();
            return false;
        }
        return result;
    }

    private static boolean writeMountInfoToFile(final String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("mount > ").append(path).append("\n").append("chmod 666 ").append(path).append("\n");
        return executeCmd(sb.toString());
    }

    private SuTools() {
    }

    public boolean isCmccSucess() {
        return isCmccSucess;
    }
}
