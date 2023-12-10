package com.example.runusandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpStep2Activity extends AppCompatActivity {
    private TextView signUpNicknameInput;
    private TextView signUpPhoneNumberInput;
    private Button nextButton2;

    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step2);

        signUpNicknameInput = findViewById(R.id.SignUpNicknameInput);
        signUpPhoneNumberInput = findViewById(R.id.SignUpPhoneNumberInput);
        nextButton2 = findViewById(R.id.nextButton2);
        backButton = findViewById(R.id.buttonBack);

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
        EditText signUpNickname = findViewById(R.id.SignUpNicknameInput);
        signUpNicknameInput.setText(nickname);
        signUpPhoneNumberInput.setText(phoneNumber);


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

                String phoneNumber = signUpPhoneNumberInput.getText().toString();

                if (phoneNumber.length() != 11) {
                    signUpPhoneNumberInput.setError("휴대폰 번호는 11자리여야 합니다");
                    return;
                }

                Intent intentToStep3 = new Intent(SignUpStep2Activity.this, SignUpStep3Activity.class);
                intentToStep3.putExtra("userName", userName);
                intentToStep3.putExtra("password", password);
                intentToStep3.putExtra("email", email);
                intentToStep3.putExtra("nickname", nickname);
                intentToStep3.putExtra("phoneNumber", phoneNumber);
                intentToStep3.putExtra("height", height);
                intentToStep3.putExtra("weight", weight);
                intentToStep3.putExtra("gender", gender);
                intentToStep3.putExtra("age",age);
                startActivity(intentToStep3);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToStep1 = new Intent(SignUpStep2Activity.this, SignUpStep1Activity.class);
                intentToStep1.putExtra("userName", userName);
                intentToStep1.putExtra("password", password);
                intentToStep1.putExtra("email", email);
                intentToStep1.putExtra("nickname", signUpNicknameInput.getText().toString());
                intentToStep1.putExtra("phoneNumber", signUpPhoneNumberInput.getText().toString());
                intentToStep1.putExtra("height", height);
                intentToStep1.putExtra("weight", weight);
                intentToStep1.putExtra("gender", gender);
                intentToStep1.putExtra("age",age);
                startActivity(intentToStep1);
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
