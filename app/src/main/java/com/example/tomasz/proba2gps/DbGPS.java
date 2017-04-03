package com.example.tomasz.proba2gps;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbGPS extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "data";

    // Contacts table name
    private static final String TABLE_GPS = "data";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "data";

    public DbGPS(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Tworzenie tabel
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_GPS_TABLE = "CREATE TABLE " + TABLE_GPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT" + ")";
        db.execSQL(CREATE_GPS_TABLE);
    }

    // Odświeżanie bazy
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Usuń starą bazę danych jeśli taka istnieje
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GPS);

        // Create tables again
        onCreate(db);
    }


    // Dodwanie nowego wiersza do bazy danych
    void addData(String data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, data);

        // Wstawianie wiersza
        db.insert(TABLE_GPS, null, values);
        db.close();
    }


    // Pobranie zawartości bazy danych SQLite
   public List<DaneDb> getAllData() {
        List<DaneDb> dataList = new ArrayList<DaneDb>();
        // Wybierz wszystkie dane
        String selectQuery = "SELECT  * FROM " + TABLE_GPS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Dodawanie danych do listy
        if (cursor.moveToFirst()) {
            do {
                DaneDb gps = new DaneDb();
                gps.setId(Integer.parseInt(cursor.getString(0)));
                gps.setDane(cursor.getString(1));
                // Dodawanie nowej pozycji do bazy
                dataList.add(gps);
            } while (cursor.moveToNext());
        }
        return dataList;
    }

    // Usuwanie pojedynczego wiersza z bazy
    public void deleteData(DaneDb daneb) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GPS, KEY_ID + " = ?", new String[] { String.valueOf(daneb.getId())});
        db.close();
    }

}