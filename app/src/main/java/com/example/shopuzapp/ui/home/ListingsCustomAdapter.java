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

/**
 * Klasa {@code ListingsCustomAdapter} jest adapterem rozszerzającym {@link FirestoreRecyclerAdapter}
 * i służy do bindowania danych obiektów typu {@link Listing} do widoków wyświetlanych w RecyclerView.
 * Adapter korzysta z opcji ({@link FirestoreRecyclerOptions}), aby pobierać dane w czasie rzeczywistym z bazy danych Firestore.
 */
public class ListingsCustomAdapter extends FirestoreRecyclerAdapter<Listing, ListingsCustomAdapter.ListingsViewHolder> {

    /**
     * Konstruktor adaptera.
     *
     * @param options Opcje konfiguracji adaptera, zawierające między innymi zapytanie do Firestore i klasę modelu.
     */
    public ListingsCustomAdapter(@NonNull FirestoreRecyclerOptions<Listing> options) {
        super(options);
    }

    /**
     * Metoda wiąże dane modelu {@code Listing} z elementem widoku {@code ListingsViewHolder}.
     * Ustawia tytuł, cenę i miniaturę obrazu oraz definiuje akcje kliknięć dla elementu listy.
     *
     * @param holder   Widokowy obiekt {@code ListingsViewHolder} zawierający referencje do widoków elementu.
     * @param position Pozycja elementu w liście.
     * @param model    Obiekt {@code Listing} zawierający dane.
     */
    @Override
    protected void onBindViewHolder(@NonNull ListingsViewHolder holder, int position, @NonNull Listing model) {
        // Ustawienie tytułu ogłoszenia.
        holder.listingItemTitle.setText(model.getTitle());

        // Utworzenie ciągu znaków reprezentującego cenę i walutę.
        StringBuilder priceString = new StringBuilder();
        priceString.append(model.getPrice());
        priceString.append("PLN");
        holder.listingItemPrice.setText(priceString.toString());

        // Pobranie zakodowanego obrazu zapisane w postaci ciągu Base64.
        String imageBlob = model.getImageBlob();
        if (imageBlob != null && !imageBlob.isEmpty()) {
            try {
                // Dekodowanie obrazu z Base64 do tablicy bajtów.
                byte[] decodedBytes = Base64.decode(imageBlob, Base64.DEFAULT);
                // Utworzenie bitmapy na podstawie zdekodowanej tablicy bajtów.
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                // Ustawienie bitmapy w widoku obrazka.
                holder.listingItemPreviewImage.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                // W przypadku błędu dekodowania, logowanie błędu i ustawienie domyślnego obrazka.
                Log.e("ListingAdapter", "Error decoding image: " + e.getMessage());
                holder.listingItemPreviewImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            // Jeśli brak obrazu, ustawienie domyślnego obrazka.
            holder.listingItemPreviewImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Ustawienie akcji kliknięcia na cały element listy, aby przejść do szczegółów ogłoszenia.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = position;
                if (pos != RecyclerView.NO_POSITION) {
                    // Pobranie identyfikatora ogłoszenia na podstawie pozycji.
                    String listingId = getSnapshots().getSnapshot(pos).getId();
                    Bundle bundle = new Bundle();
                    bundle.putString("listingId", listingId);

                    // Nawigacja do ekranu szczegółów ogłoszenia przy użyciu NavController.
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.nav_listing_detail, bundle);
                }
            }
        });

        // Ustawienie akcji kliknięcia dla przycisku dodania ogłoszenia do koszyka.
        holder.listingItemAddToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemToCart(getSnapshots().getSnapshot(position).getId(), view);
            }
        });
    }

    /**
     * Metoda tworzy nowy obiekt {@code ListingsViewHolder} dla danego widoku elementu listy.
     *
     * @param parent   Grupowy widok rodzica, w którym zostanie umieszczony element.
     * @param viewType Typ widoku (używane przy obsłudze wielu typów elementów, tutaj tylko jeden typ).
     * @return Nowa instancja {@code ListingsViewHolder} zawierająca odniesienia do widoków elementu.
     */
    @NonNull
    @Override
    public ListingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_listing, parent, false);
        return new ListingsViewHolder(view);
    }

    /**
     * Metoda dodaje ogłoszenie do koszyka użytkownika.
     * Sprawdza, czy użytkownik jest zalogowany i dodaje wybrany element do podkolekcji "cart"
     * w dokumencie użytkownika w bazie Firestore.
     *
     * @param listingId Identyfikator ogłoszenia, które ma zostać dodane do koszyka.
     * @param v         Widok, z którego pochodzi kontekst (np. przycisk).
     */
    private void addItemToCart(String listingId, View v) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
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

    /**
     * Klasa {@code ListingsViewHolder} reprezentuje widok pojedynczego elementu listy ogłoszeń.
     * Zawiera referencje do widoków, takich jak obrazy, tytuły, ceny oraz przycisk dodania do koszyka.
     */
    static class ListingsViewHolder extends RecyclerView.ViewHolder {
        /** Obrazek podglądu ogłoszenia */
        ImageView listingItemPreviewImage;
        /** Tekstowy widok tytułu ogłoszenia */
        TextView listingItemTitle;
        /** Tekstowy widok ceny ogłoszenia */
        TextView listingItemPrice;
        /** Przycisk umożliwiający dodanie ogłoszenia do koszyka */
        ImageButton listingItemAddToCartBtn;

        /**
         * Konstruktor klasy {@code ListingsViewHolder}.
         *
         * @param itemView Widok elementu listy, który zawiera wszystkie podwidoki.
         */
        public ListingsViewHolder(@NonNull View itemView) {
            super(itemView);
            listingItemTitle = itemView.findViewById(R.id.cartItemTitle);
            listingItemPreviewImage = itemView.findViewById(R.id.cartItemPreviewImage);
            listingItemAddToCartBtn = itemView.findViewById(R.id.cartItemAddToCartBtn);
            listingItemPrice = itemView.findViewById(R.id.listingItemPrice);
        }
    }
}
