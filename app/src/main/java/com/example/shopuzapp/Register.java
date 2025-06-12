package com.example.shopuzapp;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

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


public class Register extends AppCompatActivity {

    EditText mFullName, mEmail, mPassword, mPasswordRepeat;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPasswordRepeat = findViewById(R.id.passwordRepeat);
        mRegisterBtn=findViewById(R.id.buttonRegister);
        mLoginBtn=findViewById(R.id.alreadyRegistered);

        fAuth= FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        progressBar=findViewById(R.id.progressBar);

        if (fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mEmail.getText().toString().trim();
                String password=mPassword.getText().toString().trim();
                String passwordRepeat=mPasswordRepeat.getText().toString().trim();
                String fullName=mFullName.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is required!");
                    return;
                }
                if (TextUtils.isEmpty(fullName)) {
                    mEmail.setError("Full name is required!");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required!");
                    return;
                }

                if (password.length()<8) {
                    mPassword.setError("Passwords must contain >=8 characters!");
                    return;
                }

                if (!password.equals(passwordRepeat)) {
                    mPasswordRepeat.setError("Password must be equal!");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);


                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(Register.this, "User created!", Toast.LENGTH_SHORT).show();
                            userID=fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference=fStore.collection("users").document(userID);
                            Map<String,Object> user=new HashMap<>();
                            user.put("fullName",fullName);
                            user.put("email",email);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG,"OnSuccess: user profile is created for "+userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"OnFailure: "+e.toString());
                                }
                            });

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else {
                            Toast.makeText(Register.this, "Error! "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(GONE);
                        }
                    }
                });
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });
    }
}