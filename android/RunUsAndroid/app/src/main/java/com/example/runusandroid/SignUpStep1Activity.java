package com.example.runusandroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpStep1Activity extends AppCompatActivity {
    private TextView signUpIdInput;
    private TextView signUpPasswordInput;
    private TextView signUpEmailInput;
    private Button nextButton1;
    private ImageButton backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step1);

        signUpIdInput = findViewById(R.id.SignUpIdInput);
        signUpPasswordInput = findViewById(R.id.SignUpPasswordInput);
        signUpEmailInput = findViewById(R.id.SignUpEmailInput);
        nextButton1 = findViewById(R.id.nextButton1);
        backButton = findViewById(R.id.buttonBack);
        EditText IdText = findViewById(R.id.SignUpIdInput);
        EditText editText = findViewById(R.id.SignUpEmailInput);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String password = intent.getStringExtra("password");
        String email = intent.getStringExtra("email");
        String nickname = intent.getStringExtra("nickname");
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String height = intent.getStringExtra("height");
        String weight = intent.getStringExtra("weight");
        String age = intent.getStringExtra("age");
        String gender = intent.getStringExtra("gender");

        signUpIdInput.setText(userName);
        signUpPasswordInput.setText(password);
        signUpEmailInput.setText(email);

        //아이디는 영어 대,소문자와 숫자만 입력 가능함.
        InputFilter Idfilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        IdText.setFilters(new InputFilter[]{Idfilter});


        nextButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Id = signUpIdInput.getText().toString();
                String password = signUpPasswordInput.getText().toString();
                String email = signUpEmailInput.getText().toString();

                // 이메일 주소가 올바르지 않으면 다음으로 넘어가지 않는다.
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    signUpEmailInput.setError("유효한 이메일 주소를 입력하세요");
                    return;
                }

                // 비밀번호는 8자리 이상이어야 한다.
                if (password.length() < 8) {
                    signUpPasswordInput.setError("비밀번호는 8자리 이상이어야 합니다");
                    return;
                }

                // 아이디는 있는지만 검사한다.
                if (Id.length() == 0) {
                    signUpIdInput.setError("아이디를 입력하세요.");
                    return;
                }

                Intent intent = new Intent(SignUpStep1Activity.this, SignUpStep2Activity.class);
                intent.putExtra("userName", Id);
                intent.putExtra("password", password);
                intent.putExtra("email", email);
                intent.putExtra("nickname", nickname);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("height", height);
                intent.putExtra("weight", weight);
                intent.putExtra("gender", gender);
                intent.putExtra("age",age);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToLogin = new Intent(SignUpStep1Activity.this, LoginActivity.class);
                startActivity(intentToLogin);
            }
        });
    }
}
