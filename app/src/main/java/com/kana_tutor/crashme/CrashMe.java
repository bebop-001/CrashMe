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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static android.widget.Toast.*;

public class CrashMe extends AppCompatActivity {
    private static final String TAG = CrashMe.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_me);
        Log.d(TAG, "onCreate:start");
        SimpleDateFormat sdf
            = new SimpleDateFormat("MM-dd HH:mm:ss.SSS z '(GMT'Z')'",
                    Locale.getDefault());
        Log.d(TAG, "t1 = " + sdf.format(System.currentTimeMillis()));
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Log.d(TAG, "t2 = " + sdf.format(System.currentTimeMillis()));


        // catch any unhandled file and save the stack trace.
        // Thread.setDefaultUncaughtExceptionHandler(this);

        new HomeDirectory(this);

        // Map<String, String> crashes = crashLog.getCrashes();


        findViewById(R.id.crash_me).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("UnusedAssignment")
            @Override
            public void onClick(View v) {
                int x = 99;
                x /= 0;
            }
        });

        final TextView crashView = findViewById(R.id.latest_crash);
        final Button unlinkLogFiles = findViewById(R.id.unlink_log_files);
        unlinkLogFiles.setEnabled(false);
        unlinkLogFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int unlinkCount = CrashLog.unlinkLogFiles();
                makeText(CrashMe.this
                        , "Removed " + unlinkCount + " log files.", LENGTH_LONG)
                        .show();
                crashView.setText("Latest crash log:\n\nNo crash logs found");
                unlinkLogFiles.setEnabled(false);
            }
        });

        String crashLogs [] = CrashLog.getLogNames();
        String crashLogsFound = "Found crash logs:";
        int i = crashLogs.length - 1;
        if (i > 0) {

            unlinkLogFiles.setEnabled(true);

            for(int j = 0; j <= i; j++)
                crashLogsFound += String.format("\n%2d) %s", j + 1, crashLogs[j]);
            crashLogsFound += "\n\nLatest crash:\n" + CrashLog.getLatestCrash();
        }
        else
            crashLogsFound += "\nNo crash logs found.";
        crashView.setText(crashLogsFound);

        Thread.setDefaultUncaughtExceptionHandler(new CrashLog(this));
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        new HomeDirectory(this);
    }
}
