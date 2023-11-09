package com.example.runusandroid;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // 아래 코드를 수정 후 커밋하지 마시오!!
    private final static String BASE_URL = "http://172.30.1.8:8000";
    // 위 코드를 수정 후 커밋하지 마시오!!

    private static Retrofit retrofit = null;

    private RetrofitClient() {
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}