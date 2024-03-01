package com.example.salemcircle;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import models.EventModel;
import models.UserRoleResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import network.ApiService;
import network.RetrofitClient;
import utils.SecurityUtils; // Make sure to import your SecurityUtils class

public class EventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private FloatingActionButton fabAddEvent;

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        eventAdapter = new EventAdapter(getActivity(), new ArrayList<>());
        recyclerView.setAdapter(eventAdapter);

        fabAddEvent = view.findViewById(R.id.fab_add_event);
        fabAddEvent.setVisibility(View.GONE); // Hiding the button before role check up

        checkUserRoleAndSetupFab();

        loadEvents();
        return view;
    }
    private void checkUserRoleAndSetupFab() {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getUserRole().enqueue(new Callback<UserRoleResponse>() {
            @Override
            public void onResponse(Call<UserRoleResponse> call, Response<UserRoleResponse> response) {
                if (response.isSuccessful() && response.body() != null && "admin".equals(response.body().getRole())) {
                    fabAddEvent.setVisibility(View.VISIBLE);
                    fabAddEvent.setOnClickListener(v -> startActivity(new Intent(getActivity(), CreateEventsActivity.class)));
                }
            }

            @Override
            public void onFailure(Call<UserRoleResponse> call, Throwable t) {
                // Handle failure
            }
        });
    }


    private void loadEvents() {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getEvents().enqueue(new Callback<List<EventModel>>() {
            @Override
            public void onResponse(Call<List<EventModel>> call, Response<List<EventModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventAdapter.updateEvents(response.body());
                } else {
                    // TODO: Handle API error response
                }
            }

            @Override
            public void onFailure(Call<List<EventModel>> call, Throwable t) {
                // TODO: Handle network error
            }
        });
    }
}
