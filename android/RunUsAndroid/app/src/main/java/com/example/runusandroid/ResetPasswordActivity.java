package com.example.runusandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText usernameInput;
    private EditText emailInput;
    private Button resetPasswordButton;

    private AccountApi accountApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        usernameInput = findViewById(R.id.usernameInput2);
        emailInput = findViewById(R.id.emailInput2);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        accountApi = RetrofitClient.getClient().create(AccountApi.class);
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String email = emailInput.getText().toString();
                ResetPasswordData data = new ResetPasswordData(username, email);


                Call<ResponseBody> call = accountApi.resetPassword(data);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
                        if (response.isSuccessful()) {
                            builder.setTitle("성공");
                            builder.setMessage("임시 비밀번호가 이메일로 발송되었습니다.");
                            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // LoginActivity로 이동
                                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else {
                            builder.setTitle("실패");
                            builder.setMessage("아이디나 이메일이 잘못되었습니다. 다시 시도해주세요.");
                            builder.setPositiveButton("확인", null); // 단순 확인 버튼
                        }
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // 네트워크 오류 처리
                        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
                        builder.setTitle("네트워크 오류");
                        builder.setMessage("서버에 예상치 못한 오류가 발생했습니다.");
                        builder.setPositiveButton("확인", null); // 단순 확인 버튼
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        });
    }
}
