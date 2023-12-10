package com.example.runusandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpStep3Activity extends AppCompatActivity {
    private EditText signUpHeightInput;
    private EditText signUpWeightInput;
    private EditText signUpAgeInput;
    private RadioGroup radioGroupGender;
    private Button completeButton;
    private AccountApi accountApi;

    private ImageButton backButton;

    private String selectedGender = "남성"; //NOTE: Set default value to male
    private long completeButtonLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step3);

        // Initialize views
        signUpHeightInput = findViewById(R.id.SignUpHeightInput);
        signUpWeightInput = findViewById(R.id.SignUpWeightInput);
        signUpAgeInput = findViewById(R.id.SignUpAgeInput);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioGroupGender.check(R.id.radioButtonMale); //NOTE: Set default value to male
        completeButton = findViewById(R.id.nextButton2);
        backButton = findViewById(R.id.buttonBack);

        // Get passed data from previous steps
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

        signUpHeightInput.setText(height);
        signUpWeightInput.setText(weight);
        signUpAgeInput.setText(age);
        if (gender != null) {
            if (gender.equals("남성")) {
                radioGroupGender.check(R.id.radioButtonMale);
                selectedGender = "남성";
            } else if (gender.equals("여성")) {
                radioGroupGender.check(R.id.radioButtonFemale);
                selectedGender = "여성";
            }
        }


        accountApi = RetrofitClient.getClient().create(AccountApi.class);

        radioGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
                RadioButton selectedGenderButton = findViewById(selectedGenderId);
                selectedGender = selectedGenderButton.getText().toString();
            }
        });

        completeButton.setOnClickListener(view -> {
            if (SystemClock.elapsedRealtime() - completeButtonLastClickTime < 2000) {
                return;
            }
            completeButtonLastClickTime = SystemClock.elapsedRealtime();
            // Get values from input fields
            float height1 = Float.parseFloat(signUpHeightInput.getText().toString());
            float weight1 = Float.parseFloat(signUpWeightInput.getText().toString());
            int age1 = Integer.parseInt(signUpAgeInput.getText().toString());
            int gender1;
            if (selectedGender.equals("남성")) {
                gender1 = 1;
            } else if (selectedGender.equals("여성")) {
                gender1 = 2;
            } else {
                gender1 = 1;
            }
            boolean loginValidator = true;

            if (height1 < 100 || height1 > 230) {
                signUpHeightInput.setError("키는 100cm에서 230cm 사이여야 합니다");
                loginValidator = false;
            }

            if (weight1 < 20 || weight1 > 200) {
                signUpWeightInput.setError("체중은 20kg에서 200kg 사이여야 합니다");
                loginValidator = false;
            }

            if (age1 < 10 || age1 > 180) {
                signUpAgeInput.setError("나이는 10세에서 180세 사이여야 합니다");
                loginValidator = false;
            }
            if (!loginValidator) return;

            // Create sign up data object
            SignUpData requestData = new SignUpData(
                    userName,
                    password,
                    nickname,
                    email,
                    phoneNumber,
                    gender1,
                    height1,
                    weight1,
                    age1
            );

            // Make a network request to sign up
            accountApi.postSignUpData(requestData).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(SignUpStep3Activity.this, "회원가입 되었습니다", Toast.LENGTH_SHORT).show();
                        // Redirect to login activity after successful sign up
                        Intent loginIntent = new Intent(SignUpStep3Activity.this, LoginActivity.class);
                        startActivity(loginIntent);
                        finish();
                    } else {
                        Toast.makeText(SignUpStep3Activity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(SignUpStep3Activity.this, "네트워크 에러, 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                    Log.e("Retrofit", "Error: " + t.getMessage());
                }
            });
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentToStep2 = new Intent(SignUpStep3Activity.this, SignUpStep2Activity.class);
                intentToStep2.putExtra("userName", userName);
                intentToStep2.putExtra("password", password);
                intentToStep2.putExtra("email", email);
                intentToStep2.putExtra("nickname", nickname);
                intentToStep2.putExtra("phoneNumber", phoneNumber);
                intentToStep2.putExtra("height", signUpHeightInput.getText().toString());
                intentToStep2.putExtra("weight", signUpWeightInput.getText().toString());
                intentToStep2.putExtra("age", signUpAgeInput.getText().toString());
                intentToStep2.putExtra("gender", selectedGender);
                startActivity(intentToStep2);
            }
        });
    }

    //NOTE: Method for Test
    public String getSelectedGenderText() {
        int selectedId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedButton = findViewById(selectedId);
            return selectedButton.getText().toString();
        }
        return "";
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
