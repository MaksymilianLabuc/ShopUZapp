package com.example.shopuzapp.ui.home;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopuzapp.DB.DatabaseContract;
import com.example.shopuzapp.DB.DatabaseHelper;
import com.example.shopuzapp.R;
import com.example.shopuzapp.databinding.FragmentHomeBinding;
import com.example.shopuzapp.models.Listing;
import com.example.shopuzapp.ui.addLIsting.AddListingActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.OutputStream;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private FloatingActionButton addListingFAB;
    private FloatingActionButton goToCartFAB;
    private FloatingActionButton sortFiltertFAB;
    private ListingsCustomAdapter adapter;
    private RecyclerView listingsRV;
    private ImageView testPictureListing;
    private DatabaseHelper dh;
    private FirebaseFirestore db;
    private String currentSortOption = "Default (No Sort)";
    private Double currentMinPrice = null;
    private Double currentMaxPrice = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        dh = new DatabaseHelper(binding.getRoot().getContext());
        db = FirebaseFirestore.getInstance();
        View root = binding.getRoot();
        initWidgets();
        initRV();
        return root;
    }
    public void initWidgets(){
        addListingFAB = binding.AddListingFAB;
        goToCartFAB = binding.goToCartFAB;
        sortFiltertFAB = binding.sortFiltertFAB;
        //testPictureListing = binding.testPictureListing;
        listingsRV = binding.listingsRV;
        addListingFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(getContext(), AddListingActivity.class);
                startActivity(sendIntent);
                Log.d("FAB","fab");

            }
        });
        goToCartFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.nav_cart);
            }
        });
        sortFiltertFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortFilterPopup();
            }
        });

    }
    public void initRV(){
        Query query = db.collection("listings");

        // Apply Filtering
        if (currentMinPrice != null) {
            query = query.whereGreaterThanOrEqualTo("price", currentMinPrice);
        }
        if (currentMaxPrice != null) {
            query = query.whereLessThanOrEqualTo("price", currentMaxPrice);
        }

        // Apply Sorting
        switch (currentSortOption) {
            case "Price: Low to High":
                query = query.orderBy("price", Query.Direction.ASCENDING);
                break;
            case "Price: High to Low":
                query = query.orderBy("price", Query.Direction.DESCENDING);
                break;
            case "Title: A-Z":
                query = query.orderBy("title", Query.Direction.ASCENDING);
                break;
            case "Title: Z-A":
                query = query.orderBy("title", Query.Direction.DESCENDING);
                break;
            case "Default (No Sort)":
            default:
                // No specific order, Firestore will return by document ID by default or whatever internal order it has
                // If you want a consistent default, you might add orderBy(FieldPath.documentId())
                break;
        }

        FirestoreRecyclerOptions<Listing> options = new FirestoreRecyclerOptions.Builder<Listing>()
                .setQuery(query, Listing.class)
                .setLifecycleOwner(this)
                .build();
        if (adapter == null) {
            adapter = new ListingsCustomAdapter(options);
            listingsRV.setLayoutManager(new LinearLayoutManager(getContext()));
            listingsRV.setItemAnimator(null);
            listingsRV.setAdapter(adapter);
        } else{
            adapter.updateOptions(options); // Update the adapter with new query options
            adapter.notifyDataSetChanged(); // Notify data set changed
        }
    }
    private void showSortFilterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sort & Filter Listings");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.popup_sort_filter, null);
        final Spinner spinnerSortOptions = viewInflated.findViewById(R.id.spinnerSortOptions);
        final EditText etMinPrice = viewInflated.findViewById(R.id.etMinPrice);
        final EditText etMaxPrice = viewInflated.findViewById(R.id.etMaxPrice);

        // Set up the spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOptions.setAdapter(spinnerAdapter);

        // Set initial selections/values based on current state
        int selectionPosition = spinnerAdapter.getPosition(currentSortOption);
        spinnerSortOptions.setSelection(selectionPosition);
        if (currentMinPrice != null) {
            etMinPrice.setText(String.valueOf(currentMinPrice));
        }
        if (currentMaxPrice != null) {
            etMaxPrice.setText(String.valueOf(currentMaxPrice));
        }

        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton("Apply", (dialog, which) -> {
            // Get selected sort option
            currentSortOption = spinnerSortOptions.getSelectedItem().toString();

            // Get filter prices
            try {
                String minPriceStr = etMinPrice.getText().toString();
                currentMinPrice = minPriceStr.isEmpty() ? null : Double.parseDouble(minPriceStr);
            } catch (NumberFormatException e) {
                currentMinPrice = null;
                Toast.makeText(getContext(), "Invalid Min Price", Toast.LENGTH_SHORT).show();
            }

            try {
                String maxPriceStr = etMaxPrice.getText().toString();
                currentMaxPrice = maxPriceStr.isEmpty() ? null : Double.parseDouble(maxPriceStr);
            } catch (NumberFormatException e) {
                currentMaxPrice = null;
                Toast.makeText(getContext(), "Invalid Max Price", Toast.LENGTH_SHORT).show();
            }

            // Validate price range
            if (currentMinPrice != null && currentMaxPrice != null && currentMinPrice > currentMaxPrice) {
                Toast.makeText(getContext(), "Min price cannot be greater than Max price", Toast.LENGTH_LONG).show();
                return; // Don't apply filter
            }

            // Re-initialize RecyclerView with new query
            initRV();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        try{
//            Listing listing = dh.getFristListing();
//            Uri imageUri = Uri.parse(listing.getImageURI());
//            testPictureListing.setImageURI(imageUri);
            //fetchLatestListingFromFirestore();
        } catch(Exception e){

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }

}