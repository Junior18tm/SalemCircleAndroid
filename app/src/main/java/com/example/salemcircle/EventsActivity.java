package com.example.salemcircle;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class EventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Finish activity when clicking on the arrow
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get the event details passed from the EventsFragment
        String eventName = getIntent().getStringExtra("eventName");
        String eventDescription = getIntent().getStringExtra("eventDescription");
        String eventDateTime = getIntent().getStringExtra("eventDateTime");
        int eventCapacity = getIntent().getIntExtra("eventCapacity", 0); // Provide a default value

        // Set the event details to the TextViews on activity_events
        TextView eventNameTextView = findViewById(R.id.eventNameTextView);
        TextView eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        TextView eventDateTimeTextView = findViewById(R.id.eventDateTimeTextView);
        TextView eventCapacityTextView = findViewById(R.id.eventCapacityTextView);

        eventNameTextView.setText(eventName);
        eventDescriptionTextView.setText(eventDescription);
        eventDateTimeTextView.setText(eventDateTime);

        eventCapacityTextView.setText(String.valueOf(eventCapacity));
    }
}


