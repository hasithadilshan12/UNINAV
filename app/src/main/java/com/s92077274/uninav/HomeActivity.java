package com.s92077274.uninav;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.s92077274.uninav.models.MapPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * HomeActivity displays the main dashboard of the UniNav app,
 * including recent searches, a mini-map, navigation options,
 * and now integrates a light sensor for ambient light detection.
 */
public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private TextView tvSearchHint;
    private ImageView ivProfile;
    private LinearLayout navHome, navSearch, navMap, navProfile;
    private View mapClickOverlay;

    private LinearLayout recentSearchesSection;
    private RecyclerView recyclerRecentSearches;
    private RecentSearchesAdapter recentSearchesAdapter;
    private List<MapPoint> recentSearchesList;

    private FirebaseAuth mAuth;
    private FirebaseUserManager firebaseUserManager;

    private static final String PREFS_NAME = "UniNavPrefs";
    private static final String KEY_RECENT_SEARCHES = "recent_searches";
    private static final int MAX_RECENT_SEARCHES = 5;
    private static final String TAG = "HomeActivity";
    private static final String KEY_MAP_TYPE = "map_type";

    private AppSensorManager appSensorManager;
    private long lastLightToastTime = 0;
    private static final long MIN_TOAST_INTERVAL = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        firebaseUserManager = FirebaseUserManager.getInstance();

        initViews();
        loadRecentSearches();
        setupRecyclerView();
        setClickListeners();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.homeMapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentSearches();
        updateRecentSearchesUI();
        applyMapTypeSetting();
        fetchAndDisplayUserProfilePicture();

        // Initialize sensor manager if needed
        if (appSensorManager == null) {
            appSensorManager = new AppSensorManager(this, new AppSensorManager.OnLightChangeListener() {
                @Override
                public void onLightChanged(float lux, String advice) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastLightToastTime > MIN_TOAST_INTERVAL) {
                        String message = String.format(Locale.getDefault(), "Light: %.2f lux. %s", lux, advice);
                        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_LONG).show();
                        Log.d(TAG, message);
                        lastLightToastTime = currentTime;
                    }
                }
            });
        }

        // Start sensor updates
        if (appSensorManager != null) {
            appSensorManager.start();
        } else {
            Log.w(TAG, "Light sensor not available on this device");
            Toast.makeText(this, "Light sensor not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop light sensor updates on pause to save battery
        if (appSensorManager != null) {
            appSensorManager.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up sensor manager to prevent memory leaks
        if (appSensorManager != null) {
            appSensorManager.stop();
            appSensorManager = null;
        }
    }

    /**
     * Initializes all UI components from the layout.
     */
    private void initViews() {
        tvSearchHint = findViewById(R.id.tvSearchHint);
        ivProfile = findViewById(R.id.ivProfile);
        navHome = findViewById(R.id.navHome);
        navSearch = findViewById(R.id.navSearch);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);
        mapClickOverlay = findViewById(R.id.mapClickOverlay);
        recentSearchesSection = findViewById(R.id.recentSearchesSection);
        recyclerRecentSearches = findViewById(R.id.recyclerRecentSearches);
    }

    /**
     * Sets up click listeners for all interactive UI elements.
     */
    private void setClickListeners() {
        tvSearchHint.setOnClickListener(v -> {
            Intent searchIntent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(searchIntent);
        });

        ivProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        mapClickOverlay.setOnClickListener(v -> {
            Intent mapIntent = new Intent(HomeActivity.this, MapActivity.class);
            startActivity(mapIntent);
        });

        navHome.setOnClickListener(v -> Toast.makeText(HomeActivity.this, "Already on Home page", Toast.LENGTH_SHORT).show());
        navSearch.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SearchActivity.class)));
        navMap.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MapActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
    }

    /**
     * Callback method called when the Google Map is ready to be used.
     * Configures map UI, sets default camera position, and adds a marker.
     * @param map The GoogleMap object.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        applyMapTypeSetting();

        // Configure map UI settings
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Set default camera position to OUSL campus center and add a marker
        LatLng ouslCampusCenter = new LatLng(6.883019826740543, 79.88670615788185);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ouslCampusCenter, 15f));
        googleMap.addMarker(new MarkerOptions()
                .position(ouslCampusCenter)
                .title("The Open University of Sri Lanka")
                .snippet("Your Campus"));
    }

    /**
     * Applies the map type setting (Normal or Satellite) loaded from SharedPreferences.
     */
    private void applyMapTypeSetting() {
        if (googleMap != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int mapType = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMapType(mapType);
            Log.d(TAG, "Applied map type: " + (mapType == GoogleMap.MAP_TYPE_NORMAL ? "Normal" : "Satellite"));
        }
    }

    /**
     * Fetches the user's profile picture Base64 string from Firestore and displays it.
     * If no picture is set or an error occurs, a default placeholder is used.
     */
    private void fetchAndDisplayUserProfilePicture() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            firebaseUserManager.getUserProfile(user.getUid())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String base64Image = task.getResult().getString("profilePictureBase64");

                            if (base64Image != null && !base64Image.isEmpty()) {
                                try {
                                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                                    if (bitmap != null) {
                                        ivProfile.setImageBitmap(bitmap);
                                    } else {
                                        Log.e(TAG, "Bitmap decode returned null.");
                                        ivProfile.setImageResource(R.drawable.ic_profile);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error decoding Base64 image", e);
                                    ivProfile.setImageResource(R.drawable.ic_profile);
                                }
                            } else {
                                Log.d(TAG, "No profile picture set.");
                                ivProfile.setImageResource(R.drawable.ic_profile);
                            }
                        } else {
                            Log.e(TAG, "Failed to fetch user profile document", task.getException());
                            ivProfile.setImageResource(R.drawable.ic_profile);
                        }
                    });
        }
    }

    /**
     * Sets up the RecyclerView for displaying recent searches.
     */
    private void setupRecyclerView() {
        recentSearchesList = new ArrayList<>();
        recentSearchesAdapter = new RecentSearchesAdapter(recentSearchesList, location -> {
            Log.d(TAG, "Recent search item clicked: " + location.name +
                    " X: " + location.x + ", Y: " + location.y);

            // Navigate to LocationShowActivity with the selected location details
            Intent intent = new Intent(HomeActivity.this, LocationShowActivity.class);
            intent.putExtra("location_name", location.name);
            intent.putExtra("location_lat", location.x);
            intent.putExtra("location_lng", location.y);
            startActivity(intent);
        });

        recyclerRecentSearches.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecentSearches.setAdapter(recentSearchesAdapter);
        updateRecentSearchesUI();
    }

    /**
     * Adds a location to the list of recent searches, maintaining a maximum limit.
     * Oldest searches are removed if the list exceeds the limit.
     * @param context The application context.
     * @param location The MapPoint to be added as a recent search.
     */
    public static void addRecentSearch(Context context, MapPoint location) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_RECENT_SEARCHES, null);
        Type type = new TypeToken<List<MapPoint>>() {}.getType();
        List<MapPoint> currentList = (json == null) ? new ArrayList<>() : gson.fromJson(json, type);

        Log.d(TAG, "Adding recent search: " + location.name +
                " X: " + location.x + ", Y: " + location.y);

        // Remove if already exists to add it to the top
        currentList.removeIf(p -> p.name.equals(location.name));
        currentList.add(0, location); // Add to the beginning

        // Keep only the most recent searches
        if (currentList.size() > MAX_RECENT_SEARCHES) {
            currentList = currentList.subList(0, MAX_RECENT_SEARCHES);
        }

        String updatedJson = gson.toJson(currentList);
        prefs.edit().putString(KEY_RECENT_SEARCHES, updatedJson).apply();
    }

    /**
     * Loads the list of recent searches from SharedPreferences.
     */
    private void loadRecentSearches() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_RECENT_SEARCHES, null);
        Type type = new TypeToken<List<MapPoint>>() {}.getType();
        recentSearchesList = (json == null) ? new ArrayList<>() : gson.fromJson(json, type);
        if (recentSearchesList == null) { // Ensure list is not null after deserialization
            recentSearchesList = new ArrayList<>();
        }
        for (MapPoint p : recentSearchesList) {
            Log.d(TAG, "Loaded recent search: " + p.name +
                    " X: " + p.x + ", Y: " + p.y);
        }
    }

    /**
     * Updates the visibility of the recent searches section and refreshes the RecyclerView.
     */
    private void updateRecentSearchesUI() {
        if (recentSearchesList.isEmpty()) {
            recentSearchesSection.setVisibility(View.GONE);
        } else {
            recentSearchesSection.setVisibility(View.VISIBLE);
            recentSearchesAdapter.updateData(recentSearchesList);
        }
    }
}