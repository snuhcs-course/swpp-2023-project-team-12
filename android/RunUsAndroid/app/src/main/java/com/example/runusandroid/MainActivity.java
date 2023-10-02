package com.example.runusandroid;

import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import org.w3c.dom.Text;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button loginButton;
    private Button signUpButton;
    private JsonPlaceHolderAPI jsonPlaceHolderAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://ec2-43-201-102-185.ap-northeast-2.compute.amazonaws.com:3000")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        JsonPlaceHolderAPI jsonPlaceHolderAPI = retrofit.create(JsonPlaceHolderAPI.class);
/*
        jsonPlaceHolderAPI = RetrofitClient.getClient().create(JsonPlaceHolderAPI.class);
        Call<List<Post>> call = jsonPlaceHolderAPI.getPosts();

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if(!response.isSuccessful())
                {
                    textView.setText("Code:" + response.code());
                    return;
                }
                List<Post> posts = response.body();

                for(Post post : posts){
                    String content = "";
                    content += "ID : " + post.getId() + "\n";
                    content += "test : " + post.getTest() + "\n\n";

                    textView.append(content);
                }
            }


            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                textView.setText(t.getMessage());
            }

        });*/
        loginButton = findViewById(R.id.LoginActivityButton);
        signUpButton = findViewById(R.id.SignUpActivityButton);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}