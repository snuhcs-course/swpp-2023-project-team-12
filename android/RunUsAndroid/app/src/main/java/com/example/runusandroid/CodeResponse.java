package com.example.runusandroid;

import com.google.gson.annotations.SerializedName;

public class CodeResponse {
    @SerializedName("code")
    private int code;
    public int getCode() {
        return code;
    }
}
