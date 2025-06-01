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

public class EditListingFragment extends Fragment {

    private ImageView editListingPhotoImageView;
    private EditText editListingTitleET;
    private EditText editListingPriceET;
    private EditText editListingDescriptionET;
    private Button editSaveListingButton;
    private String listingId;
    private FirebaseFirestore db;
    private NavController navController;

    public EditListingFragment() {
        // Required empty public constructor
    }

    public static EditListingFragment newInstance() {
        return new EditListingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listingId = getArguments().getString("listingId");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_listing, container, false);
        initViews(view);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);

        if (listingId != null) {
            loadListingDetails(listingId);
        } else {
            Toast.makeText(getContext(), "Error: Listing ID not found.", Toast.LENGTH_SHORT).show();
            // Optionally navigate back or disable editing
        }

        editSaveListingButton.setOnClickListener(v -> saveEditedListing());

        return view;
    }

    private void initViews(View view) {
        editListingPhotoImageView = view.findViewById(R.id.editListingPhotoImageView);
        editListingTitleET = view.findViewById(R.id.editListingTitleET);
        editListingPriceET = view.findViewById(R.id.editListingPriceET);
        editListingDescriptionET = view.findViewById(R.id.editListingDescriptionET);
        editSaveListingButton = view.findViewById(R.id.editSaveListingButton);
    }

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
                        editListingPhotoImageView.setImageResource(R.drawable.ic_launcher_foreground); // Or a default image
                    }
                }
            } else {
                Toast.makeText(getContext(), "Listing not found.", Toast.LENGTH_SHORT).show();
                // Optionally navigate back
            }
        }).addOnFailureListener(e -> {
            Log.e("EditListingFragment", "Error fetching listing: ", e);
            Toast.makeText(getContext(), "Failed to load listing details.", Toast.LENGTH_SHORT).show();
        });
    }

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

    private void saveEditedListing() {
        if (listingId == null) {
            Toast.makeText(getContext(), "Error: Listing ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = editListingTitleET.getText().toString().trim();
        String description = editListingDescriptionET.getText().toString().trim();
        String priceStr = editListingPriceET.getText().toString().trim();

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

        // For now, we are not handling image updates in this simplified edit.
        // If you need to update the image, you'll need to add image selection
        // functionality and handle the new image blob here.

        DocumentReference listingRef = db.collection("listings").document(listingId);
        listingRef.update(
                "title", title,
                "description", description,
                "price", price
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Listing updated successfully.", Toast.LENGTH_SHORT).show();
            // Optionally navigate back to the detail view
            if (navController != null) {
                Bundle bundle = new Bundle();
                bundle.putString("listingId", listingId);
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_home, true) // Pop up to the homeFragment (inclusive)
                        .build();
                navController.navigate(R.id.nav_listing_detail, bundle, navOptions);
            }
        }).addOnFailureListener(e -> {
            Log.e("EditListingFragment", "Error updating listing: ", e);
            Toast.makeText(getContext(), "Failed to update listing.", Toast.LENGTH_SHORT).show();
        });
    }
}