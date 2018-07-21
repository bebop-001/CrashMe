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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import java.io.File;

import static android.support.v4.app.ActivityCompat.*;
import static android.Manifest.permission.*;
import static android.os.Build.*;
import static android.content.pm.PackageManager.*;
import static android.os.Environment.*;

/*
 *  Allow the user to select whether the home directory used by the
 *  app is in external or internal memory.
 */
@SuppressWarnings("unused")
public class HomeDirectory {
    private static final String TAG = HomeDirectory.class.getSimpleName();

    private static String homeDirPath = "";
    private static boolean useExternalMemory;
    private static boolean requestPermissionPending;

    private final SharedPreferences userPreferences;

    public static String getPath() {
        return homeDirPath;
    }
    public static boolean isExternal() {
        return useExternalMemory;
    }

    private void makeHomeDir(Activity activity, File homeDir, boolean external) {
        File f = new File(homeDir.getPath() + "/" + activity.getString(R.string.app_name));
        if (!f.exists())
            if (!f.mkdir())
                throw new RuntimeException("Failed to make directory:" + f.getAbsoluteFile());
        homeDirPath = f.getAbsolutePath();
        // and update the preferences.
        userPreferences.edit()
            .putString("homeDirPath", homeDirPath)
            .putBoolean("homeDirPath", external)
            .apply();
    }

    private void setHomeDirPath(final Activity activity) {
    new AlertDialog.Builder(activity)
        .setMessage(R.string.permission_storage)
        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                homeDirPath = activity.getFilesDir().toString();
                makeHomeDir(activity, activity.getFilesDir(), false);
            }
        })
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    /*
                     * if this is the case, we must request permision from the
                     * OS, the OS replies yes/no in main/onRequestPermissionsResult
                     * which calls the HomeDirectory class which checks for request
                     * pending, then checks results and sets the home directory path
                     * accordigly.
                     */
                    requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                    requestPermissionPending = true;
                }
                // pre Marshmallo, as long as you're here, it's all good.
                else
                    makeHomeDir(activity, getExternalStorageDirectory(), false);
            }
        })
        .show();
    }
    private boolean stringsContain(String[] strings, String query) {
        boolean doesContain = false;
        for(int i = 0; i < strings.length && !doesContain; i++)
            doesContain = strings[i].equals(query);
        return doesContain;
    }
    HomeDirectory(final Activity activity) {
        userPreferences = activity.getSharedPreferences(
                activity.getString(R.string.app_name)
                + "UserPreferences", Context.MODE_PRIVATE);
        if (homeDirPath.equals("")) {
            PackageInfo packageInfo;
            // Make sure we asked for the WRITE_EXTERNAL permission.
            boolean writeRequested = false;
            try {
                packageInfo = activity.getPackageManager().getPackageInfo(
                        activity.getPackageName(), PackageManager.GET_PERMISSIONS
                );
                if (!stringsContain(packageInfo.requestedPermissions, WRITE_EXTERNAL_STORAGE))
                    throw new RuntimeException(
                            "manifest needs permission WRITE_EXTERNAL_STORAGE");
            }
            catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            /*
             * if this is the case, we must request permision from the
             * OS, the OS replies yes/no in main/onRequestPermissionsResult
             * which calls the HomeDirectory class which checks for request
             * pending, then checks results and sets the home directory path
             * accordigly.
             */
            if(requestPermissionPending) {
                if (checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE)
                        == PERMISSION_GRANTED)
                    makeHomeDir(activity, getExternalStorageDirectory(), true);
                else
                    makeHomeDir(activity, activity.getFilesDir(), false);
                requestPermissionPending = false;
            }
            else {
                // get the current homedir path.
                homeDirPath = userPreferences.getString(
                        "homeDirPath", "");
                useExternalMemory = userPreferences.getBoolean(
                        "useExternalMemory", false);
                // if the user hasn't set this up already, get his preferences.
                if (homeDirPath.equals(""))
                    setHomeDirPath(activity);
            }
        }
    }
}
