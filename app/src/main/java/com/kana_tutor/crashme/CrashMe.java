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
import android.widget.TextView;

import static android.widget.Toast.*;

public class CrashMe extends AppCompatActivity {
    private static final String TAG = CrashMe.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_me);
        Log.d(TAG, "onCreate:start");

        // catch any unhandled file and save the stack trace.
        // Thread.setDefaultUncaughtExceptionHandler(this);

        new HomeDirectory(this);
        new CrashLog(this);

        Thread.setDefaultUncaughtExceptionHandler(new CrashLog(this));
        // Map<String, String> crashes = crashLog.getCrashes();

        final TextView crashView = findViewById(R.id.latest_crash);

        findViewById(R.id.crash_me).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("UnusedAssignment")
            @Override
            public void onClick(View v) {
                int x = 99;
                x /= 0;
            }
        });
        findViewById(R.id.show_most_recent_crash)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String latestCrash = CrashLog.getLatestCrash();
                if (latestCrash.length() > 0) {
                    crashView.setText(latestCrash);
                }
            }
        });
        findViewById(R.id.unlink_log_files).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int unlinkCount = CrashLog.unlinkLogFiles();
                makeText(CrashMe.this
                        , "Removed " + unlinkCount + " log files.", LENGTH_LONG)
                    .show();
                crashView.setText("");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        new HomeDirectory(this);
    }
}
