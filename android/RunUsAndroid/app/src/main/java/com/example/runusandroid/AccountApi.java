package com.example.runusandroid;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AccountApi {
    @POST("/signup/")
    Call<ResponseBody> postSignUpData(@Body SignUpData data);

    @POST("/login/")
    Call<ResponseBody> postLoginData(@Body AccountData data);
}
