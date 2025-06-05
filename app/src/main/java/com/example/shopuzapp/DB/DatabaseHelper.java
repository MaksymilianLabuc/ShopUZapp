package com.example.shopuzapp.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.shopuzapp.models.Listing;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ShopUZ.db";
    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public static final String SQL_CREATE_LISTINGS_TABLE = "CREATE TABLE " + DatabaseContract.Listings.TABLE_NAME +
            " (" + DatabaseContract.Listings.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DatabaseContract.Listings.COLUMN_NAME_TITLE + " TEXT, " +
            DatabaseContract.Listings.COLUMN_NAME_DESCRIPTION + " TEXT, " +
        DatabaseContract.Listings.COLUMN_NAME_IMAGE_URI + " TEXT)";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_LISTINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Listings.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
    public void addListing(Listing listing){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.Listings.COLUMN_NAME_TITLE, listing.getTitle());
        cv.put(DatabaseContract.Listings.COLUMN_NAME_DESCRIPTION, listing.getDescription());
        cv.put(DatabaseContract.Listings.COLUMN_NAME_IMAGE_URI, listing.getImageBlob());
        db.insert(DatabaseContract.Listings.TABLE_NAME,null,cv);

    }
    public Listing getFristListing(){
        SQLiteDatabase dh = getReadableDatabase();
        String query = "SELECT " + DatabaseContract.Listings.COLUMN_NAME_IMAGE_URI + " FROM " + DatabaseContract.Listings.TABLE_NAME;
        Cursor cursor = dh.rawQuery(query,null);
        cursor.moveToLast();
        Listing listing = new Listing();
        listing.setImageBlob(cursor.getString(0));
        return listing;
    }
}
