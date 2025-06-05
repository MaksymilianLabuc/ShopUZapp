package com.example.shopuzapp;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Klasa {@code Register} reprezentuje aktywność rejestracji użytkownika.
 * Umożliwia wprowadzenie danych takich jak pełne imię, adres e-mail oraz hasło,
 * a następnie rejestrację użytkownika przy użyciu Firebase Authentication oraz zapis profilu w Firestore.
 *
 * <p>Jeśli użytkownik jest już zalogowany, następuje przekierowanie do {@link MainActivity}.</p>
 */
public class Register extends AppCompatActivity {

    /** Pole typu EditText przechowujące pełne imię użytkownika */
    EditText mFullName;
    /** Pole typu EditText przechowujące adres e-mail użytkownika */
    EditText mEmail;
    /** Pole typu EditText przechowujące hasło użytkownika */
    EditText mPassword;
    /** Pole typu EditText przechowujące powtórzenie hasła użytkownika */
    EditText mPasswordRepeat;
    /** Przycisk rejestracji użytkownika */
    Button mRegisterBtn;
    /** Tekstowy przycisk pozwalający przejść do aktywności logowania */
    TextView mLoginBtn;
    /** Instancja FirebaseAuth do obsługi uwierzytelniania */
    FirebaseAuth fAuth;
    /** Instancja FirebaseFirestore do przechowywania dodatkowych danych użytkownika */
    FirebaseFirestore fStore;
    /** Identyfikator użytkownika pozyskany po rejestracji */
    String userID;
    /** Pasek postępu informujący o trwającej operacji */
    ProgressBar progressBar;

    /**
     * Metoda {@code onCreate} inicjalizuje aktywność rejestracji.
     * Ustawia widok oraz inicjuje referencje do widoków i instancji Firebase.
     * Jeżeli użytkownik jest już zalogowany, następuje przekierowanie do {@link MainActivity}.
     *
     * @param savedInstanceState Zapisany stan aktywności (jeśli istnieje)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Opcjonalne włączenie trybu EdgeToEdge (zakomentowane, gdyż nie jest używane)
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Przypisanie widoków do zmiennych przy użyciu metody findViewById()
        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPasswordRepeat = findViewById(R.id.passwordRepeat);
        mRegisterBtn = findViewById(R.id.buttonRegister);
        mLoginBtn = findViewById(R.id.alreadyRegistered);

        // Inicjalizacja instancji FirebaseAuth i FirebaseFirestore
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        // Jeżeli użytkownik jest już zalogowany, przekierowanie do MainActivity i zakończenie tej aktywności
        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        // Ustawienie listenera kliknięcia dla przycisku rejestracji
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Pobranie wpisanych wartości z pól tekstowych
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String passwordRepeat = mPasswordRepeat.getText().toString().trim();
                String fullName = mFullName.getText().toString();

                // Walidacja pola e-mail: nie może być puste
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is required!");
                    return;
                }

                // Walidacja pola pełnego imienia: nie może być puste
                if (TextUtils.isEmpty(fullName)) {
                    // UWAGA: Błąd - powinno dotyczyć mFullName, a nie mEmail
                    mEmail.setError("Full name is required!");
                    return;
                }

                // Walidacja pola hasła: nie może być puste
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required!");
                    return;
                }

                // Walidacja długości hasła: musi zawierać co najmniej 8 znaków
                if (password.length() < 8) {
                    mPassword.setError("Passwords must contain >=8 characters!");
                    return;
                }

                // Walidacja: hasło musi być zgodne z powtórzeniem hasła
                if (!password.equals(passwordRepeat)) {
                    mPasswordRepeat.setError("Password must be equal!");
                    return;
                }

                // Ustawienie paska postępu na widoczny, aby pokazać trwającą operację
                progressBar.setVisibility(View.VISIBLE);

                // Rejestracja użytkownika przy użyciu Firebase Authentication
                fAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Jeśli rejestracja zakończyła się sukcesem
                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "User created!", Toast.LENGTH_SHORT)
                                            .show();
                                    // Pobranie identyfikatora nowo utworzonego użytkownika
                                    userID = fAuth.getCurrentUser().getUid();
                                    // Utworzenie referencji do dokumentu użytkownika w kolekcji "users"
                                    DocumentReference documentReference = fStore.collection("users").document(userID);
                                    // Utworzenie mapy zawierającej dane użytkownika
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("fullName", fullName);
                                    user.put("email", email);
                                    // Zapisanie danych użytkownika w Firestore
                                    documentReference.set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d(TAG, "OnSuccess: user profile is created for " + userID);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "OnFailure: " + e.toString());
                                                }
                                            });

                                    // Po udanej rejestracji przekierowanie do MainActivity
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                } else {
                                    // W przypadku błędu rejestracji wyświetlenie komunikatu o błędzie
                                    Toast.makeText(Register.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT)
                                            .show();
                                    // Ukrycie paska postępu, gdy operacja się nie powiodła
                                    progressBar.setVisibility(GONE);
                                }
                            }
                        });
            }
        });

        // Ustawienie listenera kliknięcia dla przycisku logowania (przejście do aktywności logowania)
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });
    }
}
