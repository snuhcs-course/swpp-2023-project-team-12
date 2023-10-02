package com.example.runusandroid;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
public interface JsonPlaceHolderAPI {

    @GET("tests")
    Call<List<Post>> getPosts();
}
