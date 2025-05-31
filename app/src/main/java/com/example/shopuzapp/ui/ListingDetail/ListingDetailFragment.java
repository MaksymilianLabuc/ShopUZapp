package com.example.shopuzapp.ui.ListingDetail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shopuzapp.R;
import com.example.shopuzapp.databinding.ActivityAddListingBinding;
import com.example.shopuzapp.databinding.FragmentListingDetailBinding;
import com.example.shopuzapp.models.Listing;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListingDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListingDetailFragment extends Fragment {

    private FragmentListingDetailBinding binding;
    private ImageView listingDetailImage;
    private TextView listingDetailTitle;
    private TextView listingDetailDescription;
//    private Listing listing;

    public ListingDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListingDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
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
                        if(listing != null){
                            listingDetailTitle.setText(listing.getTitle());
                            listingDetailDescription.setText(listing.getDescription());
                            listingDetailImage.setImageBitmap(decodeBlob(listing.getImageBlob()));
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