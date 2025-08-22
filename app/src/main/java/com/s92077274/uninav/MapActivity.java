package com.s92077274.uninav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
// import android.view.animation.RotateAnimation; // Removed - now handled by CompassSensorManager
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// Removed direct sensor imports - now handled by CompassSensorManager
// import android.hardware.Sensor;
// import android.hardware.SensorEvent;
// import android.hardware.SensorEventListener;
// import android.hardware.SensorManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.bumptech.glide.Glide;
import com.s92077274.uninav.utils.ImageUtils;
import com.s92077274.uninav.models.MapPoint;
import com.s92077274.uninav.utils.AppPaths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Removed SensorEventListener interface from MapActivity
public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    // Map and location related variables
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE_MAP = 1;
    private LatLng currentUserLatLng;

    // UI components
    private TextView tvSearchHint;
    private ImageView btnReCenter, ivCompass;
    private LinearLayout navHome, navSearch, navMap, navProfile;

    // Data storage
    private List<MapPoint> mapPoints;
    private Map<String, MapPoint> markerMapPointMap;
    private BottomSheetDialog bottomSheetDialog;
    private MapPoint selectedDestinationPoint;
    private static final String PREFS_NAME = "UniNavPrefs";
    private static final String KEY_MAP_TYPE = "map_type";

    // ⭐ NEW: Instance of our custom CompassSensorManager ⭐
    private CompassSensorManager compassSensorManager;
    // float currentDegree removed - now managed by CompassSensorManager internally,
    // or by the ImageView's animation directly. If you need raw degrees in MapActivity,
    // use the OnHeadingChangeListener in the CompassSensorManager constructor.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize location service and UI components
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initViews();
        initMapPointsFromAppPaths();
        setClickListeners();

        // ⭐ MODIFIED: Initialize CompassSensorManager ⭐
        // Pass the ImageView that needs to be rotated
        compassSensorManager = new CompassSensorManager(this, ivCompass, degrees -> {
            // Optional: If MapActivity needs to do something else with the heading besides rotating the ImageView,
            // you can implement it here. For now, the ImageView rotation is handled internally by CompassSensorManager.
            // Example: Log.d("MapActivity", "Compass Heading: " + degrees);
            // If you want to rotate the map camera with the compass:
            // if (googleMap != null && currentUserLatLng != null) {
            //     googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
            //             new com.google.android.gms.maps.model.CameraPosition.Builder()
            //                     .target(currentUserLatLng)
            //                     .zoom(googleMap.getCameraPosition().zoom)
            //                     .bearing(degrees)
            //                     .tilt(0)
            //                     .build()
            //     ));
            // }
        });


        // Set up map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        requestLocationPermission();
    }


    // ⭐ REMOVED: setupCompassSensors() method is no longer needed ⭐


    @Override
    protected void onResume() {
        super.onResume();
        // Restore map type preference
        if (googleMap != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int mapType = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMapType(mapType);
        }

        // ⭐ MODIFIED: Start the custom CompassSensorManager ⭐
        if (compassSensorManager != null) {
            compassSensorManager.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // ⭐ MODIFIED: Stop the custom CompassSensorManager ⭐
        if (compassSensorManager != null) {
            compassSensorManager.stop();
        }
    }

    // ⭐ REMOVED: onSensorChanged and onAccuracyChanged are no longer implemented directly here ⭐
    // The CompassSensorManager handles these internally.

    // Initialize all view references
    private void initViews() {
        tvSearchHint = findViewById(R.id.tvSearchHint);
        btnReCenter = findViewById(R.id.btnReCenter);
        navHome = findViewById(R.id.navHome);
        navSearch = findViewById(R.id.navSearch);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);
        ivCompass = findViewById(R.id.ivCompass);
    }

    // Set up all click listeners for UI elements
    private void setClickListeners() {
        tvSearchHint.setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, SearchActivity.class));
        });

        btnReCenter.setOnClickListener(v -> {
            centerMapOnUserLocation();
        });

        ivCompass.setOnClickListener(v -> {
            // ⭐ MODIFIED: Now uses the heading from CompassSensorManager for orienting map ⭐
            if (googleMap != null && currentUserLatLng != null && compassSensorManager != null) {
                float currentMapBearing = googleMap.getCameraPosition().bearing;
                float targetBearing = compassSensorManager.getLastHeadingDegrees(); // Get device's current heading
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new com.google.android.gms.maps.model.CameraPosition.Builder()
                                .target(currentUserLatLng)
                                .zoom(googleMap.getCameraPosition().zoom)
                                .bearing(targetBearing) // Orient map to device's current heading
                                .tilt(0)
                                .build()
                ));
                Toast.makeText(this, "Map oriented to device heading", Toast.LENGTH_SHORT).show();
            } else if (googleMap != null && currentUserLatLng != null) {
                // Fallback if compassSensorManager is null, orient map North
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new com.google.android.gms.maps.model.CameraPosition.Builder()
                                .target(currentUserLatLng)
                                .zoom(googleMap.getCameraPosition().zoom)
                                .bearing(0)
                                .tilt(0)
                                .build()
                ));
                Toast.makeText(this, "Map oriented North", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Current location not available for map orientation.", Toast.LENGTH_SHORT).show();
            }
        });

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, HomeActivity.class));
            finish();
        });

        navSearch.setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, SearchActivity.class));
            finish();
        });

        navMap.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Map", Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(MapActivity.this, ProfileActivity.class));
            finish();
        });
    }

    // Center map on user's current location or default to OUSL
    private void centerMapOnUserLocation() {
        if (googleMap == null) return;

        if (currentUserLatLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLatLng, 17f));
            Toast.makeText(this, "Centered on your location", Toast.LENGTH_SHORT).show();
        } else {
            LatLng ouslCenter = new LatLng(6.883019826740543, 79.88670615788185);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ouslCenter, 15f));
            Toast.makeText(this, "Centered on OUSL", Toast.LENGTH_LONG).show();
        }
    }

    // ⭐ REMOVED/MODIFIED: orientMapNorth() logic is now handled in ivCompass click listener ⭐
    private void orientMapNorth() {
        if (googleMap != null && currentUserLatLng != null) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new com.google.android.gms.maps.model.CameraPosition.Builder()
                            .target(currentUserLatLng)
                            .zoom(googleMap.getCameraPosition().zoom)
                            .bearing(0)
                            .tilt(0)
                            .build()
            ));
            Toast.makeText(this, "Map oriented North", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Current location not available for map orientation.", Toast.LENGTH_SHORT).show();
        }
    }


    // Load map points from AppPaths utility class
    private void initMapPointsFromAppPaths() {
        mapPoints = new ArrayList<>();
        markerMapPointMap = new HashMap<>();
        for (String name : AppPaths.getAllMapPointNames()) {
            MapPoint point = AppPaths.getMapPointByName(name);
            if (point != null) {
                mapPoints.add(point);
            }
        }
    }

    // Called when map is ready to be used
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Set map type from preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int mapType = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMapType(mapType);

        // Configure map UI settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Set up map interactions
        googleMap.setOnMarkerClickListener(this);
        addCampusMarkers();
        zoomToOUSLBounds();

        // Enable location layer if permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                googleMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Log.e("MapActivity", "Location permission error", e);
            }
        }
    }

    // Add markers for all campus locations
    private void addCampusMarkers() {
        if (googleMap == null) return;

        googleMap.clear();
        markerMapPointMap.clear();

        for (MapPoint point : mapPoints) {
            if (point != null) {
                LatLng latLng = new LatLng(point.x, point.y);
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(point.name)
                        .snippet(point.description));

                if (marker != null) {
                    markerMapPointMap.put(marker.getId(), point);
                }
            }
        }
    }

    // Zoom map to show all OUSL locations
    private void zoomToOUSLBounds() {
        if (googleMap == null) return;

        if (mapPoints.isEmpty()) {
            LatLng ouslCenter = new LatLng(6.883019826740543, 79.88670615788185);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ouslCenter, 15f));
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean hasValidPoint = false;
        for (MapPoint point : mapPoints) {
            if (point != null) {
                builder.include(new LatLng(point.x, point.y));
                hasValidPoint = true;
            }
        }

        if (hasValidPoint) {
            try {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
            } catch (IllegalStateException e) {
                if (!mapPoints.isEmpty() && mapPoints.get(0) != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapPoints.get(0).toLatLng(), 17f));
                }
            }
        }
    }

    // Handle marker clicks
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        selectedDestinationPoint = markerMapPointMap.get(marker.getId());
        if (selectedDestinationPoint != null) {
            showBottomActionPanel(selectedDestinationPoint);
        }
        return true;
    }

    // Request location permission if not granted
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE_MAP);
        } else {
            getDeviceLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Handle location permission result
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE_MAP) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        Log.e("MapActivity", "Location permission error", e);
                    }
                }
                getDeviceLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Get device's last known location
    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                currentUserLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            } else {
                                Log.d("MapActivity", "Current user location is null, likely not available yet.");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("MapActivity", "Failed to get last location: " + e.getMessage(), e);
                        });
            }
        } catch (Exception e) {
            Log.e("MapActivity", "Location error", e);
        }
    }

    // Check if device location services are enabled globally
    private boolean isLocationServicesEnabled() {
        LocationManager locationManager = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        if (locationManager == null) {
            return false;
        }
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.e("MapActivity", "GPS Provider check failed", ex);
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.e("MapActivity", "Network Provider check failed", ex);
        }
        return gpsEnabled || networkEnabled;
    }

    // Show bottom panel with location actions
    private void showBottomActionPanel(final MapPoint destinationPoint) {
        if (destinationPoint == null) return;

        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
        }

        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_location_actions, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Initialize panel views
        TextView tvPanelLocationName = bottomSheetView.findViewById(R.id.tvPanelLocationName);
        TextView tvPanelLocationDescription = bottomSheetView.findViewById(R.id.tvPanelLocationDescription);
        ImageView locationImage = bottomSheetView.findViewById(R.id.locationImage);
        Button btnPanelDirections = bottomSheetView.findViewById(R.id.btnPanelDirections);
        Button btnPanelStartNav = bottomSheetView.findViewById(R.id.btnPanelStartNav);

        // Set location info
        tvPanelLocationName.setText(destinationPoint.name != null ? destinationPoint.name : "Unknown Location");
        tvPanelLocationDescription.setText(destinationPoint.description != null ? destinationPoint.description : "No description available.");

        // Load location image
        int imageResId = ImageUtils.getDrawableIdForLocation(this, destinationPoint.name);
        Glide.with(this)
                .load(imageResId)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(locationImage);
        locationImage.setVisibility(View.VISIBLE);

        // Set up button click handlers
        // "Directions" button always opens the Start Location Dialog
        btnPanelDirections.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showStartLocationDialog(destinationPoint, DestinationActivity.class);
        });

        // "Start" button now directly attempts live navigation or toasts if location is off
        btnPanelStartNav.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            handleDirectNavigationStart(destinationPoint); // Call new method for direct start
        });

        bottomSheetDialog.show();
    }

    // Handles direct start of navigation (bypassing start location dialog)
    private void handleDirectNavigationStart(MapPoint destinationPoint) {
        // First, check if global device location services are enabled
        if (!isLocationServicesEnabled()) {
            Toast.makeText(this, "Please enable device location services to start navigation.", Toast.LENGTH_LONG).show();
            return;
        }

        // Then, check if we have a current user location
        if (currentUserLatLng != null) {
            MapPoint nearestCampusPoint = AppPaths.findNearestMapPoint(currentUserLatLng);

            if (nearestCampusPoint == null) {
                Toast.makeText(this, "Could not find a nearby campus point for routing from your location.", Toast.LENGTH_LONG).show();
                return;
            }

            if (nearestCampusPoint.name.equals(destinationPoint.name)) {
                Toast.makeText(this, "Start and destination cannot be the same!", Toast.LENGTH_SHORT).show();
                return;
            }

            List<LatLng> actualRoute = AppPaths.getRoute(nearestCampusPoint.name, destinationPoint.name);

            if (actualRoute == null || actualRoute.isEmpty()) {
                Toast.makeText(this, "No predefined route from " + nearestCampusPoint.name + " to " + destinationPoint.name + ". Cannot start navigation.", Toast.LENGTH_LONG).show();
                return;
            }

            ArrayList<LatLng> finalRoute = new ArrayList<>(actualRoute);
            // Prepend current user location to the route if it's not the first point
            if (!finalRoute.isEmpty() && !finalRoute.get(0).equals(currentUserLatLng)) {
                finalRoute.add(0, currentUserLatLng);
            } else if (finalRoute.isEmpty()) { // Handle case where route is effectively just current to destination
                finalRoute.add(currentUserLatLng);
                finalRoute.add(new LatLng(destinationPoint.x, destinationPoint.y));
            }

            // Proceed directly to NavigationActivity with live location
            proceedToNavigation(
                    "Your Current Location",
                    (float) currentUserLatLng.latitude, (float) currentUserLatLng.longitude,
                    true, // isLiveLocation is true
                    destinationPoint,
                    NavigationActivity.class,
                    finalRoute
            );
        } else {
            // If location services are ON but currentUserLatLng is still null, it means location is not yet available.
            // This can happen if getLastLocation() hasn't returned yet, or GPS is still acquiring.
            Toast.makeText(this, "Your current location is not yet available. Please try again or ensure GPS signal.", Toast.LENGTH_LONG).show();
        }
    }


    // Show dialog to select start location (Used for "Directions" flow)
    private void showStartLocationDialog(MapPoint destinationPoint, Class<?> targetActivityClass) {
        if (destinationPoint == null) {
            Log.e(getLocalClassName(), "Cannot show start location dialog for null destination point.");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_start_location, null);
        builder.setView(dialogView);

        // Initialize dialog views
        AutoCompleteTextView etStartLocation = dialogView.findViewById(R.id.etStartLocation);
        Button btnUseCurrentLocation = dialogView.findViewById(R.id.btnUseCurrentLocation);
        Button btnConfirmStart = dialogView.findViewById(R.id.btnConfirmStart);

        // Set up autocomplete adapter
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
                boolean locationServicesOn = isLocationServicesEnabled(); // Check device location status

                // "Use Current Location" button logic
                // Enabled if location services are ON AND a location is available AND the input is empty
                btnUseCurrentLocation.setEnabled(locationServicesOn && isEmpty && currentUserLatLng != null);
                if (!locationServicesOn) {
                    btnUseCurrentLocation.setText("Enable Device Location");
                } else if (currentUserLatLng == null) {
                    btnUseCurrentLocation.setText("Current Location Unavailable");
                } else {
                    btnUseCurrentLocation.setText("Use My Current Location");
                }

                // "Confirm Selected Location" button logic
                // Enabled if input is not empty AND a valid predefined point is selected
                // This does NOT depend on isLocationServicesEnabled() as it can be a simulated route for DIRECTIONS preview
                boolean isValidPointSelected = AppPaths.getMapPointByName(s.toString().trim()) != null;
                btnConfirmStart.setEnabled(!isEmpty && isValidPointSelected);

                // Set button text explicitly if needed, otherwise rely on default from layout
                btnConfirmStart.setText("Confirm Selected Location");
            }
        });

        // Handle current location button click
        btnUseCurrentLocation.setOnClickListener(v -> {
            if (!isLocationServicesEnabled()) {
                Toast.makeText(this, "Please enable device location services to use current location.", Toast.LENGTH_LONG).show();
                return;
            }
            if (currentUserLatLng != null) {
                MapPoint nearestPoint = AppPaths.findNearestMapPoint(currentUserLatLng);
                if (nearestPoint == null) {
                    Toast.makeText(this, "No nearby campus point found for routing.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (nearestPoint.name.equals(destinationPoint.name)) {
                    Toast.makeText(this, "Start and destination cannot be the same!", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<LatLng> route = AppPaths.getRoute(nearestPoint.name, destinationPoint.name);
                if (route == null || route.isEmpty()) {
                    Toast.makeText(this, "No route found from " + nearestPoint.name + " to " + destinationPoint.name, Toast.LENGTH_LONG).show();
                    return;
                }

                ArrayList<LatLng> finalRoute = new ArrayList<>(route);
                if (!finalRoute.isEmpty() && !finalRoute.get(0).equals(currentUserLatLng)) {
                    finalRoute.add(0, currentUserLatLng);
                } else if (finalRoute.isEmpty()) {
                    finalRoute.add(currentUserLatLng);
                    finalRoute.add(new LatLng(destinationPoint.x, destinationPoint.y));
                }

                dialog.dismiss();
                proceedToNavigation(
                        "Your Current Location",
                        (float) currentUserLatLng.latitude, (float) currentUserLatLng.longitude,
                        true,
                        destinationPoint,
                        targetActivityClass, // targetActivityClass is DestinationActivity for directions
                        finalRoute
                );
            } else {
                Toast.makeText(this, "Your current location is not yet available. Please try again or ensure GPS signal.", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle confirm button click
        btnConfirmStart.setOnClickListener(v -> {
            // For directions preview, selecting a predefined start does NOT require live location to be ON
            String selectedStart = etStartLocation.getText().toString().trim();
            MapPoint startPoint = AppPaths.getMapPointByName(selectedStart);

            if (startPoint != null) {
                if (startPoint.name.equals(destinationPoint.name)) {
                    Toast.makeText(this, "Start and destination cannot be the same!", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<LatLng> route = AppPaths.getRoute(startPoint.name, destinationPoint.name);
                if (route == null || route.isEmpty()) {
                    Toast.makeText(this, "No route found from " + startPoint.name + " to " + destinationPoint.name, Toast.LENGTH_LONG).show();
                    return;
                }

                dialog.dismiss();
                proceedToNavigation(
                        startPoint.name,
                        startPoint.x, startPoint.y,
                        false, // isLiveLocation is false for predefined start
                        destinationPoint,
                        targetActivityClass, // targetActivityClass is DestinationActivity for directions
                        route
                );
            } else {
                Toast.makeText(this, "Select a valid start location", Toast.LENGTH_SHORT).show();
            }
        });

        // Manually trigger afterTextChanged once to set initial button states
        etStartLocation.setText(etStartLocation.getText()); // Trigger to update button states initially

        dialog.show();
    }

    // Start navigation activity with route data
    private void proceedToNavigation(String startLocationName, float startLat, float startLng,
                                     boolean isLiveLocation, MapPoint destinationPoint,
                                     Class<?> targetActivityClass, List<LatLng> routePath) {
        Intent intent = new Intent(this, targetActivityClass);

        // Add destination data
        intent.putExtra("destination_name", destinationPoint.name);
        intent.putExtra("destination_lat", destinationPoint.x);
        intent.putExtra("destination_lng", destinationPoint.y);

        // Add start location data
        intent.putExtra("start_location_name", startLocationName);
        intent.putExtra("user_lat", startLat);
        intent.putExtra("user_lng", startLng);
        intent.putExtra("is_live_location", isLiveLocation);

        // Add route path
        intent.putParcelableArrayListExtra("route_path", new ArrayList<>(routePath));

        startActivity(intent);
    }
}
