package com.sagar.screenshift2.profileDb;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sagar.screenshift2.profileDb.ProfileDbContract.AppProfileEntry;
import com.sagar.screenshift2.profileDb.ProfileDbContract.ProfileEntry;

/**
 * Created by aravind on 17/6/15.
 * Content provider for profile db
 */
public class ProfileDbProvider extends ContentProvider {

    private static final int PROFILE         = 100;
    private static final int PROFILE_WITH_ID = 101;
    private static final int APP             = 200;
    private static final int APP_WITH_ID     = 201;
    private static final String LOG_TAG      = ProfileDbProvider.class.getSimpleName();

    private ProfileDbHelper dbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String  authority  = ProfileDbContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, ProfileDbContract.PATH_PROFILES,            PROFILE);
        matcher.addURI(authority, ProfileDbContract.PATH_PROFILES + "/#",     PROFILE_WITH_ID);
        matcher.addURI(authority, ProfileDbContract.PATH_APP_PROFILES,        APP);
        matcher.addURI(authority, ProfileDbContract.PATH_APP_PROFILES + "/#", APP_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = ProfileDbHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (match) {
            case PROFILE_WITH_ID:
                selection     = ProfileEntry._ID + " = ? ";
                selectionArgs = new String[]{getIdFromUriAsString(uri)};
            case PROFILE:
                return db.query(ProfileEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
            case APP_WITH_ID:
                selection     = AppProfileEntry._ID + " = ? ";
                selectionArgs = new String[]{getIdFromUriAsString(uri)};
            case APP:
                return db.query(AppProfileEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
        }
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PROFILE:
                return ProfileEntry.CONTENT_TYPE;
            case PROFILE_WITH_ID:
                return ProfileEntry.CONTENT_ITEM_TYPE;
            case APP:
                return AppProfileEntry.CONTENT_TYPE;
            case APP_WITH_ID:
                return AppProfileEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long _id;
        Uri returnUri;
        switch (sUriMatcher.match(uri)){
            case PROFILE:
                _id = db.insert(ProfileEntry.TABLE_NAME, null, contentValues);
                if(_id > 0){
                    returnUri = ProfileEntry.buildProfileUriWithId(_id);
                }
                else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            case APP:
                _id = db.insert(AppProfileEntry.TABLE_NAME, null, contentValues);
                if(_id > 0){
                    returnUri = ProfileEntry.buildProfileUriWithId(_id);
                }
                else{
                    Log.e(LOG_TAG, "Failed to insert row into " + uri);
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        try {
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (NullPointerException e) {
            Log.e("ScreenShift Db Provider", e.getMessage());
            e.printStackTrace();
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int returnValue;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)){
            case PROFILE_WITH_ID:
                selection     = ProfileEntry._ID + " = ? ";
                selectionArgs = new String[]{getIdFromUriAsString(uri)};
            case PROFILE:
                returnValue   = db.delete(ProfileEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case APP_WITH_ID:
                selection     = AppProfileEntry._ID + " = ? ";
                selectionArgs = new String[]{getIdFromUriAsString(uri)};
            case APP:
                returnValue   = db.delete(AppProfileEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
        if(selection == null || returnValue != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return returnValue;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int returnValue;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)){
            case PROFILE_WITH_ID:
                selection     = ProfileEntry._ID + " = ? ";
                selectionArgs = new String[]{getIdFromUriAsString(uri)};
            case PROFILE:
                returnValue   = db.update(ProfileEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case APP_WITH_ID:
                selection     = AppProfileEntry._ID + " = ? ";
                selectionArgs = new String[]{getIdFromUriAsString(uri)};
            case APP:
                returnValue   = db.update(AppProfileEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnValue;
    }

    private static String getIdFromUriAsString(Uri uri){
        return String.valueOf(ContentUris.parseId(uri));
    }
}
