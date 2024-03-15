package com.example.salemcircle;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import models.EventModel;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<EventModel> eventList;
    private Context context;
    private boolean isEditMode;

    public EventAdapter(Context context, List<EventModel> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.card_event, parent, false);
        return new EventViewHolder(itemView);
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel currentEvent = eventList.get(position);
        holder.eventName.setText(currentEvent.getEventName());
        holder.description.setText(currentEvent.getDescription());
        holder.dateTime.setText(currentEvent.getDateTime().toString()); // Make sure this date is formatted as needed
        holder.capacity.setText(String.valueOf(currentEvent.getCapacity()));

        holder.viewButton.setOnClickListener(v -> {
            Toast.makeText(context, "Viewing event: " + currentEvent.getEventName(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, EventsActivity.class);
            intent.putExtra("EVENT_ID", currentEvent.getEventId()); // Pass eventId to EventsActivity
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void updateEvents(List<EventModel> newEvents) {
        eventList.clear();
        eventList.addAll(newEvents);
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, description, dateTime, capacity;
        Button viewButton, deleteButton; // Reference to the "View" button

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            description = itemView.findViewById(R.id.eventDescription);
            dateTime = itemView.findViewById(R.id.eventDateTime);
            capacity = itemView.findViewById(R.id.eventCapacity);
            viewButton = itemView.findViewById(R.id.viewButton);
        }
    }

}

