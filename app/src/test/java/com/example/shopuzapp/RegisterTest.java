package com.example.shopuzapp;

import static android.view.View.GONE;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28}) // lub Build.VERSION_CODES.P
public class RegisterTest {

    private ActivityController<Register> controller;
    private Register registerActivity;

    @Before
    public void setUp() {
        // Uruchamiamy aktywność Register
        controller = Robolectric.buildActivity(Register.class)
                .create()
                .start()
                .resume()
                .visible();
        registerActivity = controller.get();
    }

    @After
    public void tearDown() {
        controller.pause().stop().destroy();
    }

    /**
     * Test walidacji – gdy pole email jest puste, powinien zostać ustawiony błąd.
     */
    @Test
    public void testEmptyEmailShowsError() {
        EditText emailField = registerActivity.findViewById(R.id.email);
        EditText fullNameField = registerActivity.findViewById(R.id.fullName);
        EditText passwordField = registerActivity.findViewById(R.id.password);
        EditText passwordRepeatField = registerActivity.findViewById(R.id.passwordRepeat);
        Button registerBtn = registerActivity.findViewById(R.id.buttonRegister);

        // Ustawiamy puste pole email i poprawne dane w pozostałych polach.
        emailField.setText("");
        fullNameField.setText("John Doe");
        passwordField.setText("validPassword");
        passwordRepeatField.setText("validPassword");

        registerBtn.performClick();

        assertEquals("Email is required!", emailField.getError().toString());
    }

    /**
     * Test walidacji – gdy pole pełnego imienia jest puste.
     * Zwróć uwagę, że w tej implementacji błąd zostaje ustawiony na polu email.
     */
    @Test
    public void testEmptyFullNameShowsError() {
        EditText emailField = registerActivity.findViewById(R.id.email);
        EditText fullNameField = registerActivity.findViewById(R.id.fullName);
        EditText passwordField = registerActivity.findViewById(R.id.password);
        EditText passwordRepeatField = registerActivity.findViewById(R.id.passwordRepeat);
        Button registerBtn = registerActivity.findViewById(R.id.buttonRegister);

        emailField.setText("user@example.com");
        fullNameField.setText(""); // puste imię
        passwordField.setText("validPassword");
        passwordRepeatField.setText("validPassword");

        registerBtn.performClick();

        // W tej implementacji błąd jest ustawiany na mEmail – należy to zweryfikować
        assertEquals("Full name is required!", emailField.getError().toString());
    }

    /**
     * Test walidacji – gdy pole hasła jest puste.
     */
    @Test
    public void testEmptyPasswordShowsError() {
        EditText emailField = registerActivity.findViewById(R.id.email);
        EditText fullNameField = registerActivity.findViewById(R.id.fullName);
        EditText passwordField = registerActivity.findViewById(R.id.password);
        EditText passwordRepeatField = registerActivity.findViewById(R.id.passwordRepeat);
        Button registerBtn = registerActivity.findViewById(R.id.buttonRegister);

        emailField.setText("user@example.com");
        fullNameField.setText("John Doe");
        passwordField.setText("");
        passwordRepeatField.setText("anything");

        registerBtn.performClick();

        assertEquals("Password is required!", passwordField.getError().toString());
    }

    /**
     * Test walidacji – gdy hasło jest zbyt krótkie (mniej niż 8 znaków).
     */
    @Test
    public void testShortPasswordShowsError() {
        EditText emailField = registerActivity.findViewById(R.id.email);
        EditText fullNameField = registerActivity.findViewById(R.id.fullName);
        EditText passwordField = registerActivity.findViewById(R.id.password);
        EditText passwordRepeatField = registerActivity.findViewById(R.id.passwordRepeat);
        Button registerBtn = registerActivity.findViewById(R.id.buttonRegister);

        emailField.setText("user@example.com");
        fullNameField.setText("John Doe");
        passwordField.setText("short"); // hasło krótsze niż 8 znaków
        passwordRepeatField.setText("short");

        registerBtn.performClick();

        assertEquals("Passwords must contain >=8 characters!", passwordField.getError().toString());
    }

    /**
     * Test walidacji – gdy pole powtórzonego hasła nie pasuje do pola hasła.
     */
    @Test
    public void testPasswordMismatchShowsError() {
        EditText emailField = registerActivity.findViewById(R.id.email);
        EditText fullNameField = registerActivity.findViewById(R.id.fullName);
        EditText passwordField = registerActivity.findViewById(R.id.password);
        EditText passwordRepeatField = registerActivity.findViewById(R.id.passwordRepeat);
        Button registerBtn = registerActivity.findViewById(R.id.buttonRegister);

        emailField.setText("user@example.com");
        fullNameField.setText("John Doe");
        passwordField.setText("validPassword");
        passwordRepeatField.setText("differentPassword");

        registerBtn.performClick();

        assertEquals("Password must be equal!", passwordRepeatField.getError().toString());
    }

    /**
     * Test sprawdzający, czy kliknięcie przycisku logowania (TextView "alreadyRegistered")
     * uruchamia aktywność Login.
     */
    @Test
    public void testLoginButtonLaunchesLoginActivity() {
        TextView loginBtn = registerActivity.findViewById(R.id.alreadyRegistered);
        loginBtn.performClick();

        ShadowActivity shadowActivity = shadowOf(registerActivity);
        Intent nextIntent = shadowActivity.getNextStartedActivity();
        assertNotNull("Powinien być uruchomiony intent do logowania", nextIntent);
        assertEquals(Login.class.getName(), nextIntent.getComponent().getClassName());
    }

    /**
     * Test symuluje próbę rejestracji z poprawnymi danymi, która jednak,
     * ze względu na brak rzeczywistej integracji FirebaseAuth, powinna zakończyć się niepowodzeniem.
     * Następuje weryfikacja, że:
     * - Pasek postępu jest początkowo widoczny po kliknięciu rejestracji,
     * - Po niepowodzeniu wyświetlany jest Toast z komunikatem błędu,
     * - Pasek postępu zostaje ukryty (GONE).
     */
    @Test
    public void testRegistrationFailureShowsErrorAndHidesProgressBar() {
        EditText emailField = registerActivity.findViewById(R.id.email);
        EditText fullNameField = registerActivity.findViewById(R.id.fullName);
        EditText passwordField = registerActivity.findViewById(R.id.password);
        EditText passwordRepeatField = registerActivity.findViewById(R.id.passwordRepeat);
        Button registerBtn = registerActivity.findViewById(R.id.buttonRegister);
        ProgressBar progressBar = registerActivity.findViewById(R.id.progressBar);

        emailField.setText("user@example.com");
        fullNameField.setText("John Doe");
        passwordField.setText("validpassword");
        passwordRepeatField.setText("validpassword");

        // Kliknięcie rejestracji – próbujemy zarejestrować użytkownika
        registerBtn.performClick();

        // Pasek postępu powinien być widoczny natychmiast po kliknięciu
        assertEquals(View.VISIBLE, progressBar.getVisibility());

        // W Robolectric zadania asynchroniczne wykonują się synchronicznie – po ich wykonaniu
        // spodziewamy się, że FirebaseAuth zwróci błąd, Toast zostanie wyświetlony, a progressBar ukryty.
        String toastText = ShadowToast.getTextOfLatestToast();
        assertNotNull("Toast z komunikatem błędu powinien zostać wyświetlony", toastText);
        assertTrue("Toast powinien zaczynać się od 'Error!'", toastText.startsWith("Error!"));

        // Po niepowodzeniu rejestracji progressBar powinien zostać ukryty
        assertEquals(GONE, progressBar.getVisibility());
    }
}
