package com.example.salemcircle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import models.EventModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import network.ApiService;
import network.RetrofitClient;
import utils.SecurityUtils;

public class EventsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        recyclerView = findViewById(R.id.recyclerView);

        eventAdapter = new EventAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(eventAdapter);

        loadEvents();

    }

    private void loadEvents() {

        ApiService apiService = RetrofitClient.getClient(getApplicationContext()).create(ApiService.class);
        apiService.getEvents().enqueue(new Callback<List<EventModel>>() {
            @Override
            public void onResponse(Call<List<EventModel>> call, Response<List<EventModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventAdapter.updateEvents(response.body());
                } else {
                    //<!-- TODO: Handle error
                }
            }

            @Override
            public void onFailure(Call<List<EventModel>> call, Throwable t) {
                //<!-- TODO: Handle network error
            }
        });
    }

}