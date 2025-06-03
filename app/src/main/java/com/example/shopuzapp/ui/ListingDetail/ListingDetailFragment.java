package com.example.shopuzapp.ui.ListingDetail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shopuzapp.R;
import com.example.shopuzapp.databinding.ActivityAddListingBinding;
import com.example.shopuzapp.databinding.FragmentListingDetailBinding;
import com.example.shopuzapp.models.Listing;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.UiSettings;
import org.maplibre.android.plugins.annotation.SymbolManager;
import org.maplibre.android.plugins.annotation.SymbolOptions;
import org.maplibre.android.utils.BitmapUtils;
import org.maplibre.android.utils.ColorUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListingDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListingDetailFragment extends Fragment {
    private static final String ICON_ID = "custom-marker-icon";

    private FragmentListingDetailBinding binding;
    private ImageView listingDetailImage;
    private TextView listingDetailTitle;
    private TextView listingDetailDescription;
    private Button editListingButton;
    private Listing currentListing;
    private MapView listingDetailMapView;
    private MapController mapController;

    public ListingDetailFragment() {
        // Required empty public constructor
    }
    public static ListingDetailFragment newInstance(String param1, String param2) {
        ListingDetailFragment fragment = new ListingDetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListingDetailBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        initWidgets();
        Bundle bundle = getArguments();
        if(bundle != null){
            String listingId = bundle.getString("listingId");
            if(listingId != null){
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference listingRef = db.collection("listings").document(listingId);
                listingRef.get().addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        Listing listing = documentSnapshot.toObject(Listing.class);
                        listing.setId(documentSnapshot.getId());
                        currentListing = listing;
                        if(listing != null){
                            listingDetailTitle.setText(listing.getTitle());
                            listingDetailDescription.setText(listing.getDescription());
                            listingDetailImage.setImageBitmap(decodeBlob(listing.getImageBlob()));
                            checkIfOwnerAndEnableEdit();
                        }
                    }
                }).addOnFailureListener(e -> {

                });
            }
        }
        return root;
    }
    private void initWidgets(){
        listingDetailImage = binding.listingDetailImage;
        listingDetailTitle = binding.listingDetailTitle;
        listingDetailDescription = binding.listingDetailDescription;
        editListingButton = binding.editListingBtn;
        listingDetailMapView = binding.listingDetailMapView;
//        mapController = new MapController(this.getContext(), listingDetailMapView);
//        mapController.setupMap(51.93, 15.50);
        listingDetailMapView.getMapAsync(mapLibreMap -> mapLibreMap.setStyle("https://tiles.openfreemap.org/styles/bright", style -> {
            LatLng location;
            if(currentListing.getLocation() != null) location = new LatLng(Double.parseDouble(currentListing.location.get("lat")),Double.parseDouble(currentListing.location.get("lng")));
//            else location = new LatLng(0,0);
            else {
                location = new LatLng(0,0);
                listingDetailMapView.setVisibility(View.GONE);
//                return;
            }
            UiSettings uiSettings = mapLibreMap.getUiSettings();
            uiSettings.setScrollGesturesEnabled(false);
            uiSettings.setZoomGesturesEnabled(false);
            uiSettings.setRotateGesturesEnabled(false);
            uiSettings.setTiltGesturesEnabled(false);
            uiSettings.setDoubleTapGesturesEnabled(false);
            uiSettings.setQuickZoomGesturesEnabled(false);

            try {
                Drawable drawable = ContextCompat.getDrawable(this.getContext(),R.drawable.red_marker);
                if (drawable != null) {
                    style.addImage(ICON_ID, BitmapUtils.getBitmapFromDrawable(drawable));
                } else {
                    Log.e("MapController", "Drawable for custom icon is null.");
                }
            } catch (Exception e) {
                Log.e("MapController", "Error adding image to style: " + e.getMessage());
            }
            CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(8).build();
            mapLibreMap.setCameraPosition(cameraPosition);

            SymbolManager symbolManager = new SymbolManager(listingDetailMapView, mapLibreMap, style);
            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);
            symbolManager.setIconIgnorePlacement(true);
            symbolManager.setTextIgnorePlacement(true);

            SymbolOptions symbolOptions = new SymbolOptions().withLatLng(location)
                    .withIconImage(ICON_ID)
                    .withIconSize(0.5f)
                    .withIconAnchor("bottom")
//                    .withSymbolSortKey(5.0f)
                    .withDraggable(false);
            symbolManager.create(symbolOptions);
        }));
        editListingButton.setVisibility(View.GONE);
        editListingButton.setOnClickListener(v -> {
            Bundle editBundle = new Bundle();
            editBundle.putString("listingId", currentListing.getId());


            Navigation.findNavController(v).navigate(R.id.nav_edit_listing, editBundle);

        });

    }
    private void checkIfOwnerAndEnableEdit() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && !currentUser.getUid().isEmpty() && currentUser.getUid().equals(currentListing.getOwnerId())) {
            editListingButton.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap decodeBlob(String imageBlob){
        if (imageBlob != null && !imageBlob.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(imageBlob, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//                holder.listingItemPreviewImage.setImageBitmap(decodedBitmap);
                return decodedBitmap;
            } catch (IllegalArgumentException e) {
                Log.e("ListingAdapter", "Error decoding image: " + e.getMessage());
//                holder.listingItemPreviewImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
//            holder.listingItemPreviewImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
        return null;
    }
}