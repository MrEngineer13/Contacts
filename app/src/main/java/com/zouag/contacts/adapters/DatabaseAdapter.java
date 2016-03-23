package com.zouag.contacts.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zouag.contacts.models.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohammed Aouf ZOUAG on 18/03/2016.
 */
public class DatabaseAdapter {

    /**
     * The singleton instance.
     */
    private static DatabaseAdapter instance;

    public static final String KEY_ROWID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_IMG_PATH = "imgPath";

    private static final String TAG = DatabaseAdapter.class.getSimpleName();
    private static final String DATABASE_NAME = "contactsDatabase";
    private static final String DATABASE_TABLE = "contacts";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLES_CREATE =
            "CREATE TABLE " + DATABASE_TABLE + " (" +
                    KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_NAME + " TEXT NOT NULL, " +
                    KEY_EMAIL + " TEXT NOT NULL, " +
                    KEY_PHONE + " TEXT NOT NULL, " +
                    KEY_ADDRESS + " TEXT NOT NULL, " +
                    KEY_IMG_PATH + " TEXT NOT NULL)";
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    /**
     * A static factory method.
     *
     * @param context
     * @return the singleton instance of the DatabaseAdapter.
     */
    public static DatabaseAdapter getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseAdapter.class) {
                if (instance == null)
                    instance = new DatabaseAdapter(context).open();
            }
        }

        return instance;
    }

    private DatabaseAdapter(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private DatabaseAdapter open() {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * @param contact to be added
     * @return
     */
    public long insertContact(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_EMAIL, contact.getEmail());
        values.put(KEY_PHONE, contact.getPhoneNumber());
        values.put(KEY_ADDRESS, contact.getAddress());
        values.put(KEY_IMG_PATH, contact.getImgPath());

        return db.insert(DATABASE_TABLE, null, values);
    }

    /**
     * @param rowId the ID of the contact to delete
     * @return true if the contact associated with the passed-in ID was successfully
     * deleted, or false otherwise.
     */
    public boolean deleteContact(long rowId) {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * @param rowId the ID of the contact to retrieve
     * @return the Contact object associated with the passed-in ID, or null otherwise.
     */
    public Contact getContact(long rowId) {
        try (Cursor cursor = db.query(
                true, // distinct
                DATABASE_TABLE, // table name
                new String[]{KEY_ROWID, KEY_NAME, KEY_EMAIL, KEY_PHONE, KEY_ADDRESS, KEY_IMG_PATH},
                KEY_ROWID + "=" + rowId, null, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new Contact.Builder()
                        .id(cursor.getInt(0))
                        .name(cursor.getString(1))
                        .email(cursor.getString(2))
                        .phoneNumber(cursor.getString(3))
                        .address(cursor.getString(4))
                        .imgPath(cursor.getString(5))
                        .createContact();
            }
        }

        return null;
    }

    /**
     * @return a list of all contacts.
     */
    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();

        try (Cursor cursor = db.query(DATABASE_TABLE,
                new String[]{KEY_ROWID, KEY_NAME, KEY_EMAIL, KEY_PHONE, KEY_ADDRESS, KEY_IMG_PATH},
                null, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    contacts.add(new Contact.Builder()
                            .id(cursor.getInt(0))
                            .name(cursor.getString(1))
                            .email(cursor.getString(2))
                            .phoneNumber(cursor.getString(3))
                            .address(cursor.getString(4))
                            .imgPath(cursor.getString(5))
                            .createContact());
                } while (cursor.moveToNext());
            }
        }

        return contacts;
    }

    /**
     * @param rowId      of the contact to update
     * @param newContact
     * @return true if the update was successful, and false otherwise.
     */
    public boolean updateContact(long rowId, Contact newContact) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, newContact.getName());
        values.put(KEY_EMAIL, newContact.getEmail());
        values.put(KEY_PHONE, newContact.getPhoneNumber());
        values.put(KEY_ADDRESS, newContact.getAddress());
        values.put(KEY_IMG_PATH, newContact.getImgPath());

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
