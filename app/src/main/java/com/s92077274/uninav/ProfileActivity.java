package com.s92077274.uninav;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        btnLogout = findViewById(R.id.btnLogout);
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navSearch = findViewById(R.id.navSearch);
        LinearLayout navMap = findViewById(R.id.navMap);
        LinearLayout navProfile = findViewById(R.id.navProfile);


        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();
            Toast.makeText(ProfileActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();



            Intent logoutIntent = new Intent(ProfileActivity.this, LoginActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
        });


        navHome.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            finish();
        });

        navSearch.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SearchActivity.class));
            finish();
        });

        navMap.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MapActivity.class));
            finish();
        });


        navProfile.setOnClickListener(v -> {

        });
    }
}
