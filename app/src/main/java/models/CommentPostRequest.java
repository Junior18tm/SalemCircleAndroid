package models;


public class CommentPostRequest {
    private String eventId;
    private String text;
    private String user; // Adjust according to whether this is an ID or username based on your backend expectation

    public CommentPostRequest(String eventId, String text, String user) {
        this.eventId = eventId;
        this.text = text;
        this.user = user;
    }

    // Getters and setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
