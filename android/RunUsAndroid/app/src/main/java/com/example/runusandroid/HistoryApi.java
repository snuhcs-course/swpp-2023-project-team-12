package com.example.runusandroid;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HistoryApi {
    @POST("/history/")
    Call<ResponseBody> postHistoryData(@Body HistoryData data);

    @POST("/history/group/")
    Call<ResponseBody> postGroupHistoryData(@Body GroupHistoryData data);
}
