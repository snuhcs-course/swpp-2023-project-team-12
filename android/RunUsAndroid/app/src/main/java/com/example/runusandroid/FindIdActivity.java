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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindIdActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button findIdButton;
    private AccountApi accountApi;
    private long completeButtonLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id);

        emailInput = findViewById(R.id.emailInput);
        findIdButton = findViewById(R.id.findIdButton);

        accountApi = RetrofitClient.getClient().create(AccountApi.class);

        findIdButton.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - completeButtonLastClickTime < 2000) {
                return;
            }
            completeButtonLastClickTime = SystemClock.elapsedRealtime();
            String email = emailInput.getText().toString();
            EmailData emailData = new EmailData(email);

            Call<ResponseBody> call = accountApi.findUsername(emailData);
// Retrofit 호출 결과 처리
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FindIdActivity.this);
                    if (response.isSuccessful()) {
                        builder.setTitle("이메일 전송 완료");
                        builder.setMessage("아이디를 입력하신 이메일로 보내드렸어요!");
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 로그인 액티비티로 이동
                                Intent intent = new Intent(FindIdActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        if (positiveButton != null) {
                            positiveButton.setTextColor(Color.BLACK);  // 원하는 색상으로 설정
                        }
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                // 다이얼로그가 닫히면 LoginActivity로 이동
                                Intent intent = new Intent(FindIdActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        builder.setTitle("찾기 실패");
                        builder.setMessage("가입된 이메일이 없어요.");
                        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        if (positiveButton != null) {
                            positiveButton.setTextColor(Color.BLACK);  // 원하는 색상으로 설정
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // 네트워크 요청 실패 처리
                    Toast.makeText(FindIdActivity.this, "서버에 예상치 못한 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
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
