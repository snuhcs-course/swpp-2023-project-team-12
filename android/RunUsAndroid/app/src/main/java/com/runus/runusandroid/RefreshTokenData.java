package com.runus.runusandroid;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenData {
    @SerializedName("refresh")
    String refreshToken;

    public RefreshTokenData(String refresh) {
        this.refreshToken = refresh;
    }
}
