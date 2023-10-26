package com.example.runusandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

        // 이전 액티비티에서 전달받은 데이터를 가져옵니다.
        Intent intent = getIntent();
        final String userName = intent.getStringExtra("userName");
        final String password = intent.getStringExtra("password");
        final String email = intent.getStringExtra("email");

        nextButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname = signUpNicknameInput.getText().toString();

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
