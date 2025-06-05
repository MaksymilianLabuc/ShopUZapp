package com.example.shopuzapp.ui.Cart;

import static org.junit.Assert.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.test.core.app.ApplicationProvider;

import com.example.shopuzapp.R;
import com.example.shopuzapp.models.Listing;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CartItemsCustomAdapterTest {

    private Context context;
    private CartItemsCustomAdapter adapter;

    @Before
    public void setUp() {
        // Pobieramy kontekst aplikacji – można użyć ApplicationProvider lub RuntimeEnvironment
        context = ApplicationProvider.getApplicationContext();
        adapter = new CartItemsCustomAdapter();
    }

    @Test
    public void testSetAndGetCartItems() {
        // Przygotowujemy przykładowe dane
        Listing listing1 = new Listing();
        listing1.setTitle("Item1");
        listing1.setPrice(100.0);
        listing1.setImageBlob("dummyBlob");
        listing1.setId("1");

        Listing listing2 = new Listing();
        listing2.setTitle("Item2");
        listing2.setPrice(200.0);
        listing2.setImageBlob("");
        listing2.setId("2");

        List<Listing> newCartItems = new ArrayList<>();
        newCartItems.add(listing1);
        newCartItems.add(listing2);

        // Ustawiamy nowe elementy koszyka
        adapter.setCartItems(newCartItems);

        // Pobieramy elementy i weryfikujemy
        List<Listing> retrievedItems = adapter.getCartItems();
        assertEquals("Lista powinna zawierać 2 elementy", 2, retrievedItems.size());
        assertEquals("Pierwszy element powinien mieć tytuł 'Item1'", "Item1", retrievedItems.get(0).getTitle());
        assertEquals("Drugi element powinien mieć tytuł 'Item2'", "Item2", retrievedItems.get(1).getTitle());
        // Sprawdzamy również getItemCount
        assertEquals("getItemCount() powinno zwrócić 2", 2, adapter.getItemCount());
    }

    @Test
    public void testOnBindViewHolder_validBase64Image() {
        // Przygotowujemy małą bitmapę i kodujemy ją do Base64
        Bitmap testBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        testBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        Listing listing = new Listing();
        listing.setTitle("Test Item");
        listing.setPrice(123.45);
        listing.setImageBlob(encodedImage);
        listing.setId("item1");

        List<Listing> newCartItems = new ArrayList<>();
        newCartItems.add(listing);
        adapter.setCartItems(newCartItems);

        // Tworzymy widok rodzica i inflację widoku elementu
        ViewGroup parent = new FrameLayout(context);
        CartItemsCustomAdapter.CartViewHolder viewHolder = adapter.onCreateViewHolder(parent, 0);

        // Powiązujemy dane z widokiem (onBindViewHolder)
        adapter.onBindViewHolder(viewHolder, 0);

        // Weryfikujemy, czy tytuł i cena zostały poprawnie ustawione
        assertEquals("Test Item", viewHolder.cartItemTitle.getText().toString());
        assertEquals("Price: 123.45PLN", viewHolder.cartItemPrice.getText().toString());

        // Sprawdzamy, czy obraz został poprawnie przetworzony.
        // Jeśli metoda Base64.decode i BitmapFactory.decodeByteArray działała poprawnie,
        // then ImageView powinno mieć Drawable typu BitmapDrawable.
        assertNotNull(viewHolder.cartItemPreviewImage.getDrawable());
        assertTrue("Drawable powinno być typu BitmapDrawable",
                viewHolder.cartItemPreviewImage.getDrawable() instanceof BitmapDrawable);
    }

    @Test
    public void testOnBindViewHolder_invalidBase64Image() {
        // Ustawiamy listing z niepoprawnym Base64
        Listing listing = new Listing();
        listing.setTitle("Test Invalid Image");
        listing.setPrice(50.0);
        listing.setImageBlob("invalidBase64");
        listing.setId("item2");

        List<Listing> newCartItems = new ArrayList<>();
        newCartItems.add(listing);
        adapter.setCartItems(newCartItems);

        ViewGroup parent = new FrameLayout(context);
        CartItemsCustomAdapter.CartViewHolder viewHolder = adapter.onCreateViewHolder(parent, 0);

        // Powiązujemy dane z widokiem
        adapter.onBindViewHolder(viewHolder, 0);

        // W przypadku niepoprawnego Base64 powinna wystąpić zmiana obrazu na defaultowy (R.drawable.ic_launcher_foreground).
        // Nie możemy bezpośrednio porównać resourca, ale sprawdzimy, czy ImageView ma ustawiony Drawable.
        assertNotNull("ImageView nie powinno być puste", viewHolder.cartItemPreviewImage.getDrawable());
    }
}
