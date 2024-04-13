package models;

import java.util.Date;
import java.util.List;

public class EventModel {
    private String _id;
    private String eventId;
    private String eventName;
    private String description;
    private Date dateTime;
    private int capacity;
    // private List<Comment> comments; // implement Comment model later
    private List<String> participants;
    private boolean isFavorited;
    private int favoriteCount;


    // Constructors
    public EventModel(String _id, String eventId, String eventName, String description, Date dateTime, int capacity, int favoriteCount) {
        this._id = _id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.dateTime = dateTime;
        this.capacity = capacity;
        this.favoriteCount = favoriteCount;
    }
    public EventModel(String eventId, String eventName, String description, Date dateTime, int capacity) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.dateTime = dateTime;
        this.capacity = capacity;
    }
    // Getters
    public String get_id() {
        return _id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<String> getParticipants() {
        return participants;
    }
    public boolean isFavorited() {
        return isFavorited;
    }
    public int getFavoriteCount() {
        return favoriteCount;
    }

// Setters
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public void setCapacity(int capacity) {this.capacity = capacity;}

    public void setFavorited(boolean favorite) {
        isFavorited = favorite;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }
}
