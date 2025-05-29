package com.example.shopuzapp.ui.home;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopuzapp.DB.DatabaseContract;
import com.example.shopuzapp.DB.DatabaseHelper;
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
    private ListingsCustomAdapter adapter;
    private RecyclerView listingsRV;
    private ImageView testPictureListing;
    private DatabaseHelper dh;
    private FirebaseFirestore db;

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
    }
    public void initRV(){
        Query query = db.collection("listings");
        FirestoreRecyclerOptions<Listing> options = new FirestoreRecyclerOptions.Builder<Listing>()
                .setQuery(query, Listing.class)
                .setLifecycleOwner(this)
                .build();
        adapter = new ListingsCustomAdapter(options);
        listingsRV.setLayoutManager(new LinearLayoutManager(getContext()));
        listingsRV.setItemAnimator(null);
        listingsRV.setAdapter(adapter);
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
    }





}