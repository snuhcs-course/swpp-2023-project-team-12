package com.example.runusandroid;

import com.google.gson.annotations.SerializedName;

public class IdValidationData {
    @SerializedName("username")
    String username;

    public IdValidationData(String username) {
        this.username = username;
    }
}
