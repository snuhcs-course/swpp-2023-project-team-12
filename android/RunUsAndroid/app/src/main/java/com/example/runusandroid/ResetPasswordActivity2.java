package com.example.runusandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity2 extends AppCompatActivity {
    String username;
    String email;
    private EditText resetPassword;
    private Button resetPasswordButton;
    private TextView passwordValidationText;
    private AccountApi accountApi;
    private long completeButtonLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password2);
        Intent intent = getIntent();

        // Intent에서 데이터 추출
        username = intent.getStringExtra("username");
        email = intent.getStringExtra("email");
        resetPassword = findViewById(R.id.resetPasswordInput);
        passwordValidationText = findViewById(R.id.passwordValidationText);
        resetPasswordButton = findViewById(R.id.authCheckButton);
        accountApi = RetrofitClient.getClient().create(AccountApi.class);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - completeButtonLastClickTime < 2000) {
                    return;
                }
                completeButtonLastClickTime = SystemClock.elapsedRealtime();
                if (resetPassword.getText().toString().length() < 8) {
                    passwordValidationText.setVisibility(View.VISIBLE);
                    return;
                }

                ResetPasswordData data = new ResetPasswordData(username, email, resetPassword.getText().toString());


                Call<ResponseBody> call = accountApi.resetPassword(data);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity2.this, R.style.AlertDialogCustom);
                        if (response.isSuccessful()) {
                            passwordValidationText.setVisibility(View.GONE);
                            builder.setTitle("성공");
                            builder.setMessage("비밀번호가 변경되었습니다.");

                            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // LoginActivity로 이동
                                    Intent intent = new Intent(ResetPasswordActivity2.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        } else {
                            builder.setTitle("실패");
                            builder.setMessage("비밀번호 변경에 실패하였습니다. 다시 시도해주세요.");
                            builder.setPositiveButton("확인", null); // 단순 확인 버튼
                        }
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

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // 네트워크 오류 처리
                        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity2.this);
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
