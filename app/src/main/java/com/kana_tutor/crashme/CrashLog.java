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
        String crashTimestamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS 'GMT'",
                    Locale.UK).format(System.currentTimeMillis());
        File crashFile = getCrashFile();
        try {
            // printStackTrace only works with PrintWriter.  Work-around
            // to get a string stacktrace is to lit PrintWriter wrap a
            // StringWriter and dump that using toString().
            StringWriter errors = new StringWriter();
            PrintWriter pw = new PrintWriter(errors);
            pw.print(crashTimestamp + "\n");
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
            e1.printStackTrace();        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        if(android.os.Build.VERSION.SDK_INT >= 21)
            activity.finishAndRemoveTask();
        else
            activity.finish();
        t.destroy();
        System.exit(1);
    }

    private List<File> getLogs() {
        List<File> files = new ArrayList<>();
        for (File f : new File(HomeDirectory.getPath()).listFiles()) {
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
        return files;
    }
    private String getLogTag(File logFile) {
        String baseName = logFile.getName();
        int start = baseName.indexOf('.'), end = baseName.lastIndexOf('.');
        return baseName.substring(start +  1, end);
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private String getLog (File logFile) {
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

    public Map<String, String> getCrashes() {
        Map<String, String> crashes = new HashMap<>();
        List<File> logs = getLogs();
        for (File logFile : logs) {
            crashes.put(getLogTag(logFile), getLog(logFile));
        }
        return crashes;
    }
    public String getLatestCrash() {
        String rv = "";
        List<File> logs = getLogs();
        if (logs.size() > 0) {
            Collections.sort(logs);
            rv = getLog(logs.get(logs.size() - 1));
        }
        return rv;
    }

    CrashLog(Activity activity) {
        this.activity = activity;
    }
}
