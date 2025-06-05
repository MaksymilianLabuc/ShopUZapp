package com.example.shopuzapp.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.shopuzapp.models.Listing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DatabaseHelperTest {

    private Context context;
    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        // Uzyskujemy kontekst aplikacji z Robolectric
        context = RuntimeEnvironment.getApplication();
        dbHelper = new DatabaseHelper(context);
    }

    @Test
    public void onCreate() {
        // Pobieramy czytelne połączenie z bazą i sprawdzamy, czy tabela Listings istnieje.
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{DatabaseContract.Listings.TABLE_NAME});
        assertTrue("Tabela Listings powinna być utworzona", cursor.moveToFirst());
        assertEquals(DatabaseContract.Listings.TABLE_NAME, cursor.getString(0));
        cursor.close();
    }

    @Test
    public void onUpgrade() {
        // Na potrzeby testu dodajemy przykładowy rekord, a następnie wywołujemy onUpgrade,
        // aby zasymulować zmianę wersji bazy (drop oraz recreate tabeli).
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Wstawiamy przykładowy rekord
        db.execSQL("INSERT INTO " + DatabaseContract.Listings.TABLE_NAME + " (" +
                DatabaseContract.Listings.COLUMN_NAME_TITLE + ", " +
                DatabaseContract.Listings.COLUMN_NAME_DESCRIPTION + ", " +
                DatabaseContract.Listings.COLUMN_NAME_IMAGE_URI +
                ") VALUES ('Title', 'Description', 'ImageURI')");

        // Wywołanie onUpgrade (tu symulujemy upgrade z wersji 1 do 2)
        dbHelper.onUpgrade(db, 1, 2);

        // Po upgrade tabela powinna być utworzona na nowo i pusta.
        Cursor cursor = db.query(DatabaseContract.Listings.TABLE_NAME, null, null, null, null, null, null);
        assertFalse("Tabela Listings powinna być pusta po upgrade", cursor.moveToFirst());
        cursor.close();
    }

    @Test
    public void addListing() {
        // Tworzymy obiekt Listing z przykładowymi danymi.
        Listing listing = new Listing();
        listing.setTitle("Test Title");
        listing.setDescription("Test Description");
        listing.setImageBlob("TestImageURI");

        // Dodajemy listing do bazy.
        dbHelper.addListing(listing);

        // Weryfikujemy, czy listing został poprawnie zapisany.
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.Listings.TABLE_NAME,
                null,
                DatabaseContract.Listings.COLUMN_NAME_TITLE + "=?",
                new String[]{"Test Title"},
                null, null, null);

        assertTrue("W bazie danych powinna być przynajmniej jedna oferta z tytułem 'Test Title'", cursor.moveToFirst());

        // Sprawdzamy pozostałe dane
        int descIndex = cursor.getColumnIndex(DatabaseContract.Listings.COLUMN_NAME_DESCRIPTION);
        int imageIndex = cursor.getColumnIndex(DatabaseContract.Listings.COLUMN_NAME_IMAGE_URI);
        String description = cursor.getString(descIndex);
        String imageUri = cursor.getString(imageIndex);
        assertEquals("Test Description", description);
        assertEquals("TestImageURI", imageUri);
        cursor.close();
    }

    @Test
    public void getFristListing() {
        // Aby mieć pewność, że baza nie zawiera starych danych, resetujemy tabelę.
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onUpgrade(db, 1, 2);

        // Tworzymy i dodajemy ofertę.
        Listing listing = new Listing();
        listing.setTitle("Test Title");
        listing.setDescription("Test Description");
        listing.setImageBlob("TestImageURI");
        dbHelper.addListing(listing);

        // Pobieramy ostatnio dodany listing przy użyciu metody getFristListing().
        Listing retrievedListing = dbHelper.getFristListing();
        assertNotNull("Pobrany listing nie powinien być null", retrievedListing);

        // Metoda getFristListing() zwraca jedynie wartość IMAGE_URI – weryfikujemy tę wartość.
        assertEquals("TestImageURI", retrievedListing.getImageBlob());
    }
}
