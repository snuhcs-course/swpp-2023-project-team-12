package com.example.runusandroid;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AccountApi {
    @POST("/account/signup/")
    Call<ResponseBody> postSignUpData(@Body SignUpData data);

    @POST("/account/login/")
    Call<ResponseBody> postLoginData(@Body AccountData data);

    @POST("/account/find_username/")
    Call<ResponseBody> findUsername(@Body EmailData emailData);

    @POST("/account/reset_password/")
    Call<ResponseBody> resetPassword(@Body ResetPasswordData data);

    @Multipart
    @POST("/account/profile_image/")
    Call<ImageResponse> uploadProfileImage(@Part MultipartBody.Part file);
}
