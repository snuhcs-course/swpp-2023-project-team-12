package com.example.runusandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
        if (gender != null){
            if(gender.equals("남성")){
            radioGroupGender.check(R.id.radioButtonMale);
            selectedGender="남성";
            }
            else if(gender.equals("여성")) {
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
                    gender = 1;
                }

                if (height < 100 || height > 230) {
                    signUpHeightInput.setError("키는 100cm에서 230cm 사이여야 합니다");
                    return;
                }

                if (weight < 20 || weight > 200) {
                    signUpWeightInput.setError("체중은 20kg에서 200kg 사이여야 합니다");
                    return;
                }

                if (age < 10 || age > 180) {
                    signUpAgeInput.setError("나이는 10세에서 180세 사이여야 합니다");
                    return;
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
            }
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
}
