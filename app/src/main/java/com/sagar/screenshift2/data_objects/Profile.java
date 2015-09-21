package com.sagar.screenshift2.data_objects;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.sagar.screenshift2.PreferencesHelper;
import com.sagar.screenshift2.ScreenShiftService;
import com.sagar.screenshift2.profileDb.ProfileDbContract.ProfileEntry;

import static com.sagar.screenshift2.PreferencesHelper.KEY_DENSITY_ENABLED;
import static com.sagar.screenshift2.PreferencesHelper.KEY_DENSITY_VALUE;
import static com.sagar.screenshift2.PreferencesHelper.KEY_MASTER_SWITCH_ON;
import static com.sagar.screenshift2.PreferencesHelper.KEY_OVERSCAN_BOTTOM;
import static com.sagar.screenshift2.PreferencesHelper.KEY_OVERSCAN_ENABLED;
import static com.sagar.screenshift2.PreferencesHelper.KEY_OVERSCAN_LEFT;
import static com.sagar.screenshift2.PreferencesHelper.KEY_OVERSCAN_RIGHT;
import static com.sagar.screenshift2.PreferencesHelper.KEY_OVERSCAN_TOP;
import static com.sagar.screenshift2.PreferencesHelper.KEY_RESOLUTION_ENABLED;
import static com.sagar.screenshift2.PreferencesHelper.KEY_RESOLUTION_HEIGHT;
import static com.sagar.screenshift2.PreferencesHelper.KEY_RESOLUTION_WIDTH;
import static com.sagar.screenshift2.ScreenShiftService.ACTION_START;

/**
 * Created by aravind on 17/6/15.
 * A class representing a profile
 */
public class Profile {
    public String name;
    public boolean isResolutionEnabled;
    public boolean isOverscanEnabled;
    public boolean isDensityEnabled;
    public int resolutionWidth;
    public int resolutionHeight;
    public int densityValue;
    public int overscanLeft;
    public int overscanRight;
    public int overscanTop;
    public int overscanBottom;
    public int id;

    public static Profile[] getAllProfiles(Context context) {
        Profile[] profiles = null;
        Cursor cursor = context.getContentResolver()
                .query(ProfileEntry.CONTENT_URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            profiles = new Profile[cursor.getCount()];
            for (int i = 0; !cursor.isAfterLast(); cursor.moveToNext(), i++) {
                profiles[i] = getProfileFromCursor(cursor);
            }
            cursor.close();
        }
        return profiles;
    }

    public static Profile getProfile(Context context, int id) {
        Cursor cursor = context.getContentResolver()
                .query(ProfileEntry.buildProfileUriWithId(id), null, null, null, null);
        if(cursor != null && cursor.moveToFirst()) {
            Profile profile = getProfileFromCursor(cursor);
            cursor.close();
            return profile;
        }
        return null;
    }

    private static Profile getProfileFromCursor(Cursor cursor) {
        Profile profile = new Profile();
        profile.id                  = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry._ID));
        profile.isResolutionEnabled = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry.COLUMN_RESOLUTION_ENABLED)) == 1;
        profile.isDensityEnabled    = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry.COLUMN_DENSITY_ENABLED)) == 1;
        profile.isOverscanEnabled   = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry.COLUMN_OVERSCAN_ENABLED)) == 1;
        profile.resolutionWidth     = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry.COLUMN_RESOLUTION_WIDTH));
        profile.resolutionHeight    = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry.COLUMN_RESOLUTION_HEIGHT));
        profile.densityValue        = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry.COLUMN_DENSITY_VALUE));
        profile.overscanLeft        = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry.COLUMN_OVERSCAN_LEFT));
        profile.overscanRight       = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry.COLUMN_OVERSCAN_RIGHT));
        profile.overscanTop         = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry.COLUMN_OVERSCAN_TOP));
        profile.overscanBottom      = cursor.getInt(cursor.getColumnIndex(
                ProfileEntry.COLUMN_OVERSCAN_BOTTOM));
        profile.name                = cursor.getString(cursor.getColumnIndex(
                ProfileEntry.COLUMN_NAME));
        return profile;
    }

    public void saveAsCurrent(Context context) {
        saveWithKeySuffix(context, "");

        if(PreferencesHelper.getBoolPreference(context, KEY_MASTER_SWITCH_ON)) {
            context.startService(new Intent(context, ScreenShiftService.class)
                    .setAction(ACTION_START));
        }
    }

    public void saveAsDefault(Context context) {
        saveWithKeySuffix(context, "_default");
    }

    private void saveWithKeySuffix(Context context, String suffix) {
        PreferencesHelper.setPreference(context, KEY_RESOLUTION_ENABLED + suffix, isResolutionEnabled);
        PreferencesHelper.setPreference(context, KEY_DENSITY_ENABLED    + suffix, isDensityEnabled);
        PreferencesHelper.setPreference(context, KEY_OVERSCAN_ENABLED   + suffix, isOverscanEnabled);
        PreferencesHelper.setPreference(context, KEY_RESOLUTION_WIDTH   + suffix, resolutionWidth);
        PreferencesHelper.setPreference(context, KEY_RESOLUTION_HEIGHT  + suffix, resolutionHeight);
        PreferencesHelper.setPreference(context, KEY_OVERSCAN_LEFT      + suffix, overscanLeft);
        PreferencesHelper.setPreference(context, KEY_OVERSCAN_RIGHT     + suffix, overscanRight);
        PreferencesHelper.setPreference(context, KEY_OVERSCAN_TOP       + suffix, overscanTop);
        PreferencesHelper.setPreference(context, KEY_OVERSCAN_BOTTOM    + suffix, overscanBottom);
        PreferencesHelper.setPreference(context, KEY_DENSITY_VALUE + suffix, densityValue);
    }

    public static Profile fromSavedValues(Context context) {
        return fromSavedValuesWithKeySuffix(context, "");
    }

    public static Profile fromSavedDefaultValues(Context context) {
        return fromSavedValuesWithKeySuffix(context, "_default");
    }

    private static Profile fromSavedValuesWithKeySuffix(Context context, String suffix) {
        Profile current = new Profile();
        current.isResolutionEnabled = PreferencesHelper.getBoolPreference(context, KEY_RESOLUTION_ENABLED + suffix);
        current.isOverscanEnabled   = PreferencesHelper.getBoolPreference(context, KEY_OVERSCAN_ENABLED + suffix);
        current.isDensityEnabled    = PreferencesHelper.getBoolPreference(context, KEY_DENSITY_ENABLED + suffix);
        current.resolutionWidth     = PreferencesHelper.getIntPreference (context, KEY_RESOLUTION_WIDTH + suffix, -1);
        current.resolutionHeight    = PreferencesHelper.getIntPreference (context, KEY_RESOLUTION_HEIGHT + suffix, -1);
        current.overscanLeft        = PreferencesHelper.getIntPreference (context, KEY_OVERSCAN_LEFT + suffix, 0);
        current.overscanRight       = PreferencesHelper.getIntPreference (context, KEY_OVERSCAN_RIGHT + suffix, 0);
        current.overscanTop         = PreferencesHelper.getIntPreference (context, KEY_OVERSCAN_TOP + suffix, 0);
        current.overscanBottom      = PreferencesHelper.getIntPreference (context, KEY_OVERSCAN_BOTTOM + suffix, 0);
        current.densityValue        = PreferencesHelper.getIntPreference (context, KEY_DENSITY_VALUE + suffix, -1);
        return current;
    }
}
