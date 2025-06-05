package com.example.shopuzapp.ui.ListingDetail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.shopuzapp.R;
import com.example.shopuzapp.databinding.FragmentListingDetailBinding;
import com.example.shopuzapp.models.Listing;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.UiSettings;
import org.maplibre.android.plugins.annotation.SymbolManager;
import org.maplibre.android.plugins.annotation.SymbolOptions;
import org.maplibre.android.utils.BitmapUtils;

/**
 * Fragment odpowiadający za wyświetlanie szczegółów ogłoszenia.
 * Wyświetla dane takie jak tytuł, opis, obraz oraz lokalizację ogłoszenia na mapie.
 * Umożliwia również przejście do ekranu edycji ogłoszenia, jeśli użytkownik jest jego właścicielem.
 */
public class ListingDetailFragment extends Fragment {

    /** Stały identyfikator dla niestandardowej ikony markera na mapie */
    private static final String ICON_ID = "custom-marker-icon";

    /** Binding widoku fragmentu ListingDetail */
    private FragmentListingDetailBinding binding;

    /** Obraz wyświetlający wizualizację ogłoszenia */
    private ImageView listingDetailImage;

    /** Tekstowy widok wyświetlający tytuł ogłoszenia */
    private TextView listingDetailTitle;

    /** Tekstowy widok wyświetlający opis ogłoszenia */
    private TextView listingDetailDescription;

    /** Przycisk umożliwiający przejście do edycji ogłoszenia */
    private Button editListingButton;

    /** Przechowywany obiekt ogłoszenia pobrany z bazy */
    private Listing currentListing;

    /** MapView wyświetlający lokalizację ogłoszenia */
    private MapView listingDetailMapView;

    /**
     * Pole przechowujące instancję kontrolera mapy.
     * Obiekt ten może służyć do dodatkowej konfiguracji mapy (obecnie nieaktywny).
     */
    private MapController mapController;

    /**
     * Domyślny konstruktor publiczny wymagany dla fragmentu.
     */
    public ListingDetailFragment() {
        // Wymagany pusty konstruktor publiczny.
    }

    /**
     * Fabryczna metoda tworząca nową instancję fragmentu.
     *
     * @param param1 opcjonalny parametr (nieużywany)
     * @param param2 opcjonalny parametr (nieużywany)
     * @return nowa instancja {@code ListingDetailFragment}
     */
    public static ListingDetailFragment newInstance(String param1, String param2) {
        ListingDetailFragment fragment = new ListingDetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Metoda wywoływana podczas tworzenia fragmentu.
     *
     * @param savedInstanceState zawiera zapisany stan fragmentu, jeśli istnieje
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Metoda tworząca widok fragmentu.
     * Inflatuje layout przy użyciu View Binding, inicjalizuje widoki oraz pobiera dane ogłoszenia
     * na podstawie przekazanego identyfikatora. Po pobraniu danych aktualizuje interfejs użytkownika.
     *
     * @param inflater służący do inflacji layoutu
     * @param container kontener, do którego dołączony zostanie widok
     * @param savedInstanceState zapisany stan fragmentu (jeśli istnieje)
     * @return główny widok fragmentu
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListingDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initWidgets();

        // Pobranie argumentów przekazanych do fragmentu
        Bundle bundle = getArguments();
        if (bundle != null) {
            String listingId = bundle.getString("listingId");
            if (listingId != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference listingRef = db.collection("listings").document(listingId);
                listingRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Listing listing = documentSnapshot.toObject(Listing.class);
                        if (listing != null) {
                            listing.setId(documentSnapshot.getId());
                            currentListing = listing;

                            // Aktualizacja widoków danymi ogłoszenia
                            listingDetailTitle.setText(listing.getTitle());
                            listingDetailDescription.setText(listing.getDescription());
                            listingDetailImage.setImageBitmap(decodeBlob(listing.getImageBlob()));
                            checkIfOwnerAndEnableEdit();
                        }
                    }
                }).addOnFailureListener(e -> {
                    // Można dodać obsługę błędu, np. wyświetlenie komunikatu dla użytkownika.
                    Log.e("ListingDetailFragment", "Błąd pobierania ogłoszenia: " + e.getMessage());
                });
            }
        }
        return root;
    }

    /**
     * Inicjalizuje widoki oraz konfigurację mapy i przycisku edycji.
     * Ustawia również zachowanie przycisków i konfigurację mapy przy użyciu MapLibre.
     */
    private void initWidgets() {
        listingDetailImage = binding.listingDetailImage;
        listingDetailTitle = binding.listingDetailTitle;
        listingDetailDescription = binding.listingDetailDescription;
        editListingButton = binding.editListingBtn;
        listingDetailMapView = binding.listingDetailMapView;

        // Opcjonalna inicjalizacja kontrolera mapy (zakomentowana)
        // mapController = new MapController(this.getContext(), listingDetailMapView);
        // mapController.setupMap(51.93, 15.50);

        listingDetailMapView.getMapAsync(mapLibreMap -> {
            mapLibreMap.setStyle("https://tiles.openfreemap.org/styles/bright", style -> {
                LatLng location;
                if (currentListing.getLocation() != null) {
                    // Utworzenie lokalizacji na podstawie współrzędnych zapisanych w ogłoszeniu
                    location = new LatLng(
                            Double.parseDouble(currentListing.location.get("lat")),
                            Double.parseDouble(currentListing.location.get("lng"))
                    );
                } else {
                    // Jeśli lokalizacja nie jest dostępna, ustawienie domyślnej wartości oraz ukrycie mapy
                    location = new LatLng(0, 0);
                    listingDetailMapView.setVisibility(View.GONE);
                }

                // Konfiguracja ustawień interfejsu mapy, wyłączanie gestów interakcyjnych
                UiSettings uiSettings = mapLibreMap.getUiSettings();
                uiSettings.setScrollGesturesEnabled(false);
                uiSettings.setZoomGesturesEnabled(false);
                uiSettings.setRotateGesturesEnabled(false);
                uiSettings.setTiltGesturesEnabled(false);
                uiSettings.setDoubleTapGesturesEnabled(false);
                uiSettings.setQuickZoomGesturesEnabled(false);

                try {
                    // Pobranie niestandardowego obrazka markera z zasobów
                    Drawable drawable = ContextCompat.getDrawable(this.getContext(), R.drawable.red_marker);
                    if (drawable != null) {
                        style.addImage(ICON_ID, BitmapUtils.getBitmapFromDrawable(drawable));
                    } else {
                        Log.e("MapController", "Drawable dla niestandardowej ikony jest null.");
                    }
                } catch (Exception e) {
                    Log.e("MapController", "Błąd przy dodawaniu obrazu do stylu: " + e.getMessage());
                }

                // Ustawienie pozycji kamery na mapie z określonym przybliżeniem
                CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(8).build();
                mapLibreMap.setCameraPosition(cameraPosition);

                // Konfiguracja SymbolManagera dla dodania markera na mapie
                SymbolManager symbolManager = new SymbolManager(listingDetailMapView, mapLibreMap, style);
                symbolManager.setIconAllowOverlap(true);
                symbolManager.setTextAllowOverlap(true);
                symbolManager.setIconIgnorePlacement(true);
                symbolManager.setTextIgnorePlacement(true);

                SymbolOptions symbolOptions = new SymbolOptions()
                        .withLatLng(location)
                        .withIconImage(ICON_ID)
                        .withIconSize(0.5f)
                        .withIconAnchor("bottom")
                        .withDraggable(false);
                symbolManager.create(symbolOptions);
            });
        });

        // Domyślne ustawienie przycisku edycji jako niewidocznego
        editListingButton.setVisibility(View.GONE);
        editListingButton.setOnClickListener(v -> {
            Bundle editBundle = new Bundle();
            editBundle.putString("listingId", currentListing.getId());
            // Nawigacja do ekranu edycji ogłoszenia z przekazanym identyfikatorem
            Navigation.findNavController(v).navigate(R.id.nav_edit_listing, editBundle);
        });
    }

    /**
     * Sprawdza, czy aktualnie zalogowany użytkownik jest właścicielem ogłoszenia.
     * Jeśli tak, przycisk edycji staje się widoczny.
     */
    private void checkIfOwnerAndEnableEdit() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && !currentUser.getUid().isEmpty() &&
                currentUser.getUid().equals(currentListing.getOwnerId())) {
            editListingButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Metoda dekodująca zakodowany ciąg Base64 reprezentujący obraz na obiekt Bitmap.
     *
     * @param imageBlob tekstowy ciąg Base64 zawierający dane obrazu
     * @return obiekt Bitmap utworzony na podstawie zakodowanego ciągu lub null, jeśli dekodowanie się nie powiedzie
     */
    private Bitmap decodeBlob(String imageBlob) {
        if (imageBlob != null && !imageBlob.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(imageBlob, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                return decodedBitmap;
            } catch (IllegalArgumentException e) {
                Log.e("ListingAdapter", "Błąd dekodowania obrazu: " + e.getMessage());
            }
        }
        return null;
    }
}
