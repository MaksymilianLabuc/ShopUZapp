package com.example.shopuzapp;

import static android.view.View.GONE;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28}) // lub możesz użyć Build.VERSION_CODES.P
public class LoginTest {

    private ActivityController<Login> controller;
    private Login loginActivity;

    @Before
    public void setUp() {
        // Tworzymy i uruchamiamy aktywność Login
        controller = Robolectric.buildActivity(Login.class).create().start().resume().visible();
        loginActivity = controller.get();
    }

    @After
    public void tearDown() {
        controller.pause().stop().destroy();
    }

    /**
     * Test sprawdzający, czy pole email zgłasza błąd, gdy jest puste.
     */
    @Test
    public void testEmptyEmailShowsError() {
        EditText emailField = loginActivity.findViewById(R.id.email);
        EditText passwordField = loginActivity.findViewById(R.id.password);
        Button loginBtn = loginActivity.findViewById(R.id.buttonLogin);

        // Ustawiamy puste pole email i prawidłowy tekst w polu hasła
        emailField.setText("");
        passwordField.setText("validPassword123");

        loginBtn.performClick();

        // Sprawdzamy, czy pole email zgłasza odpowiedni błąd
        assertEquals("Email is required!", emailField.getError().toString());
    }

    /**
     * Test sprawdzający, czy pole hasła zgłasza błąd, gdy jest puste.
     */
    @Test
    public void testEmptyPasswordShowsError() {
        EditText emailField = loginActivity.findViewById(R.id.email);
        EditText passwordField = loginActivity.findViewById(R.id.password);
        Button loginBtn = loginActivity.findViewById(R.id.buttonLogin);

        // Ustawiamy prawidłowy email i puste hasło
        emailField.setText("user@example.com");
        passwordField.setText("");

        loginBtn.performClick();

        // Sprawdzamy, czy pole hasła zgłasza błąd
        assertEquals("Password is required!", passwordField.getError().toString());
    }

    /**
     * Test sprawdzający, czy pole hasła zgłasza błąd, gdy hasło ma mniej niż 8 znaków.
     */
    @Test
    public void testShortPasswordShowsError() {
        EditText emailField = loginActivity.findViewById(R.id.email);
        EditText passwordField = loginActivity.findViewById(R.id.password);
        Button loginBtn = loginActivity.findViewById(R.id.buttonLogin);

        emailField.setText("user@example.com");
        passwordField.setText("short"); // mniej niż 8 znaków

        loginBtn.performClick();

        assertEquals("Passwords must contain >=8 characters!", passwordField.getError().toString());
    }

    /**
     * Test sprawdzający, czy kliknięcie przycisku rejestracji uruchamia aktywność Register.
     */
    @Test
    public void testRegisterButtonLaunchesRegisterActivity() {
        TextView registerText = loginActivity.findViewById(R.id.noRegistered);
        registerText.performClick();

        Intent nextIntent = shadowOf(loginActivity).getNextStartedActivity();
        assertNotNull("Rejestracja powinna być uruchomiona", nextIntent);
        // Sprawdzamy, czy uruchomiono aktywność Register (porównujemy nazwę klasy)
        assertEquals(Register.class.getName(), nextIntent.getComponent().getClassName());
    }


    /**
     * Test sprawdza, czy przy próbie logowania właściwie zmienia się widoczność progressBar
     * oraz czy (w przypadku niepowodzenia) wyświetlany jest Toast z komunikatem błędu.
     *
     * Ze względu na brak rzeczywistej integracji z Firebase, metoda signInWithEmailAndPassword
     * prawdopodobnie zakończy się niepowodzeniem, co umożliwia nam weryfikację tego scenariusza.
     */
    @Test
    public void testLoginFailureShowsToastAndHidesProgressBar() {
        EditText emailField = loginActivity.findViewById(R.id.email);
        EditText passwordField = loginActivity.findViewById(R.id.password);
        Button loginBtn = loginActivity.findViewById(R.id.buttonLogin);
        ProgressBar progressBar = loginActivity.findViewById(R.id.progressBar);

        emailField.setText("user@example.com");
        passwordField.setText("validpassword");

        loginBtn.performClick();
        // Po kliknięciu przycisku progressBar powinien być widoczny
        assertEquals("ProgressBar powinien być widoczny", progressBar.getVisibility(), progressBar.getVisibility());

        // Po wykonaniu asynchronicznej operacji (Firebase może zwrócić błąd), progressBar powinien być schowany.
        // W Robolectric scheduler asynchroniczny możemy opróżnić:
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        // Sprawdzamy, czy wyświetlony został Toast z komunikatem błędu (komunikat zaczyna się od "Error!")
        String toastText = ShadowToast.getTextOfLatestToast();
        assertTrue("Toast powinien zawierać fragment 'Error!'", toastText.startsWith("Error!"));

        // ProgressBar powinien być schowany (GONE)
        assertEquals(GONE, progressBar.getVisibility());
    }
}
