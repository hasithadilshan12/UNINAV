package com.s92077274.uninav; // Or com.s92077274.uninav.data;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.s92077274.uninav.models.UserProfile; // Ensure this import path is correct

import java.util.HashMap;
import java.util.Map;

/**
 * Manages user profile data interactions with Firebase Firestore.
 * This class centralizes operations like saving, fetching, and updating UserProfile objects.
 */
public class FirebaseUserManager {

    private static final String TAG = "FirebaseUserManager";
    private static final String COLLECTION_USERS = "users";

    private FirebaseFirestore db;

    // Singleton instance to ensure only one instance of the manager exists
    private static FirebaseUserManager instance;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private FirebaseUserManager() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Returns the singleton instance of FirebaseUserManager.
     * @return The singleton instance.
     */
    public static synchronized FirebaseUserManager getInstance() {
        if (instance == null) {
            instance = new FirebaseUserManager();
        }
        return instance;
    }

    /**
     * Saves a new user profile to Firestore.
     * This method is typically called after a user successfully registers via Firebase Authentication.
     *
     * @param userId The UID of the authenticated user.
     * @param email The user's email.
     * @param name The user's name.
     * @return A Task that completes when the data is written to Firestore.
     */
    public Task<Void> saveNewUser(String userId, String email, String name) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("name", name);
        user.put("userId", userId);
        user.put("registrationDate", System.currentTimeMillis());
        user.put("isActive", true);
        user.put("profilePictureBase64", ""); // Initialize with empty string for profile picture

        return db.collection(COLLECTION_USERS).document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User data for " + userId + " successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing user document for " + userId, e));
    }

    /**
     * Fetches a user's profile from Firestore.
     *
     * @param userId The UID of the user whose profile is to be fetched.
     * @return A Task that, when complete, contains the DocumentSnapshot.
     */
    public Task<DocumentSnapshot> getUserProfile(String userId) {
        return db.collection(COLLECTION_USERS).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "User profile for " + userId + " fetched successfully.");
                    } else {
                        Log.d(TAG, "User profile for " + userId + " does not exist.");
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error fetching user profile for " + userId, e));
    }

    /**
     * Updates a specific field in a user's profile.
     *
     * @param userId The UID of the user whose profile is to be updated.
     * @param updates A Map containing the fields to update and their new values.
     * @return A Task that completes when the update is applied.
     */
    public Task<Void> updateProfileField(String userId, Map<String, Object> updates) {
        return db.collection(COLLECTION_USERS).document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile for " + userId + " updated successfully."))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating user profile for " + userId, e));
    }

    /**
     * Updates the user's name in Firestore.
     *
     * @param userId The UID of the user.
     * @param newName The new name for the user.
     * @return A Task that completes when the name is updated.
     */
    public Task<Void> updateUserName(String userId, String newName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        return updateProfileField(userId, updates);
    }

    /**
     * Updates the user's profile picture Base64 string in Firestore.
     *
     * @param userId The UID of the user.
     * @param base64Image The Base64 string of the new profile picture.
     * @return A Task that completes when the picture is updated.
     */
    public Task<Void> updateProfilePicture(String userId, String base64Image) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("profilePictureBase64", base64Image);
        return updateProfileField(userId, updates);
    }

    /**
     * Clears the user's profile picture in Firestore (sets it to an empty string).
     *
     * @param userId The UID of the user.
     * @return A Task that completes when the profile picture is cleared.
     */
    public Task<Void> deleteProfilePicture(String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("profilePictureBase64", "");
        return updateProfileField(userId, updates);
    }
}

