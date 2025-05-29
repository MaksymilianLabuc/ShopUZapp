package com.example.shopuzapp.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopuzapp.R;
import com.example.shopuzapp.models.Listing;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

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
    }

    @NonNull
    @Override
    public ListingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new ListingsViewHolder(view);
    }

    static class ListingsViewHolder extends RecyclerView.ViewHolder {
        ImageView listingItemPreviewImage;
        TextView listingItemTitle;
        public ListingsViewHolder(@NonNull View itemView) {
            super(itemView);
            listingItemTitle = itemView.findViewById(R.id.listingItemTitle);
            listingItemPreviewImage = itemView.findViewById(R.id.listingItemPreviewImage);
        }
    }
}
