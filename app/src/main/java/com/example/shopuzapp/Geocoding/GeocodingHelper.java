package com.example.shopuzapp.Geocoding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GeocodingHelper {


        public interface GeocodingCallback {
            void onResult(Map<String,String> result);
            void onError(String message);
        }

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

        private static void parseGeocodingResponse(String jsonResponse, GeocodingCallback callback) {
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONArray features = jsonObject.getJSONArray("features");
                if (features.length() > 0) {
                    JSONObject firstResult = features.getJSONObject(0);
                    JSONObject properties = firstResult.getJSONObject("properties");
                    JSONObject geometry = firstResult.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    double longitude = coordinates.getDouble(0);
                    double latitude = coordinates.getDouble(1);
                    Map<String, String> result = Map.ofEntries(
                            Map.entry("lat",String.valueOf(latitude)),
                            Map.entry("lng",String.valueOf(longitude))
                    );
                    callback.onResult(result);
                } else {
                    callback.onResult(null);
                }
            } catch (Exception e) {
                callback.onError("JSON parsing error: " + e.getMessage());
            }
        }

        // Example usage (in your Activity or Fragment):
        // GeocodingTask.geocode("Zielona Góra", new GeocodingTask.GeocodingCallback() {
        //     @Override
        //     public void onResult(String result) {
        //         runOnUiThread(() -> Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show());
        //     }
        //
        //     @Override
        //     public void onError(String message) {
        //         runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_LONG).show());
        //     }
        // });



}
