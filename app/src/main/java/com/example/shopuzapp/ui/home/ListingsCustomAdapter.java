package com.example.shopuzapp.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopuzapp.R;
import com.example.shopuzapp.models.Listing;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ListingsCustomAdapter extends FirestoreRecyclerAdapter<Listing, ListingsCustomAdapter.ListingsViewHolder> {
    public ListingsCustomAdapter(@NonNull FirestoreRecyclerOptions<Listing> options){
        super(options);
    }
    @Override
    protected void onBindViewHolder(@NonNull ListingsViewHolder holder, int position, @NonNull Listing model){
        holder.listingItemTitle.setText(model.getTitle());
        String imageBlob = model.getImageBlob();
        if (imageBlob != null && !imageBlob.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(imageBlob, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.listingItemPreviewImage.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                Log.e("ListingAdapter", "Error decoding image: " + e.getMessage());
                holder.listingItemPreviewImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            holder.listingItemPreviewImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = position;
                if(pos != RecyclerView.NO_POSITION){
                    String listingId = getSnapshots().getSnapshot(pos).getId();
                    Bundle bundle = new Bundle();
                    bundle.putString("listingId",listingId);

                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.nav_listing_detail, bundle);
                }
            }
        });
        holder.listingItemAddToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemToCart(getSnapshots().getSnapshot(position).getId(), view);
            }
        });
    }

    @NonNull
    @Override
    public ListingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new ListingsViewHolder(view);
    }
    private void addItemToCart(String listingId, View v){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null){
            String userId = auth.getCurrentUser().getUid();
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(listingId)
                    .set(new HashMap<>())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(), "Dodano do koszyka", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(v.getContext(), "Nie udalo sie dodac itemu do koszyka", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    static class ListingsViewHolder extends RecyclerView.ViewHolder {
        ImageView listingItemPreviewImage;
        TextView listingItemTitle;
        ImageButton listingItemAddToCartBtn;
        public ListingsViewHolder(@NonNull View itemView) {
            super(itemView);
            listingItemTitle = itemView.findViewById(R.id.cartItemTitle);
            listingItemPreviewImage = itemView.findViewById(R.id.cartItemPreviewImage);
            listingItemAddToCartBtn = itemView.findViewById(R.id.cartItemAddToCartBtn);

        }
    }
}
