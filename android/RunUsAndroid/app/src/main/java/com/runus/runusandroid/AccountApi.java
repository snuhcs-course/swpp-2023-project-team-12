package com.runus.runusandroid;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface AccountApi {
    @POST("/account/signup/")
    Call<ResponseBody> postSignUpData(@Body SignUpData data);

    @POST("/account/validate_id/")
    Call<ResponseBody> postIdValidationData(@Body IdValidationData data);

    @POST("/account/login/")
    Call<ResponseBody> postLoginData(@Body AccountData data);

    @POST("/account/find_username/")
    Call<ResponseBody> findUsername(@Body EmailData emailData);

    @POST("/account/reset_password/")
    Call<ResponseBody> sendMail(@Body SendMailData data);

    @PATCH("/account/reset_password/")
    Call<ResponseBody> resetPassword(@Body ResetPasswordData data);

    @Multipart
    @POST("/account/profile_image/")
    Call<ImageResponse> uploadProfileImage(@Part MultipartBody.Part file);

    @GET("/account/user_profile/{user_id}/")
    Call<UserProfileResponse> getUserProfile(@Path("user_id") String userId);

    @POST("/account/auth/refresh/")
    Call<ResponseBody> refreshToken(@Body RefreshTokenData data);
}
