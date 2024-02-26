package com.example.salemcircle;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import models.EventModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import network.ApiService;
import network.RetrofitClient;

public class EventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;

    public EventsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        eventAdapter = new EventAdapter(getActivity(), new ArrayList<>());
        recyclerView.setAdapter(eventAdapter);

        loadEvents();

        return view;
    }

    private void loadEvents() {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getEvents().enqueue(new Callback<List<EventModel>>() {
            @Override
            public void onResponse(Call<List<EventModel>> call, Response<List<EventModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventAdapter.updateEvents(response.body());
                } else {
                    // TODO: Handle error
                }
            }

            @Override
            public void onFailure(Call<List<EventModel>> call, Throwable t) {
                // TODO: Handle network error
            }
        });
    }
}
