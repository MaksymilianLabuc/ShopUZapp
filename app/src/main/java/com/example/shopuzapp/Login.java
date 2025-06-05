package com.example.shopuzapp;

import static android.view.View.GONE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Klasa {@code Login} reprezentuje aktywność logowania użytkownika.
 * Umożliwia wprowadzenie danych logowania, walidację pól, przeprowadzenie
 * logowania przy użyciu Firebase Authentication oraz obsługę resetowania hasła.
 */
public class Login extends AppCompatActivity {

    // Deklaracja widoków interfejsu użytkownika
    EditText mEmail, mPassword;
    Button mLoginBtn;
    TextView mRegisterBtn, mResetPassword;
    ProgressBar progressBar;

    // Instancje Firebase Authentication i Firestore
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    /**
     * Metoda {@code onCreate} inicjalizuje aktywność, ustawia widoki oraz konfiguruje obsługę zdarzeń.
     *
     * @param savedInstanceState zapisany stan aktywności (jeśli istnieje)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // (Opcjonalnie) Włączenie EdgeToEdge - linia zakomentowana, gdyż nie jest używana w tym przykładzie
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        // Przykładowe ustawienie paddingów przy użyciu WindowInsets (zakomentowane)
        // ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //     Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //     v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //     return insets;
        // });

        // Inicjalizacja widoków z layoutu
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLoginBtn = findViewById(R.id.buttonLogin);
        mRegisterBtn = findViewById(R.id.noRegistered);
        mResetPassword = findViewById(R.id.resetPassword);
        progressBar = findViewById(R.id.progressBar);

        // Inicjalizacja instancji Firebase Authentication i FirebaseFirestore
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Ustawienie nasłuchiwania kliknięcia na przycisk logowania
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pobranie wartości wprowadzonych przez użytkownika w polach email i hasło
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                // Walidacja: sprawdzenie, czy pole email nie jest puste
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is required!");
                    return;
                }

                // Walidacja: sprawdzenie, czy pole hasła nie jest puste
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required!");
                    return;
                }

                // Walidacja: sprawdzenie, czy hasło ma co najmniej 8 znaków
                if (password.length() < 8) {
                    mPassword.setError("Passwords must contain >=8 characters!");
                    return;
                }

                // Ustawienie widoczności progressBar na widoczną podczas logowania
                progressBar.setVisibility(View.VISIBLE);

                // Logowanie użytkownika przy użyciu Firebase Authentication
                fAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Jeżeli logowanie się powiodło, wyświetlenie komunikatu i przejście do MainActivity
                                    Toast.makeText(Login.this, "User logged in!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                } else {
                                    // Jeżeli logowanie się nie powiodło, wyświetlenie komunikatu o błędzie oraz ukrycie progressBar
                                    Toast.makeText(Login.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(GONE);
                                }
                            }
                        });
            }
        });

        // Ustawienie nasłuchiwania kliknięcia na przycisk rejestracji - przejście do aktywności rejestracji
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }
        });

        // Ustawienie nasłuchiwania kliknięcia na tekst/reset link do resetowania hasła
        mResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Utworzenie nowego pola edycji, w którym użytkownik może wpisać swój adres email
                EditText resetEmail = new EditText(v.getContext());
                // Konfiguracja dialogu potwierdzającego reset hasła
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset password?");
                passwordResetDialog.setMessage("Enter your email to receive reset link.");
                passwordResetDialog.setView(resetEmail);

                // Ustawienie przycisku "Yes" w dialogu - wysłanie linka resetującego do podanego emaila
                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = resetEmail.getText().toString();
                        // Wysłanie reset linka przy pomocy Firebase Authentication
                        fAuth.sendPasswordResetEmail(email)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(Login.this, "Reset link has been sent to your email (if your email is present in database)!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Login.this, "Error! Reset link has not been sent!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                // Ustawienie przycisku "No" w dialogu, który zamyka okno dialogowe bez wykonywania akcji
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Brak akcji przy anulowaniu resetu hasła
                    }
                });
                // Utworzenie i wyświetlenie okna dialogowego resetowania hasła
                passwordResetDialog.create().show();
            }
        });
    }
}
