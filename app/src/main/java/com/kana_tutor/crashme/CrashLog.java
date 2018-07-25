/*
 *  Copyright 2018 Steven Smith kana-tutor.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *
 *  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *  either express or implied.
 *
 *  See the License for the specific language governing permissions
 *  and limitations under the License.
 */
package com.kana_tutor.crashme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static android.os.Process.killProcess;
import static android.os.Process.myPid;

public class CrashLog
        implements Thread.UncaughtExceptionHandler {
    private static final String TAG = CrashLog.class.getSimpleName();

    private final Activity activity;

    @SuppressLint("DefaultLocale")
    private File getCrashFile() {
        int i = 0;
        String dateStamp = new SimpleDateFormat("yyyy-MM-dd",
            Locale.UK).format(System.currentTimeMillis());
        File f;
        do {
            f = new File(String.format("%s/CrashLog.%s.%02d.txt"
                    , HomeDirectory.getPath(), dateStamp, i++));
        } while(f.exists());
        return f;
    }

    // catch any unhandled file and save the stack trace.
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        SimpleDateFormat fmt = new SimpleDateFormat("MM-dd HH:mm:ss.SSS z '(GMT'Z')'",
                Locale.getDefault());
        // output GMT
        // SimpleDateFormat gmtFmt = (SimpleDateFormat) fmt.clone();
        // gmtFmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        File crashFile = getCrashFile();
        try {
            // printStackTrace only works with PrintWriter.  Work-around
            // to get a string stacktrace is to lit PrintWriter wrap a
            // StringWriter and dump that using toString().
            StringWriter errors = new StringWriter();
            PrintWriter pw = new PrintWriter(errors);
            pw.print(fmt.format(System.currentTimeMillis()) + "\n");
            e.printStackTrace(new PrintWriter(errors));
            String stackTrace = errors.toString();

            BufferedWriter bw = new BufferedWriter(
                new FileWriter(crashFile));
            bw.write( stackTrace);
            bw.flush();
            bw.close();
            Log.d(TAG
                ,"Crash file saved as:"
                + crashFile.getAbsoluteFile()
            );
        }
        catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        /*
         * I tried finish/finishAndRemoveTask() and task.destroy()
         * and even System.exit() to exit this thread handler.  In
         * every case the app the app wouldn't restart correctly
         * afterward.  I noticed ps showed the app was still active in
         * 'S' suspended state.  I found a thread on stack overflow
         * that said
         * "When you use the finish() method, it does not
         * close the process completely , it is STILL working
         * in background." so tried killing the app process and
         * that fixed the problem.
         * (see: https://stackoverflow.com/questions/3105673
         *     /how-to-kill-an-application-with-all-its-activities)
         */
        Log.d(TAG, "Crashlog: kill ps " + myPid());
        if (Build.VERSION.SDK_INT > 21)
            activity.finishAndRemoveTask();
        else
            activity.finishActivity(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        killProcess(myPid());
    }

    private static List<File> getLogs() {
        List<File> files = new ArrayList<>();
        String homeDir = HomeDirectory.getPath();
        File[] logFiles = new File(homeDir).listFiles();
        if (logFiles != null) {
            for (File f : logFiles) {
                String baseName = f.getName();
                if (baseName.startsWith("CrashLog.") && baseName.endsWith((".txt"))) {
                    int start = baseName.indexOf('.'), end = baseName.lastIndexOf('.');
                    if (start > 0 && end > 0 && start != end) {
                        String dateStamp = baseName.substring(start + 1, end);
                        if (dateStamp.matches("\\d{4}-\\d{2}-\\d{2}.\\d{2}")) {
                            files.add(f);
                        }
                    }
                }
            }
        }
        return files;
    }
    private static String getLogTagFromFile(File logFile) {
        String baseName = logFile.getName();
        int start = baseName.indexOf('.'), end = baseName.lastIndexOf('.');
        return baseName.substring(start +  1, end);
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static String getLog (File logFile) {
        String rv = "";
        try {
            FileInputStream is = new FileInputStream(logFile);
            byte [] buffer = new byte[(int) logFile.length()];
            is.read(buffer);
            rv = new String(buffer);
            is.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return rv;
    }
    public static String [] getLogNames() {
        List<File> logs = getLogs();
        String[] rv = new String[logs.size()];
        for (int i = 0; i < logs.size(); i++)
            rv[i] = logs.get(i).getName();
        return rv;
    }
    public static Map<String, String> getCrashes() {
        Map<String, String> crashes = new HashMap<>();
        List<File> logs = getLogs();
        for (File logFile : logs) {
            crashes.put(getLogTagFromFile(logFile), getLog(logFile));
        }
        return crashes;
    }
    public static String getLatestCrash() {
        String rv = "";
        List<File> logs = getLogs();
        if (logs.size() > 0) {
            Collections.sort(logs);
            rv = getLog(logs.get(logs.size() - 1));
        }
        return rv;
    }
    // Remove all the log files.  Throw a runtime error
    // if removal fails.
    public static int unlinkLogFiles() {
        List<File> logs = getLogs();
        for (File log : logs) {
            if( !log.delete())
                throw new RuntimeException(
                    "Failed to unlink logfile " + log.getAbsoluteFile()
                );
        }
        return logs.size();
    }

    CrashLog(Activity activity) {
        this.activity = activity;
    }
}
