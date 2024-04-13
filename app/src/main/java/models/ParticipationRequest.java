package models;

public class ParticipationRequest {
    private String eventId;
    private String userId;

    // Constructor
    public ParticipationRequest(String eventId, String userId) {
        this.eventId = eventId;
        this.userId = userId;
    }

    // Getters
    public String getEventId() {
        return eventId;
    }

    public String getUserId() {
        return userId;
    }

    // Setters
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
