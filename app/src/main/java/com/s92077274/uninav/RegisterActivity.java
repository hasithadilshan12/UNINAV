package com.s92077274.uninav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// import com.google.firebase.firestore.FirebaseFirestore; // Removed - now handled by FirebaseUserManager

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etRegisterName, etRegisterEmail, etRegisterPassword;
    private Button btnRegister;
    private ProgressBar progressBarRegister;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private FirebaseUserManager firebaseUserManager; // ⭐ NEW: Instance of our custom manager ⭐

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firebaseUserManager = FirebaseUserManager.getInstance(); // ⭐ Initialize our custom manager ⭐

        initViews();
        setClickListeners();
    }

    // Initialize UI components
    private void initViews() {
        etRegisterName = findViewById(R.id.etRegisterName);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBarRegister = findViewById(R.id.progressBarRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    // Set up click listeners for buttons
    private void setClickListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        if (tvLogin != null) {
            tvLogin.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    // Handles user registration
    private void registerUser() {
        String name = etRegisterName.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(name)) {
            etRegisterName.setError("Name is required");
            etRegisterName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etRegisterEmail.setError("Email is required");
            etRegisterEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegisterEmail.setError("Please enter a valid email");
            etRegisterEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etRegisterPassword.setError("Password is required");
            etRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etRegisterPassword.setError("Password must be at least 6 characters");
            etRegisterPassword.requestFocus();
            return;
        }

        // Show progress bar and disable button
        progressBarRegister.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Create user with email and password in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // ⭐ MODIFIED: Use FirebaseUserManager to save user details to Firestore ⭐
                                firebaseUserManager.saveNewUser(user.getUid(), email, name)
                                        .addOnSuccessListener(aVoid -> {
                                            // User data saved successfully, sign out and navigate to login
                                            mAuth.signOut();
                                            progressBarRegister.setVisibility(View.GONE);
                                            btnRegister.setEnabled(true);
                                            Toast.makeText(RegisterActivity.this,
                                                    "Registration successful! Please log in.",
                                                    Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Error saving user data, sign out and show error
                                            mAuth.signOut(); // Sign out the newly created auth user as profile save failed
                                            progressBarRegister.setVisibility(View.GONE);
                                            btnRegister.setEnabled(true);
                                            Toast.makeText(RegisterActivity.this,
                                                    "Error saving user data: " + e.getMessage() + ". Please try again.",
                                                    Toast.LENGTH_LONG).show();
                                        });
                            }
                        } else {
                            // Registration failed
                            progressBarRegister.setVisibility(View.GONE);
                            btnRegister.setEnabled(true);
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // ⭐ REMOVED: saveUserToFirestore() method is no longer needed, logic moved to FirebaseUserManager ⭐


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Navigate to LoginActivity when back button is pressed
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
