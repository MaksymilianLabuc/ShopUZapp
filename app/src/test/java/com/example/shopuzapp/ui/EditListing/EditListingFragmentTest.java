package com.example.shopuzapp.ui.EditListing;

import static org.junit.Assert.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.example.shopuzapp.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.FragmentScenario;
import org.robolectric.shadows.ShadowToast;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;

@RunWith(RobolectricTestRunner.class)
public class EditListingFragmentTest {

    /**
     * Testuje, czy w przypadku braku przekazanego identyfikatora oferty (listingId)
     * zostanie wyświetlony odpowiedni komunikat Toast.
     */
    @Test
    public void testMissingListingId_showsErrorToast() {
        // Uruchamiamy fragment bez argumentu "listingId"
        Bundle args = new Bundle();
        FragmentScenario<EditListingFragment> scenario =
                FragmentScenario.launchInContainer(EditListingFragment.class, args, R.style.AppTheme, null);

        scenario.onFragment(fragment -> {
            // Na podstawie warunku w onCreateView, jeśli listingId jest null
            // powinien być wyświetlony Toast z komunikatem "Error: Listing ID not found."
            String toastText = ShadowToast.getTextOfLatestToast();
            assertEquals("Error: Listing ID not found.", toastText);
        });
    }

    /**
     * Testuje metodę decodeBlob przy przekazaniu poprawnego łańcucha Base64 wygenerowanego z bitmapy.
     * Oczekujemy, że metoda zwróci bitmapę o prawidłowych wymiarach.
     */
    @Test
    public void testDecodeBlob_valid() throws Exception {
        // Utworzenie przykładowej bitmapy – 10x10 pikseli
        Bitmap testBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        testBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String imageBlob = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // Utworzenie instancji fragmentu
        EditListingFragment fragment = new EditListingFragment();

        // Uzyskujemy dostęp do prywatnej metody decodeBlob poprzez refleksję
        Method decodeBlobMethod = EditListingFragment.class.getDeclaredMethod("decodeBlob", String.class);
        decodeBlobMethod.setAccessible(true);
        Bitmap decodedBitmap = (Bitmap) decodeBlobMethod.invoke(fragment, imageBlob);
        assertNotNull("Decoded bitmap should not be null", decodedBitmap);
        // Weryfikujemy wymiary – powinny być takie same jak naszej testowej bitmapy
        assertEquals(testBitmap.getWidth(), decodedBitmap.getWidth());
        assertEquals(testBitmap.getHeight(), decodedBitmap.getHeight());
    }

    /**
     * Testuje metodę decodeBlob przy przekazaniu niepoprawnego łańcucha Base64.
     * Oczekujemy, że metoda zwróci null.
     */
    @Test
    public void testDecodeBlob_invalid() throws Exception {
        // Niepoprawny łańcuch Base64
        String invalidBlob = "notAValidBase64String";
        EditListingFragment fragment = new EditListingFragment();
        Method decodeBlobMethod = EditListingFragment.class.getDeclaredMethod("decodeBlob", String.class);
        decodeBlobMethod.setAccessible(true);
        Bitmap decodedBitmap = (Bitmap) decodeBlobMethod.invoke(fragment, invalidBlob);
        assertNull("Decoded bitmap should be null for invalid input", decodedBitmap);
    }
}
