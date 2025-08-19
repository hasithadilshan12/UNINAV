package com.s92077274.uninav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.content.pm.PackageManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.s92077274.uninav.models.MapPoint;
import com.s92077274.uninav.utils.AppPaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DestinationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private TextView tvStartLocation, tvDestinationLocation, tvEstimatedTime, tvDistance;
    private Button btnBack, btnStartNavigation;

    private MapPoint startLocation;
    private MapPoint destinationLocation;
    private boolean isLiveLocationStart;
    private List<LatLng> routePath;

    private static final int LOCATION_PERMISSION_REQUEST_CODE_DESTINATION = 4;

    // SharedPreferences constants
    private static final String PREFS_NAME = "UniNavPrefs";
    private static final String KEY_MAP_TYPE = "map_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        initViews();
        retrieveIntentData();
        updateUI();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setClickListeners();
    }

    // Initializes UI elements
    private void initViews() {
        tvStartLocation = findViewById(R.id.tvStartLocation);
        tvDestinationLocation = findViewById(R.id.tvDestinationLocation);
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime);
        tvDistance = findViewById(R.id.tvDistance);
        btnBack = findViewById(R.id.btnBack);
        btnStartNavigation = findViewById(R.id.btnStartNavigation);
    }

    // Retrieves navigation data from the intent
    private void retrieveIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            Log.e("DestinationActivity", "Intent is null in retrieveIntentData.");
            Toast.makeText(this, "Navigation data missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String startName = intent.getStringExtra("start_location_name");
        float startLat = intent.getFloatExtra("user_lat", 0.0f);
        float startLng = intent.getFloatExtra("user_lng", 0.0f);
        isLiveLocationStart = intent.getBooleanExtra("is_live_location", false);

        String destName = intent.getStringExtra("destination_name");
        float destLat = intent.getFloatExtra("destination_lat", 0.0f);
        float destLng = intent.getFloatExtra("destination_lng", 0.0f);

        startLocation = new MapPoint(startName, "Starting Point", startLat, startLng, isLiveLocationStart ? "user" : "start");
        destinationLocation = new MapPoint(destName, "Destination", destLat, destLng, "destination");

        routePath = intent.getParcelableArrayListExtra("route_path");

        if (routePath == null || routePath.isEmpty()) {
            Log.e("DestinationActivity", "Route path is null or empty, cannot proceed with route display.");
            Toast.makeText(this, "Route path data missing. Cannot show directions.", Toast.LENGTH_LONG).show();
        }
    }

    // Updates text views with route information
    private void updateUI() {
        if (startLocation == null || destinationLocation == null || routePath == null || routePath.isEmpty()) {
            Log.e("DestinationActivity", "Start/destination location or route is null, cannot update UI properly.");
            tvStartLocation.setText("From: N/A");
            tvDestinationLocation.setText("To: N/A");
            tvDistance.setText("Distance: N/A");
            tvEstimatedTime.setText("Estimated Time: N/A");
            btnStartNavigation.setVisibility(View.GONE);
            btnStartNavigation.setEnabled(false);
            Toast.makeText(this, "Error: Incomplete navigation data.", Toast.LENGTH_LONG).show();
            return;
        }

        tvStartLocation.setText("From: " + startLocation.name);
        tvDestinationLocation.setText("To: " + destinationLocation.name);

        double totalDistanceMeters = calculateTotalDistance(routePath);
        tvDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f km", totalDistanceMeters / 1000.0));

        double averageWalkingSpeedMps = 1.4;
        double estimatedTimeSeconds = totalDistanceMeters / averageWalkingSpeedMps;
        int estimatedMinutes = (int) Math.ceil(estimatedTimeSeconds / 60.0);
        tvEstimatedTime.setText(String.format(Locale.getDefault(), "Estimated Time: ~%d min", estimatedMinutes));

        if (isLiveLocationStart) {
            btnStartNavigation.setVisibility(View.VISIBLE);
            btnStartNavigation.setEnabled(true);
        } else {
            btnStartNavigation.setVisibility(View.GONE);
            btnStartNavigation.setEnabled(false);
        }
    }

    // Calculates the total distance of a path in meters
    private double calculateTotalDistance(List<LatLng> path) {
        double totalDistance = 0;
        if (path == null || path.size() < 2) {
            return 0;
        }

        float[] results = new float[1];
        for (int i = 0; i < path.size() - 1; i++) {
            LatLng point1 = path.get(i);
            LatLng point2 = path.get(i + 1);
            Location.distanceBetween(point1.latitude, point1.longitude,
                    point2.latitude, point2.longitude, results);
            totalDistance += results[0];
        }
        return totalDistance;
    }

    // Sets up button click listeners
    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnStartNavigation.setOnClickListener(v -> {
            if (routePath == null || routePath.isEmpty()) {
                Toast.makeText(this, "Cannot start navigation: route path is empty.", Toast.LENGTH_SHORT).show();
                Log.e("DestinationActivity", "Attempted to start navigation with empty routePath.");
                return;
            }

            Intent navIntent = new Intent(DestinationActivity.this, NavigationActivity.class);
            navIntent.putExtra("start_location_name", startLocation.name);
            navIntent.putExtra("user_lat", startLocation.x);
            navIntent.putExtra("user_lng", startLocation.y);
            navIntent.putExtra("is_live_location", true);

            navIntent.putExtra("destination_name", destinationLocation.name);
            navIntent.putExtra("destination_lat", destinationLocation.x);
            navIntent.putExtra("destination_lng", destinationLocation.y);

            navIntent.putParcelableArrayListExtra("route_path", new ArrayList<>(routePath));

            startActivity(navIntent);
            finish();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int mapType = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMapType(mapType);
        Log.d("DestinationActivity", "Applied map type: " + (mapType == GoogleMap.MAP_TYPE_NORMAL ? "Normal" : "Satellite"));

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        if (isLiveLocationStart) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    googleMap.setMyLocationEnabled(true);
                    Log.d("DestinationActivity", "My Location enabled.");
                } catch (SecurityException e) {
                    Log.e("DestinationActivity", "SecurityException enabling My Location: " + e.getMessage(), e);
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE_DESTINATION);
            }
        } else if (routePath != null && !routePath.isEmpty()) {
            LatLng actualStartLatLng = routePath.get(0);
            googleMap.addMarker(new MarkerOptions()
                    .position(actualStartLatLng)
                    .title("Start: " + (startLocation != null ? startLocation.name : "Unknown"))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            Log.d("DestinationActivity", "Added static start marker.");
        }

        if (routePath != null && !routePath.isEmpty()) {
            LatLng actualDestinationLatLng = routePath.get(routePath.size() - 1);
            googleMap.addMarker(new MarkerOptions()
                    .position(actualDestinationLatLng)
                    .title("Destination: " + (destinationLocation != null ? destinationLocation.name : "Unknown"))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            Log.d("DestinationActivity", "Added destination marker.");
        }

        drawCustomRoute();
        zoomToFitMarkersAndRoute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE_DESTINATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLiveLocationStart && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        googleMap.setMyLocationEnabled(true);
                        Log.d("DestinationActivity", "Location permission granted, My Location enabled.");
                    } catch (SecurityException e) {
                        Log.e("DestinationActivity", "SecurityException enabling My Location after permission grant: " + e.getMessage(), e);
                    }
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    int mapType = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
                    googleMap.setMapType(mapType);
                }
            } else {
                Toast.makeText(this, "Location permission denied. Cannot show live start location.", Toast.LENGTH_LONG).show();
                Log.w("DestinationActivity", "Location permission denied for destination activity.");
                if (googleMap != null && routePath != null && !routePath.isEmpty()) {
                    LatLng actualStartLatLng = routePath.get(0);
                    googleMap.addMarker(new MarkerOptions()
                            .position(actualStartLatLng)
                            .title("Start: " + (startLocation != null ? startLocation.name : "Unknown"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }
            }
        }
    }

    // Draws the navigation route on the map
    private void drawCustomRoute() {
        if (googleMap == null || routePath == null || routePath.isEmpty()) {
            Log.w("DestinationActivity", "Cannot draw route: map or routePath is null/empty.");
            return;
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePath)
                .width(10)
                .color(ContextCompat.getColor(this, R.color.colorPrimary))
                .geodesic(true);
        googleMap.addPolyline(polylineOptions);
        Log.d("DestinationActivity", "Route polyline drawn.");
    }

    // Zooms the map to fit markers and the entire route
    private void zoomToFitMarkersAndRoute() {
        if (googleMap == null || routePath == null || routePath.isEmpty()) {
            Log.w("DestinationActivity", "Cannot zoom to fit: map or routePath is null/empty.");
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (!routePath.isEmpty()) {
            builder.include(routePath.get(0));
            builder.include(routePath.get(routePath.size() - 1));
        } else {
            Log.w("DestinationActivity", "Route path is empty, cannot include points for bounds.");
            return;
        }

        try {
            LatLngBounds bounds = builder.build();
            int padding = 100;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            Log.d("DestinationActivity", "Zoomed to fit markers and route.");
        } catch (IllegalStateException e) {
            Log.e("DestinationActivity", "IllegalStateException when building LatLngBounds or moving camera: " + e.getMessage(), e);
            if (destinationLocation != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        destinationLocation.toLatLng(), 17f));
                Toast.makeText(this, "Adjusting zoom for destination location.", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("DestinationActivity", "Destination location is null, cannot fallback zoom.");
            }
        }
    }
}
