package dbhelper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/*This is the schema*/
public class TableContract {
    public static final String CONTENT_AUTHORITY = "com.example.apnatime";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_USER = "user";


    public TableContract() {
    }

    public static abstract class UserEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;

        public static String TABLE_NAME = "user";

        public static String USER_PHONE_NUMBER = "phone_nuber";
        public static String USER_FULL_NAME = "full_name";
        public static String USER_ADDRESS = "address";
        public static String USER_EXPERIENCE = "experience";
        public static String USER_DOB = "dob";


        public static final String SQL_CREATE = T.CREATE_TABLE + TABLE_NAME
                + T.OPEN_BRACE
                + _ID + T.TYPE_INTEGER + T.PRIMARY_KEY + T.AUTO_INCREMENT + T.SEP_COMMA
                + USER_PHONE_NUMBER + T.TYPE_TEXT + T.SEP_COMMA
                + USER_FULL_NAME + T.TYPE_TEXT + T.SEP_COMMA
                + USER_ADDRESS + T.TYPE_TEXT + T.SEP_COMMA
                + USER_EXPERIENCE + T.TYPE_INTEGER + T.SEP_COMMA
                + USER_DOB + T.TYPE_TEXT
                + T.CLOSE_BRACE + T.SEMICOLON;

        public static final String SQL_DROP = T.DROP_TABLE + TABLE_NAME;

        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
