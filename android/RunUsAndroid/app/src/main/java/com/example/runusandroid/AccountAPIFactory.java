package com.example.runusandroid;

import static com.example.runusandroid.RetrofitClient.setAuthToken;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountAPIFactory {
    @SuppressLint("StaticFieldLeak")
    private static AccountAPIFactory factory;
    private final AccountApi accountApi;
    private AccountAPIFactory() {
        accountApi = RetrofitClient.getClient().create(AccountApi.class);
    }
    public static synchronized AccountAPIFactory getInstance() {
        if (factory == null) {
            factory = new AccountAPIFactory();
        }
        return factory;
    }

    public void refreshToken(Context context) {
        if(factory == null) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String refreshToken = sharedPreferences.getString("refresh_token", "");
        RefreshTokenData requestData = new RefreshTokenData(refreshToken);
        accountApi.refreshToken(requestData).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    JSONObject responseBody = null;
                    try {
                        //String responseBodyString = response.body().string();
                        responseBody = new JSONObject(response.body().string());
                        String accessToken = responseBody.getString("access");
                        setAuthToken(accessToken);
                        editor.putString("token", accessToken);
                        editor.apply();
                    } catch (JSONException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Retrofit", "Error: " + t.getMessage());
            }
        });
    }

}
