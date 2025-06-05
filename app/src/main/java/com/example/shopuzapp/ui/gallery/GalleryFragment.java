package com.example.shopuzapp.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.shopuzapp.databinding.FragmentGalleryBinding;

/**
 * Klasa {@code GalleryFragment} reprezentuje fragment odpowiedzialny za wyświetlanie galerii.
 * Korzysta z ViewModel, aby pobierać dane oraz z View Binding do zarządzania widokami.
 */
public class GalleryFragment extends Fragment {

    /** Obiekt bindingu umożliwiający bezpośredni dostęp do widoków z layoutu fragment_gallery.xml */
    private FragmentGalleryBinding binding;

    /**
     * Metoda {@code onCreateView} tworzy widok fragmentu.
     * Inicjalizuje binding, ViewModel oraz ustawia obserwatora na dane, które mają być wyświetlone w TextView.
     *
     * @param inflater Obiekt {@link LayoutInflater} używany do rozdmuchiwania (inflacji) układu.
     * @param container Kontener, w którym fragment zostanie umieszczony.
     * @param savedInstanceState Jeśli nie jest {@code null}, zawiera wcześniej zapisany stan fragmentu.
     * @return Główny widok fragmentu.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inicjalizacja ViewModel dla galerii.
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        // Inflacja layoutu fragment_gallery.xml przy użyciu View Binding.
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Pobranie referencji do TextView i ustawienie obserwatora na dane z ViewModel.
        final TextView textView = binding.textGallery;
        // Obserwacja zmian tekstu w ViewModel i automatyczne ustawienie wartości w TextView.
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    /**
     * Metoda {@code onDestroyView} jest wywoływana przy usuwaniu widoku fragmentu.
     * Czyści referencję do bindingu, aby zapobiec wyciekom pamięci.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
