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
    //로컬에서 장고 서버 여는 법(에뮬레이터, 피지컬 디바이스)
    //django settings.py에 현재 ip주소 입력
    //서버 켤 때 python manage.py runserver 0.0.0.0:8000 커맨드로 실행
    //Base_URL ip 주소 넣는 부분에 현재 ip주소 넣고 실행
    //private final static String BASE_URL = "http://10.22.84.103:8000/";
    //for local : 본인 ip address 삽입
    //서버 실행시 python manage.py runserver 0.0.0.0:8000
    //private final static String BASE_URL = "http://192.168.0.4:8000/";

    private static String authToken = null;
    private static Retrofit retrofit = null;

    private RetrofitClient() {
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static void resetAuthToken() {
        authToken = null;
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