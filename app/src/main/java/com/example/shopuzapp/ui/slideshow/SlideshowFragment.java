package com.example.shopuzapp.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.shopuzapp.databinding.FragmentSlideshowBinding;

/**
 * Klasa {@code SlideshowFragment} reprezentuje fragment wyświetlający slajdy.
 * Fragment współpracuje z {@link SlideshowViewModel} w celu zarządzania danymi,
 * a także korzysta z View Binding do łatwego dostępu do widoków z layoutu.
 */
public class SlideshowFragment extends Fragment {

    /**
     * Obiekt bindingu umożliwiający bezpośredni dostęp do elementów
     * layoutu {@code fragment_slideshow.xml}.
     */
    private FragmentSlideshowBinding binding;

    /**
     * Metoda {@code onCreateView} tworzy i inicjalizuje widok fragmentu.
     * Inflatuje layout przy użyciu View Binding, inicjalizuje {@link SlideshowViewModel}
     * oraz ustawia obserwatora na dane wyświetlane w {@link TextView}.
     *
     * @param inflater Obiekt {@link LayoutInflater} służący do inflacji layoutu.
     * @param container Kontener, do którego zostanie dodany widok fragmentu.
     * @param savedInstanceState Zapisany stan fragmentu (jeśli istnieje).
     * @return Główny widok fragmentu.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSlideshow;
        // Ustawienie obserwatora na dane w ViewModel, aby automatycznie aktualizować TextView
        slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    /**
     * Metoda {@code onDestroyView} jest wywoływana przy niszczeniu widoku fragmentu.
     * Ustawienie bindingu na {@code null} pomaga zapobiegać wyciekom pamięci.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
