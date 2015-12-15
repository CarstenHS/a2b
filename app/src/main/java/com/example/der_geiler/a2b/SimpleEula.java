package com.example.der_geiler.a2b;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

// ressource: http://www.donnfelker.com/android-a-simple-eula-for-your-android-apps/

/**
 * Created by der_geiler on 15-12-2015.
 */
public class SimpleEula {

    private String EULA_PREFIX = "eula_";
    static private Activity mActivity;

    public SimpleEula(Activity context) {
        mActivity = context;
    }

    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
            pi = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi;
    }

    static private String readRawTextFile(int resId)
    {
        InputStream inputStream = mActivity.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder sb = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null)
            {
                sb.append(line);
                sb.append('\n');
            }
        } catch (IOException e) {return null;}
        return sb.toString();
    }

    public void show() {
        PackageInfo versionInfo = getPackageInfo();

        // the eulaKey changes every time you increment the version number in the AndroidManifest.xml
        final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean hasBeenShown = prefs.getBoolean(eulaKey, false);
        if(hasBeenShown == false)
        {
            // Show the Eula
            String title = mActivity.getString(R.string.app_name) + " v" + versionInfo.versionName;

            //Includes the updates as well so users know what changed.
            String message = mActivity.getString(R.string.updates) + "\n\n" + readRawTextFile(R.raw.eula);
            //String message = mActivity.getString(R.string.updates) + "\n\n";

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            // Mark this version as read.
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(eulaKey, true);
                            editor.commit();
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {mActivity.finish();}

                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                    public void onCancel(DialogInterface dialog)
                    {mActivity.finish();}});
            AlertDialog ad = builder.create();
            ad.setCanceledOnTouchOutside(false);
            ad.show();
        }
    }

}