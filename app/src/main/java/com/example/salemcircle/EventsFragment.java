package com.example.salemcircle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import models.EventModel;
import models.FavoriteCountResponse;
import models.FavoriteModel;
import models.FavoriteRemoveRequest;
import models.FavoriteRequest;
import models.UserRoleResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import network.ApiService;
import network.RetrofitClient;
import utils.SecurityUtils; // Make sure to import your SecurityUtils class

public class EventsFragment extends Fragment implements EventAdapter.FavoriteHandler {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private FloatingActionButton fabAddEvent;
    private List<EventModel> eventsList = new ArrayList<>();
    private ActivityResultLauncher<Intent> createEventLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the launcher
        createEventLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Refresh your events list or navigate as needed
                        loadEventsAndFavorites(); // Assuming loadEvents() fetches and refreshes the list
                    }
                });
    }


    public EventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        eventAdapter = new EventAdapter(getActivity(), eventsList, this);
        recyclerView.setAdapter(eventAdapter);

        fabAddEvent = view.findViewById(R.id.fab_add_event);
        fabAddEvent.setVisibility(View.GONE); // Hiding the FAB button before role check up

        checkUserRoleAndSetupFab();

        loadEventsAndFavorites();
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        loadEventsAndFavorites(); // Refresh the events list whenever the fragment resumes
    }
    private void checkUserRoleAndSetupFab() {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getUserRole().enqueue(new Callback<UserRoleResponse>() {
            @Override
            public void onResponse(Call<UserRoleResponse> call, Response<UserRoleResponse> response) {
                if (response.isSuccessful() && response.body() != null && "admin".equals(response.body().getRole())) {
                    fabAddEvent.setVisibility(View.VISIBLE);
                    fabAddEvent.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), CreateEventsActivity.class);
                        createEventLauncher.launch(intent);
                    });
                }
            }

            @Override
            public void onFailure(Call<UserRoleResponse> call, Throwable t) {
                // Handle failure
            }
        });
    }

    /*private void loadEvents() {
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getEvents().enqueue(new Callback<List<EventModel>>() {
            @Override
            public void onResponse(Call<List<EventModel>> call, Response<List<EventModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EventModel> events = response.body();
                    eventAdapter.updateEvents(response.body());
                    for (EventModel event : events) {
                        fetchFavoriteCount(event);  }
                } else {
                    Toast.makeText(getContext(), "Failed to load events, please try again later", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EventModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Check your network connection", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    // Favorites
    @Override
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
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.removeFavorite(new FavoriteRemoveRequest(SecurityUtils.getUserId(getContext()), event.get_id())).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    int newCount = Math.max(0, event.getFavoriteCount() - 1);
                    event.setFavoriteCount(newCount);
                    eventAdapter.notifyDataSetChanged();
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

   /* private void loadFavorites() {
        String userId = SecurityUtils.getUserId(getContext()); // Ensure you have a method to get the current user's ID
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        apiService.getFavoritesByUserId(userId).enqueue(new Callback<List<FavoriteModel>>() {
            @Override
            public void onResponse(Call<List<FavoriteModel>> call, Response<List<FavoriteModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EventModel> favoritedEvents = new ArrayList<>();
                    for (FavoriteModel favorite : response.body()) {
                        EventModel event = favorite.getEvent();
                        event.setFavorited(true); // Adjust based on your actual method name, it was setFavorited in your previous code snippet
                        favoritedEvents.add(event);
                    }
                    updateEventsWithFavorites(favoritedEvents); // Update your events list with these favorited events
                } else {
                    // This block will execute if the response from the server is not successful
                    Toast.makeText(getContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FavoriteModel>> call, Throwable t) {
                // This block will execute on network error, parsing errors, or configuration issues
                Log.e("Network Error", "Failed to fetch favorites: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/
    private void loadEventsAndFavorites() {
        final CountDownLatch latch = new CountDownLatch(2); // Two operations to wait for

        final List<EventModel> allEvents = new ArrayList<>();
        String userId = SecurityUtils.getUserId(getContext()); // Ensure you have a method to get the current user's ID
        ApiService apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);

        // Load events
        apiService.getEvents().enqueue(new Callback<List<EventModel>>() {
            @Override
            public void onResponse(Call<List<EventModel>> call, Response<List<EventModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allEvents.addAll(response.body());
                    for (EventModel event : allEvents) {
                        fetchFavoriteCount(event);  // Assuming fetchFavoriteCount updates event's favorite status in allEvents
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                }
                latch.countDown(); // Signal that this request is complete
            }

            @Override
            public void onFailure(Call<List<EventModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error while loading events", Toast.LENGTH_SHORT).show();
                latch.countDown(); // Ensure count is decreased on failure too
            }
        });

        // Load favorites
        apiService.getFavoritesByUserId(userId).enqueue(new Callback<List<FavoriteModel>>() {
            @Override
            public void onResponse(Call<List<FavoriteModel>> call, Response<List<FavoriteModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> favoritedEventIds = new ArrayList<>();
                    for (FavoriteModel favorite : response.body()) {
                        if (favorite != null && favorite.getEvent() != null) { // Add null checks here
                            favoritedEventIds.add(favorite.getEvent().getEventId());
                        }
                    }
                    // Mark events as favorited
                    for (EventModel event : allEvents) {
                        if (event != null && favoritedEventIds.contains(event.getEventId())) { // Additional null check for safety
                            event.setFavorited(true);
                        }
                    }
                }
                latch.countDown(); // Signal that this request is complete
            }


            @Override
            public void onFailure(Call<List<FavoriteModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error while loading favorites", Toast.LENGTH_SHORT).show();
                latch.countDown(); // Ensure count is decreased on failure too
            }
        });

        // Wait for both requests to complete and then update UI
        new Thread(() -> {
            try {
                latch.await(); // Wait until both requests complete
                getActivity().runOnUiThread(() -> {
                    eventAdapter.updateEvents(allEvents); // Update UI on main thread
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }


    /*private void updateEventsWithFavorites(List<EventModel> favoritedEvents) {
        HashSet<String> favoritedEventIds = new HashSet<>();
        for (EventModel event : favoritedEvents) {
            favoritedEventIds.add(event.getEventId());
        }
        for (EventModel event : eventsList) {
            event.setFavorited(favoritedEventIds.contains(event.getEventId()));
        }
        eventAdapter.notifyDataSetChanged(); // Notify your adapter to refresh the RecyclerView
    }
*/

}

