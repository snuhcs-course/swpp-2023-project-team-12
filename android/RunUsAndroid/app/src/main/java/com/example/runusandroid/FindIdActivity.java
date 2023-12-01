package com.example.runusandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id);

        emailInput = findViewById(R.id.emailInput);
        findIdButton = findViewById(R.id.findIdButton);

        accountApi = RetrofitClient.getClient().create(AccountApi.class);

        findIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                EmailData emailData = new EmailData(email);

                Call<ResponseBody> call = accountApi.findUsername(emailData);
// Retrofit 호출 결과 처리
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            // 성공적인 응답 처리: 이메일 전송 성공 대화 상자 표시
                            AlertDialog.Builder builder = new AlertDialog.Builder(FindIdActivity.this);
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
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(FindIdActivity.this);
                            builder.setTitle("찾기 실패");
                            builder.setMessage("가입된 이메일이 없어요.");
                            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // 아무런 동작을 하지 않음
                                }
                            });
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
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // 네트워크 요청 실패 처리
                        Toast.makeText(FindIdActivity.this, "서버에 예상치 못한 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }
}
