package com.sagar.screenshift2.profileDb;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by aravind on 17/6/15.
 * Specifies the Contract for Profile db
 */
public class ProfileDbContract {
    public static final String CONTENT_AUTHORITY = "com.sagar.screenshift2.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PROFILES = "profiles";

    public static final class ProfileEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PROFILES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + "/" + PATH_PROFILES;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" +
                CONTENT_AUTHORITY + "/" + PATH_PROFILES;

        public static final String TABLE_NAME = "profiles";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_RESOLUTION_ENABLED = "resolution_enabled";
        public static final String COLUMN_RESOLUTION_WIDTH = "resolution_width";
        public static final String COLUMN_RESOLUTION_HEIGHT = "resolution_height";
        public static final String COLUMN_OVERSCAN_ENABLED = "overscan_enabled";
        public static final String COLUMN_OVERSCAN_LEFT = "overscan_left";
        public static final String COLUMN_OVERSCAN_RIGHT = "overscan_right";
        public static final String COLUMN_OVERSCAN_TOP = "overscan_top";
        public static final String COLUMN_OVERSCAN_BOTTOM = "overscan_bottom";
        public static final String COLUMN_DENSITY_ENABLED = "density_enabled";
        public static final String COLUMN_DENSITY_VALUE = "density_value";

        public static Uri buildProfileUriWithId(long id){
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
        }
    }
}
