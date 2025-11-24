package com.example.helloworld;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ClipboardDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "clipboard.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CLIPBOARD = "clipboard";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_BOOK = "book";  // New!
    public static final String COLUMN_SENT = "sent";

    private static final String CREATE_TABLE = 
        "CREATE TABLE " + TABLE_CLIPBOARD + "(" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        COLUMN_TEXT + " TEXT NOT NULL," +
        COLUMN_BOOK + " TEXT," +
        COLUMN_SENT + " INTEGER DEFAULT 0" +
        ")";

    public ClipboardDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIPBOARD);
        onCreate(db);
    }


    public long addEntry(String text, String book) {
    SQLiteDatabase db = getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(COLUMN_TEXT, text);
    values.put(COLUMN_BOOK, book);
    values.put(COLUMN_SENT, 0);
    return db.insert(TABLE_CLIPBOARD, null, values);
}

public int getUnsentCount() {
    SQLiteDatabase db = getReadableDatabase();
    String query = "SELECT COUNT(*) FROM " + TABLE_CLIPBOARD + " WHERE " + COLUMN_SENT + " = 0";
    android.database.Cursor cursor = db.rawQuery(query, null);
    int count = 0;
    if (cursor.moveToFirst()) {
        count = cursor.getInt(0);
    }
    cursor.close();
    return count;
}

public android.database.Cursor getUnsentEntries() {
    SQLiteDatabase db = getReadableDatabase();
    return db.query(TABLE_CLIPBOARD, null, COLUMN_SENT + " = 0", null, null, null, null);
}

public void markAsSent(long id) {
    SQLiteDatabase db = getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(COLUMN_SENT, 1);
    db.update(TABLE_CLIPBOARD, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
}


}
