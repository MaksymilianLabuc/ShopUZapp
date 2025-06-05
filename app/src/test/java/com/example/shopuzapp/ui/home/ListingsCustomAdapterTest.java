package com.example.shopuzapp.ui.home;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;

import com.example.shopuzapp.R;
import com.example.shopuzapp.models.Listing;
import com.example.shopuzapp.ui.home.ListingsCustomAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowImageView;

import java.io.ByteArrayOutputStream;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P})
public class ListingsCustomAdapterTest {

    private Context context;
    private ListingsCustomAdapter adapter;

    @Before
    public void setUp() {
        // Używamy ApplicationProvider by pobrać kontekst
        context = ApplicationProvider.getApplicationContext();

        // Tworzymy fałszywy obiekt Query przy użyciu Mockito
        Query fakeQuery = mock(Query.class);

        // Budujemy opcje dla adaptera
        FirestoreRecyclerOptions<Listing> options =
                new FirestoreRecyclerOptions.Builder<Listing>()
                        .setQuery(fakeQuery, Listing.class)
                        .build();
        adapter = new ListingsCustomAdapter(options);
    }

    @Test
    public void testOnCreateViewHolder() {
        // Tworzymy kontener, z którego zostanie odpalony inflater
        FrameLayout parent = new FrameLayout(context);
        ListingsCustomAdapter.ListingsViewHolder viewHolder = adapter.onCreateViewHolder(parent, 0);
        assertNotNull("ViewHolder nie powinien być null", viewHolder);
    }

    @Test
    public void testOnBindViewHolder_validBase64Image() {
        // Przygotowujemy bitmapę testową (np. 10x10 pikseli)
        Bitmap testBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        testBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        // Tworzymy obiekt Listing z prawidłowymi danymi
        Listing listing = new Listing();
        listing.setTitle("Test Listing");
        listing.setPrice(123.45);
        listing.setImageBlob(base64Image);

        // Tworzymy rodzica i ViewHolder używając layoutu elementu listy
        FrameLayout parent = new FrameLayout(context);
        ListingsCustomAdapter.ListingsViewHolder viewHolder = adapter.onCreateViewHolder(parent, 0);

        // Wywołujemy metodę onBindViewHolder – w tym czasie adapter ustawi tekst, cenę oraz obraz
        adapter.onBindViewHolder(viewHolder, 0, listing);

        // Weryfikujemy tekst tytułu
        assertEquals("Test Listing", viewHolder.listingItemTitle.getText().toString());
        // Weryfikujemy, czy cena została sformatowana poprawnie (np. "123.45PLN")
        assertEquals("123.45PLN", viewHolder.listingItemPrice.getText().toString());

        // Sprawdzamy, czy w ImageView ustawiono bitmapę (w przypadku poprawnego odkodowania, setImageBitmap jest wywołane,
        // a resource id pozostaje 0)
        ShadowImageView shadowImageView = Shadows.shadowOf(viewHolder.listingItemPreviewImage);
        int resId = shadowImageView.getImageResourceId();
        assertEquals("Dla prawidłowo zakodowanego obrazu nie powinno być ustawionego resource id", 0, resId);
    }

    @Test
    public void testOnBindViewHolder_invalidBase64Image() {
        // Tworzymy obiekt Listing z nieprawidłowym łańcuchem Base64
        Listing listing = new Listing();
        listing.setTitle("Invalid Image Listing");
        listing.setPrice(99.99);
        listing.setImageBlob("not a valid base64 string");

        // Tworzymy rodzica i ViewHolder
        FrameLayout parent = new FrameLayout(context);
        ListingsCustomAdapter.ListingsViewHolder viewHolder = adapter.onCreateViewHolder(parent, 0);

        // Wywołujemy metodę onBindViewHolder z niepoprawnymi danymi obrazu
        adapter.onBindViewHolder(viewHolder, 0, listing);

        // Sprawdzamy, czy ustawiono domyślny obrazek (R.drawable.ic_launcher_foreground)
        ShadowImageView shadowImageView = Shadows.shadowOf(viewHolder.listingItemPreviewImage);
        int resId = shadowImageView.getImageResourceId();
        assertEquals("Dla nieprawidłowego Base64 powinien zostać ustawiony domyślny obrazek",
                R.drawable.ic_launcher_foreground, resId);
    }
}
