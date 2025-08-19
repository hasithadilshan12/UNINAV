package com.s92077274.uninav;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

import com.s92077274.uninav.models.UserProfile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button btnLogout;
    private ImageView btnBack;
    private LinearLayout llProfileItem, llSettingsItem, llAboutUsItem, llHelpItem;
    private LinearLayout navHome, navSearch, navMap, navProfile;

    private UserProfile currentUserProfile;
    private Uri selectedImageUri;

    private AlertDialog profileDialog;
    private TextInputEditText etDialogName, etDialogEmail;
    private CircleImageView ivDialogProfilePic;
    private ImageView btnEditProfilePic;
    private Button btnSaveProfile;
    private TextView tvDeleteProfilePic;

    private static final String PREFS_NAME = "UniNavPrefs";
    private static final String KEY_MAP_TYPE = "map_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setClickListeners();
        fetchUserProfile();
    }

    // Initialize UI components
    private void initViews() {
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);
        llProfileItem = findViewById(R.id.llProfileItem);
        llSettingsItem = findViewById(R.id.llSettingsItem);
        llAboutUsItem = findViewById(R.id.llAboutUsItem);
        llHelpItem = findViewById(R.id.llHelpItem);

        navHome = findViewById(R.id.navHome);
        navSearch = findViewById(R.id.navSearch);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);
    }

    // Set up click listeners for various UI elements
    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        llProfileItem.setOnClickListener(v -> showProfileDialog());
        llSettingsItem.setOnClickListener(v -> showSettingsDialog());
        llAboutUsItem.setOnClickListener(v -> showAboutUsDialog());
        llHelpItem.setOnClickListener(v -> showHelpDialog());
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            Intent logoutIntent = new Intent(ProfileActivity.this, LoginActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
        });

        // Navigation bar click listeners
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
            Toast.makeText(ProfileActivity.this, "Already on Profile page", Toast.LENGTH_SHORT).show();
        });
    }

    // Fetches the current user's profile data from Firestore
    private void fetchUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        currentUserProfile = new UserProfile(
                                document.getString("name"),
                                document.getString("email"),
                                document.getString("profilePictureBase64")
                        );
                    } else {
                        Toast.makeText(ProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                        createDefaultUserProfile(user.getUid(), user.getEmail());
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to fetch user data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // If no user is logged in, redirect to LoginActivity
            Intent logoutIntent = new Intent(ProfileActivity.this, LoginActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
        }
    }

    // Creates a default user profile in Firestore if one doesn't exist
    private void createDefaultUserProfile(String uid, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "New User");
        user.put("email", email);
        user.put("profilePictureBase64", "");

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Default profile created.", Toast.LENGTH_SHORT).show();
                    fetchUserProfile(); // Fetch newly created profile
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to create default profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Displays the profile edit dialog
    private void showProfileDialog() {
        if (currentUserProfile == null) {
            Toast.makeText(this, "Profile data not loaded yet. Please wait.", Toast.LENGTH_SHORT).show();
            fetchUserProfile(); // Try fetching again
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_profile_edit, null);
        builder.setView(dialogView);

        // Initialize dialog UI components
        etDialogName = dialogView.findViewById(R.id.etDialogName);
        etDialogEmail = dialogView.findViewById(R.id.etDialogEmail);
        ivDialogProfilePic = dialogView.findViewById(R.id.ivDialogProfilePic);
        btnEditProfilePic = dialogView.findViewById(R.id.btnEditProfilePic);
        btnSaveProfile = dialogView.findViewById(R.id.btnSaveProfile);
        tvDeleteProfilePic = dialogView.findViewById(R.id.tvDeleteProfilePic);

        // Populate dialog fields with current profile data
        etDialogName.setText(currentUserProfile.getName());
        etDialogEmail.setText(currentUserProfile.getEmail());
        etDialogEmail.setEnabled(false); // Email is not editable

        loadProfileImageFromBase64(currentUserProfile.getProfilePictureBase64());

        // Show/hide delete profile picture option
        if (currentUserProfile.getProfilePictureBase64() != null && !currentUserProfile.getProfilePictureBase64().isEmpty()) {
            tvDeleteProfilePic.setVisibility(View.VISIBLE);
        } else {
            tvDeleteProfilePic.setVisibility(View.GONE);
        }

        // Set dialog button listeners
        btnEditProfilePic.setOnClickListener(v -> checkPermissionAndPickImage());
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        tvDeleteProfilePic.setOnClickListener(v -> deleteProfilePicture());

        profileDialog = builder.create();
        profileDialog.show();
    }

    // Loads and displays profile image from a Base64 string
    private void loadProfileImageFromBase64(String base64) {
        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ivDialogProfilePic.setImageBitmap(bmp);
            } catch (Exception e) {
                ivDialogProfilePic.setImageResource(R.drawable.ic_profile_placeholder); // Fallback on error
            }
        } else {
            ivDialogProfilePic.setImageResource(R.drawable.ic_profile_placeholder); // Default placeholder
        }
    }

    // Checks for necessary permissions before opening image chooser
    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImageChooser();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImageChooser();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser(); // Open chooser if permission granted
            } else {
                Toast.makeText(this, "Permission denied to access media files.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Opens the image selection activity
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(ivDialogProfilePic); // Display selected image
            tvDeleteProfilePic.setVisibility(View.VISIBLE); // Show delete option
            Toast.makeText(this, "Image selected. Click 'Save Changes' to upload.", Toast.LENGTH_LONG).show();
        }
    }

    // Saves profile changes (name and/or profile picture) to Firestore
    private void saveProfileChanges() {
        String newName = etDialogName.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newName)) {
            etDialogName.setError("Name cannot be empty");
            etDialogName.requestFocus();
            return;
        }

        boolean nameChanged = !newName.equals(currentUserProfile.getName());
        boolean imageSelected = (selectedImageUri != null);

        if (!nameChanged && !imageSelected) {
            Toast.makeText(this, "No changes to save.", Toast.LENGTH_SHORT).show();
            if (profileDialog != null) profileDialog.dismiss();
            return;
        }

        Toast.makeText(this, "Saving changes...", Toast.LENGTH_SHORT).show();

        if (imageSelected) {
            new ImageToBase64Task(user.getUid(), newName).execute(selectedImageUri); // Convert and upload image
        } else {
            updateNameOnly(user.getUid(), newName); // Update name only
        }
    }

    // Updates only the user's name in Firestore
    private void updateNameOnly(String userId, String newName) {
        DocumentReference userDocRef = db.collection("users").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
                    fetchUserProfile(); // Refresh profile data
                    if (profileDialog != null) profileDialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to update name: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // AsyncTask to convert image URI to Base64 and then update profile
    private class ImageToBase64Task extends AsyncTask<Uri, Void, String> {
        private final String userId;
        private final String newName;

        ImageToBase64Task(String userId, String newName) {
            this.userId = userId;
            this.newName = newName;
        }

        @Override
        protected String doInBackground(Uri... uris) {
            Uri imageUri = uris[0];
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Compress image
                byte[] imageBytes = baos.toByteArray();
                baos.close();

                return Base64.encodeToString(imageBytes, Base64.DEFAULT); // Encode to Base64
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String base64Image) {
            if (base64Image != null) {
                DocumentReference userDocRef = db.collection("users").document(userId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("profilePictureBase64", base64Image);
                if (!newName.equals(currentUserProfile.getName())) {
                    updates.put("name", newName);
                }
                userDocRef.update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            fetchUserProfile();
                            if (profileDialog != null) profileDialog.dismiss();
                            selectedImageUri = null; // Clear selected URI after upload
                        })
                        .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to encode image.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Deletes the user's profile picture from Firestore
    private void deleteProfilePicture() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || currentUserProfile == null || TextUtils.isEmpty(currentUserProfile.getProfilePictureBase64())) {
            Toast.makeText(this, "No profile picture to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog before deleting
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Profile Picture")
                .setMessage("Are you sure you want to delete your profile picture?")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, which) -> {
                    DocumentReference userDocRef = db.collection("users").document(user.getUid());
                    userDocRef.update("profilePictureBase64", "") // Clear the Base64 string
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ProfileActivity.this, "Profile picture deleted.", Toast.LENGTH_SHORT).show();
                                selectedImageUri = null;
                                fetchUserProfile();
                                if (profileDialog != null) profileDialog.dismiss();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to clear picture in Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    // Displays the notifications dialog (removed as per previous instruction)
    private void showNotificationsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_notifications, null);
        builder.setView(dialogView);

        Button btnNotificationsClose = dialogView.findViewById(R.id.btnNotificationsClose);
        AlertDialog dialog = builder.create();
        btnNotificationsClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Displays the settings dialog (for map type selection)
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);

        Switch switchSatelliteMode = dialogView.findViewById(R.id.switchSatelliteMode);

        // Set switch state based on saved map type
        boolean isSatelliteEnabled = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL) == GoogleMap.MAP_TYPE_SATELLITE;
        switchSatelliteMode.setChecked(isSatelliteEnabled);

        // Listen for switch changes to save map type setting
        switchSatelliteMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveMapTypeSetting(isChecked);
            if (isChecked) {
                Toast.makeText(ProfileActivity.this, "Satellite view enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Normal map view enabled", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnSettingsClose = dialogView.findViewById(R.id.btnSettingsClose);
        AlertDialog dialog = builder.create();
        btnSettingsClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Saves the selected map type setting
    private void saveMapTypeSetting(boolean isSatellite) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int mapType = isSatellite ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL;
        editor.putInt(KEY_MAP_TYPE, mapType);
        editor.apply();
    }

    // Displays the "About Us" dialog
    private void showAboutUsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_about_us, null);
        builder.setView(dialogView);

        TextView tvAboutUsContent = dialogView.findViewById(R.id.tvAboutUsContent);
        tvAboutUsContent.setText(getString(R.string.about_us_text)); // Set content from string resource

        Button btnAboutUsClose = dialogView.findViewById(R.id.btnAboutUsClose);
        AlertDialog dialog = builder.create();
        btnAboutUsClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Displays the "Help" dialog
    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_help, null);
        builder.setView(dialogView);

        Button btnHelpClose = dialogView.findViewById(R.id.btnHelpClose);
        AlertDialog dialog = builder.create();
        btnHelpClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
