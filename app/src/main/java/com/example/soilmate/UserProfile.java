package com.example.soilmate;

public class UserProfile {
    private String uid;
    private String email;

    // Required empty constructor for Firestore
    public UserProfile() {}

    public UserProfile(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }
}
