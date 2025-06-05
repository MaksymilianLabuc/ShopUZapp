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


/**
 * Klasa CartFragment obsługuje koszyk zakupowy w aplikacji ShopUzApp.
 */
public class CartFragment extends Fragment {
    /** Powiązanie z plikiem XML układu. */
    private FragmentCartBinding binding;

    /** RecyclerView dla elementów koszyka. */
    private RecyclerView cartItemsRV;

    /** Instancja bazy danych Firebase. */
    private FirebaseFirestore db;

    /** Instancja uwierzytelniania Firebase. */
    private FirebaseAuth auth;

    /** Adapter dla elementów koszyka. */
    private CartItemsCustomAdapter adapter;

    /** Listener dla zmian w koszyku. */
    private ListenerRegistration cartItemsListener;

    /** Komunikat o pustym koszyku. */
    private TextView emptyCartMessage;

    /** Całkowita kwota koszyka. */
    private TextView cartTotalAmmount;

    /** Przycisk realizacji zamówienia. */
    private Button cartCheckoutBtn;

    /** Całkowita wartość kwoty koszyka. */
    private double totalAmmountValue;

    /** Lista rejestracji nasłuchu zmian ofert. */
    private List<ListenerRegistration> listingListeners = new ArrayList<>();

    /** Identyfikator powiadomień. */
    private static final int NOTIFICATION_ID = 100;

    /** Launcher do tworzenia dokumentu PDF. */
    private ActivityResultLauncher<String> createPdfDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"),
                    uri -> {
                        if (uri != null) {
                            generatePdf(uri);
                        } else {
                            Toast.makeText(getContext(), "PDF save cancelled.", Toast.LENGTH_SHORT).show();
                        }
                    });

    /**
     * Konstruktor domyślny wymagany przez system.
     */
    public CartFragment() {
        // Required empty public constructor
    }

    /**
     * Tworzy nową instancję fragmentu koszyka.
     * @return Nowa instancja CartFragment.
     */
    public static CartFragment newInstance() {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Wywoływane podczas tworzenia fragmentu.
     * @param savedInstanceState Stan zapisany w przypadku ponownego uruchomienia.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Tworzy i zwraca widok fragmentu.
     * @param inflater Obiekt inflatera układu.
     * @param container Kontener dla fragmentu.
     * @param savedInstanceState Stan zapisany.
     * @return Widok fragmentu koszyka.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initWidgets();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        initCartRV();
        return root;
    }

    /**
     * Inicjalizacja widżetów.
     */
    public void initWidgets() {
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
            String fileName = "ShopUz_Invoice_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            createPdfDocumentLauncher.launch(fileName);
        });
    }

    /**
     * Inicjalizacja RecyclerView dla koszyka.
     */
    public void initCartRV() {
        cartItemsRV.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartItemsCustomAdapter();
        cartItemsRV.setAdapter(adapter);
    }

    /**
     * Wywoływane przy rozpoczęciu fragmentu.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            startListeningForCartItems(auth.getCurrentUser().getUid());
        } else {
            emptyCartMessage.setVisibility(View.VISIBLE);
            emptyCartMessage.setText(getString(R.string.cartIsEmptyMessage));
        }
    }

    /**
     * Wywoływane przy zatrzymaniu fragmentu.
     */
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

    /**
     * Rozpoczyna nasłuch zmian w koszyku użytkownika.
     * @param userId Identyfikator użytkownika.
     */
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

    /**
     * Pobiera szczegóły ofert znajdujących się w koszyku.
     * @param listingIds Lista identyfikatorów ofert.
     */
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
                            return;
                        }

                        if (listingSnapshot != null && listingSnapshot.exists()) {
                            Listing listing = listingSnapshot.toObject(Listing.class);
                            listing.setId(listingId);
                            totalAmmountValue += listing.getPrice();
                            fetchedListings.add(listing);
                        } else {
                            fetchedListings.removeIf(l -> l.getId().equals(listingId));
                        }
                        fetchedCount[0]++;
                    });
            listingListeners.add(listener);
        }
    }

    /**
     * Sprawdza, czy wszystkie oferty w koszyku zostały pobrane i aktualizuje interfejs użytkownika.
     * @param totalExpected Całkowita liczba oczekiwanych ofert.
     * @param currentFetched Liczba obecnie pobranych ofert.
     * @param currentListings Lista aktualnych ofert w koszyku.
     */
    private void checkAllListingsFetched(int totalExpected, int currentFetched, List<Listing> currentListings) {
        adapter.setCartItems(currentListings);
        cartTotalAmmount.setText(String.valueOf(totalAmmountValue));

        // Aktualizacja widoczności pustego koszyka
        if (currentListings.isEmpty() && currentFetched == totalExpected) {
            emptyCartMessage.setVisibility(View.VISIBLE);
            emptyCartMessage.setText("Your cart is empty.");
            cartCheckoutBtn.setEnabled(false);
        } else {
            emptyCartMessage.setVisibility(View.GONE);
            cartCheckoutBtn.setEnabled(true);
        }
    }

    /**
     * Generuje plik PDF z fakturą na podstawie koszyka zakupowego.
     * @param uri Lokalizacja zapisu pliku PDF.
     */
    private void generatePdf(Uri uri) {
        List<Listing> currentCartListings = new ArrayList<>();
        if (!adapter.getCartItems().isEmpty()) {
            currentCartListings = adapter.getCartItems();
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // Rozmiar A4
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(24);

        int y = 50; // Początkowa pozycja Y

        // Nagłówek faktury
        canvas.drawText("ShopUz - Checkout Invoice", 50, y, paint);
        y += 30;
        paint.setTextSize(12);
        canvas.drawText("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()), 50, y, paint);
        y += 40;

        // Elementy koszyka
        paint.setTextSize(16);
        canvas.drawText("Items:", 50, y, paint);
        y += 20;

        DecimalFormat df = new DecimalFormat("0.00");
        for (Listing listing : currentCartListings) {
            String itemText = String.format(Locale.getDefault(), "%s - $%.2f", listing.getTitle(), listing.getPrice());
            canvas.drawText(itemText, 70, y, paint);
            y += 20;

            // Tworzenie nowej strony, jeśli potrzeba więcej miejsca
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

            // Czyszczenie koszyka po wygenerowaniu faktury
            clearUserCart();

        } catch (IOException e) {
            Log.e("PDF", "Error generating PDF: " + e.getMessage());
            Toast.makeText(getContext(), "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Usuwa wszystkie oferty z koszyka użytkownika i aktualizuje dane w Firestore.
     */
    private void clearUserCart() {
        String userId = auth.getCurrentUser().getUid();
        double tmpTotal = totalAmmountValue;

        db.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String listingId = doc.getId();

                        // Usuwanie z koszyka użytkownika
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> Log.d("Cart", "Item " + listingId + " removed from cart."))
                                .addOnFailureListener(e -> Log.e("Cart", "Error removing item " + listingId + " from cart: " + e.getMessage()));

                        // Usuwanie z kolekcji ofert
                        db.collection("listings").document(listingId)
                                .delete()
                                .addOnSuccessListener(aVoid -> Log.d("Listings", "Listing " + listingId + " removed from listings collection."))
                                .addOnFailureListener(e -> Log.e("Listings", "Error removing listing " + listingId + " from listings collection: " + e.getMessage()));
                    }

                    Log.d("Cart", "User cart cleared and corresponding listings deleted.");
                    showOrderConfirmationNotification(tmpTotal);
                })
                .addOnFailureListener(e -> Log.e("Cart", "Error clearing user cart: " + e.getMessage()));
    }

    /**
     * Wyświetla powiadomienie o potwierdzeniu zamówienia po zakończeniu procesu zakupowego.
     * @param total Całkowita kwota zamówienia.
     */
    private void showOrderConfirmationNotification(double total) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_shopping_cart_24) // Ikona powiadomienia
                .setContentTitle("Order Confirmation")
                .setContentText(String.format(Locale.getDefault(), "Thank you for your order! Total: $%.2f", total))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

        // Sprawdzanie pozwolenia na wysyłanie powiadomień w Androidzie 13+
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