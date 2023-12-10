package com.runus.runusandroid;

import com.google.gson.annotations.SerializedName;

//Data class storing login data
public class AccountData {
    @SerializedName("username")
    String username;

    @SerializedName("password")
    String password;

    public AccountData(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

