package com.example.runusandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpStep1Activity extends AppCompatActivity {
    private TextView signUpIdInput;
    private TextView signUpPasswordInput;
    private TextView signUpEmailInput;
    private Button nextButton1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step1);

        signUpIdInput = findViewById(R.id.SignUpIdInput);
        signUpPasswordInput = findViewById(R.id.SignUpPasswordInput);
        signUpEmailInput = findViewById(R.id.SignUpEmailInput);
        nextButton1 = findViewById(R.id.nextButton1);

        nextButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = signUpIdInput.getText().toString();
                String password = signUpPasswordInput.getText().toString();
                String email = signUpEmailInput.getText().toString();

                Intent intent = new Intent(SignUpStep1Activity.this, SignUpStep2Activity.class);
                intent.putExtra("userName", userName);
                intent.putExtra("password", password);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }
}
