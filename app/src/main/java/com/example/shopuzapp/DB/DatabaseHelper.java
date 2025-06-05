package com.example.shopuzapp.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.shopuzapp.models.Listing;

/**
 * Klasa pomocnicza do obsługi bazy danych aplikacji ShopUZ.
 * Zarządza operacjami tworzenia, aktualizacji i odczytu danych.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    /**
     * Wersja bazy danych.
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Nazwa pliku bazy danych.
     */
    public static final String DATABASE_NAME = "ShopUZ.db";

    /**
     * Konstruktor klasy DatabaseHelper.
     *
     * @param context Kontekst aplikacji.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Definicja zapytania SQL tworzącego tabelę Listings.
     */
    public static final String SQL_CREATE_LISTINGS_TABLE =
            "CREATE TABLE " + DatabaseContract.Listings.TABLE_NAME +
                    " (" + DatabaseContract.Listings.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseContract.Listings.COLUMN_NAME_TITLE + " TEXT, " +
                    DatabaseContract.Listings.COLUMN_NAME_DESCRIPTION + " TEXT, " +
                    DatabaseContract.Listings.COLUMN_NAME_IMAGE_URI + " TEXT)";

    /**
     * Metoda wywoływana podczas tworzenia bazy danych.
     *
     * @param sqLiteDatabase Instancja SQLiteDatabase.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_LISTINGS_TABLE);
    }

    /**
     * Metoda wywoływana podczas aktualizacji bazy danych.
     *
     * @param sqLiteDatabase Instancja SQLiteDatabase.
     * @param oldVersion Poprzednia wersja bazy danych.
     * @param newVersion Nowa wersja bazy danych.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Listings.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * Dodaje nową ofertę do bazy danych.
     *
     * @param listing Obiekt zawierający dane oferty.
     */
    public void addListing(Listing listing) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.Listings.COLUMN_NAME_TITLE, listing.getTitle());
        cv.put(DatabaseContract.Listings.COLUMN_NAME_DESCRIPTION, listing.getDescription());
        cv.put(DatabaseContract.Listings.COLUMN_NAME_IMAGE_URI, listing.getImageBlob());
        db.insert(DatabaseContract.Listings.TABLE_NAME, null, cv);
    }

    /**
     * Pobiera ostatnio dodaną ofertę z bazy danych.
     *
     * @return Obiekt Listing zawierający dane ostatniej oferty.
     */
    public Listing getFristListing() {
        SQLiteDatabase dh = getReadableDatabase();
        String query = "SELECT " + DatabaseContract.Listings.COLUMN_NAME_IMAGE_URI + " FROM " + DatabaseContract.Listings.TABLE_NAME;
        Cursor cursor = dh.rawQuery(query, null);
        cursor.moveToLast();
        Listing listing = new Listing();
        listing.setImageBlob(cursor.getString(0));
        return listing;
    }
}
