package com.example.shopuzapp.ui.Cart;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopuzapp.R;
import com.example.shopuzapp.models.Listing;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CartItemsCustomAdapter extends RecyclerView.Adapter<CartItemsCustomAdapter.CartViewHolder> {
    private List<Listing> cartItems;
    public CartItemsCustomAdapter(){
        this.cartItems = new ArrayList<>();
    }
    public void setCartItems(List<Listing> newCartItems){
        this.cartItems.clear();
        this.cartItems.addAll(newCartItems);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Listing currentListing = cartItems.get(position);
        holder.cartItemTitle.setText(currentListing.getTitle());
        holder.cartItemPrice.setText(new StringBuilder().append("Price: ").append(currentListing.getPrice()).append("PLN").toString());
        String imageBlob = currentListing.getImageBlob();
        if (imageBlob != null && !imageBlob.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(imageBlob, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.cartItemPreviewImage.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                Log.e("CartAdapter", "Error decoding image: " + e.getMessage());
                holder.cartItemPreviewImage.setImageResource(R.drawable.ic_launcher_foreground); // Fallback
            }
        } else {
            holder.cartItemPreviewImage.setImageResource(R.drawable.ic_launcher_foreground); // Fallback
        }
        holder.cartItemRemoveFromCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String listingIdToRemove = cartItems.get(position).getId();
                if (listingIdToRemove != null) {
                    removeFromUserCart(listingIdToRemove);
                } else {
                    Log.w("CartAdapter", "Listing ID is null, cannot remove from cart.");
                }

            }
        });
    }
    private void removeFromUserCart(String listingId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(listingId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Cart", "Item removed from cart: " + listingId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Cart", "Error removing item from cart: " + e.getMessage());
                    });
        } else {
            Log.w("Cart", "User not logged in, cannot remove from cart.");
        }
    }

    public List<Listing> getCartItems() {
        return cartItems;
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public ImageView cartItemPreviewImage;
        public TextView cartItemTitle;
        public TextView cartItemPrice;
        public ImageButton cartItemRemoveFromCartBtn;
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemPreviewImage = itemView.findViewById(R.id.cartItemPreviewImage);
            cartItemTitle = itemView.findViewById(R.id.cartItemTitle);
            cartItemRemoveFromCartBtn = itemView.findViewById(R.id.cartItemRemoveFromCartBtn);
            cartItemPrice = itemView.findViewById(R.id.cartItemPrice);
        }
    }
}
