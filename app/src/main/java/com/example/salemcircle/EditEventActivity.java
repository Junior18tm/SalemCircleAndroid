package com.example.salemcircle;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.EventModel;
import network.ApiService;
import network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditEventActivity extends AppCompatActivity {

    private EditText eventNameEditText, eventDescriptionEditText;
    private Button pickDateButton, pickTimeButton, updateEventButton, deleteEventButton;
    private Spinner capacitySpinner;
    private final Calendar calendar = Calendar.getInstance();
    private String selectedDate = "", selectedTime = "";
    private final List<String> capacityList = new ArrayList<>();
    private String eventId; // This should be fetched or passed to the activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        pickDateButton = findViewById(R.id.pickDateButton);
        pickTimeButton = findViewById(R.id.pickTimeButton);
        updateEventButton = findViewById(R.id.updateEventButton);
        deleteEventButton = findViewById(R.id.btnDeleteEvent);
        capacitySpinner = findViewById(R.id.capacitySpinner);

        eventId = getIntent().getStringExtra("EVENT_ID"); // Make sure EVENT_ID is passed correctly

        setupDateAndTimePickers();
        setupCapacitySpinner();
        loadEventDetails(eventId);

        updateEventButton.setOnClickListener(view -> updateEvent());
        deleteEventButton.setOnClickListener(view -> deleteEvent());
    }

    private void loadEventDetails(String eventId) {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<EventModel> call = apiService.getEventDetails(eventId);

        call.enqueue(new Callback<EventModel>() {
            @Override
            public void onResponse(Call<EventModel> call, Response<EventModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EventModel event = response.body();

                    // Now, populate the UI with the event details
                    eventNameEditText.setText(event.getEventName());
                    eventDescriptionEditText.setText(event.getDescription());

                    // Assuming you have a method to set date and time from a Date object
                    updateDateTimeUI(event.getDateTime());

                    // Assuming your capacity spinner is set up with integers as strings
                    String capacity = String.valueOf(event.getCapacity());
                    if (capacityList.contains(capacity)) {
                        int spinnerPosition = capacityList.indexOf(capacity);
                        capacitySpinner.setSelection(spinnerPosition);
                    }
                } else {
                    Toast.makeText(EditEventActivity.this, "Failed to fetch event details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventModel> call, Throwable t) {
                Toast.makeText(EditEventActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateDateTimeUI(Date dateTime) {
        if (dateTime != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            selectedDate = dateFormat.format(dateTime);
            selectedTime = timeFormat.format(dateTime);

            // Update the buttons' text to show the selected date and time
            pickDateButton.setText(selectedDate);
            pickTimeButton.setText(selectedTime);
        }
    }


    private void setupDateAndTimePickers() {
        pickDateButton.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                selectedDate = dateFormat.format(calendar.getTime());
                pickDateButton.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        pickTimeButton.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                selectedTime = timeFormat.format(calendar.getTime());
                pickTimeButton.setText(selectedTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });
    }

    private void setupCapacitySpinner() {
        for (int i = 3; i <= 50; i++) {
            capacityList.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, capacityList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        capacitySpinner.setAdapter(adapter);
    }

    private void updateEvent() {
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();
        int capacity = Integer.parseInt(capacitySpinner.getSelectedItem().toString());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date dateTime;
        try {
            dateTime = dateFormat.parse(selectedDate + " " + selectedTime);
        } catch (Exception e) {
            Toast.makeText(this, "Please select a valid date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        EventModel updatedEvent = new EventModel(eventId, eventName, eventDescription, dateTime, capacity);
        // Assuming updateEvent in ApiService takes eventId and updatedEvent as parameters
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<Void> call = apiService.updateEvent(eventId, updatedEvent);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditEventActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    Intent data = new Intent();
                    data.putExtra("EVENT_ID", eventId);
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    Toast.makeText(EditEventActivity.this, "Failed to update the event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditEventActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteEvent() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<Void> call = apiService.deleteEvent(eventId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditEventActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();

                    Intent data = new Intent();
                    data.putExtra("EVENT_DELETED", true);
                    setResult(RESULT_OK, data);
                    finish();

                } else {
                    Toast.makeText(EditEventActivity.this, "Failed to delete the event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditEventActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
