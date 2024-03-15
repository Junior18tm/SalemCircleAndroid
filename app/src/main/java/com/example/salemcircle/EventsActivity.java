package com.example.salemcircle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

import models.EventModel;
import models.UserRoleResponse;
import network.ApiService;
import network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar;
    // Assuming these TextViews are in your layout to display event details
    private TextView eventNameTextView, eventDescriptionTextView, eventDateTimeTextView, eventCapacityTextView;
    private FloatingActionButton fabEditEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        loadingProgressBar = findViewById(R.id.loadingProgressBar); // Make sure to add this in your XML layout
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventDateTimeTextView = findViewById(R.id.eventDateTimeTextView);
        eventCapacityTextView = findViewById(R.id.eventCapacityTextView);
        fabEditEvent = findViewById(R.id.fab_edit_event);

        // Retrieve the event ID passed from EventsAdapter
        String eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId != null && !eventId.isEmpty()) {
            fetchEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event ID is missing", Toast.LENGTH_LONG).show();
            finish(); // Exit if no event ID is provided
        }
    }

    private void fetchEventDetails(String eventId) {
        showLoading(true);
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        // Update this call to match the actual method name in your ApiService
        Call<EventModel> call = apiService.getEventDetails(eventId);

        call.enqueue(new Callback<EventModel>() {
            @Override
            public void onResponse(Call<EventModel> call, Response<EventModel> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    EventModel event = response.body();
                    // Assuming your EventModel's getDateTime() returns a Date object,
                    // you might need to format it to a String for display
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String formattedDateTime = sdf.format(event.getDateTime());

                    eventNameTextView.setText(event.getEventName());
                    eventDescriptionTextView.setText(event.getDescription());
                    eventDateTimeTextView.setText(formattedDateTime); // Use formatted date-time string
                    eventCapacityTextView.setText(String.valueOf(event.getCapacity()));

                    fabEditEvent.setOnClickListener(view -> {
                        Intent intent = new Intent(EventsActivity.this, EditEventActivity.class);
                        intent.putExtra("EVENT_ID", eventId); // Pass the eventId to EditEventActivity
                        startActivity(intent);
                    });
                    fabEditEvent.show();
                } else {
                    Toast.makeText(EventsActivity.this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventModel> call, Throwable t) {
                showLoading(false);
                Toast.makeText(EventsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    private void checkUserRoleAndSetupFab(FloatingActionButton fab) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        apiService.getUserRole().enqueue(new Callback<UserRoleResponse>() {
            @Override
            public void onResponse(Call<UserRoleResponse> call, Response<UserRoleResponse> response) {
                if (response.isSuccessful() && response.body() != null && "admin".equals(response.body().getRole())) {
                    fab.setVisibility(View.VISIBLE);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Open EditEventActivity to edit the current event
                            Intent intent = new Intent(EventsActivity.this, EditEventActivity.class);
                            intent.putExtra("EVENT_ID", "738"); // Make sure you're using the correct key and value
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<UserRoleResponse> call, Throwable t) {
                // Optionally handle failure
            }
        });
    }
}
