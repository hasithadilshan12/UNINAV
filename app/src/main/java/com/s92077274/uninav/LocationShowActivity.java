package com.s92077274.uninav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import com.s92077274.uninav.models.MapPoint;
import com.s92077274.uninav.utils.AppPaths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationShowActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE_LOCATION_SHOW = 2;

    private TextView tvLocationName, tvLocationDescription;
    private Button btnNavigateTo;
    private MapPoint shownLocation;
    private LatLng currentUserLatLng;

    // Constants for SharedPreferences
    private static final String PREFS_NAME = "UniNavPrefs";
    private static final String KEY_MAP_TYPE = "map_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize activity and set content view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_show);

        // Initialize location service client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get references to UI elements
        tvLocationName = findViewById(R.id.tvLocationName);
        tvLocationDescription = findViewById(R.id.tvLocationDescription);
        btnNavigateTo = findViewById(R.id.btnNavigateTo);

        // Get location data from intent extras
        String name = getIntent().getStringExtra("location_name");
        float lat = getIntent().getFloatExtra("location_lat", 0.0f);
        float lng = getIntent().getFloatExtra("location_lng", 0.0f);

        // Create MapPoint object for the shown location
        shownLocation = new MapPoint(name, "Selected location on campus.", lat, lng, "general");

        // Set location name and description in UI
        tvLocationName.setText(name != null ? name : "Unknown Location");
        tvLocationDescription.setText("Selected location on campus.");

        // Set click listener for navigation button
        btnNavigateTo.setOnClickListener(v -> {
            showStartLocationDialog(shownLocation, DestinationActivity.class);
        });

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Get current device location
        getDeviceLocation();
    }

    @Override
    protected void onDestroy() {
        // Clean up when activity is destroyed
        super.onDestroy();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        // Callback when map is ready to use
        googleMap = map;

        // Set map type from preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int mapType = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMapType(mapType);

        // Configure map UI settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setPadding(0, 0, 30, 450);

        // Add marker for the shown location
        if (shownLocation != null) {
            LatLng locationLatLng = shownLocation.toLatLng();
            googleMap.addMarker(new MarkerOptions()
                    .position(locationLatLng)
                    .title(shownLocation.name)
                    .snippet(shownLocation.description));

            // Center camera on the location
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 17f));
        }
    }

    private void getDeviceLocation() {
        // Get last known device location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentUserLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    });
        } else {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE_LOCATION_SHOW);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Handle permission request result
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE_LOCATION_SHOW) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showStartLocationDialog(MapPoint destinationPoint, Class<?> targetActivityClass) {
        // Show dialog to select starting location
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_start_location, null);
        builder.setView(dialogView);

        // Get references to dialog views
        AutoCompleteTextView etStartLocation = dialogView.findViewById(R.id.etStartLocation);
        Button btnUseCurrentLocation = dialogView.findViewById(R.id.btnUseCurrentLocation);
        Button btnConfirmStart = dialogView.findViewById(R.id.btnConfirmStart);

        // Set up autocomplete adapter with location names
        List<String> locationNames = AppPaths.getAllMapPointNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                locationNames
        );
        etStartLocation.setAdapter(adapter);
        etStartLocation.setThreshold(1);

        AlertDialog dialog = builder.create();

        // Handle text changes in search field
        etStartLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                boolean isEmpty = s.toString().trim().isEmpty();
                btnUseCurrentLocation.setEnabled(isEmpty);
                btnConfirmStart.setEnabled(!isEmpty && AppPaths.getMapPointByName(s.toString().trim()) != null);
            }
        });

        // Handle current location button click
        btnUseCurrentLocation.setOnClickListener(v -> {
            if (currentUserLatLng != null) {
                MapPoint nearestPoint = AppPaths.findNearestMapPoint(currentUserLatLng);
                if (nearestPoint == null || nearestPoint.name.equals(destinationPoint.name)) {
                    Toast.makeText(LocationShowActivity.this, "Invalid start location", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<LatLng> route = AppPaths.getRoute(nearestPoint.name, destinationPoint.name);
                if (route == null || route.isEmpty()) {
                    Toast.makeText(LocationShowActivity.this, "No route found", Toast.LENGTH_LONG).show();
                    return;
                }

                ArrayList<LatLng> finalRoute = new ArrayList<>(route);
                if (!finalRoute.get(0).equals(currentUserLatLng)) {
                    finalRoute.add(0, currentUserLatLng);
                }

                proceedToNextActivity(
                        "Your Current Location",
                        (float) currentUserLatLng.latitude,
                        (float) currentUserLatLng.longitude,
                        true,
                        destinationPoint,
                        targetActivityClass,
                        finalRoute
                );
                dialog.dismiss();
            } else {
                Toast.makeText(LocationShowActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle confirm button click
        btnConfirmStart.setOnClickListener(v -> {
            String selectedStart = etStartLocation.getText().toString().trim();
            MapPoint startPoint = AppPaths.getMapPointByName(selectedStart);

            if (startPoint != null && !startPoint.name.equals(destinationPoint.name)) {
                List<LatLng> route = AppPaths.getRoute(startPoint.name, destinationPoint.name);
                if (route == null || route.isEmpty()) {
                    Toast.makeText(LocationShowActivity.this, "No route found", Toast.LENGTH_LONG).show();
                    return;
                }

                proceedToNextActivity(
                        startPoint.name,
                        startPoint.x,
                        startPoint.y,
                        false,
                        destinationPoint,
                        targetActivityClass,
                        route
                );
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void proceedToNextActivity(String startLocationName, float startLat, float startLng,
                                       boolean isLiveLocation, MapPoint destinationPoint,
                                       Class<?> targetActivityClass, List<LatLng> routePath) {
        // Start navigation activity with route data
        Intent intent = new Intent(this, targetActivityClass);

        // Add destination data to intent
        intent.putExtra("destination_name", destinationPoint.name);
        intent.putExtra("destination_lat", destinationPoint.x);
        intent.putExtra("destination_lng", destinationPoint.y);

        // Add start location data to intent
        intent.putExtra("start_location_name", startLocationName);
        intent.putExtra("user_lat", startLat);
        intent.putExtra("user_lng", startLng);
        intent.putExtra("is_live_location", isLiveLocation);

        // Add route path to intent
        intent.putParcelableArrayListExtra("route_path", new ArrayList<>(routePath));

        startActivity(intent);
    }
}