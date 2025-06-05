package com.example.shopuzapp.models;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ListingTest {

    private Listing listing;

    @Before
    public void setUp() {
        // Inicjalizacja obiektu przy użyciu domyślnego konstruktora
        listing = new Listing();
    }

    @Test
    public void testDefaultConstructor() {
        // Test sprawdzający, czy domyślny konstruktor utworzył obiekt
        // i czy pola mają wartości domyślne.
        assertNotNull("Obiekt Listing nie powinien być null", listing);
        assertNull("Title powinien być null", listing.getTitle());
        assertNull("Description powinien być null", listing.getDescription());
        assertNull("ImageBlob powinien być null", listing.getImageBlob());
        assertEquals("Price powinno być zerowe", 0.0, listing.getPrice(), 0.001);
        assertNull("OwnerId powinien być null", listing.getOwnerId());
        assertNull("Location powinien być null", listing.getLocation());
    }

    @Test
    public void testCustomConstructor() {
        // Test sprawdzający, czy konstruktor niestandardowy poprawnie ustawia
        // wartości dla title, description oraz imageBlob.
        Listing customListing = new Listing("Sample Title", "Sample Description", "SampleImageURI");
        assertEquals("Sample Title", customListing.getTitle());
        assertEquals("Sample Description", customListing.getDescription());
        assertEquals("SampleImageURI", customListing.getImageBlob());
        // Pozostałe pola nie są ustawiane przez konstruktor niestandardowy
        assertEquals(0.0, customListing.getPrice(), 0.001);
        assertNull(customListing.getOwnerId());
        assertNull(customListing.getLocation());
    }

    @Test
    public void testSettersAndGetters() {
        // Ustawiamy wartości przy użyciu setterów i sprawdzamy, czy gettery zwracają
        // te same, poprawnie ustawione dane.
        listing.setTitle("Test Title");
        listing.setDescription("Test Description");
        listing.setImageBlob("TestImageBlob");
        listing.setPrice(123.45);
        listing.setOwnerId("Owner123");

        Map<String, String> location = new HashMap<>();
        location.put("lat", "12.34");
        location.put("lng", "56.78");
        listing.setLocation(location);

        assertEquals("Test Title", listing.getTitle());
        assertEquals("Test Description", listing.getDescription());
        assertEquals("TestImageBlob", listing.getImageBlob());
        assertEquals(123.45, listing.getPrice(), 0.001);
        assertEquals("Owner123", listing.getOwnerId());
        assertNotNull(listing.getLocation());
        assertEquals("12.34", listing.getLocation().get("lat"));
        assertEquals("56.78", listing.getLocation().get("lng"));
    }
}
