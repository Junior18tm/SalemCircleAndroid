package com.example.salemcircle;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class CreateEventsActivity extends AppCompatActivity {

    private EditText eventNameEditText, eventDescriptionEditText;
    private Button createEventButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_events);

        eventNameEditText = findViewById(R.id.eventName);
        eventDescriptionEditText = findViewById(R.id.eventDescription);
        createEventButton = findViewById(R.id.createEventButton);

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent();
            }
        });
    }

    private void createEvent() {
        String eventName = eventNameEditText.getText().toString().trim();
        String eventDescription = eventDescriptionEditText.getText().toString().trim();

        // TODO: Implement event creation logic, e.g., API call to backend
    }
}



