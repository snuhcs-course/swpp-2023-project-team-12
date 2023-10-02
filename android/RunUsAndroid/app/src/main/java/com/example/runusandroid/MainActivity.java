package com.example.runusandroid;

import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import org.w3c.dom.Text;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    private JsonPlaceHolderAPI jsonPlaceHolderAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);

//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://ec2-43-201-102-185.ap-northeast-2.compute.amazonaws.com:3000")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        JsonPlaceHolderAPI jsonPlaceHolderAPI = retrofit.create(JsonPlaceHolderAPI.class);

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

        });

    }
}