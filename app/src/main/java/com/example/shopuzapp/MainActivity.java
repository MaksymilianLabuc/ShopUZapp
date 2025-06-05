package com.example.shopuzapp;

import static android.view.View.GONE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shopuzapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

import org.maplibre.android.MapLibre;

/**
 * Klasa {@code MainActivity} jest główną aktywnością aplikacji, która obsługuje
 * interfejs głównej nawigacji, wyświetla pasek narzędzi, menu nawigacyjne w szufladzie
 * oraz tworzy kanał powiadomień dla zleconych zamówień.
 *
 * <p>W kontekście architektury aplikacji, ta aktywność stanowi główny punkt wejścia po
 * zalogowaniu i zarządza ogólną nawigacją przy pomocy Navigation Architecture Component.</p>
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Konfiguracja paska narzędzi i nawigacji, która definiuje top level destinations.
     */
    private AppBarConfiguration mAppBarConfiguration;

    /**
     * Obiekt binding umożliwiający bezpośredni dostęp do widoków z layoutu activity_main.xml.
     */
    private ActivityMainBinding binding;

    /**
     * Stały identyfikator kanału powiadomień.
     */
    public static final String CHANNEL_ID = "order_notification_channel";

    /**
     * Metoda {@code onCreate} inicjalizuje główną aktywność, ustawia widok,
     * konfiguruje menu nawigacyjne oraz tworzy kanał powiadomień.
     *
     * @param savedInstanceState Zapisany stan aktywności (jeśli istnieje).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicjalizacja bindingu, który inflatuje layout activity_main.xml
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicjalizacja biblioteki MapLibre - zapewnia obsługę map w aplikacji.
        MapLibre.getInstance(this);

        // Ustawienie paska narzędzi jako ActionBar dla aktywności
        setSupportActionBar(binding.appBarMain.toolbar);

        // Konfiguracja szuflady nawigacyjnej
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Definiowanie top level destinations (ekrany główne) dla nawigacji;
        // tutaj domyślnie ekran główny, galeria oraz slajdy.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        // Pobranie NavController odpowiedzialnego za obsługę nawigacji
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Ustawienie paska akcji i szuflady nawigacji z NavController
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Utworzenie kanału powiadomień dla zamówień, wymagane od Android 8.0 (Oreo)
        createNotificationChannel();
    }

    /**
     * Metoda {@code createNotificationChannel} tworzy kanał powiadomień, jeśli urządzenie
     * działa na Android Oreo (API 26) lub nowszym, umożliwiając wysyłanie powiadomień o zamówieniach.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Nazwa i opis kanału powiadomień
            CharSequence name = "Order Notifications";
            String description = "Notifications for completed orders";

            // Ustawienie wysokiego poziomu ważności powiadomień
            int importance = NotificationManager.IMPORTANCE_HIGH;

            // Utworzenie nowego kanału powiadomień
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Rejestracja kanału w systemie
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Metoda {@code onCreateOptionsMenu} inflatuje menu akcji dla aktywności.
     *
     * @param menu Menu, do którego zostaną dodane elementy akcji.
     * @return {@code true} jeśli menu zostało pomyślnie utworzone.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Metoda {@code onSupportNavigateUp} obsługuje przycisk "wstecz" z paska nawigacji.
     *
     * @return {@code true} jeśli nawigacja się powiodła, lub wynik metody nadrzędnej.
     */
    @Override
    public boolean onSupportNavigateUp() {
        // Pobranie NavController oraz delegowanie nawigacji do frameworka NavigationUI
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Metoda {@code logout} wykonuje wylogowanie użytkownika z Firebase Authentication,
     * a następnie przekierowuje użytkownika do ekranu logowania.
     *
     * @param view Widok wywołujący operację wylogowania (np. przycisk).
     */
    public void logout(View view) {
        // Wylogowanie użytkownika za pomocą FirebaseAuth
        FirebaseAuth.getInstance().signOut();

        // Przejście do aktywności logowania i zakończenie bieżącej aktywności
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }
}
