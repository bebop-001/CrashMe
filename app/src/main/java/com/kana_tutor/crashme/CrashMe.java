package com.kana_tutor.crashme;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashMe extends AppCompatActivity
        implements Thread.UncaughtExceptionHandler
        , View.OnClickListener {
    private static final String TAG = CrashMe.class.getSimpleName();
    private static final String DMP_FILENAME = "CrashDump.txt";

    private HomeDirectory homeDirectory;
    public static String homeDirPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_me);

        // catch any unhandled file and save the stack trace.
        // Thread.setDefaultUncaughtExceptionHandler(this);

        Button crash = findViewById(R.id.crash_me);
        crash.setOnClickListener(this);

        homeDirPath = new HomeDirectory(CrashMe.this).getPath();
    }

    @Override
    public void onClick(View v) {
        // Crash!
        int x = 1;
        x /= 0;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        homeDirectory = new HomeDirectory(this);
    }
    // catch any unhandled file and save the stack trace.
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String timestamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS")
                .format(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
        try {
            // printStackTrace only works with PrintWriter.  Work-around
            // to get a string stacktrace is to lit PrintWriter wrap a
            // StringWriter and dump that using toString().
            StringWriter errors = new StringWriter();
            PrintWriter pw = new PrintWriter(errors);
            pw.print(timestamp + " ");
            e.printStackTrace(new PrintWriter(errors));
            String stackTrace = errors.toString();



            pw = new PrintWriter(new OutputStreamWriter(
                    openFileOutput(DMP_FILENAME, MODE_APPEND)));
            pw.print(timestamp + " ");
            e.printStackTrace(pw);
            pw.flush();
            pw.close();
            File f = new File(getFilesDir() + "/" + DMP_FILENAME);
            String lastModified = new Date(f.lastModified()).toString();

        } catch (FileNotFoundException e1) {
            // do nothing
        }
        CrashMe.this.finish();
    }
}
