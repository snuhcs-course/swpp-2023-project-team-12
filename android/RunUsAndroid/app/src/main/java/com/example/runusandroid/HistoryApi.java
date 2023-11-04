package com.example.runusandroid;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface HistoryApi {
    @POST("/history/")
    Call<ResponseBody> postHistoryData(@Body HistoryData data);

    @GET("/history/recent-history/{username}/")
    Call<ResponseBody> getRecentHistoryData(@Path("username") String username);

    @POST("/history/group/")
    Call<ResponseBody> postGroupHistoryData(@Body GroupHistoryData data);
}
