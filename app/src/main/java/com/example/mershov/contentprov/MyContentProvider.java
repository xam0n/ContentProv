package com.example.mershov.contentprov;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MyContentProvider extends ContentProvider {
    final String LOG_TAG = "myLogs";

    static final String DB_NAME = "mydb";
    static final int DB_VERSION = 2;

    static final String GAMES_TABLE = "xboxgames";

    static final String GAMES_ID = "_id";
    static final String GAMES_NAME = "name";
    static final String GAMES_PRICE = "price";

    static final String DB_CREATE = "create table " + GAMES_TABLE + "("
            + GAMES_ID + " integer primary key autoincrement, "
            + GAMES_NAME + " text, " + GAMES_PRICE + " text" + ");";

    static final String AUTHORITY = "com.example.mershov.contentprov";

    static final String GAMES_PATH = "games";

    public static final Uri GAMES_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + GAMES_PATH);

    static final String GAMES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + GAMES_PATH;

    static final String GAMES_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + GAMES_PATH;

    static final int URI_GAMES = 1;

    static final int URI_GAMES_ID = 2;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, GAMES_PATH, URI_GAMES);
        uriMatcher.addURI(AUTHORITY, GAMES_PATH + "/#", URI_GAMES_ID);
    }

    DBHelper dbHelper;
    SQLiteDatabase db;

    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate");
        dbHelper = new DBHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query, " + uri.toString());

        switch (uriMatcher.match(uri)) {
            case URI_GAMES:
                Log.d(LOG_TAG, "URI_GAMES");
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = GAMES_NAME + " ASC";
                }
                break;
            case URI_GAMES_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_GAMES_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = GAMES_ID + " = " + id;
                } else {
                    selection = selection + " AND " + GAMES_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(GAMES_TABLE, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(),
                GAMES_CONTENT_URI);
        return cursor;
    }

    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert, " + uri.toString());
        if (uriMatcher.match(uri) != URI_GAMES)
            throw new IllegalArgumentException("Wrong URI: " + uri);

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(GAMES_TABLE, null, values);
        Uri resultUri = ContentUris.withAppendedId(GAMES_CONTENT_URI, rowID);
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_GAMES:
                Log.d(LOG_TAG, "URI_GAMES");
                break;
            case URI_GAMES_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_GAMES_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = GAMES_ID + " = " + id;
                } else {
                    selection = selection + " AND " + GAMES_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(GAMES_TABLE, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(LOG_TAG, "update, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_GAMES:
                Log.d(LOG_TAG, "URI_GAMES");

                break;
            case URI_GAMES_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_GAMES_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = GAMES_ID + " = " + id;
                } else {
                    selection = selection + " AND " + GAMES_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.update(GAMES_TABLE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    public String getType(Uri uri) {
        Log.d(LOG_TAG, "getType, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_GAMES:
                return GAMES_CONTENT_TYPE;
            case URI_GAMES_ID:
                return GAMES_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
            ContentValues cv = new ContentValues();
            for (int i = 1; i <= 30; i++) {
                cv.put(GAMES_NAME, "game " + i);
                cv.put(GAMES_PRICE, "price " + i);
                db.insert(GAMES_TABLE, null, cv);
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}