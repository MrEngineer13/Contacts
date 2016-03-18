package com.zouag.contacts.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Moham on 18/03/2016.
 */
public class DatabaseAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    private static final String TAG = "DBAdapter";
    private static final String DATABASE_NAME = "MyDB";
    private static final String DATABASE_TABLE = "contacts";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLES_CREATE =
            "CREATE TABLE " + DATABASE_TABLE + " (" +
                    KEY_ROWID + " integer primary key autoincrement, " +
                    KEY_NAME + " text not null, " +
                    KEY_EMAIL + " text not null)";
    private final Context context;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DatabaseAdapter(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public DatabaseAdapter open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long insertContact(String name, String email) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_EMAIL, email);

        return db.insert(DATABASE_TABLE, null, values);
    }

    public boolean deleteContact(long rowId) {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor getContact(long rowId) {
        return db.query(
                true, // distinct
                DATABASE_TABLE, // table name
                new String[]{KEY_ROWID, KEY_NAME, KEY_EMAIL}, // columns
                KEY_ROWID + "=" + rowId, // selection
                null,
                null,
                null,
                null,
                null);
    }

    public Cursor getAllContacts() {
        return db.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_NAME, KEY_EMAIL},
                null, null, null, null, null);
    }

    public boolean updateContact(long rowId, String name, String email) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_EMAIL, email);

        return db.update(DATABASE_TABLE, values, KEY_ROWID + "=" + rowId, null) > 0;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLES_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion +
                    " to " + newVersion + ", which will destroy all old data.");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }
}
