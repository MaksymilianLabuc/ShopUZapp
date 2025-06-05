package com.example.shopuzapp.ui.Settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.widget.Switch;

import com.example.shopuzapp.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {28}) // lub Build.VERSION_CODES.P (Android 9.0)
public class SettingsFragmentTest {

    private Context context;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "app_settings";
    private static final String THEME_KEY = "dark_mode";

    @Before
    public void setUp() {
        // Używamy ApplicationProvider do pobrania kontekstu aplikacji
        context = ApplicationProvider.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Czyścimy SharedPreferences przed każdym testem
        sharedPreferences.edit().clear().commit();
        // Ustawiamy domyślny tryb aplikacji na jasny
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    @After
    public void tearDown() {
        // Na końcu testu czyścimy SharedPreferences
        sharedPreferences.edit().clear().commit();
    }

    /**
     * Test sprawdza, czy przy uruchomieniu fragmentu, gdy nie zapisano ustawień,
     * przełącznik motywu jest ustawiony na false (tryb jasny), a globalny tryb aplikacji to MODE_NIGHT_NO.
     */
    @Test
    public void testDefaultThemeIsLight() {
        FragmentScenario<SettingsFragment> scenario = FragmentScenario.launchInContainer(SettingsFragment.class);
        scenario.onFragment(fragment -> {
            // Pobieramy referencję do przełącznika z widoku (zakładamy, że w layout XML przełącznik ma id: themeSwitch)
            Switch themeSwitch = fragment.getView().findViewById(R.id.themeSwitch);
            assertFalse("Domyślny stan przełącznika powinien być wyłączony", themeSwitch.isChecked());
            // Globalna wartość trybu aplikacji
            assertEquals("Globalny tryb aplikacji powinien być MODE_NIGHT_NO",
                    AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode());
        });
    }

    /**
     * Test symuluje sytuację, gdy użytkownik przełącza tryb na ciemny.
     * Powinno to spowodować zapis w SharedPreferences oraz wywołanie trybu ciemnego.
     */
    @Test
    public void testToggleThemeSwitchToDark() {
        FragmentScenario<SettingsFragment> scenario = FragmentScenario.launchInContainer(SettingsFragment.class);
        scenario.onFragment(fragment -> {
            Switch themeSwitch = fragment.getView().findViewById(R.id.themeSwitch);
            // Ustawiamy przełącznik na true – symulujemy przełączenie na tryb ciemny
            themeSwitch.setChecked(true);
            // Po zmianie, wartość w SharedPreferences powinna być true
            boolean darkMode = sharedPreferences.getBoolean(THEME_KEY, false);
            assertTrue("Wartość ustawiona w SharedPreferences powinna być true", darkMode);
            // Globalny tryb aplikacji powinien zostać ustawiony na tryb ciemny
            assertEquals("Globalny tryb aplikacji powinien być MODE_NIGHT_YES",
                    AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode());
        });
    }

    /**
     * Test sprawdza scenariusz, gdy na początku użytkownik miał ustawiony tryb ciemny,
     * a następnie przełącza go na tryb jasny.
     */
    @Test
    public void testToggleThemeSwitchToLight() {
        // Najpierw zapisujemy, że tryb ciemny jest aktywny
        sharedPreferences.edit().putBoolean(THEME_KEY, true).commit();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        FragmentScenario<SettingsFragment> scenario = FragmentScenario.launchInContainer(SettingsFragment.class);
        scenario.onFragment(fragment -> {
            Switch themeSwitch = fragment.getView().findViewById(R.id.themeSwitch);
            // Na starcie, ponieważ w preferencjach zapisano true, przełącznik powinien być włączony
            assertTrue("Przełącznik powinien być włączony", themeSwitch.isChecked());
            // Symulujemy zmianę na tryb jasny – ustawiamy przełącznik na false
            themeSwitch.setChecked(false);
            boolean darkMode = sharedPreferences.getBoolean(THEME_KEY, true);
            assertFalse("W SharedPreferences tryb ciemny powinien być ustawiony jako false", darkMode);
            // Globalny tryb aplikacji powinien zostać ustawiony na jasny
            assertEquals("Globalny tryb aplikacji powinien być MODE_NIGHT_NO",
                    AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode());
        });
    }
}
