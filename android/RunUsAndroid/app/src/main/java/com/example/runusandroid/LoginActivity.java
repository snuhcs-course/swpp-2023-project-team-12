package com.example.runusandroid;

import static com.example.runusandroid.RetrofitClient.resetAuthToken;
import static com.example.runusandroid.RetrofitClient.setAuthToken;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
    private long loginButtonLastClickTime = 0;

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

        // 회원가입 부분에 클릭 이벤트 적용
        ClickableSpan clickableSpanforSignUp = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // 회원가입 클릭시 SignUpActivity 시작
                Intent intent = new Intent(LoginActivity.this, SignUpStep1Activity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#4AA570"));
            }
        };
        spannableString.setSpan(clickableSpanforSignUp, text.length(), text.length() + signUpText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        signInMessage.setText(spannableString);
        signInMessage.setMovementMethod(LinkMovementMethod.getInstance());

        TextView forgotIdText = findViewById(R.id.ForgotIdText);
        String fullText = "아이디를 잊어버리셨나요? 아이디 찾기";
        SpannableString ss = new SpannableString(fullText);
        forgotIdText.setMovementMethod(LinkMovementMethod.getInstance());

        // '아이디 찾기' 부분에 클릭 이벤트와 색상 변경 추가
        ClickableSpan clickableSpanforFindId = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                // 아이디 찾기 액티비티로 이동
                Intent intent = new Intent(LoginActivity.this, FindIdActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#4AA570"));
            }
        };
        ss.setSpan(clickableSpanforFindId, 14, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        forgotIdText.setText(ss);
        forgotIdText.setMovementMethod(LinkMovementMethod.getInstance());

        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        SpannableString ssforpw = new SpannableString("비밀번호를 잊어버리셨나요? 비밀번호 찾기");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                // ResetPasswordActivity로 이동
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity1.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#4AA570")); // 링크 색상
            }
        };

        resetAuthToken();

        // '비밀번호 찾기' 부분에 클릭 이벤트 추가
        String linkText = "비밀번호 찾기";
        int start = ssforpw.toString().indexOf(linkText);
        int end = start + linkText.length();
        ssforpw.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        forgotPasswordText.setText(ssforpw);
        forgotPasswordText.setMovementMethod(LinkMovementMethod.getInstance());

        loginButton.setOnClickListener(view -> {
            if (SystemClock.elapsedRealtime() - loginButtonLastClickTime < 2000) {
                return;
            }
            loginButtonLastClickTime = SystemClock.elapsedRealtime();
            String userName = idInput.getText().toString();
            String password = passwordInput.getText().toString();

            AccountData requestData = new AccountData(userName, password);

            accountApi.postLoginData(requestData).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d("Login", "Login Success");
                        Toast.makeText(LoginActivity.this, "반가워요!", Toast.LENGTH_SHORT).show();
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
                            String profileImageUrl = userObject.optString("profile_image", "");
                            String phone_num = userObject.getString("phone_num");
                            int gender = userObject.getInt("gender");
                            float height = (float) userObject.getDouble("height");
                            float weight = (float) userObject.getDouble("weight");
                            int age = userObject.getInt("age");
                            int exp = userObject.getInt("exp");
                            int level = ExpSystem.getLevel(exp);
                            int badge_collection = userObject.getInt("badge_collection");
                            long lastLoginTime = System.currentTimeMillis();
                            Log.d("exp", "user's exp is " + exp);

                            String token = responseBody.getJSONObject("jwt_token").getString("access_token");
                            String refreshToken = responseBody.getJSONObject("jwt_token")
                                    .getString("refresh_token");

                            setAuthToken(token);
                            // 로그인 성공 시 SharePreferences에 유저 정보 및 토큰 저장
                            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putLong("userid", user_id);
                            editor.putString("token", token);
                            editor.putString("refresh_token", refreshToken);
                            editor.putString("username", username);
                            editor.putString("nickname", nickname);
                            editor.putString("email", email);
                            editor.putString("profile_image", profileImageUrl);
                            editor.putString("phone_num", phone_num);
                            editor.putInt("gender", gender);
                            editor.putFloat("height", height);
                            editor.putFloat("weight", weight);
                            editor.putInt("age", age);
                            editor.putInt("exp", exp);
                            editor.putInt("level", level);
                            editor.putInt("badge_collection", badge_collection);
                            editor.putLong("lastLoginTime", lastLoginTime);
                            editor.apply();
                            Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
                            startActivity(intent);
                        } catch (JSONException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.d("Login", "Login Failed, Status Code : " + response.code());
                        Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                    Log.e("Retrofit", "Error: " + t.getMessage());
                }
            });
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}