package com.example.runusandroid;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {
    private String username;
    @SerializedName("profile_image_url")
    private String profileImageUrl;

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
