package com.example.shopuzapp.Geocoding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Klasa pomocnicza do obsługi geokodowania.
 * Odpowiada za wyszukiwanie współrzędnych geograficznych na podstawie zapytania.
 */
public class GeocodingHelper {

    /**
     * Interfejs dla obsługi asynchronicznych wyników geokodowania.
     */
    public interface GeocodingCallback {
        /**
         * Wywoływane po pomyślnym przetworzeniu danych geokodowania.
         *
         * @param result Mapa zawierająca współrzędne geograficzne (lat, lng).
         */
        void onResult(Map<String, String> result);

        /**
         * Wywoływane w przypadku błędu geokodowania.
         *
         * @param message Komunikat błędu.
         */
        void onError(String message);
    }

    /**
     * Metoda asynchronicznie wykonująca zapytanie geokodowania.
     *
     * @param query Zapytanie geokodowania (np. nazwa miejsca).
     * @param callback Interfejs GeocodingCallback do obsługi wyniku.
     */
    public static void geocode(String query, final GeocodingCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String baseUrl = "https://photon.komoot.io/api/";
                    String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
                    URL url = new URL(baseUrl + "?q=" + encodedQuery);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        parseGeocodingResponse(response.toString(), callback);
                    } else {
                        callback.onError("HTTP error: " + responseCode);
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Metoda do parsowania odpowiedzi JSON z geokodowania.
     *
     * @param jsonResponse Odpowiedź JSON pobrana z API geokodowania.
     * @param callback Interfejs GeocodingCallback do przekazania wyniku lub błędu.
     */
    private static void parseGeocodingResponse(String jsonResponse, GeocodingCallback callback) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray features = jsonObject.getJSONArray("features");
            if (features.length() > 0) {
                JSONObject firstResult = features.getJSONObject(0);
                JSONObject geometry = firstResult.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                double longitude = coordinates.getDouble(0);
                double latitude = coordinates.getDouble(1);
                Map<String, String> result = Map.ofEntries(
                        Map.entry("lat", String.valueOf(latitude)),
                        Map.entry("lng", String.valueOf(longitude))
                );
                callback.onResult(result);
            } else {
                callback.onResult(null);
            }
        } catch (Exception e) {
            callback.onError("JSON parsing error: " + e.getMessage());
        }
    }
}
