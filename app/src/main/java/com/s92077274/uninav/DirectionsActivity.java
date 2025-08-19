package com.s92077274.uninav;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.s92077274.uninav.models.MapPoint;
import com.s92077274.uninav.views.MapOverlayView;
import java.util.ArrayList;
import java.util.List;

public class DirectionsActivity extends AppCompatActivity {
    private MapOverlayView mapOverlay;
    private TextView tvDirectionsInstructions;
    private Button btnStartNavigation;
    private MapPoint userLocation, destination;
    private List<MapPoint> mapPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        // Initialize UI components
        mapOverlay = findViewById(R.id.mapOverlay);
        tvDirectionsInstructions = findViewById(R.id.tvDirectionsInstructions);
        btnStartNavigation = findViewById(R.id.btnStartNavigation);

        // Retrieve destination and user coordinates from intent
        String name = getIntent().getStringExtra("destination_name");
        float destX = getIntent().getFloatExtra("destination_x", 0.5f);
        float destY = getIntent().getFloatExtra("destination_y", 0.5f);
        float userX = getIntent().getFloatExtra("user_x", 0.25f);
        float userY = getIntent().getFloatExtra("user_y", 0.75f);

        // Create MapPoint objects for destination and user's location
        destination = new MapPoint(name, "", destX, destY, "general");
        userLocation = new MapPoint("Your Location", "", userX, userY, "user");
        userLocation.isUserLocation = true; // Mark as user's location

        // Add user and destination to map points list
        mapPoints = new ArrayList<>();
        mapPoints.add(userLocation);
        mapPoints.add(destination);

        // Set points for the custom map overlay view
        mapOverlay.setMapPoints(mapPoints);
        mapOverlay.setUserLocation(userLocation);

        // Build and display simple instructions
        StringBuilder instructions = new StringBuilder();
        instructions.append("1. Start at your location.\n");
        instructions.append("2. Head towards ").append(destination.name).append(".\n");
        instructions.append("3. You have arrived at your destination.");

        tvDirectionsInstructions.setText(instructions.toString());

        // Set click listener for the navigation button
        btnStartNavigation.setOnClickListener(v -> {
            Intent intent = new Intent(DirectionsActivity.this, NavigationActivity.class);
            // Pass necessary data for navigation
            intent.putExtra("destination_name", name);
            intent.putExtra("destination_x", destX);
            intent.putExtra("destination_y", destY);
            intent.putExtra("user_x", userX);
            intent.putExtra("user_y", userY);
            startActivity(intent);
        });
    }
}
