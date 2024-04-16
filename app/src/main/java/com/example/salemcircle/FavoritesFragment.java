package com.example.salemcircle;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import models.EventModel;
import models.FavoriteCountResponse;
import models.FavoriteModel;
import models.FavoriteRemoveRequest;
import models.FavoriteRequest;
import network.ApiService;
import network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SecurityUtils;

public class FavoritesFragment extends Fragment implements EventAdapter.FavoriteHandler {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<EventModel> favoriteEvents = new ArrayList<>();
    private FloatingActionButton fabAddEvent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment using the same layout as for events
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        eventAdapter = new EventAdapter(getActivity(), favoriteEvents, this);
        recyclerView.setAdapter(eventAdapter);
        fabAddEvent = view.findViewById(R.id.fab_add_event);
        fabAddEvent.setVisibility(View.GONE);

        if (SecurityUtils.isLoggedIn(getContext())) {
            loadFavoriteEvents(); // Only load favorites if user is logged in
        } else {
            promptLoginOrRegister();
        }

        return view;
    }
    public void onFavoriteToggled(EventModel event, boolean isFavorite) {
        if (isFavorite) {
            addFavorite(event);
        } else {
            removeFavorite(event);
        }
    }
    private void addFavorite(EventModel event) {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.addFavorite(new FavoriteRequest(SecurityUtils.getUserId(getContext()), event.get_id())).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    // Optionally handle UI update or log success
                    int newCount = event.getFavoriteCount() + 1;
                    event.setFavoriteCount(newCount);
                    eventAdapter.notifyDataSetChanged();
                } else {
                    Log.e("API Error", "Failed with response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to add to favorites: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                Log.e("Network Error", "Failed to communicate with the server: " + t.getMessage());
                Toast.makeText(getContext(), "Check your network connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFavorite(EventModel event) {
        // Create a confirmation dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Remove Favorite")
                .setMessage("Are you sure you want to remove this event from your favorites?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User clicked "Yes", proceed with unfavorite
                    ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
                    apiService.removeFavorite(new FavoriteRemoveRequest(SecurityUtils.getUserId(getContext()), event.get_id())).enqueue(new Callback<Response<Void>>() {
                        @Override
                        public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                            if (response.isSuccessful()) {
                                event.setFavorited(false); // Update the local state
                                event.setFavoriteCount(Math.max(0, event.getFavoriteCount() - 1)); // Decrement count safely
                                loadFavoriteEvents(); // Refresh the list to reflect changes
                                Toast.makeText(getContext(), "Event removed from favorites", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e("API Error", "Failed with response code: " + response.code());
                                Toast.makeText(getContext(), "Failed to remove from favorites: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Response<Void>> call, Throwable t) {
                            Log.e("Network Error", "Failed to communicate with the server: " + t.getMessage());
                            Toast.makeText(getContext(), "Check your network connection", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null) // User clicked "Cancel", just dismiss the dialog
                .show();
    }
    private void fetchFavoriteCount(final EventModel event) {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getFavoriteCount(event.get_id()).enqueue(new Callback<FavoriteCountResponse>() {
            @Override
            public void onResponse(Call<FavoriteCountResponse> call, Response<FavoriteCountResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    event.setFavoriteCount(response.body().getCount());
                    eventAdapter.notifyDataSetChanged(); // Refresh the adapter to update the display
                    Log.d("FavoriteCountFetch", "Successfully fetched favorite count for Event ID: " + event.getEventId() + " - Count: " + response.body().getCount());
                } else {
                    Log.e("FetchCount Error", "Error fetching favorite count: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<FavoriteCountResponse> call, Throwable t) {
                Log.e("Network Error", "Failed to fetch favorite count: " + t.getMessage());
            }
        });
    }
    private void loadFavoriteEvents() {
        String userId = SecurityUtils.getUserId(getContext());
        if (userId == null || userId.isEmpty()) {
            promptLoginOrRegister();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getFavoritesByUserId(userId).enqueue(new Callback<List<FavoriteModel>>() {
            @Override
            public void onResponse(Call<List<FavoriteModel>> call, Response<List<FavoriteModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteEvents.clear();
                    for (FavoriteModel favorite : response.body()) {
                        if (favorite != null && favorite.getEvent() != null) {
                            EventModel event = favorite.getEvent();
                            event.setFavorited(true); // Explicitly set the favorited state
                            favoriteEvents.add(event);
                            fetchFavoriteCount(event); // Fetch and update favorite counts
                        }
                    }
                    eventAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FavoriteModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void promptLoginOrRegister() {
        new AlertDialog.Builder(getContext())
                .setTitle("Access Restricted")
                .setMessage("You need to log in or register to view favorites.")
                .setPositiveButton("Login", (dialog, which) -> {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                })
                .setNegativeButton("Register", (dialog, which) -> {
                    startActivity(new Intent(getContext(), SignUpActivity.class));
                })
                .show();
    }
}

