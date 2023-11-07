package com.example.runusandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpStep4Activity extends AppCompatActivity {
    private EditText signUpHeightInput;
    private EditText signUpWeightInput;
    private EditText signUpAgeInput;
    private RadioGroup radioGroupGender;
    private Button completeButton;
    private AccountApi accountApi;

    private String selectedGender = "남성"; //NOTE: Set default value to male

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_step4);

        // Initialize views
        signUpHeightInput = findViewById(R.id.SignUpHeightInput);
        signUpWeightInput = findViewById(R.id.SignUpWeightInput);
        signUpAgeInput = findViewById(R.id.SignUpAgeInput);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioGroupGender.check(R.id.radioButtonMale); //NOTE: Set default value to male
        completeButton = findViewById(R.id.nextButton2);

        // Get passed data from previous steps
        Intent intent = getIntent();
        final String userName = intent.getStringExtra("userName");
        final String password = intent.getStringExtra("password");
        final String email = intent.getStringExtra("email");
        final String nickname = intent.getStringExtra("nickname");
        final String phoneNumber = intent.getStringExtra("phoneNumber");

        accountApi = RetrofitClient.getClient().create(AccountApi.class);

        radioGroupGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
                RadioButton selectedGenderButton = findViewById(selectedGenderId);
                selectedGender = selectedGenderButton.getText().toString();
            }
        });

        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get values from input fields
                float height = Float.parseFloat(signUpHeightInput.getText().toString());
                float weight = Float.parseFloat(signUpWeightInput.getText().toString());
                int age = Integer.parseInt(signUpAgeInput.getText().toString());
                int gender;
                if (selectedGender.equals("남성")) {
                    gender = 1;
                } else if (selectedGender.equals("여성")) {
                    gender = 2;
                } else {
                    gender = 1; // FIXME: 예상치 못한 값에는 에러는 우선 남성으로 대처
                }

                // Create sign up data object
                SignUpData requestData = new SignUpData(
                        userName,
                        password,
                        nickname,
                        email,
                        phoneNumber,
                        gender,
                        height,
                        weight,
                        age
                );

                // Make a network request to sign up
                accountApi.postSignUpData(requestData).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignUpStep4Activity.this, "SignUp Success", Toast.LENGTH_SHORT).show();
                            // Redirect to login activity after successful sign up
                            Intent loginIntent = new Intent(SignUpStep4Activity.this, LoginActivity.class);
                            startActivity(loginIntent);
                            finish();
                        } else {
                            Toast.makeText(SignUpStep4Activity.this, "SignUp Failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(SignUpStep4Activity.this, "Network Error", Toast.LENGTH_SHORT).show();
                        Log.e("Retrofit", "Error: " + t.getMessage());
                    }
                });
            }
        });
    }
}
