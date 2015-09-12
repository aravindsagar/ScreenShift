package com.sagar.screenshift2.profileDb;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sagar.screenshift2.profileDb.ProfileDbContract.ProfileEntry;

/**
 * Created by aravind on 17/6/15.
 * SQLiteHelper class for profile db
 */
public class ProfileDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "profiles.db";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_PROFILE = "CREATE TABLE " + ProfileEntry.TABLE_NAME + " ( "
                + ProfileEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProfileEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ProfileEntry.COLUMN_DENSITY_ENABLED + " INTEGER DEFAULT 0, "
                + ProfileEntry.COLUMN_RESOLUTION_ENABLED + " INTEGER DEFAULT 0, "
                + ProfileEntry.COLUMN_OVERSCAN_ENABLED + " INTEGER DEFAULT 0, "
                + ProfileEntry.COLUMN_OVERSCAN_LEFT + " INTEGER DEFAULT 0, "
                + ProfileEntry.COLUMN_OVERSCAN_RIGHT + " INTEGER DEFAULT 0, "
                + ProfileEntry.COLUMN_OVERSCAN_TOP + " INTEGER DEFAULT 0, "
                + ProfileEntry.COLUMN_OVERSCAN_BOTTOM + " INTEGER DEFAULT 0, "
                + ProfileEntry.COLUMN_DENSITY_VALUE + " INTEGER DEFAULT -1, "
                + ProfileEntry.COLUMN_RESOLUTION_WIDTH + " INTEGER DEFAULT -1, "
                + ProfileEntry.COLUMN_RESOLUTION_HEIGHT + " INTEGER DEFAULT -1); ";
        sqLiteDatabase.execSQL(SQL_CREATE_PROFILE);
        for (int i = 1; i <= DATABASE_VERSION; i++) {
            ContentValues[] values = getAddedProfileValues(i);
            if (values != null) {
                for(ContentValues values1: values) {
                    sqLiteDatabase.insert(ProfileEntry.TABLE_NAME, null, values1);
                }
            }
        }
    }

    private ContentValues[] getAddedProfileValues(int version) {
        //TODO add more popular devices
        switch (version) {
            case 1:
                ContentValues[] valuesArray = new ContentValues[5];
                ContentValues n4Values = new ContentValues();
                n4Values.put(ProfileEntry.COLUMN_NAME, "Nexus 4");
                n4Values.put(ProfileEntry.COLUMN_RESOLUTION_ENABLED, 1);
                n4Values.put(ProfileEntry.COLUMN_RESOLUTION_HEIGHT, 1280);
                n4Values.put(ProfileEntry.COLUMN_RESOLUTION_WIDTH, 768);
                valuesArray[0] = n4Values;

                ContentValues n5Values = new ContentValues();
                n5Values.put(ProfileEntry.COLUMN_NAME, "Nexus 5/HTC One M9");
                n5Values.put(ProfileEntry.COLUMN_RESOLUTION_ENABLED, 1);
                n5Values.put(ProfileEntry.COLUMN_RESOLUTION_HEIGHT, 1920);
                n5Values.put(ProfileEntry.COLUMN_RESOLUTION_WIDTH, 1080);
                valuesArray[1] = n5Values;

                ContentValues n6Values = new ContentValues();
                n6Values.put(ProfileEntry.COLUMN_NAME, "Nexus 6/Note 4/Galaxy S6/LG G4");
                n6Values.put(ProfileEntry.COLUMN_RESOLUTION_ENABLED, 1);
                n6Values.put(ProfileEntry.COLUMN_RESOLUTION_HEIGHT, 2560);
                n6Values.put(ProfileEntry.COLUMN_RESOLUTION_WIDTH, 1440);
                valuesArray[2] = n6Values;

                ContentValues n7Values = new ContentValues();
                n7Values.put(ProfileEntry.COLUMN_NAME, "Nexus 7 2013");
                n7Values.put(ProfileEntry.COLUMN_RESOLUTION_ENABLED, 1);
                n7Values.put(ProfileEntry.COLUMN_RESOLUTION_HEIGHT, 1920);
                n7Values.put(ProfileEntry.COLUMN_RESOLUTION_WIDTH, 1200);
                valuesArray[3] = n7Values;

                ContentValues gnValues = new ContentValues();
                gnValues.put(ProfileEntry.COLUMN_NAME, "Galaxy Nexus");
                gnValues.put(ProfileEntry.COLUMN_RESOLUTION_ENABLED, 1);
                gnValues.put(ProfileEntry.COLUMN_RESOLUTION_HEIGHT, 1280);
                gnValues.put(ProfileEntry.COLUMN_RESOLUTION_WIDTH, 720);
                valuesArray[4] = gnValues;

                return valuesArray;
        }
        return null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private static ProfileDbHelper mInstance = null;

    public static ProfileDbHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (mInstance == null) {
            mInstance = new ProfileDbHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private ProfileDbHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
