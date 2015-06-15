package com.sagar.screenshift;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by aravind on 12/6/15.
 * Constants for SharedPrefernce and convenience methods for getting and setting them
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

    public static void setPreference(Context context, final String KEY, boolean value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(KEY, value).apply();
    }

    public static void setPreference(Context context, final String KEY, int value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putInt(KEY, value).apply();
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
}
