package com.example.fetchreward;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;

public interface ApiService {
    @GET("hiring.json")
    Call<List<Item>> getItems();
}
