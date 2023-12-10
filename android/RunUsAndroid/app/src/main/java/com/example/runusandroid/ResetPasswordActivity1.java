package com.example.runusandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity1 extends AppCompatActivity {

    private EditText usernameInput;
    private EditText emailInput;
    private Button authCheckButton;
    private Button sendMailButton;

    private EditText authInput;
    private TextView sendMailValidationMessage;
    private TextView authCheckValidationMessage;

    private AccountApi accountApi;

    private String authString = "";
    private long completeButtonLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password1);

        usernameInput = findViewById(R.id.usernameInput2);
        emailInput = findViewById(R.id.emailInput2);
        sendMailButton = findViewById(R.id.sendMailButton);
        authCheckButton = findViewById(R.id.authCheckButton);
        accountApi = RetrofitClient.getClient().create(AccountApi.class);
        authInput = findViewById(R.id.authStringInput);
        authCheckValidationMessage = findViewById(R.id.authCheckValidationMessage);
        sendMailValidationMessage = findViewById(R.id.sendMailValidationMessage);
        authCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (authString.equals(authInput.getText().toString())) {
                    Log.d("success", "success");
                    authCheckValidationMessage.setVisibility(View.GONE);
                    Intent intent = new Intent(ResetPasswordActivity1.this, ResetPasswordActivity2.class);
                    intent.putExtra("username", usernameInput.getText().toString());
                    intent.putExtra("email", emailInput.getText().toString());

                    startActivity(intent);
                    finish();
                } else {
                    authCheckValidationMessage.setVisibility(View.VISIBLE);
                }
            }
        });
        sendMailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - completeButtonLastClickTime < 2000) {
                    return;
                }
                completeButtonLastClickTime = SystemClock.elapsedRealtime();
                String username = usernameInput.getText().toString();
                String email = emailInput.getText().toString();
                SendMailData data = new SendMailData(username, email);


                Call<ResponseBody> call = accountApi.sendMail(data);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity1.this);
                        if (response.isSuccessful()) {
//                            builder.setTitle("성공");
//                            builder.setMessage("인증번호가 이메일로 발송되었습니다.");
                            try {
                                usernameInput.setEnabled(false);
                                emailInput.setEnabled(false);
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                // "message" 키를 사용하여 temp_password 추출
                                authString = jsonObject.getString("message");
                                Log.d("get response", "onResponse: " + authString);
                                authInput.setVisibility(View.VISIBLE);
                                sendMailValidationMessage.setVisibility(View.GONE);
                                sendMailButton.setVisibility(View.GONE);
                                authCheckButton.setVisibility(View.VISIBLE);
                            } catch (IOException | JSONException e) {
                                throw new RuntimeException(e);
                            }

                        } else {
                            authInput.setVisibility(View.GONE);
                            sendMailValidationMessage.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // 네트워크 오류 처리
                        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity1.this);
                        builder.setTitle("네트워크 오류");
                        builder.setMessage("서버에 예상치 못한 오류가 발생했습니다.");
                        builder.setPositiveButton("확인", null); // 단순 확인 버튼
                        AlertDialog dialog = builder.create();
                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface arg0) {
                                // Title의 텍스트 색상 변경
                                int titleId = getResources().getIdentifier("alertTitle", "id", "android");
                                TextView titleTextView = dialog.findViewById(titleId);
                                if (titleTextView != null) {
                                    titleTextView.setTextColor(Color.BLACK);
                                }

                                // Message의 텍스트 색상 변경
                                int messageId = getResources().getIdentifier("message", "id", "android");
                                TextView messageTextView = dialog.findViewById(messageId);
                                if (messageTextView != null) {
                                    messageTextView.setTextColor(Color.BLACK);
                                }
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                            }
                        });
                        dialog.show();
                    }
                });
            }
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
