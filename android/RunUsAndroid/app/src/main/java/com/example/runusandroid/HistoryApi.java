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

    @GET("/history/recent-history/{user_id}/")
    Call<ResponseBody> getRecentHistoryData(@Path("user_id") Long user_id);

    @POST("/history/group/")
    Call<ResponseBody> postGroupHistoryData(@Body GroupHistoryData data);

    @GET("/history/monthly/{year}/{month}/{user_id}")
    Call<HistoryDataforRendering> getMonthlyData(@Path("year") int year, @Path("month") int month, @Path("user_id") long userId);

    @GET("/history/daily/{year}/{month}/{day}/{user_id}")
    Call<HistoryDataforRendering> getDailyData(@Path("year") int year, @Path("month") int month, @Path("day") int day, @Path("user_id") long userId);

}
