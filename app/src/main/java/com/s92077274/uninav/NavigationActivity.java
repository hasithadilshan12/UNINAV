package com.s92077274.uninav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.s92077274.uninav.models.MapPoint;
import com.s92077274.uninav.utils.AppPaths;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NavigationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private TextView tvNextInstruction, tvRemainingDistance;
    private Button btnEndNavigation;
    private ImageView btnReCenter;
    private ImageView ivCompassNav;
    private LinearLayout navigationInfoCard;

    private MapPoint startLocation;
    private MapPoint destinationLocation;
    private boolean isLiveLocationStart;

    private Polyline traveledPolyline;
    private Polyline remainingPolyline;
    private List<LatLng> fullRoutePath;
    private List<String> navigationInstructions;
    private int currentInstructionIndex = 0;
    private int currentPathSegmentIndex = 0;

    private Handler handler = new Handler();
    private Runnable runnable;

    private LatLng lastKnownLocation;
    private boolean isSimulationRunning = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE_NAVIGATION = 3;

    // SharedPreferences constants
    private static final String PREFS_NAME = "UniNavPrefs";
    private static final String KEY_MAP_TYPE = "map_type";


    private AppSensorManager compassSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        retrieveIntentData();
        setClickListeners();

        // Pass the ImageView that needs to be rotated
        compassSensorManager = new AppSensorManager(this, ivCompassNav, degrees -> {
        });


        // Initialize Google Map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // Initialize UI components
    private void initViews() {
        navigationInfoCard = findViewById(R.id.navigationInfoCard);
        tvNextInstruction = findViewById(R.id.tvNextInstruction);
        tvRemainingDistance = findViewById(R.id.tvRemainingDistance);
        btnEndNavigation = findViewById(R.id.btnEndNavigation);
        btnReCenter = findViewById(R.id.btnReCenter);
        ivCompassNav = findViewById(R.id.ivCompassNav);

        if (btnReCenter != null) {
            btnReCenter.setVisibility(View.VISIBLE);
        } else {
            Log.e("NavigationActivity", "btnReCenter is null in initViews.");
        }
    }

    // Retrieve navigation data from the intent
    private void retrieveIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            Log.e("NavigationActivity", "Intent is null in retrieveIntentData.");
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

        fullRoutePath = intent.getParcelableArrayListExtra("route_path");

        if (fullRoutePath != null && !fullRoutePath.isEmpty()) {
            navigationInstructions = AppPaths.getRouteInstructions(fullRoutePath, startLocation.name, destinationLocation.name);
            if (navigationInstructions != null && !navigationInstructions.isEmpty()) {
                tvNextInstruction.setText(navigationInstructions.get(0));
            } else {
                tvNextInstruction.setText("No detailed instructions available.");
                Log.w("NavigationActivity", "AppPaths.getRouteInstructions returned empty instructions.");
            }
            lastKnownLocation = fullRoutePath.get(0);
        } else {
            tvNextInstruction.setText("No route available.");
            Log.e("NavigationActivity", "Full route path is null or empty. Cannot proceed with navigation.");
            Toast.makeText(this, "Route path data missing. Cannot start navigation.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        tvRemainingDistance.setText("Calculating route...");
    }

    // Set up click listeners for buttons
    private void setClickListeners() {
        btnEndNavigation.setOnClickListener(v -> {
            stopNavigation();
            finish();
        });

        // Click listener for compass to orient map North
        if (ivCompassNav != null && compassSensorManager != null) { // ⭐ MODIFIED: Check if compassSensorManager is available ⭐
            ivCompassNav.setOnClickListener(v -> {
                if (googleMap != null && lastKnownLocation != null) {
                    float targetBearing = compassSensorManager.getLastHeadingDegrees(); // Get device's current heading
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new com.google.android.gms.maps.model.CameraPosition.Builder()
                                    .target(lastKnownLocation)
                                    .zoom(googleMap.getCameraPosition().zoom)
                                    .bearing(targetBearing) // Orient map to device's current heading
                                    .tilt(0)
                                    .build()
                    ));
                    Toast.makeText(this, "Map oriented to device heading", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Current location not available to orient map.", Toast.LENGTH_SHORT).show();
                }
            });
        }


        btnReCenter.setOnClickListener(v -> {
            if (googleMap == null) {
                Toast.makeText(this, "Map not ready.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (lastKnownLocation != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 18f));
                Toast.makeText(this, "Re-centered on your last known location", Toast.LENGTH_SHORT).show();
            } else if (startLocation != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation.toLatLng(), 18f));
                Toast.makeText(this, "Re-centered on start location", Toast.LENGTH_SHORT).show();
            } else if (destinationLocation != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation.toLatLng(), 15f));
                Toast.makeText(this, "Re-centered on destination", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Cannot re-center: no location data.", Toast.LENGTH_SHORT).show();
                Log.w("NavigationActivity", "No location data available to re-center map.");
            }
        });
    }

    // The CompassSensorManager handles these internally.

    @Override
    protected void onResume() {
        super.onResume();
        if (googleMap != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int mapType = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMapType(mapType);
        }

        // Start the custom CompassSensorManager
        if (compassSensorManager != null) {
            compassSensorManager.start();
        }

        // Restart live location or simulation if applicable
        if (isLiveLocationStart) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Log.w("NavigationActivity", "Cannot resume live updates: permission not granted.");
            }
        } else if (!isSimulationRunning && fullRoutePath != null && !fullRoutePath.isEmpty() && currentPathSegmentIndex < fullRoutePath.size()) {
            simulateNavigation();
        } else {
            Log.d("NavigationActivity", "Not resuming navigation. Live: " + isLiveLocationStart +
                    ", Simulation Running: " + isSimulationRunning +
                    ", Path Left: " + (fullRoutePath != null && currentPathSegmentIndex < fullRoutePath.size()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the custom CompassSensorManager
        if (compassSensorManager != null) {
            compassSensorManager.stop();
        }

        stopNavigation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopNavigation();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int mapType = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMapType(mapType);
        Log.d("NavigationActivity", "Applied map type: " + (mapType == GoogleMap.MAP_TYPE_NORMAL ? "Normal" : "Satellite"));

        // Configure map UI settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Move zoom controls
        googleMap.setPadding(0, 0, 30, 350);

        addMarkersToMap();
        if (fullRoutePath != null && !fullRoutePath.isEmpty()) {
            updatePolylines(fullRoutePath.get(0));
        }
        zoomToFitRoute();

        if (isLiveLocationStart) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    googleMap.setMyLocationEnabled(true);
                    startLocationUpdates();
                    Log.d("NavigationActivity", "Live location enabled and updates started.");
                } catch (SecurityException e) {
                    Log.e("NavigationActivity", "SecurityException enabling My Location layer: " + e.getMessage(), e);
                    Toast.makeText(this, "Cannot show live location: permission issue.", Toast.LENGTH_SHORT).show();
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE_NAVIGATION);
                Log.d("NavigationActivity", "Requesting location permission for live navigation.");
            }
        } else { // Simulated navigation
            if (fullRoutePath != null && !fullRoutePath.isEmpty()) {
                simulateNavigation();
                Log.d("NavigationActivity", "Simulated navigation started.");
            } else {
                Toast.makeText(this, "No valid route to simulate navigation.", Toast.LENGTH_LONG).show();
                Log.w("NavigationActivity", "Cannot start simulation: fullRoutePath is null or empty.");
            }
        }
    }


    // Adds start and destination markers to the map
    private void addMarkersToMap() {
        if (googleMap == null || fullRoutePath == null || fullRoutePath.isEmpty()) {
            Log.w("NavigationActivity", "Cannot add markers: map or routePath is null/empty.");
            return;
        }

        if (destinationLocation != null) {
            LatLng destinationLatLng = fullRoutePath.get(fullRoutePath.size() - 1);
            googleMap.addMarker(new MarkerOptions()
                    .position(destinationLatLng)
                    .title("Destination: " + destinationLocation.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            Log.d("NavigationActivity", "Destination marker added.");
        } else {
            Log.w("NavigationActivity", "Destination location is null, skipping destination marker.");
        }

        if (!isLiveLocationStart && startLocation != null) {
            LatLng startLatLng = fullRoutePath.get(0);
            googleMap.addMarker(new MarkerOptions()
                    .position(startLatLng)
                    .title("Start: " + startLocation.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            Log.d("NavigationActivity", "Static start marker added.");
        } else if (!isLiveLocationStart && startLocation == null) {
            Log.w("NavigationActivity", "Start location is null, skipping static start marker.");
        }
    }

    // Updates the traveled and remaining route polylines
    private void updatePolylines(LatLng currentLatLng) {
        if (googleMap == null || fullRoutePath == null || fullRoutePath.isEmpty()) {
            Log.w("NavigationActivity", "Cannot update polylines: map or routePath is null/empty.");
            return;
        }

        if (traveledPolyline != null) {
            traveledPolyline.remove();
            traveledPolyline = null;
        }
        if (remainingPolyline != null) {
            remainingPolyline.remove();
            remainingPolyline = null;
        }

        List<LatLng> traveledPath = new ArrayList<>();
        List<LatLng> remainingPath = new ArrayList<>();

        int closestSegmentIndex = findClosestSegmentIndex(currentLatLng);

        // Add points up to the closest segment
        for (int i = 0; i <= closestSegmentIndex && i < fullRoutePath.size(); i++) {
            traveledPath.add(fullRoutePath.get(i));
        }
        traveledPath.add(currentLatLng); // Add current location to traveled path

        // Add remaining points from current location
        remainingPath.add(currentLatLng);
        for (int i = closestSegmentIndex + 1; i < fullRoutePath.size(); i++) {
            remainingPath.add(fullRoutePath.get(i));
        }

        if (!traveledPath.isEmpty()) {
            PolylineOptions traveledOptions = new PolylineOptions()
                    .addAll(traveledPath)
                    .width(10)
                    .color(Color.GRAY)
                    .geodesic(true);
            traveledPolyline = googleMap.addPolyline(traveledOptions);
        } else {
            Log.w("NavigationActivity", "Traveled path is empty, not drawing traveled polyline.");
        }

        if (!remainingPath.isEmpty()) {
            PolylineOptions remainingOptions = new PolylineOptions()
                    .addAll(remainingPath)
                    .width(10)
                    .color(Color.BLUE)
                    .geodesic(true);
            remainingPolyline = googleMap.addPolyline(remainingOptions);
        } else {
            Log.w("NavigationActivity", "Remaining path is empty, not drawing remaining polyline.");
        }
    }

    // Finds the index of the route segment closest to the current location
    private int findClosestSegmentIndex(LatLng currentLatLng) {
        if (fullRoutePath == null || fullRoutePath.isEmpty()) return 0;

        int closestIndex = 0;
        float minDistance = Float.MAX_VALUE;
        float[] results = new float[1];

        for (int i = 0; i < fullRoutePath.size(); i++) {
            LatLng pathPoint = fullRoutePath.get(i);
            if (pathPoint != null) {
                Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude,
                        pathPoint.latitude, pathPoint.longitude, results);
                float distance = results[0];

                if (distance < minDistance) {
                    minDistance = distance;
                    closestIndex = i;
                }
            }
        }
        if (closestIndex == fullRoutePath.size() - 1 && fullRoutePath.size() > 1) {
            return fullRoutePath.size() - 2;
        }
        return closestIndex;
    }

    // Zooms the camera to fit the entire route path
    private void zoomToFitRoute() {
        if (googleMap == null || fullRoutePath == null || fullRoutePath.isEmpty()) {
            Log.w("NavigationActivity", "Cannot zoom to fit route: map or routePath is null/empty.");
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean hasValidPoint = false;
        for (LatLng point : fullRoutePath) {
            if (point != null) {
                builder.include(point);
                hasValidPoint = true;
            }
        }

        if (hasValidPoint) {
            try {
                LatLngBounds bounds = builder.build();
                int padding = 150;
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                Log.d("NavigationActivity", "Zoomed to fit entire route.");
            } catch (IllegalStateException e) {
                Log.e("NavigationActivity", "IllegalStateException building bounds for route: " + e.getMessage(), e);
                if (!fullRoutePath.isEmpty() && fullRoutePath.get(0) != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fullRoutePath.get(0), 17f));
                    Toast.makeText(this, "Adjusted zoom for route.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("NavigationActivity", "Route path is empty, cannot fallback zoom.");
                }
            }
        } else {
            Log.w("NavigationActivity", "No valid points in route path to build bounds for zooming.");
        }
    }

    // Simulates user movement along the route
    private void simulateNavigation() {
        if (fullRoutePath == null || fullRoutePath.isEmpty()) {
            Log.w("NavigationActivity", "Cannot simulate navigation: fullRoutePath is null or empty.");
            return;
        }
        if (isSimulationRunning) {
            Log.d("NavigationActivity", "Simulation already running, not restarting.");
            return;
        }

        isSimulationRunning = true;
        Log.d("NavigationActivity", "Simulated navigation started (Index: " + currentPathSegmentIndex + ")");

        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPathSegmentIndex < fullRoutePath.size()) {
                    LatLng currentSimulatedLatLng = fullRoutePath.get(currentPathSegmentIndex);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentSimulatedLatLng, 18f), 1000, null);

                    updateInstructionAndDistance(currentSimulatedLatLng);

                    currentPathSegmentIndex++;
                    handler.postDelayed(this, 2000);
                } else {
                    tvNextInstruction.setText("You have arrived at " + destinationLocation.name + "!");
                    tvRemainingDistance.setText("Remaining Distance: 0 km\nEstimated Time: 0 min");
                    Toast.makeText(NavigationActivity.this, "You have arrived at your destination!", Toast.LENGTH_LONG).show();
                    stopNavigation();
                    Log.d("NavigationActivity", "Simulated navigation finished.");
                    isSimulationRunning = false;
                }
            }
        };
        handler.post(runnable);
    }

    // Updates navigation instructions, remaining distance, and estimated time
    private void updateInstructionAndDistance(LatLng currentLatLng) {
        if (fullRoutePath == null || fullRoutePath.isEmpty() || navigationInstructions == null || navigationInstructions.isEmpty() || destinationLocation == null) {
            tvNextInstruction.setText("Route unavailable.");
            tvRemainingDistance.setText("Distance: N/A\nTime: N/A");
            Log.w("NavigationActivity", "Route, instructions or destination are null/empty during update.");
            return;
        }

        updatePolylines(currentLatLng);
        lastKnownLocation = currentLatLng;

        float[] results = new float[1];
        Location.distanceBetween(
                currentLatLng.latitude, currentLatLng.longitude,
                destinationLocation.x, destinationLocation.y,
                results
        );
        double distanceMeters = results[0];
        double distanceKm = distanceMeters / 1000.0;

        double averageWalkingSpeedMps = 1.4;
        double estimatedTimeSeconds = distanceMeters / averageWalkingSpeedMps;
        int estimatedMinutes = (int) Math.ceil(estimatedTimeSeconds / 60.0);

        tvRemainingDistance.setText(String.format(Locale.getDefault(), "Remaining Distance: %.2f km\nEstimated Time: ~%d min", distanceKm, estimatedMinutes));

        // Logic to advance instructions
        if (currentInstructionIndex < navigationInstructions.size() - 1) {
            int currentSegmentClosest = findClosestSegmentIndex(currentLatLng);

            if (isLiveLocationStart) {
                if (currentSegmentClosest > currentInstructionIndex) {
                    currentInstructionIndex = currentSegmentClosest;
                    if (currentInstructionIndex < navigationInstructions.size()) {
                        tvNextInstruction.setText(navigationInstructions.get(currentInstructionIndex));
                        Log.d("NavigationActivity", "Advanced live instruction to: " + navigationInstructions.get(currentInstructionIndex));
                    }
                }
            } else {
                if (currentPathSegmentIndex + 1 > currentInstructionIndex) {
                    currentInstructionIndex = currentPathSegmentIndex + 1;
                    if (currentInstructionIndex < navigationInstructions.size()) {
                        tvNextInstruction.setText(navigationInstructions.get(currentInstructionIndex));
                        Log.d("NavigationActivity", "Advanced simulated instruction to: " + navigationInstructions.get(currentInstructionIndex));
                    }
                }
            }
        } else if (currentInstructionIndex < navigationInstructions.size()) {
            tvNextInstruction.setText(navigationInstructions.get(currentInstructionIndex));
        }

        // Arrival detection
        if (distanceMeters < 10) {
            tvNextInstruction.setText("You have arrived at " + destinationLocation.name + "!");
            tvRemainingDistance.setText("Remaining Distance: 0 km\nEstimated Time: 0 min");
            stopNavigation();
            Toast.makeText(this, "You have arrived!", Toast.LENGTH_SHORT).show();
            Log.d("NavigationActivity", "Arrived at destination.");
        }
    }

    // Starts continuous live location updates
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("NavigationActivity", "Attempted to start location updates without permission.");
            return;
        }

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    Log.w("NavigationActivity", "LocationResult is null.");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 18f), 1000, null);
                        updateInstructionAndDistance(newLatLng);
                        Log.d("NavigationActivity", "Live location updated: " + newLatLng.latitude + ", " + newLatLng.longitude);
                    }
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
            Toast.makeText(this, "Started live location updates.", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Log.e("NavigationActivity", "SecurityException requesting location updates: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to start live location updates due to permission.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE_NAVIGATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("NavigationActivity", "Location permission granted for navigation.");
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        Log.e("NavigationActivity", "SecurityException enabling My Location layer after permission: " + e.getMessage(), e);
                    }
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    int mapType = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
                    googleMap.setMapType(mapType);
                }
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot use live navigation.", Toast.LENGTH_LONG).show();
                Log.w("NavigationActivity", "Location permission denied for navigation.");
            }
        }
    }

    // Stops location updates or simulated navigation
    private void stopNavigation() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d("NavigationActivity", "Stopped live location updates.");
        }
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
            Log.d("NavigationActivity", "Stopped simulated navigation.");
        }
        if (traveledPolyline != null) {
            traveledPolyline.remove();
            traveledPolyline = null;
        }
        if (remainingPolyline != null) {
            remainingPolyline.remove();
            remainingPolyline = null;
        }
        isSimulationRunning = false;
    }
}
