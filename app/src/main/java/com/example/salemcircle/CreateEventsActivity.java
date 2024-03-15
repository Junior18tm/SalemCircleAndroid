package com.example.salemcircle;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
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

public class CreateEventsActivity extends AppCompatActivity {

    private EditText eventNameEditText, eventDescriptionEditText;

    private Button pickDateButton, pickTimeButton, createEventButton;
    private Spinner capacitySpinner;
    private final Calendar calendar = Calendar.getInstance();
    private int year, month, day, hour, minute;
    private String selectedDate, selectedTime;
    private final List<String> capacityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_events);

        eventNameEditText = findViewById(R.id.eventNameEditText);
        eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
        pickDateButton = findViewById(R.id.pickDateButton);
        pickTimeButton = findViewById(R.id.pickTimeButton);
        createEventButton = findViewById(R.id.createEventButton);
        capacitySpinner = findViewById(R.id.capacitySpinner);

        setupDateAndTimePickers();
        setupCapacitySpinner();

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent();
            }
        });
    }

    private void setupDateAndTimePickers() {
        pickDateButton.setOnClickListener(view -> {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateEventsActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                            pickDateButton.setText(selectedDate);
                        }
                    }, year, month, day);
            datePickerDialog.show();
        });

        pickTimeButton.setOnClickListener(view -> {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(CreateEventsActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            selectedTime = selectedHour + ":" + selectedMinute;
                            pickTimeButton.setText(selectedTime);
                        }
                    }, hour, minute, false);
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

    private void createEvent() {
        // Get values from UI
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();
        int capacity = Integer.parseInt(capacitySpinner.getSelectedItem().toString());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date dateTime = null;
        try {
            dateTime = dateFormat.parse(selectedDate + " " + selectedTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        EventModel event = new EventModel(null, eventName, eventDescription, dateTime, capacity);


        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<EventModel> call = apiService.createEvent(event);
        call.enqueue(new Callback<EventModel>() {
            @Override
            public void onResponse(Call<EventModel> call, Response<EventModel> response) {
                if (response.isSuccessful()) {
                    // Event created successfully
                    Toast.makeText(CreateEventsActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateEventsActivity.this, EventsFragment.class);
                    startActivity(intent);
                } else {
                    // Handle errors (e.g., invalid input, server error)
                    Toast.makeText(CreateEventsActivity.this, "Failed to create event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<EventModel> call, Throwable t) {
                // Network error or exception thrown
                Toast.makeText(CreateEventsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    }




