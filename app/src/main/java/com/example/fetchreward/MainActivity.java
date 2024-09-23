package com.example.fetchreward;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> displayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        displayList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);

        fetchItems();
    }

    private void fetchItems() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fetch-hiring.s3.amazonaws.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<Item>> call = apiService.getItems();

        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(@NonNull Call<List<Item>> call, @NonNull Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processItems(response.body());
                } else {
                    Log.e("API Error", "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Item>> call, @NonNull Throwable t) {
                Log.e("API Error", "Network call failed: " + t.getMessage(), t);
            }
        });
    }


    private void processItems(List<Item> items) {
        Map<String, List<Item>> groupedItems = new HashMap<>();

        // Group items by listId and filter out null or empty names
        for (Item item : items) {
            if (item.getName() != null && !item.getName().isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    groupedItems.computeIfAbsent(item.getListId(), k -> new ArrayList<>()).add(item);
                }
            }
        }

        // Prepare display list by sorting
        for (String listId : groupedItems.keySet()) {
            List<Item> itemList = groupedItems.get(listId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                itemList.sort((a, b) -> a.getName().compareTo(b.getName())); // Sort by name
            }
            for (Item item : itemList) {
                displayList.add(listId + ": " + item.getName());
            }
        }

        adapter.notifyDataSetChanged();
    }
}
