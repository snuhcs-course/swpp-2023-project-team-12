package com.example.runusandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpStep3Activity extends AppCompatActivity {
    private TextView signUpPhoneNumberInput;
    private Button nextButton3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step3);

        signUpPhoneNumberInput = findViewById(R.id.SignUpPhoneNumberInput);
        nextButton3 = findViewById(R.id.nextButton3);

        Intent intent = getIntent();
        final String userName = intent.getStringExtra("userName");
        final String password = intent.getStringExtra("password");
        final String email = intent.getStringExtra("email");
        final String nickname = intent.getStringExtra("nickname");


        nextButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = signUpPhoneNumberInput.getText().toString();

                if (phoneNumber.length() != 11) {
                    signUpPhoneNumberInput.setError("휴대폰 번호는 11자리여야 합니다");
                    return;
                }

                Intent intentToStep4 = new Intent(SignUpStep3Activity.this, SignUpStep4Activity.class);
                intentToStep4.putExtra("userName", userName);
                intentToStep4.putExtra("password", password);
                intentToStep4.putExtra("email", email);
                intentToStep4.putExtra("nickname", nickname);
                intentToStep4.putExtra("phoneNumber", phoneNumber);
                startActivity(intentToStep4);
            }
        });
    }
}
