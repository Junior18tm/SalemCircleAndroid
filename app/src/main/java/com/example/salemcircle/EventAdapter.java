package com.example.salemcircle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.EventModel;
import models.FavoriteCountResponse;
import models.FavoriteRemoveRequest;
import models.FavoriteRequest;
import models.FavoriteResponse;
import network.ApiService;
import network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utils.SecurityUtils;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<EventModel> eventList;
    private Context context;
    private FavoriteHandler favoriteHandler;


    public interface FavoriteHandler {
        void onFavoriteToggled(EventModel event, boolean isFavorite);
    }

    // Constructor
    public EventAdapter(Context context, List<EventModel> eventList, FavoriteHandler favoriteHandler) {
        this.context = context;
        this.eventList = eventList;
        this.favoriteHandler = favoriteHandler;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.card_event, parent, false);
        return new EventViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel currentEvent = eventList.get(position);
        holder.eventName.setText(currentEvent.getEventName());
        // description
        String description = currentEvent.getDescription();
        if (description.length() > 150) {
            description = description.substring(0, 150) + "..."; // Truncate and append ellipsis
        }
        holder.description.setText(description);

        //date time
        String formattedDateTime = formatDateWithOrdinalIndicator(currentEvent.getDateTime());
        holder.dateTime.setText(formattedDateTime);

        //Capacity
        String capacityText = String.format(Locale.getDefault(), "%d/%d Participants",
                currentEvent.getParticipants().size(),
                currentEvent.getCapacity());
        holder.capacity.setText(capacityText);

        //view button
        holder.viewButton.setOnClickListener(v -> {
            Toast.makeText(context, "Viewing event: " + currentEvent.getEventName(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, EventsActivity.class);
            intent.putExtra("EVENT_ID", currentEvent.getEventId());// Pass eventId to EventsActivity
            intent.putExtra("EVENT_MONGO_ID", currentEvent.get_id()); // Pass eventid from database
            context.startActivity(intent);
        });

        holder.favoriteCount.setText(String.valueOf(currentEvent.getFavoriteCount()));

        updateFavoriteButtonUI(holder.favoriteButton, currentEvent.isFavorited());
        //favorite button
        holder.favoriteButton.setOnClickListener(v -> {
            if (SecurityUtils.isLoggedIn(context)) {
                boolean isFavorited = !currentEvent.isFavorited();
                currentEvent.setFavorited(isFavorited); // Toggle local state
                updateFavoriteButtonUI(holder.favoriteButton, isFavorited);
                if (favoriteHandler != null) {
                    favoriteHandler.onFavoriteToggled(currentEvent, isFavorited); // Notify fragment
                }
            } else {
                // User is not logged in, prompt to log in
                promptLogin(context);
            }
        });

    }
    public static void promptLogin(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Login Required")
                .setMessage("You must log in to favorite events.")
                .setPositiveButton("Login", (dialog, which) -> {
                    context.startActivity(new Intent(context, LoginActivity.class));
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        TextView eventName, description, dateTime, capacity, favoriteCount;
        Button viewButton;
        ImageButton favoriteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            description = itemView.findViewById(R.id.eventDescription);
            dateTime = itemView.findViewById(R.id.eventDateTime);
            capacity = itemView.findViewById(R.id.eventCapacity);
            viewButton = itemView.findViewById(R.id.viewButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            favoriteCount = itemView.findViewById(R.id.favoriteCount);
        }
    }

        private void updateFavoriteButtonUI(ImageButton button, boolean isFavorited) {
         if (isFavorited) {
                button.setImageResource(R.drawable.favorited); // Assuming ic_favorite_filled is a drawable resource
            } else {
            button.setImageResource(R.drawable.favoriteborder); // Assuming ic_favorite_border is a drawable resource
            }
    }

        private String formatDateWithOrdinalIndicator(Date date) {
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault()); // Month as a string
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault()); // Year

            int day = Integer.parseInt(new SimpleDateFormat("d", Locale.getDefault()).format(date)); // Day of the month as an integer
            String daySuffix = getDaySuffix(day);

            return String.format(Locale.getDefault(), "%s %d%s, %s",
                    monthFormat.format(date), // Month as a string
                    day, // Day of the month as an integer
                    daySuffix, // Suffix for the day (st, nd, rd, th)
                    yearFormat.format(date)); // Year
        }

        private String getDaySuffix(final int day) {
            if (day >= 11 && day <= 13) {
                return "th";
            }
            switch (day % 10) {
                case 1:
                    return "st";
                case 2:
                    return "nd";
                case 3:
                    return "rd";
                default:
                    return "th";
            }
        }


}

