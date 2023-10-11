package com.example.runusandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private TextView signUpIdInput;
    private TextView signUpPasswordInput;
    private TextView signUpNicknameInput;
    private TextView signUpEmailInput;
    private TextView signUpPhoneNumberInput;
    private TextView signUpGenderInput;
    private TextView signUpHeightInput;
    private TextView signUpWeightInput;
    private TextView signUpAgeInput;
    private Button signUpButton;
    private AccountApi accountApi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpIdInput = findViewById(R.id.SignUpIdInput);
        signUpPasswordInput = findViewById(R.id.SignUpPasswordInput);
        signUpNicknameInput = findViewById(R.id.SignUpNicknameInput);
        signUpPhoneNumberInput = findViewById(R.id.SignUpPhoneNumberInput);
        signUpEmailInput = findViewById(R.id.SignUpEmailInput);
        signUpGenderInput = findViewById(R.id.SignUpGenderInput);
        signUpHeightInput = findViewById(R.id.SignUpHeightInput);
        signUpWeightInput = findViewById(R.id.SignUpWeightInput);
        signUpAgeInput = findViewById(R.id.SignUpAgeInput);
        signUpButton = findViewById(R.id.SignUpBtn);

        accountApi = RetrofitClient.getClient().create(AccountApi.class);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = signUpIdInput.getText().toString();
                String password = signUpPasswordInput.getText().toString();
                String nickname = signUpNicknameInput.getText().toString();
                String email = signUpEmailInput.getText().toString();
                String phoneNumber = signUpPhoneNumberInput.getText().toString();
                int gender = Integer.parseInt(signUpGenderInput.getText().toString());
                float height = Float.parseFloat(signUpHeightInput.getText().toString());
                float weight = Float.parseFloat(signUpWeightInput.getText().toString());
                int age = Integer.parseInt(signUpAgeInput.getText().toString());

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

                accountApi.postSignUpData(requestData).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()) {
                            Log.d("Sign Up","Sign Up Success");
                            Toast.makeText(SignUpActivity.this, "SignUp Success", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Log.d("Sign Up","Sign Up Failed, Status Code : " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(SignUpActivity.this, "SignUp Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}