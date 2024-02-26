package models;

import java.util.Date;

public class EventModel {
    private String eventId;
    private String eventName;
    private String description;
    private Date dateTime;
    private int capacity;
    // private List<Comment> comments; // implement Comment model later

    // Constructor
    public EventModel(String eventId, String eventName, String description, Date dateTime, int capacity) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.dateTime = dateTime;
        this.capacity = capacity;
    }

    // Getters
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

    // Setters
}