package com.example.runusandroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpStep2Activity extends AppCompatActivity {
    private TextView signUpNicknameInput;
    private Button nextButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step2);

        signUpNicknameInput = findViewById(R.id.SignUpNicknameInput);
        nextButton2 = findViewById(R.id.nextButton2);

        Intent intent = getIntent();
        final String userName = intent.getStringExtra("userName");
        final String password = intent.getStringExtra("password");
        final String email = intent.getStringExtra("email");
        EditText signUpNickname = findViewById(R.id.SignUpNicknameInput);


        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (!Character.isLetterOrDigit(c) && !((c >= 0xAC00) && (c <= 0xD7A3))) { // 한글 범위 체크
                        return "";
                    }
                }
                return null;
            }
        };
        signUpNickname.setFilters(new InputFilter[]{filter});


        nextButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname = signUpNicknameInput.getText().toString();

                if (nickname.length() == 0) {
                    signUpNicknameInput.setError("닉네임을 입력하세요.");
                    return;
                }

                Intent intentToStep3 = new Intent(SignUpStep2Activity.this, SignUpStep3Activity.class);
                intentToStep3.putExtra("userName", userName);
                intentToStep3.putExtra("password", password);
                intentToStep3.putExtra("email", email);
                intentToStep3.putExtra("nickname", nickname);
                startActivity(intentToStep3);
            }
        });
    }
}
