package com.example.shopuzapp.Geocoding;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class GeocodingHelperTest {

    // Fałszywa odpowiedź JSON: zawiera jedną geometrię z koordynatami [12.34,56.78]
    private static final String FAKE_JSON_RESPONSE = "{\"features\":[{\"geometry\":{\"coordinates\":[12.34,56.78]}}]}";

    // Ustawienie fabryki, aby przechwytywać wywołania URL.openConnection()
    static {
        try {
            URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
                @Override
                public URLStreamHandler createURLStreamHandler(String protocol) {
                    if ("https".equals(protocol)) {
                        return new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(URL url) throws IOException {
                                // Przy zapytaniu zawierającym "q=error" symulujemy błąd HTTP (np. 400 Bad Request)
                                if (url.toString().contains("q=error")) {
                                    return new FakeHttpURLConnection(url, "", HttpURLConnection.HTTP_BAD_REQUEST);
                                }
                                // W przeciwnym przypadku zwracamy prawidłową odpowiedź
                                return new FakeHttpURLConnection(url, FAKE_JSON_RESPONSE, HttpURLConnection.HTTP_OK);
                            }
                        };
                    }
                    return null;
                }
            });
        } catch (Error e) {
            // Fabrykę można ustawić tylko raz – w razie ponownego ustawienia ignorujemy wyjątek.
        }
    }

    // Klasa symulująca HttpURLConnection
    private static class FakeHttpURLConnection extends HttpURLConnection {

        private final String fakeResponse;
        private final int responseCode;

        protected FakeHttpURLConnection(URL url, String fakeResponse, int responseCode) {
            super(url);
            this.fakeResponse = fakeResponse;
            this.responseCode = responseCode;
        }

        @Override
        public void disconnect() {
            // Nic nie robimy
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() throws IOException {
            // Nic nie robimy
        }

        @Override
        public int getResponseCode() throws IOException {
            return responseCode;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(fakeResponse.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void geocode_success() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Map<String, String>> resultRef = new AtomicReference<>();
        final AtomicReference<String> errorRef = new AtomicReference<>();

        // Wywołanie metody geocode; przy normalnym zapytaniu oczekujemy poprawnej odpowiedzi
        GeocodingHelper.geocode("TestQuery", new GeocodingHelper.GeocodingCallback() {
            @Override
            public void onResult(Map<String, String> result) {
                resultRef.set(result);
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                errorRef.set(message);
                latch.countDown();
            }
        });

        // Czekamy maksymalnie 5 sekund na wywołanie callbacku
        boolean awaitSuccess = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Callback nie został wywołany na czas", awaitSuccess);
        assertNull("Błąd nie powinien być zgłoszony", errorRef.get());
        Map<String, String> result = resultRef.get();
        assertNotNull("Wynik nie powinien być null", result);
        // Zgodnie z metodą parseGeocodingResponse:
        // - "lat" powinno być wartością "56.78"
        // - "lng" powinno być wartością "12.34"
        assertEquals("56.78", result.get("lat"));
        assertEquals("12.34", result.get("lng"));
    }

    @Test
    public void geocode_httpError() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Map<String, String>> resultRef = new AtomicReference<>();
        final AtomicReference<String> errorRef = new AtomicReference<>();

        // Wywołujemy geocode z zapytaniem zawierającym "error", aby zasymulować błąd HTTP.
        GeocodingHelper.geocode("error", new GeocodingHelper.GeocodingCallback() {
            @Override
            public void onResult(Map<String, String> result) {
                resultRef.set(result);
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                errorRef.set(message);
                latch.countDown();
            }
        });

        boolean awaitSuccess = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Callback nie został wywołany na czas", awaitSuccess);
        assertNull("Wynik powinien być null przy błędzie", resultRef.get());
        String error = errorRef.get();
        assertNotNull("Wiadomość o błędzie nie powinna być null", error);
        assertTrue("Wiadomość o błędzie powinna zawierać 'HTTP error:'", error.contains("HTTP error:"));
    }
}
