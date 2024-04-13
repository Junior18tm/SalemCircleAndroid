package models;

import com.google.gson.annotations.SerializedName;

public class FavoriteModel {
    @SerializedName("_id")
    private String id;

    @SerializedName("user")
    private String userId;
    @SerializedName("event")
    private EventModel event;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public EventModel getEvent() {
        return event;
    }

    public void setEvent(EventModel event) {
        this.event = event;
    }
}
