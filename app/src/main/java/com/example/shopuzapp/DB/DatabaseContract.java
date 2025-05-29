package com.example.shopuzapp.DB;

import android.provider.BaseColumns;

public class DatabaseContract {
    private DatabaseContract(){}
    public static class Listings implements BaseColumns{
        public static final String TABLE_NAME = "Listings";
        public static final String COLUMN_NAME_ID = "ID";
        public static final String COLUMN_NAME_TITLE = "TITLE";
        public static final String COLUMN_NAME_DESCRIPTION = "DESCRIPTION";
        public static final String COLUMN_NAME_IMAGE_URI = "IMAGE_URI";
    }
}
