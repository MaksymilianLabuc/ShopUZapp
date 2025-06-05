/**
 * Pakiet zawierający fragment do edycji oferty w aplikacji ShopUzApp.
 */
package com.example.shopuzapp.ui.EditListing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.shopuzapp.R;
import com.example.shopuzapp.models.Listing;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment umożliwiający edycję istniejącej oferty w aplikacji ShopUzApp.
 */
public class EditListingFragment extends Fragment {

    /** Obrazek oferty. */
    private ImageView editListingPhotoImageView;

    /** Pole tekstowe do wpisania tytułu oferty. */
    private EditText editListingTitleET;

    /** Pole tekstowe do wpisania ceny oferty. */
    private EditText editListingPriceET;

    /** Pole tekstowe do wpisania opisu oferty. */
    private EditText editListingDescriptionET;

    /** Przycisk do zapisania zmian w ofercie. */
    private Button editSaveListingButton;

    /** Identyfikator oferty. */
    private String listingId;

    /** Instancja bazy danych Firebase Firestore. */
    private FirebaseFirestore db;

    /** Kontroler nawigacji. */
    private NavController navController;

    /**
     * Konstruktor domyślny wymagany przez system.
     */
    public EditListingFragment() {
        // Wymagany pusty konstruktor publiczny
    }

    /**
     * Tworzy nową instancję fragmentu edycji oferty.
     *
     * @return Nowa instancja EditListingFragment.
     */
    public static EditListingFragment newInstance() {
        return new EditListingFragment();
    }
    /**
     * Wywoływane podczas tworzenia fragmentu.
     * Pobiera identyfikator oferty przekazany w argumentach.
     *
     * @param savedInstanceState Stan zapisany w przypadku ponownego uruchomienia.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listingId = getArguments().getString("listingId");
        }
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Tworzy i zwraca widok fragmentu.
     *
     * @param inflater  Obiekt inflatera układu.
     * @param container Kontener dla fragmentu.
     * @param savedInstanceState Stan zapisany.
     * @return Widok fragmentu edycji oferty.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_listing, container, false);
        initViews(view);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);

        if (listingId != null) {
            loadListingDetails(listingId);
        } else {
            Toast.makeText(getContext(), "Error: Listing ID not found.", Toast.LENGTH_SHORT).show();
        }

        editSaveListingButton.setOnClickListener(v -> saveEditedListing());

        return view;
    }

    /**
     * Inicjalizuje widżety interfejsu użytkownika.
     *
     * @param view Widok fragmentu.
     */
    private void initViews(View view) {
        editListingPhotoImageView = view.findViewById(R.id.editListingPhotoImageView);
        editListingTitleET = view.findViewById(R.id.editListingTitleET);
        editListingPriceET = view.findViewById(R.id.editListingPriceET);
        editListingDescriptionET = view.findViewById(R.id.editListingDescriptionET);
        editSaveListingButton = view.findViewById(R.id.editSaveListingButton);
    }

    /**
     * Pobiera szczegóły oferty na podstawie jej identyfikatora i wypełnia formularz edycji.
     *
     * @param listingId Identyfikator oferty.
     */
    private void loadListingDetails(String listingId) {
        DocumentReference listingRef = db.collection("listings").document(listingId);
        listingRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Listing listing = documentSnapshot.toObject(Listing.class);
                if (listing != null) {
                    editListingTitleET.setText(listing.getTitle());
                    editListingPriceET.setText(String.valueOf(listing.getPrice()));
                    editListingDescriptionET.setText(listing.getDescription());
                    Bitmap image = decodeBlob(listing.getImageBlob());
                    if (image != null) {
                        editListingPhotoImageView.setImageBitmap(image);
                    } else {
                        editListingPhotoImageView.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                }
            } else {
                Toast.makeText(getContext(), "Listing not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("EditListingFragment", "Error fetching listing: ", e);
            Toast.makeText(getContext(), "Failed to load listing details.", Toast.LENGTH_SHORT).show();
        });
    }
    /**
     * Dekoduje obraz zapisany jako blob w bazie danych Firebase Firestore.
     *
     * @param imageBlob Dane obrazu zakodowane w formacie Base64.
     * @return Bitmapa obrazu lub null, jeśli wystąpił błąd.
     */
    private Bitmap decodeBlob(String imageBlob) {
        if (imageBlob != null && !imageBlob.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(imageBlob, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            } catch (IllegalArgumentException e) {
                Log.e("EditListingFragment", "Error decoding image: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Zapisuje edytowane dane oferty w bazie danych Firebase Firestore.
     */
    private void saveEditedListing() {
        if (listingId == null) {
            Toast.makeText(getContext(), "Error: Listing ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = editListingTitleET.getText().toString().trim();
        String description = editListingDescriptionET.getText().toString().trim();
        String priceStr = editListingPriceET.getText().toString().trim();

        // Walidacja pól wejściowych
        if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid price format.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aktualizacja oferty w Firestore
        DocumentReference listingRef = db.collection("listings").document(listingId);
        listingRef.update(
                "title", title,
                "description", description,
                "price", price
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Listing updated successfully.", Toast.LENGTH_SHORT).show();
            if (navController != null) {
                Bundle bundle = new Bundle();
                bundle.putString("listingId", listingId);
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_home, true)
                        .build();
                navController.navigate(R.id.nav_listing_detail, bundle, navOptions);
            }
        }).addOnFailureListener(e -> {
            Log.e("EditListingFragment", "Error updating listing: ", e);
            Toast.makeText(getContext(), "Failed to update listing.", Toast.LENGTH_SHORT).show();
        });
    }
}
