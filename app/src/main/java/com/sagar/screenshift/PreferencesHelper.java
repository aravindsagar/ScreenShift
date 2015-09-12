package com.sagar.screenshift;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_SHORT;
import static com.sagar.screenshift.ScreenShiftService.ACTION_RESET_DENSITY;

/**
 * Created by aravind on 12/6/15.
 * Constants for SharedPreference and convenience methods for getting and setting them
 */
public class PreferencesHelper {

    private PreferencesHelper() {

    }

    public static final String KEY_MASTER_SWITCH_ON = "master_switch";
    public static final String KEY_RESOLUTION_ENABLED = "resolution_enabled";
    public static final String KEY_RESOLUTION_WIDTH = "resolution_width";
    public static final String KEY_RESOLUTION_HEIGHT = "resolution_height";
    public static final String KEY_OVERSCAN_ENABLED = "overscan_enabled";
    public static final String KEY_OVERSCAN_TOP = "overscan_top";
    public static final String KEY_OVERSCAN_BOTTOM = "overscan_bottom";
    public static final String KEY_OVERSCAN_LEFT = "overscan_left";
    public static final String KEY_OVERSCAN_RIGHT = "overscan_right";
    public static final String KEY_DENSITY_ENABLED = "density_enabled";
    public static final String KEY_DENSITY_VALUE = "density_value";
    public static final String KEY_DISPLAY_HEIGHT = "display_height";
    public static final String KEY_DISPLAY_WIDTH = "display_width";

    // Stores whether changing density triggers a reboot
    public static final String KEY_DENSITY_REBOOT = "density_reboot";

    public static final String KEY_LAST_BOOT_TIME = "last_boot_time";
    public static final String KEY_TUTORIAL_DONE = "tutorial_done";

    public static void setPreference(Context context, final String KEY, boolean value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(KEY, value).apply();
    }

    public static void setPreference(Context context, final String KEY, int value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putInt(KEY, value).apply();
    }

    public static void setPreference(Context context, final String KEY, long value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(KEY, value).apply();
    }

    public static boolean getBoolPreference(Context context, final String KEY){
        return getBoolPreference(context, KEY, false);
    }

    public static boolean getBoolPreference(Context context, final String KEY, boolean defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY, defaultValue);
    }

    public static int getIntPreference(Context context, final String KEY, int defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY, defaultValue);
    }

    public static long getLongPreference(Context context, final String KEY, long defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY, defaultValue);
    }

    public static String getStringPreference(Context context, final String KEY, String defaultValue){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY, defaultValue);
    }

    public static void testDensityReboot(final Activity activity){
        new AlertDialog.Builder(activity).setTitle(R.string.density_reboot_test_title)
                .setMessage(R.string.density_reboot_test_message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.startService(new Intent(activity, ScreenShiftService.class)
                                .setAction(ACTION_RESET_DENSITY));
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                Toast.makeText(activity, R.string.density_no_reboot_toast,
                                        LENGTH_SHORT).show();
                                setPreference(activity, KEY_DENSITY_REBOOT, false);
                            }
                        }.execute();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setPreference(activity, KEY_DENSITY_REBOOT, true);
                    }
                })
                .show();
    }
}
