package com.s92077274.uninav;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etForgotEmail;
    private Button btnSendReset;
    private ProgressBar progressBarForgot;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        // Initialize UI components
        etForgotEmail = findViewById(R.id.etForgotEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        progressBarForgot = findViewById(R.id.progressBarForgot);
        mAuth = FirebaseAuth.getInstance();

        // Set click listener for the send reset button
        btnSendReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPasswordReset();
            }
        });
    }

    // Handles sending the password reset email
    private void sendPasswordReset() {
        String email = etForgotEmail.getText().toString().trim();

        // Validate email input
        if (TextUtils.isEmpty(email)) {
            etForgotEmail.setError("Email is required");
            etForgotEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etForgotEmail.setError("Please enter a valid email");
            etForgotEmail.requestFocus();
            return;
        }

        // Show progress bar and disable button during reset process
        progressBarForgot.setVisibility(View.VISIBLE);
        btnSendReset.setEnabled(false);

        // Send password reset email via Firebase Auth
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    // Hide progress bar and enable button after process
                    progressBarForgot.setVisibility(View.GONE);
                    btnSendReset.setEnabled(true);

                    if (task.isSuccessful()) {
                        showConfirmationDialog(); // Show success dialog
                    } else {
                        // Show error message if reset failed
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Shows a confirmation dialog after sending reset email
    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Check Your Email")
                .setMessage("A password reset link has been sent to your email. Please follow the instructions.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate back to LoginActivity
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false) // Prevent dialog dismissal by outside touch or back button
                .show();
    }
}
