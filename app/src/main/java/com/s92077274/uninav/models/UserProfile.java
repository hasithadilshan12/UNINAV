package com.s92077274.uninav.models;

public class UserProfile {
    private String name;
    private String email;
    private String profilePictureBase64;

    // Default constructor required for Firestore deserialization
    public UserProfile() {
    }

    // Constructor to initialize user profile
    public UserProfile(String name, String email, String profilePictureBase64) {
        this.name = name;
        this.email = email;
        this.profilePictureBase64 = profilePictureBase64;
    }

    // Getters for user profile properties
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePictureBase64() {
        return profilePictureBase64;
    }

    // Setters for user profile properties
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfilePictureBase64(String profilePictureBase64) {
        this.profilePictureBase64 = profilePictureBase64;
    }
}


