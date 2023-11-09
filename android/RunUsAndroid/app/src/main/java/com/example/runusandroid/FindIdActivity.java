package com.example.runusandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class FindIdActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button findIdButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_id);

        emailInput = findViewById(R.id.emailInput);
        findIdButton = findViewById(R.id.findIdButton);

        findIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                // 이메일을 사용하여 아이디 찾기 로직 구현
                // 예: 서버에 요청 보내기 등
            }
        });
    }
}
