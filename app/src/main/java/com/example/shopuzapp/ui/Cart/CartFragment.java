package com.example.shopuzapp.ui.Cart;

import static com.example.shopuzapp.MainActivity.CHANNEL_ID;

import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shopuzapp.R;
import com.example.shopuzapp.databinding.FragmentCartBinding;
import com.example.shopuzapp.models.Listing;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private RecyclerView cartItemsRV;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CartItemsCustomAdapter adapter;
    private ListenerRegistration cartItemsListener;
    private TextView emptyCartMessage;
    private TextView cartTotalAmmount;
    private Button cartCheckoutBtn;
    private double totalAmmountValue;
    private List<ListenerRegistration> listingListeners = new ArrayList<>();
    private static final int NOTIFICATION_ID = 100;

    private ActivityResultLauncher<String> createPdfDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"),
                    uri -> {
                        if (uri != null) {
                            generatePdf(uri);
                        } else {
                            Toast.makeText(getContext(), "PDF save cancelled.", Toast.LENGTH_SHORT).show();
                        }
                    });



    public CartFragment() {
        // Required empty public constructor
    }


    public static CartFragment newInstance(String param1, String param2) {
        CartFragment fragment = new CartFragment();
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
        binding = FragmentCartBinding.inflate(inflater,container,false);
        View root = binding.getRoot();
        initWidgets();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        initCartRV();
        return root;
    }
    public void initWidgets(){
        cartItemsRV = binding.cartItemsRV;
        emptyCartMessage = binding.emptyCartMessage;
        cartTotalAmmount = binding.cartTotalAmmount;
        cartCheckoutBtn = binding.cartCheckoutBtn;
        cartCheckoutBtn.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) {
                Toast.makeText(getContext(), "Please log in to checkout.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (adapter.getCartItems().isEmpty()) {
                Toast.makeText(getContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Trigger PDF creation intent
            String fileName = "ShopUz_Invoice_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            createPdfDocumentLauncher.launch(fileName);
        });
    }
    public void initCartRV(){
        cartItemsRV.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartItemsCustomAdapter();
        cartItemsRV.setAdapter(adapter);
    }
    @Override
    public void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            startListeningForCartItems(auth.getCurrentUser().getUid());
        } else {
            // User not logged in, show empty cart message or prompt login
//            progressBar.setVisibility(View.GONE);
            emptyCartMessage.setVisibility(View.VISIBLE);
            emptyCartMessage.setText(getString(R.string.cartIsEmptyMessage));
//            cartAdapter.setCartListings(new ArrayList<>()); // Clear any existing items
        }


    }

    @Override
    public void onStop() {
        super.onStop();

        if (cartItemsListener != null) {
            cartItemsListener.remove();
        }
        for (ListenerRegistration listener : listingListeners) {
            listener.remove();
        }
        listingListeners.clear();
    }
    private void startListeningForCartItems(String userId) {

        emptyCartMessage.setVisibility(View.GONE);


        for (ListenerRegistration listener : listingListeners) {
            listener.remove();
        }
        listingListeners.clear();

        cartItemsListener = db.collection("users")
                .document(userId)
                .collection("cart")
                .addSnapshotListener((cartSnapshots, e) -> {
                    if (e != null) {
                        Log.w("CartFragment", "Listen failed for cart items.", e);

                        emptyCartMessage.setVisibility(View.VISIBLE);
                        emptyCartMessage.setText("Error loading cart.");
                        return;
                    }

                    if (cartSnapshots == null || cartSnapshots.isEmpty()) {
                        Log.d("CartFragment", "Cart is empty.");

                        emptyCartMessage.setVisibility(View.VISIBLE);
                        emptyCartMessage.setText(getString(R.string.cartIsEmptyMessage));
                        adapter.setCartItems(new ArrayList<>());
                        totalAmmountValue = 0.0;
                        cartTotalAmmount.setText(String.valueOf(totalAmmountValue));
                        cartCheckoutBtn.setEnabled(false);
                        return;
                    }


                    List<String> listingIdsInCart = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : cartSnapshots) {
                        listingIdsInCart.add(doc.getId());
                    }

                    fetchListingDetails(listingIdsInCart);
                });
    }

    private void fetchListingDetails(List<String> listingIds) {
        totalAmmountValue = 0.0;
        List<Listing> fetchedListings = new ArrayList<>();
        final int[] fetchedCount = {0};

        if (listingIds.isEmpty()) {
            adapter.setCartItems(new ArrayList<>());

            emptyCartMessage.setVisibility(View.VISIBLE);
            emptyCartMessage.setText(getString(R.string.cartIsEmptyMessage));
            return;
        }

        for (String listingId : listingIds) {
            ListenerRegistration listener = db.collection("listings")
                    .document(listingId)
                    .addSnapshotListener((listingSnapshot, e) -> {
                        if (e != null) {
                            Log.w("CartFragment", "Listen failed for listing " + listingId, e);

                            fetchedCount[0]++;
                            checkAllListingsFetched(listingIds.size(), fetchedCount[0], fetchedListings);
                            return;
                        }

                        if (listingSnapshot != null && listingSnapshot.exists()) {
                            Listing listing = listingSnapshot.toObject(Listing.class);
                            listing.setId(listingId);
                            totalAmmountValue += listing.getPrice();
                            if (listing != null) {

                                boolean found = false;
                                for (int i = 0; i < fetchedListings.size(); i++) {
                                    if (fetchedListings.get(i).getId().equals(listing.getId())) {
                                        fetchedListings.set(i, listing);
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    fetchedListings.add(listing);
                                }
                            }
                        } else {
                            Log.d("CartFragment", "Listing " + listingId + " does not exist or was removed.");


                            fetchedListings.removeIf(l -> l.getId().equals(listingId));
                        }
                        fetchedCount[0]++;
                        checkAllListingsFetched(listingIds.size(), fetchedCount[0], fetchedListings);
                    });
            listingListeners.add(listener);

        }

    }

    private void checkAllListingsFetched(int totalExpected, int currentFetched, List<Listing> currentListings) {
        adapter.setCartItems(currentListings);
        cartTotalAmmount.setText(String.valueOf(totalAmmountValue));

        if (currentListings.isEmpty() && currentFetched == totalExpected) {
            emptyCartMessage.setVisibility(View.VISIBLE);
            emptyCartMessage.setText("Your cart is empty.");
            cartCheckoutBtn.setEnabled(false);
        } else {
            emptyCartMessage.setVisibility(View.GONE);
            cartCheckoutBtn.setEnabled(true);
        }
    }
    private void generatePdf(Uri uri) {
        List<Listing> currentCartListings = new ArrayList<>();
        if(!adapter.getCartItems().isEmpty()){
            currentCartListings = adapter.getCartItems();
        }
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(24);

        int y = 50;


        canvas.drawText("ShopUz - Checkout Invoice", 50, y, paint);
        y += 30;
        paint.setTextSize(12);
        canvas.drawText("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()), 50, y, paint);
        y += 40;


        paint.setTextSize(16);
        canvas.drawText("Items:", 50, y, paint);
        y += 20;

        DecimalFormat df = new DecimalFormat("0.00");
        for (Listing listing : currentCartListings) {
            String itemText = String.format(Locale.getDefault(), "%s - $%.2f", listing.getTitle(), listing.getPrice());
            canvas.drawText(itemText, 70, y, paint);
            y += 20;
            if (y > pageInfo.getPageHeight() - 50) {
                document.finishPage(page);
                page = document.startPage(new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create());
                canvas = page.getCanvas();
                y = 50;
                paint.setColor(Color.BLACK);
                paint.setTextSize(16);
            }
        }

        y += 30;
        paint.setTextSize(20);
        canvas.drawText(String.format(Locale.getDefault(), "Total Amount: $%.2f", totalAmmountValue), 50, y, paint);

        document.finishPage(page);

        try {
            FileOutputStream fos = (FileOutputStream) requireContext().getContentResolver().openOutputStream(uri);
            document.writeTo(fos);
            document.close();
            fos.close();
            Toast.makeText(getContext(), "Invoice saved to PDF!", Toast.LENGTH_LONG).show();
            Log.d("PDF", "PDF generated successfully at: " + uri.getPath());


            Log.d("value",String.valueOf(totalAmmountValue));
            clearUserCart();

        } catch (IOException e) {
            Log.e("PDF", "Error generating PDF: " + e.getMessage());
            Toast.makeText(getContext(), "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void clearUserCart() {
        String userId = auth.getCurrentUser().getUid();
        Log.d("value",String.valueOf(totalAmmountValue));
        double tmpTotal = totalAmmountValue;
        db.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    Log.d("value",String.valueOf(totalAmmountValue));
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String listingId = doc.getId();

                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> Log.d("Cart", "Item " + listingId + " removed from cart."))
                                .addOnFailureListener(e -> Log.e("Cart", "Error removing item " + listingId + " from cart: " + e.getMessage()));


                        db.collection("listings").document(listingId)
                                .delete()
                                .addOnSuccessListener(aVoid -> Log.d("Listings", "Listing " + listingId + " removed from listings collection."))
                                .addOnFailureListener(e -> Log.e("Listings", "Error removing listing " + listingId + " from listings collection: " + e.getMessage()));
                    }
                    Log.d("Cart", "User cart cleared and corresponding listings deleted.");
                    showOrderConfirmationNotification(tmpTotal);
                })
                .addOnFailureListener(e -> {
                    Log.e("Cart", "Error clearing user cart: " + e.getMessage());
                });
    }
    private void showOrderConfirmationNotification(double total) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_shopping_cart_24)
                .setContentTitle("Order Confirmation")
                .setContentText(String.format(Locale.getDefault(), "Thank you for your order! Total: $%.2f", total))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            } else {
                Log.d("Notification", "POST_NOTIFICATIONS permission not granted.");

            }
        } else {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }



}