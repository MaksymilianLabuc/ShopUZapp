package com.example.shopuzapp.DB;

import android.provider.BaseColumns;

/**
 * Klasa definiująca strukturę bazy danych dla aplikacji ShopUzApp.
 */
public class DatabaseContract {

    /**
     * Prywatny konstruktor zapobiegający tworzeniu instancji klasy.
     */
    private DatabaseContract(){}

    /**
     * Klasa definiująca tabelę Listings w bazie danych.
     */
    public static class Listings implements BaseColumns {
        /**
         * Nazwa tabeli przechowującej listy produktów.
         */
        public static final String TABLE_NAME = "Listings";

        /**
         * Nazwa kolumny przechowującej identyfikator wpisu.
         */
        public static final String COLUMN_NAME_ID = "ID";

        /**
         * Nazwa kolumny przechowującej tytuł produktu.
         */
        public static final String COLUMN_NAME_TITLE = "TITLE";

        /**
         * Nazwa kolumny przechowującej opis produktu.
         */
        public static final String COLUMN_NAME_DESCRIPTION = "DESCRIPTION";

        /**
         * Nazwa kolumny przechowującej URI obrazu produktu.
         */
        public static final String COLUMN_NAME_IMAGE_URI = "IMAGE_URI";
    }
}
