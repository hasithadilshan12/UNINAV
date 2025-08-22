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
    private FirebaseUserManager firebaseUserManager;

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
        firebaseUserManager = FirebaseUserManager.getInstance();

        initViews();
        setClickListeners();
        fetchUserProfile();
    }

    /**
     * Initializes all views from the activity_profile.xml layout file.
     */
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

    /**
     * Sets up click listeners for all interactive UI elements.
     */
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

    /**
     * Fetches the current user's profile from Firebase. If no profile exists, a default one is created.
     */
    private void fetchUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            firebaseUserManager.getUserProfile(user.getUid())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                currentUserProfile = task.getResult().toObject(UserProfile.class);
                            } else {
                                Toast.makeText(ProfileActivity.this, "User data not found. Creating default profile.", Toast.LENGTH_SHORT).show();
                                createDefaultUserProfile(user.getUid(), user.getEmail());
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to fetch user data: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // If the user is not logged in, navigate back to the login screen.
            Intent logoutIntent = new Intent(ProfileActivity.this, LoginActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
        }
    }

    /**
     * Creates a new default user profile in Firestore.
     * @param uid The user's unique ID.
     * @param email The user's email address.
     */
    private void createDefaultUserProfile(String uid, String email) {
        firebaseUserManager.saveNewUser(uid, email, "New User")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Default profile created.", Toast.LENGTH_SHORT).show();
                    fetchUserProfile();
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to create default profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Displays a dialog for editing the user's profile information.
     */
    private void showProfileDialog() {
        if (currentUserProfile == null) {
            Toast.makeText(this, "Profile data not loaded yet. Please wait.", Toast.LENGTH_SHORT).show();
            fetchUserProfile();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_profile_edit, null);
        builder.setView(dialogView);

        etDialogName = dialogView.findViewById(R.id.etDialogName);
        etDialogEmail = dialogView.findViewById(R.id.etDialogEmail);
        ivDialogProfilePic = dialogView.findViewById(R.id.ivDialogProfilePic);
        btnEditProfilePic = dialogView.findViewById(R.id.btnEditProfilePic);
        btnSaveProfile = dialogView.findViewById(R.id.btnSaveProfile);
        tvDeleteProfilePic = dialogView.findViewById(R.id.tvDeleteProfilePic);

        etDialogName.setText(currentUserProfile.getName());
        etDialogEmail.setText(currentUserProfile.getEmail());
        etDialogEmail.setEnabled(false); // Email cannot be edited

        loadProfileImageFromBase64(currentUserProfile.getProfilePictureBase64());

        if (currentUserProfile.getProfilePictureBase64() != null && !currentUserProfile.getProfilePictureBase64().isEmpty()) {
            tvDeleteProfilePic.setVisibility(View.VISIBLE);
        } else {
            tvDeleteProfilePic.setVisibility(View.GONE);
        }

        btnEditProfilePic.setOnClickListener(v -> checkPermissionAndPickImage());
        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        tvDeleteProfilePic.setOnClickListener(v -> deleteProfilePicture());

        profileDialog = builder.create();
        profileDialog.show();
    }

    /**
     * Loads a profile image from a Base64 string and displays it in the ImageView.
     * @param base64 The Base64 encoded string of the image.
     */
    private void loadProfileImageFromBase64(String base64) {
        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ivDialogProfilePic.setImageBitmap(bmp);
            } catch (Exception e) {
                ivDialogProfilePic.setImageResource(R.drawable.ic_profile_placeholder);
            }
        } else {
            ivDialogProfilePic.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    /**
     * Checks for necessary read media permissions before allowing the user to pick an image.
     */
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

    /**
     * Handles the result of the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            } else {
                Toast.makeText(this, "Permission denied to access media files.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Opens the system's image chooser to select a profile picture.
     */
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result of the image chooser activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(ivDialogProfilePic);
            tvDeleteProfilePic.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Image selected. Click 'Save Changes' to upload.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Saves the profile changes (name and/or profile picture) to Firebase.
     */
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

        boolean nameChanged = (currentUserProfile != null && !newName.equals(currentUserProfile.getName()));
        boolean imageSelected = (selectedImageUri != null);

        if (!nameChanged && !imageSelected) {
            Toast.makeText(this, "No changes to save.", Toast.LENGTH_SHORT).show();
            if (profileDialog != null) profileDialog.dismiss();
            return;
        }

        Toast.makeText(this, "Saving changes...", Toast.LENGTH_SHORT).show();

        if (imageSelected) {
            new ImageToBase64Task(user.getUid(), newName, nameChanged).execute(selectedImageUri);
        } else {
            if (nameChanged) {
                firebaseUserManager.updateUserName(user.getUid(), newName)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProfileActivity.this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
                            fetchUserProfile();
                            if (profileDialog != null) profileDialog.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to update name: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }
    }

    /**
     * AsyncTask to handle the conversion of an image from a URI to a Base64 string in the background.
     */
    private class ImageToBase64Task extends AsyncTask<Uri, Void, String> {
        private final String userId;
        private final String newName;
        private final boolean nameChanged;

        ImageToBase64Task(String userId, String newName, boolean nameChanged) {
            this.userId = userId;
            this.newName = newName;
            this.nameChanged = nameChanged;
        }

        @Override
        protected String doInBackground(Uri... uris) {
            Uri imageUri = uris[0];
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) inputStream.close();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageBytes = baos.toByteArray();
                baos.close();

                return Base64.encodeToString(imageBytes, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String base64Image) {
            if (base64Image != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("profilePictureBase64", base64Image);
                if (nameChanged) {
                    updates.put("name", newName);
                }

                firebaseUserManager.updateProfileField(userId, updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            fetchUserProfile();
                            if (profileDialog != null) profileDialog.dismiss();
                            selectedImageUri = null;
                        })
                        .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to encode image.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Deletes the user's profile picture from Firebase.
     */
    private void deleteProfilePicture() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || currentUserProfile == null || TextUtils.isEmpty(currentUserProfile.getProfilePictureBase64())) {
            Toast.makeText(this, "No profile picture to delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Profile Picture")
                .setMessage("Are you sure you want to delete your profile picture?")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, which) -> {
                    firebaseUserManager.deleteProfilePicture(user.getUid())
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

    /**
     * Displays a dialog for changing map settings.
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);

        Switch switchSatelliteMode = dialogView.findViewById(R.id.switchSatelliteMode);

        boolean isSatelliteEnabled = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL) == GoogleMap.MAP_TYPE_SATELLITE;
        switchSatelliteMode.setChecked(isSatelliteEnabled);

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

    /**
     * Saves the selected map type to SharedPreferences.
     * @param isSatellite True if satellite view is enabled, false otherwise.
     */
    private void saveMapTypeSetting(boolean isSatellite) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int mapType = isSatellite ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL;
        editor.putInt(KEY_MAP_TYPE, mapType);
        editor.apply();
    }

    /**
     * Displays an "About Us" dialog with information about the app.
     */
    private void showAboutUsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_about_us, null);
        builder.setView(dialogView);

        TextView tvAboutUsContent = dialogView.findViewById(R.id.tvAboutUsContent);
        tvAboutUsContent.setText(getString(R.string.about_us_text));

        Button btnAboutUsClose = dialogView.findViewById(R.id.btnAboutUsClose);
        AlertDialog dialog = builder.create();
        btnAboutUsClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Displays a "Help" dialog for user assistance.
     */
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