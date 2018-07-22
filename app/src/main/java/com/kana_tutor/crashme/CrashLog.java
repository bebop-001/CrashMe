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

import android.app.Activity;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CrashLog
        implements Thread.UncaughtExceptionHandler {
    private static final String TAG = CrashLog.class.getSimpleName();

    private final Activity activity;

    // catch any unhandled file and save the stack trace.
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String timestamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS z '(GMT'Z')'",
                Locale.getDefault()).format(System.currentTimeMillis()),
                DMP_EVENT_FILENAME =
                    HomeDirectory.getPath() + CrashLog.class.getSimpleName() + ".txt",
                DMP_LOG_FILENAME =
                    HomeDirectory.getPath() +  CrashLog.class.getSimpleName() + ".log.txt";
        try {
            // printStackTrace only works with PrintWriter.  Work-around
            // to get a string stacktrace is to lit PrintWriter wrap a
            // StringWriter and dump that using toString().
            StringWriter errors = new StringWriter();
            PrintWriter pw = new PrintWriter(errors);
            pw.print(timestamp + "\n");
            e.printStackTrace(new PrintWriter(errors));
            String stackTrace = errors.toString();

            File crashReport = new File(DMP_EVENT_FILENAME);
            FileWriter fw = new FileWriter(crashReport);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write( stackTrace);
            bw.flush();
            bw.close();
            Log.d(TAG, "CrashLog:event:" + crashReport.getAbsoluteFile());

            File crashLog = new File(DMP_LOG_FILENAME);
            fw = new FileWriter(crashLog, true);
            bw = new BufferedWriter(fw);
            bw.write("======\n" + stackTrace + "\n");
            bw.flush();
            bw.close();
            Log.d(TAG, "CrashLog:log:" + crashLog.getAbsoluteFile());
        }
        catch (FileNotFoundException e1) {
            e1.printStackTrace();        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        activity.finish();
    }

    CrashLog(Activity activity) {
        this.activity = activity;
    }
}
