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

/**
 * Klasa HomeFragment reprezentuje ekran główny aplikacji.
 * Odpowiada za wyświetlanie listy ogłoszeń oraz obsługę interakcji użytkownika,
 * takich jak dodanie nowego ogłoszenia, przejście do koszyka oraz sortowanie/filtrowanie ogłoszeń.
 * Wykorzystuje Firestore jako bazę danych i view binding do pracy z interfejsem użytkownika.
 */
public class HomeFragment extends Fragment {

    /** Obiekt bindingu umożliwiający dostęp do widoków z layoutu fragment_home.xml */
    private FragmentHomeBinding binding;
    /** Przycisk FloatingActionButton do dodawania nowego ogłoszenia */
    private FloatingActionButton addListingFAB;
    /** Przycisk FloatingActionButton do przejścia do koszyka */
    private FloatingActionButton goToCartFAB;
    /** Przycisk FloatingActionButton do sortowania i filtrowania ogłoszeń */
    private FloatingActionButton sortFiltertFAB;
    /** Niestandardowy adapter do wyświetlania ogłoszeń w RecyclerView */
    private ListingsCustomAdapter adapter;
    /** RecyclerView wyświetlający listę ogłoszeń */
    private RecyclerView listingsRV;
    /** ImageView do testowego wyświetlania zdjęcia (obecnie nieużywane, linia zakomentowana) */
    private ImageView testPictureListing;
    /** Obiekt pomocniczy do operacji na lokalnej bazie danych */
    private DatabaseHelper dh;
    /** Instancja FirebaseFirestore umożliwiająca interakcję z bazą danych Firestore */
    private FirebaseFirestore db;
    /** Obecnie wybrana opcja sortowania, domyślnie "Default (No Sort)" */
    private String currentSortOption = "Default (No Sort)";
    /** Aktualnie ustawiony filtr minimalnej ceny; null oznacza brak filtra */
    private Double currentMinPrice = null;
    /** Aktualnie ustawiony filtr maksymalnej ceny; null oznacza brak filtra */
    private Double currentMaxPrice = null;

    /**
     * Metoda onCreateView tworzy widok interfejsu użytkownika dla fragmentu.
     * Inicjalizuje view binding, pomocnicze obiekty bazy danych, widgety oraz RecyclerView.
     *
     * @param inflater LayoutInflater służący do tworzenia widoków
     * @param container Rodzicielski widok, do którego ma zostać dołączony interfejs użytkownika
     * @param savedInstanceState Jeśli nie null, zawiera zapisany stan fragmentu
     * @return Główny widok fragmentu
     */
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

    /**
     * Inicjalizuje wszystkie elementy interfejsu użytkownika oraz ustawia zdarzenia kliknięć.
     * Łączy widoki z layoutu z kodem przy użyciu view binding i definiuje akcje dla przycisków.
     */
    public void initWidgets(){
        addListingFAB = binding.AddListingFAB;
        goToCartFAB = binding.goToCartFAB;
        sortFiltertFAB = binding.sortFiltertFAB;
        //testPictureListing = binding.testPictureListing;
        listingsRV = binding.listingsRV;

        // Ustawienie działania przycisku dodawania ogłoszenia, które uruchamia AddListingActivity.
        addListingFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(getContext(), AddListingActivity.class);
                startActivity(sendIntent);
                Log.d("FAB","fab");
            }
        });
        // Ustawienie działania przycisku przejścia do koszyka, korzystającego z NavController do nawigacji.
        goToCartFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.nav_cart);
            }
        });
        // Ustawienie działania przycisku sortowania i filtrowania, wywołującego wyswietlenie dialogu.
        sortFiltertFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortFilterPopup();
            }
        });
    }

    /**
     * Inicjalizuje RecyclerView poprzez skonfigurowanie zapytania do Firestore dla kolekcji ogłoszeń.
     * Uwzględnia aktualne ustawienia filtrowania oraz sortowania, konfiguruje FirestoreRecyclerOptions i
     * przypisuje adapter do RecyclerView.
     */
    public void initRV(){
        // Tworzymy zapytanie do kolekcji "listings" w Firestore.
        Query query = db.collection("listings");

        // Zastosowanie filtra - ogłoszenia o cenie większej lub równej currentMinPrice.
        if (currentMinPrice != null) {
            query = query.whereGreaterThanOrEqualTo("price", currentMinPrice);
        }
        // Zastosowanie filtra - ogłoszenia o cenie mniejszej lub równej currentMaxPrice.
        if (currentMaxPrice != null) {
            query = query.whereLessThanOrEqualTo("price", currentMaxPrice);
        }

        // Zastosowanie sortowania na podstawie wybranej opcji.
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
                // Brak specyficznego sortowania.
                // Opcjonalnie można dodać orderBy(FieldPath.documentId()) dla stabilnego porządku.
                break;
        }

        // Budowanie opcji FirestoreRecyclerOptions z zapytaniem i modelem Listing.
        FirestoreRecyclerOptions<Listing> options = new FirestoreRecyclerOptions.Builder<Listing>()
                .setQuery(query, Listing.class)
                .setLifecycleOwner(this)
                .build();

        if (adapter == null) {
            // Inicjalizacja adaptera oraz przypisanie go do RecyclerView przy pierwszym ładowaniu.
            adapter = new ListingsCustomAdapter(options);
            listingsRV.setLayoutManager(new LinearLayoutManager(getContext()));
            listingsRV.setItemAnimator(null);
            listingsRV.setAdapter(adapter);
        } else{
            // Aktualizacja istniejącego adaptera nowymi opcjami zapytania i odświeżenie danych.
            adapter.updateOptions(options); // Aktualizacja opcji adaptera.
            adapter.notifyDataSetChanged(); // Powiadomienie adaptera o zmianach danych.
        }
    }

    /**
     * Wyświetla okno dialogowe umożliwiające sortowanie i filtrowanie ogłoszeń.
     * Dialog pozwala użytkownikowi wybrać opcję sortowania przy użyciu Spinnera
     * oraz ustawić wartości minimalnej i maksymalnej ceny. Po zatwierdzeniu dialogu,
     * RecyclerView zostanie zaktualizowany z nowymi ustawieniami.
     */
    private void showSortFilterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sort & Filter Listings");

        // Inflacja widoku dla popup sortowania i filtrowania.
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.popup_sort_filter, null);
        final Spinner spinnerSortOptions = viewInflated.findViewById(R.id.spinnerSortOptions);
        final EditText etMinPrice = viewInflated.findViewById(R.id.etMinPrice);
        final EditText etMaxPrice = viewInflated.findViewById(R.id.etMaxPrice);

        // Konfiguracja adaptera Spinnera z opcjami sortowania zdefiniowanymi w zasobach.
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOptions.setAdapter(spinnerAdapter);

        // Ustawienie Spinnera na pozycję zgodną z aktualnie wybraną opcją sortowania.
        int selectionPosition = spinnerAdapter.getPosition(currentSortOption);
        spinnerSortOptions.setSelection(selectionPosition);
        // Ustawienie wartości pola edycyjnego minimalnej ceny, jeśli została wcześniej podana.
        if (currentMinPrice != null) {
            etMinPrice.setText(String.valueOf(currentMinPrice));
        }
        // Ustawienie wartości pola edycyjnego maksymalnej ceny, jeśli została wcześniej podana.
        if (currentMaxPrice != null) {
            etMaxPrice.setText(String.valueOf(currentMaxPrice));
        }

        // Ustawienie widoku dialogu.
        builder.setView(viewInflated);

        // Konfiguracja przycisku "Apply" (Zastosuj)
        builder.setPositiveButton("Apply", (dialog, which) -> {
            // Pobranie wybranej opcji sortowania.
            currentSortOption = spinnerSortOptions.getSelectedItem().toString();

            // Przetwarzanie wartości minimalnej ceny wpisanej przez użytkownika.
            try {
                String minPriceStr = etMinPrice.getText().toString();
                currentMinPrice = minPriceStr.isEmpty() ? null : Double.parseDouble(minPriceStr);
            } catch (NumberFormatException e) {
                currentMinPrice = null;
                Toast.makeText(getContext(), "Nieprawidłowa wartość ceny minimalnej", Toast.LENGTH_SHORT).show();
            }

            // Przetwarzanie wartości maksymalnej ceny wpisanej przez użytkownika.
            try {
                String maxPriceStr = etMaxPrice.getText().toString();
                currentMaxPrice = maxPriceStr.isEmpty() ? null : Double.parseDouble(maxPriceStr);
            } catch (NumberFormatException e) {
                currentMaxPrice = null;
                Toast.makeText(getContext(), "Nieprawidłowa wartość ceny maksymalnej", Toast.LENGTH_SHORT).show();
            }

            // Walidacja zakresu cen: wartość minimalna nie może być większa niż maksymalna.
            if (currentMinPrice != null && currentMaxPrice != null && currentMinPrice > currentMaxPrice) {
                Toast.makeText(getContext(), "Cena minimalna nie może być większa niż cena maksymalna", Toast.LENGTH_LONG).show();
                return; // W przypadku błędnych danych filtr nie zostaje zastosowany.
            }

            // Re-inicjalizacja RecyclerView z nowymi ustawieniami sortowania i filtrowania.
            initRV();
        });
        // Konfiguracja przycisku "Cancel" (Anuluj) zamykającego dialog bez wprowadzania zmian.
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Wyświetlenie okna dialogowego.
        builder.show();
    }

    /**
     * Metoda onResume jest wywoływana, kiedy fragment staje się widoczny.
     * Obecnie zawiera pusty blok try-catch jako miejsce na przyszłą logikę.
     */
    @Override
    public void onResume() {
        super.onResume();
        try{
            // Miejsce na dodatkową logikę przy wznowieniu fragmentu.
        } catch(Exception e){
            // Obsługa ewentualnych wyjątków.
        }
    }

    /**
     * Metoda onDestroyView jest wywoływana przy usuwaniu widoku fragmentu.
     * Czyści referencje do bindingu i adaptera, aby zapobiec wyciekom pamięci.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }
}
