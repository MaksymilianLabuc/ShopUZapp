package com.example.shopuzapp.ui.ListingDetail

import android.content.Context
import org.maplibre.android.annotations.Marker
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

/**
 * Klasa [MapController] odpowiada za kontrolę mapy przy użyciu biblioteki MapLibre.
 *
 * Umożliwia:
 * - Konfigurację mapy (ustawianie stylu, pozycji kamery),
 * - Zarządzanie cyklem życia [MapView] (start, wznowienie, pauza, zatrzymanie, obsługa niskiej pamięci oraz zniszczenie).
 *
 * @param context Kontekst wykorzystywany do dostępu do zasobów i usług systemowych.
 * @param mapView Instancja [MapView] wyświetlająca mapę.
 */
class MapController(private val context: Context, private val mapView: MapView) {

    /**
     * Konfiguruje mapę przez ustawienie stylu oraz pozycji kamery.
     *
     * Styl mapy ustawiany jest na "dark" (ciemny) pobierany z podanego adresu URL.
     * Pozycja kamery jest ustawiana na współrzędne określone przez parametry [latitude] i [longitude]
     * z przybliżeniem ustawionym na 15.0.
     *
     * @param latitude Szerokość geograficzna lokalizacji docelowej.
     * @param longitude Długość geograficzna lokalizacji docelowej.
     */
    fun setupMap(latitude: Double, longitude: Double) {
        mapView.getMapAsync { map ->
            map.setStyle("https://tiles.openfreemap.org/styles/dark")
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(latitude, longitude))
                .zoom(15.0)
                .build()
        }
    }

    /**
     * Wywoływana, gdy [MapView] powinien rozpocząć działanie.
     */
    fun onStart() {
        mapView.onStart()
    }

    /**
     * Wywoływana, gdy [MapView] powinien wznowić działanie.
     */
    fun onResume() {
        mapView.onResume()
    }

    /**
     * Wywoływana, gdy [MapView] powinien wejść w stan pauzy.
     */
    fun onPause() {
        mapView.onPause()
    }

    /**
     * Wywoływana, gdy [MapView] powinien zostać zatrzymany.
     */
    fun onStop() {
        mapView.onStop()
    }

    /**
     * Metoda wywoływana w przypadku niskiej ilości pamięci.
     * Przekazuje wywołanie do [MapView], aby mogło odpowiednio zareagować.
     */
    fun onLowMemory() {
        mapView.onLowMemory()
    }

    /**
     * Wywoływana, gdy [MapView] jest niszczony, aby zwolnić zajmowane zasoby.
     */
    fun onDestroy() {
        mapView.onDestroy()
    }
}
