package com.example.shopuzapp.ui.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import com.example.shopuzapp.R;
import com.example.shopuzapp.databinding.FragmentSettingsBinding;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

/**
 * Klasa {@code SettingsFragment} reprezentuje fragment ustawień aplikacji.
 * Umożliwia użytkownikowi zmianę motywu (tryb ciemny/jasny) oraz potencjalnie innych ustawień,
 * takich jak język. Ustawienia są zapisywane przy użyciu {@link SharedPreferences}.
 */
public class SettingsFragment extends Fragment {

    /** Binding widoku umożliwiający łatwy dostęp do elementów layoutu fragment_settings.xml */
    private FragmentSettingsBinding binding;

    /** Obiekt SharedPreferences do przechowywania ustawień aplikacji */
    private SharedPreferences sharedPreferences;

    /** Stała nazwa pliku SharedPreferences */
    private static final String PREFS_NAME = "app_settings";

    /** Klucz dla ustawienia trybu ciemnego w SharedPreferences */
    private static final String THEME_KEY = "dark_mode";

    /**
     * Domyślny, publiczny konstruktor wymagany przez system.
     */
    public SettingsFragment() {
        // Wymagany pusty konstruktor publiczny.
    }

    /**
     * Fabryczna metoda tworząca nową instancję fragmentu ustawień.
     *
     * @return nowa instancja {@code SettingsFragment}
     */
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    /**
     * Metoda wywoływana podczas tworzenia fragmentu.
     *
     * Inicjalizuje SharedPreferences, aby przechowywać i odczytywać ustawienia aplikacji.
     *
     * @param savedInstanceState zapisany stan fragmentu (jeśli istnieje)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Metoda tworzy widok fragmentu poprzez inflację layoutu przy użyciu View Binding.
     *
     * @param inflater LayoutInflater do tworzenia widoków
     * @param container Kontener, w którym zostanie umieszczony widok fragmentu
     * @param savedInstanceState zapisany stan fragmentu (jeśli istnieje)
     * @return główny widok fragmentu
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflacja layoutu fragment_settings.xml przy użyciu bindingu
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Metoda wywoływana po utworzeniu widoku fragmentu.
     * Ustawia listener dla przełącznika motywu, aby zapisywać i stosować tryb ciemny/jasny.
     *
     * @param view Korzeń widoku fragmentu
     * @param savedInstanceState zapisany stan fragmentu (jeśli istnieje)
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ustawienie listenera dla przełącznika motywu
        binding.themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Zapisanie stanu motywu w SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(THEME_KEY, isChecked);
                editor.apply();

                // Zastosowanie wybranego motywu (ciemny lub jasny)
                setAppTheme(isChecked);
            }
        });

        // Odczytanie zapisanego stanu motywu i aktualizacja przełącznika
        boolean isDarkMode = sharedPreferences.getBoolean(THEME_KEY, false);
        binding.themeSwitch.setChecked(isDarkMode);
        setAppTheme(isDarkMode);

        // Załadowanie zapisanego stanu językowego (opcjonalnie)
        // Nie ma potrzeby wywoływania metody setAppLanguage tutaj, ponieważ attachBaseContext
        // zajmuje się ustawieniem języka przy tworzeniu aktywności.
    }

    /**
     * Metoda wywoływana przy niszczeniu widoku fragmentu.
     * Ustawia binding na null, aby zapobiec wyciekom pamięci.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Ważne, aby zapobiec wyciekom pamięci.
    }

    /**
     * Ustawia motyw aplikacji na podstawie stanu przełącznika.
     *
     * @param isDarkMode Jeśli wartość jest true, aplikacja przełącza się na tryb ciemny;
     *                   w przeciwnym razie na tryb jasny.
     */
    private void setAppTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
