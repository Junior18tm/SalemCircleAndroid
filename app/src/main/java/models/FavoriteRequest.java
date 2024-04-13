package models;

public class FavoriteRequest {
    private String userId;
    private String eventId;

    public FavoriteRequest(String userId, String eventId) {
        this.userId = userId;
        this.eventId = eventId;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
