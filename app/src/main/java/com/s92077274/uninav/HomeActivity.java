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
// import com.google.firebase.firestore.DocumentReference; // Removed
// import com.google.firebase.firestore.DocumentSnapshot; // Removed
// import com.google.firebase.firestore.FirebaseFirestore; // Removed
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.s92077274.uninav.models.MapPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private FirebaseUserManager firebaseUserManager; // ⭐ NEW: Instance of our custom manager ⭐

    private static final String PREFS_NAME = "UniNavPrefs";
    private static final String KEY_RECENT_SEARCHES = "recent_searches";
    private static final int MAX_RECENT_SEARCHES = 5;
    private static final String TAG = "HomeActivity";
    private static final String KEY_MAP_TYPE = "map_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        firebaseUserManager = FirebaseUserManager.getInstance(); // ⭐ Initialize our custom manager ⭐

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
        // Fetch and display the user's profile picture on resume
        fetchAndDisplayUserProfilePicture();
    }

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

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        applyMapTypeSetting();

        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        LatLng ouslCampusCenter = new LatLng(6.883019826740543, 79.88670615788185);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ouslCampusCenter, 15f));
        googleMap.addMarker(new MarkerOptions()
                .position(ouslCampusCenter)
                .title("The Open University of Sri Lanka")
                .snippet("Your Campus"));
    }

    private void applyMapTypeSetting() {
        if (googleMap != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int mapType = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setMapType(mapType);
            Log.d(TAG, "Applied map type: " + (mapType == GoogleMap.MAP_TYPE_NORMAL ? "Normal" : "Satellite"));
        }
    }

    /**
     * Fetches the user's profile picture URL from Firestore and displays it.
     */
    private void fetchAndDisplayUserProfilePicture() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // ⭐ MODIFIED: Use FirebaseUserManager to fetch user profile ⭐
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
                                        Log.e("HomeActivity", "Bitmap decode returned null.");
                                        ivProfile.setImageResource(R.drawable.ic_profile);
                                    }
                                } catch (Exception e) {
                                    Log.e("HomeActivity", "Error decoding Base64 image", e);
                                    ivProfile.setImageResource(R.drawable.ic_profile);
                                }
                            } else {
                                Log.d("HomeActivity", "No profile picture set.");
                                ivProfile.setImageResource(R.drawable.ic_profile);
                            }
                        } else {
                            Log.e("HomeActivity", "Failed to fetch document", task.getException());
                            ivProfile.setImageResource(R.drawable.ic_profile);
                        }
                    });
        }
    }



    private void setupRecyclerView() {
        recentSearchesList = new ArrayList<>();
        recentSearchesAdapter = new RecentSearchesAdapter(recentSearchesList, location -> {
            Log.d(TAG, "Recent search item clicked: " + location.name +
                    " X: " + location.x + ", Y: " + location.y);

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

    public static void addRecentSearch(Context context, MapPoint location) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_RECENT_SEARCHES, null);
        Type type = new TypeToken<List<MapPoint>>() {}.getType();
        List<MapPoint> currentList = (json == null) ? new ArrayList<>() : gson.fromJson(json, type);

        Log.d(TAG, "Adding recent search: " + location.name +
                " X: " + location.x + ", Y: " + location.y);

        currentList.removeIf(p -> p.name.equals(location.name));
        currentList.add(0, location);

        if (currentList.size() > MAX_RECENT_SEARCHES) {
            currentList = currentList.subList(0, MAX_RECENT_SEARCHES);
        }

        String updatedJson = gson.toJson(currentList);
        prefs.edit().putString(KEY_RECENT_SEARCHES, updatedJson).apply();
    }

    private void loadRecentSearches() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(KEY_RECENT_SEARCHES, null);
        Type type = new TypeToken<List<MapPoint>>() {}.getType();
        recentSearchesList = (json == null) ? new ArrayList<>() : gson.fromJson(json, type);
        if (recentSearchesList == null) {
            recentSearchesList = new ArrayList<>();
        }
        for (MapPoint p : recentSearchesList) {
            Log.d(TAG, "Loaded recent search: " + p.name +
                    " X: " + p.x + ", Y: " + p.y);
        }
    }

    private void updateRecentSearchesUI() {
        if (recentSearchesList.isEmpty()) {
            recentSearchesSection.setVisibility(View.GONE);
        } else {
            recentSearchesSection.setVisibility(View.VISIBLE);
            recentSearchesAdapter.updateData(recentSearchesList);
        }
    }
}
