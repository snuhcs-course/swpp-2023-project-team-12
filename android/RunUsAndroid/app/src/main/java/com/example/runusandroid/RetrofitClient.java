package com.example.runusandroid;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
//    private final static String BASE_URL = "http://ec2-43-201-102-185.ap-northeast-2.compute.amazonaws.com:8080/"; // Replace EC2 endpoint with yours

    private final static String BASE_URL = "http://192.168.56.1:8000/"; // Replace EC2 endpoint with yours
//
//    private final static String BASE_URL = "http://ec2-3-36-116-64.ap-northeast-2.compute.amazonaws.com:3000/";
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