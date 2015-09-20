package com.sagar.screenshift2.data_objects;

import android.content.Context;
import android.database.Cursor;

import com.sagar.screenshift2.profileDb.ProfileDbContract.ProfileEntry;

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
}
