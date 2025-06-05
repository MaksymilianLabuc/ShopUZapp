package com.example.shopuzapp.ui.addLIsting;

import com.example.shopuzapp.Geocoding.GeocodingHelper;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.HashMap;
import java.util.Map;

/**
 * Shadow klasy GeocodingHelper – zastępuje statyczną metodę geocode.
 */
@Implements(GeocodingHelper.class)
public class ShadowGeocodingHelper {

    @Implementation
    public static void geocode(String query, GeocodingHelper.GeocodingCallback callback) {
        // W symulacji zwracamy przykładowe współrzędne:
        Map<String, String> fakeResult = new HashMap<>();
        fakeResult.put("lat", "50.0");
        fakeResult.put("lng", "20.0");
        callback.onResult(fakeResult);
    }
}
