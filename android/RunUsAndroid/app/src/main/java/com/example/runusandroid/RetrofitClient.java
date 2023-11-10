package com.example.runusandroid;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // 아래 코드를 수정 후 커밋하지 마시오!!
    private final static String BASE_URL = "http://ec2-3-36-116-64.ap-northeast-2.compute.amazonaws.com:3000";
    // 위 코드를 수정 후 커밋하지 마시오!!
    private static String authToken = null;
    private static Retrofit retrofit = null;

    private RetrofitClient() {
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder();
                    if (authToken != null && !authToken.isEmpty()) {
                        // 토큰이 있을 때만 헤더 추가
                        requestBuilder.header("Authorization", "Bearer " + authToken);
                    }

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

}