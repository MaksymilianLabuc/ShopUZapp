package com.example.shopuzapp.ui.addLIsting;

import static org.junit.Assert.*;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.test.core.app.ApplicationProvider;

import com.example.shopuzapp.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

@RunWith(RobolectricTestRunner.class)
// Używamy naszego shadow, aby zastąpić metodę geocode
@Config(shadows = {ShadowGeocodingHelper.class})
public class AddListingActivityTest {

    @Test
    public void testSubmitListingProcessesGeocodingAndFinishesActivity() {
        // Uruchamiamy aktywność przez Robolectric
        AddListingActivity activity = Robolectric.setupActivity(AddListingActivity.class);
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        // Pobieramy referencje do widżetów
        EditText listingLocationET = activity.findViewById(R.id.listingLocationET);
        Button addListingButton = activity.findViewById(R.id.addListingButton);
        ProgressBar listingProgressBar = activity.findViewById(R.id.listingProgressBar);

        // Ustawiamy przykładową lokalizację; ShadowGeocodingHelper zwróci natychmiast "50.0" i "20.0"
        listingLocationET.setText("Warszawa");

        // Klikamy przycisk dodania oferty, co wywoła validateListing() i następnie finish()
        addListingButton.performClick();

        // Ze względu na wywołanie finish() w onClick, aktywność powinna być w trakcie zamykania
        assertTrue("Activity powinna być zamykana", shadowActivity.isFinishing());

        // Zakładamy, że w callbacku geokodowania pasek postępu zostanie ustawiony na GONE.
        // Upewniamy się, że progress bar nie jest widoczny:
        assertEquals("ProgressBar powinien być schowany", ProgressBar.GONE, listingProgressBar.getVisibility());
    }
}
