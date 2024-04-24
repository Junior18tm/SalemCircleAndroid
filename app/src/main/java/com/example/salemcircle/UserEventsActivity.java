package com.example.salemcircle;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import models.EventModel;
import network.ApiService;
import network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SecurityUtils;

public class UserEventsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<EventModel> eventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_events); // Use the same layout as the events fragment
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(this, eventList, null); // Assuming null for the handler if not needed
        recyclerView.setAdapter(eventAdapter);

        FloatingActionButton fabAddEvent = findViewById(R.id.fab_add_event);
        fabAddEvent.setVisibility(View.GONE); // Hide the FAB if not required

        String userId = SecurityUtils.getUserId(this);
        fetchUserEvents(userId);
    }

    private void fetchUserEvents(String userId) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<List<EventModel>> call = apiService.getUserEvents(userId);
        call.enqueue(new Callback<List<EventModel>>() {
            @Override
            public void onResponse(Call<List<EventModel>> call, Response<List<EventModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventList.clear();
                    eventList.addAll(response.body());
                    eventAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UserEventsActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EventModel>> call, Throwable t) {
                Toast.makeText(UserEventsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

