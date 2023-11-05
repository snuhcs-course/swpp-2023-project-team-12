package com.example.runusandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextView idInput;
    private TextView passwordInput;
    private Button loginButton;
    private AccountApi accountApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        idInput = findViewById(R.id.IdInput);
        passwordInput = findViewById(R.id.PasswordInput);
        loginButton = findViewById(R.id.LoginBtn);

        accountApi = RetrofitClient.getClient().create(AccountApi.class);

        TextView signInMessage = findViewById(R.id.SignInMessage);

        String text = "아직 회원이 아니신가요? ";
        String signUpText = "회원가입";
        SpannableString spannableString = new SpannableString(text + signUpText);

        // 회원가입 부분에 색상 적용
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#4AA570"));
        spannableString.setSpan(colorSpan, text.length(), text.length() + signUpText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 회원가입 부분에 클릭 이벤트 적용
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // 회원가입 클릭시 SignUpActivity 시작
                Intent intent = new Intent(LoginActivity.this, SignUpStep1Activity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
            }
        };
        spannableString.setSpan(clickableSpan, text.length(), text.length() + signUpText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // TextView에 SpannableString 설정
        signInMessage.setText(spannableString);
        signInMessage.setMovementMethod(LinkMovementMethod.getInstance());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = idInput.getText().toString();
                String password = passwordInput.getText().toString();

                AccountData requestData = new AccountData(userName, password);

                accountApi.postLoginData(requestData).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d("Login", "Login Success");
                            Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                            JSONObject responseBody = null;
                            try {
                                String responseBodyString = response.body().string();
                                responseBody = new JSONObject(responseBodyString);
                                Log.d("response", responseBodyString);
                                JSONObject userObject = responseBody.getJSONObject("user");
                                Long user_id = userObject.getLong("user_id");
                                String username = userObject.getString("username");
                                String nickname = userObject.getString("nickname");
                                String email = userObject.getString("email");
                                String phone_num = userObject.getString("phone_num");
                                int gender = userObject.getInt("gender");
                                float height = (float) userObject.getDouble("height");
                                float weight = (float) userObject.getDouble("weight");
                                int age = userObject.getInt("age");

                                String token = responseBody.getJSONObject("jwt_token").getString("access_token");

                                // 로그인 성공 시 SharePreferences에 유저 정보 및 토큰 저장
                                SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong("userid", user_id);
                                editor.putString("token", token);
                                editor.putString("username", username);
                                editor.putString("nickname", nickname);
                                editor.putString("email", email);
                                editor.putString("phone_num", phone_num);
                                editor.putInt("gender", gender);
                                editor.putFloat("height", height);
                                editor.putFloat("weight", weight);
                                editor.putInt("age", age);
                                editor.apply();
                                Log.d("Login", sharedPreferences.getString("phone_num", null));
                                Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
                                startActivity(intent);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Log.d("Login", "Login Failed, Status Code : " + response.code());
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        Log.e("Retrofit", "Error: " + t.getMessage());
                    }
                });
            }
        });
    }
}