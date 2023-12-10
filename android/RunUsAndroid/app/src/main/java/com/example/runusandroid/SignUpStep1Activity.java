package com.example.runusandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpStep1Activity extends AppCompatActivity {
    private TextView signUpIdInput;
    private TextView signUpPasswordInput;
    private TextView signUpEmailInput;
    private Button nextButton1;
    private ImageButton backButton;
    private AccountApi accountApi;
    private long nextButtonLastClickTime = 0;
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

        accountApi = RetrofitClient.getClient().create(AccountApi.class);

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


        nextButton1.setOnClickListener(view -> {
            if (SystemClock.elapsedRealtime() - nextButtonLastClickTime < 2000) {
                return;
            }
            nextButtonLastClickTime = SystemClock.elapsedRealtime();
            String Id = signUpIdInput.getText().toString();
            String password1 = signUpPasswordInput.getText().toString();
            String email1 = signUpEmailInput.getText().toString();

            // 이메일 주소가 올바르지 않으면 다음으로 넘어가지 않는다.
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email1).matches()) {
                signUpEmailInput.setError("유효한 이메일 주소를 입력하세요");
                return;
            }

            // 비밀번호는 8자리 이상이어야 한다.
            if (password1.length() < 8) {
                signUpPasswordInput.setError("비밀번호는 8자리 이상이어야 합니다");
                return;
            }

            // 아이디는 있는지만 검사한다.
            if (Id.length() == 0) {
                signUpIdInput.setError("아이디를 입력하세요.");
                return;
            }
            IdValidationData requestData = new IdValidationData(Id);
            accountApi.postIdValidationData(requestData).enqueue(new Callback<ResponseBody>(){

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()) {
                        Intent intent1 = new Intent(SignUpStep1Activity.this, SignUpStep2Activity.class);
                        intent1.putExtra("userName", Id);
                        intent1.putExtra("password", password1);
                        intent1.putExtra("email", email1);
                        intent1.putExtra("nickname", nickname);
                        intent1.putExtra("phoneNumber", phoneNumber);
                        intent1.putExtra("height", height);
                        intent1.putExtra("weight", weight);
                        intent1.putExtra("gender", gender);
                        intent1.putExtra("age", age);
                        startActivity(intent1);
                    }
                    else {
                        signUpIdInput.setError("동일한 아이디가 이미 존재합니다.");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    signUpIdInput.setError("동일한 아이디가 이미 존재합니다.");
                }
            });

        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToLogin = new Intent(SignUpStep1Activity.this, LoginActivity.class);
                startActivity(intentToLogin);
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
