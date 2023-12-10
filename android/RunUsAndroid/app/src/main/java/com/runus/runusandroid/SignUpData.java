package com.runus.runusandroid;

import com.google.gson.annotations.SerializedName;

//Data class storing Sign up data
public class SignUpData {
    @SerializedName("username")
    String username;
    @SerializedName("password")
    String password;
    @SerializedName("nickname")
    String nickname;
    @SerializedName("email")
    String email;
    @SerializedName("phone_num")
    String phone_number;
    @SerializedName("gender")
    int gender;
    @SerializedName("height")
    float height;
    @SerializedName("weight")
    float weight;
    @SerializedName("age")
    int age;


    public SignUpData(
            String username,
            String password,
            String nickname,
            String email,
            String phone_number,
            int gender,
            float height,
            float weight,
            int age
    ) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.phone_number = phone_number;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.age = age;
    }
}
